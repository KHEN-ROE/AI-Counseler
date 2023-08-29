package study.counsel.service;

import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.counsel.dto.gpt.GPTCompletionChatRequest;
import study.counsel.dto.gpt.GPTCompletionChatResponse;
import study.counsel.entity.CounselHistory;
import study.counsel.entity.Member;
import study.counsel.repository.CounselHistoryRepository;
import study.counsel.repository.MemberRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatGPTService {

    private final OpenAiService openAiService;

    private final MemberRepository memberRepository;

    private final CounselHistoryRepository counselHistoryRepository;

    // 동시성 문제 해결과 데이터의 순서 유지를 위해 이 구현체 사용.
    // 생각해보니까 순서는 리스트에서 지켜지기 때문에, 굳이 맵에서 순서를 고려할 필요는 없을 듯
    Map<String, List<ChatMessage>> conversationHistory = Collections.synchronizedMap(new LinkedHashMap<>()); // 식별자, conversationList(키, 값)

    // SYSTEM에 역할 부여
    String prompt1 = "당신은 이제 고민상담 전문가입니다. "
            + "사용자의 고민을 들어주고 공감해주고 위로해주면 됩니다. "
            + "현실적인 해결책도 제시해주세요 "
            + "20대 여성처럼 친근하게 답변해주세요. "
            + "인공지능임을 밝히지 마세요. "
            + "그리고 당신의 이름은 이제 연수입니다.";

    String prompt2 = "당신은 독설가입니다. 사용자의 고민들 들어주고 따끔한 충고를 해주세요. 냉정하고 차갑게 말하세요." +
            "현실적인 해결방법을 제시하세요" +
            "인공지능임을 밝히지 마세요.";

    String prompt3 = "당신은 연애상담 전문가입니다. 사용자의 이성에 대한 고민을 상담해주세요" +
            "인공지능임을 밝히지 마세요.";

    public List<CounselHistory> completionChat(GPTCompletionChatRequest request, HttpServletRequest httpServletRequest) {

        // 대화내역 리스트를 만든다. 그 안에 리스트를 넣는다(역할과 content로 구성된 ChatMessage 인터페이스 쓰면 됨),
        // 맨 처음 요소로는 system, content가 온다.
        // 그래서, 바로 아래 of()의 파라미터로 대화내역 리스트, request를 준다,
        // 대화 내역이 이제 기억되기는 하는데, 각 사용자 별로 conversationList가 있어야 하는 거 아닐까?
        // 여러명이 동시에 요청하면? -> 대화 내용이 섞이게 됨.
        // 따라서 사용자 별 세션을 이용하여 구분해야 함
        // Map에 사용자의 식별자, 대화기록을 넣는다.

        Member findMember = memberRepository.findByMemberId(request.getMemberId()).orElseThrow(() -> new IllegalStateException("존재하지 않는 유저"));

        // 나중에 엔티티에 저장할 때 필요(식별자로 쓸 예정)
        HttpSession session = httpServletRequest.getSession();
        String sessionId = session.getId();

        if (sessionId == null) {
            throw new IllegalStateException("로그인하지 않은 사용자");
        }

        // 기존의 대화 내용을 가져옴
        List<ChatMessage> conversationList = conversationHistory.get(sessionId); // "role", "content"만 들어감


        // 대화 내용이 없다면 새 리스트 생성
        // 그리고 시스템의 역할 부여
        if (conversationList == null) {
            conversationList = new ArrayList<>();

            String counselMode = request.getCounselMode();
            String prompt = getPromptMode(counselMode);

            conversationList.add(new ChatMessage("system", prompt));
        }

        // 리스트에 사용자의 요청 저장
        conversationList.add(new ChatMessage(request.getRole(), request.getMessage()));

        log.info("대화 내역={}", conversationList);

        // 업데이트된 대화 내용을 다시 저장
        conversationHistory.put(sessionId, conversationList);

        log.info("Map에 있는 내용={}", conversationHistory);

        // open AI 서버에 요청 전송
        ChatCompletionResult chatCompletion = openAiService.createChatCompletion(GPTCompletionChatRequest.of(request, conversationHistory, httpServletRequest));

        GPTCompletionChatResponse response = GPTCompletionChatResponse.of(chatCompletion);

        // message만 추출
        List<String> messages = response.getMessages().stream()
                .map(GPTCompletionChatResponse.Message::getMessage)
                .collect(Collectors.toList());

        // usage 추출(총 토큰 개수)
        GPTCompletionChatResponse.Usage usage = response.getUsage();
        Long totalTokens = usage.getTotalTokens();

        // 토큰 개수 초과 시 3, 4, 5 번째 대화 삭제
        if (totalTokens >= 3000) {
            for (int i = 0; i < 3 && conversationList.size() > 2; i++) {
                conversationList.remove(2);
            }
        }

        log.info("삭제 후 토큰 수={}", totalTokens);

        String responseMessage = "";
        for (String message : messages) {
            responseMessage = message;
        }

        // ai의 답변 또한 리스트에 저장한다.
        conversationList.add(new ChatMessage("assistant", responseMessage));

        // List를 String 으로 변환
        String answerMsg = getAnswerToString(messages);

        // 상담 내역 리스트에 표시될 제목은 6자로 자름
        String title = getTitle(request);

        CounselHistory counselHistory = new CounselHistory(title, request.getMessage(), answerMsg, findMember, sessionId);

        counselHistoryRepository.save(counselHistory);

        return counselHistoryRepository.findByJSESSIONID(sessionId);
    }

    @NotNull
    private static String getTitle(GPTCompletionChatRequest request) {
        String title = "";
        if (request.getMessage().length() >= 8) {
            title = request.getMessage().substring(0, 8) + "...";
        } else {
            title = request.getMessage();
        }
        return title;
    }

    @NotNull
    private static String getAnswerToString(List<String> messages) {
        return messages.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.joining());
    }

    private String getPromptMode(String counselMode) {
        Map<String, String> promptModeMap = new ConcurrentHashMap<>();
        promptModeMap.put("친절한 상담", prompt1);
        promptModeMap.put("독설가", prompt2);
        promptModeMap.put("연애 상담", prompt3);

        return promptModeMap.getOrDefault(counselMode, prompt1);
    }

    public List<CounselHistory> getCounselList(HttpServletRequest request) {

        Object loginMember = request.getSession().getAttribute("loginMember");
        Member findMember = memberRepository.findByMemberId(loginMember.toString()).orElseThrow(() -> new IllegalStateException("존재하지 않는 회원"));
        Long id = findMember.getId();

        List<CounselHistory> rawList = counselHistoryRepository.findByMemberId(id);

        //JSESSIONID 기준으로 중복 제거
        return rawList.stream()
                .filter(distinctByKey(CounselHistory::getJSESSIONID))
                .collect(Collectors.toList());
    }

    // 주어진 키에 따라 중복을 제거하는 유틸리티 메서드
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    public List<CounselHistory> getCounselListDetail(String JSESSIONID) {
        return counselHistoryRepository.findByJSESSIONID(JSESSIONID);
    }
}
