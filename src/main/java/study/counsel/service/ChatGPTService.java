package study.counsel.service;

import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.counsel.dto.gpt.GPTCompletionChatRequest;
import study.counsel.dto.gpt.GPTCompletionChatResponse;
import study.counsel.entity.ChatSequenceNumber;
import study.counsel.entity.CounselHistory;
import study.counsel.entity.Member;
import study.counsel.repository.ChatSequenceNumberRepository;
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

    private final ChatSequenceNumberRepository chatSequenceNumberRepository;

    // 동시성 문제 해결과 데이터의 순서 유지를 위해 이 구현체 사용.
    // 생각해보니까 순서는 리스트에서 지켜지기 때문에, 굳이 맵에서 순서를 고려할 필요는 없을 듯
//    Map<String, List<ChatMessage>> conversationHistory = Collections.synchronizedMap(new LinkedHashMap<>()); // 식별자, conversationList(키, 값)
    Map<String, List<ChatMessage>> conversationHistory = new ConcurrentHashMap<>(); // 식별자, conversationList(키, 값)

    // SYSTEM에 역할 부여
    @Value("${prompts.prompt1}")
    private String prompt1;

    @Value("${prompts.prompt2}")
    private String prompt2;

    @Value("${prompts.prompt3}")
    private String prompt3;

    public List<CounselHistory> completionChat(GPTCompletionChatRequest request, HttpServletRequest httpServletRequest) {

        // 대화내역 리스트를 만든다. 그 안에 리스트를 넣는다(역할과 content로 구성된 ChatMessage 인터페이스 쓰면 됨),
        // 맨 처음 요소로는 system, content가 온다.
        // 그래서, 바로 아래 of()의 파라미터로 대화내역 리스트, request를 준다,
        // 대화 내역이 이제 기억되기는 하는데, 각 사용자 별로 conversationList가 있어야 하는 거 아닐까?
        // 여러명이 동시에 요청하면? -> 대화 내용이 섞이게 됨.
        // 따라서 사용자 별 세션을 이용하여 구분해야 함
        // Map에 사용자의 식별자, 대화기록을 넣는다.

        Member findMember = getMemberByRequestId(request);

        HttpSession session = httpServletRequest.getSession();
        String sessionId = session.getId();

        ChatSequenceNumber chatSequenceNumber = (ChatSequenceNumber) session.getAttribute("chatSequenceNumber");

        if (sessionId == null) {
            throw new IllegalStateException("로그인하지 않은 사용자");
        }

        // 기존의 대화 내용을 가져옴
        List<ChatMessage> conversationList = conversationHistory.get(sessionId); // "role", "content"만 들어감

        // 대화 내용이 없다면 새 리스트 생성
        // 그리고 시스템의 역할 부여 -> counselMode가 null이 아니면, 리스트를 비우고 새로운 역할 부여.
        if (conversationList == null) {
            conversationList = new ArrayList<>();
            registerPrompt(request, conversationList);
        }

        if (!request.getCounselMode().equals("")) {
            conversationList.clear();
            registerPrompt(request, conversationList);
        }

        // api요청, 대화 저장 및 삭제
        List<String> messages = requestChatAndDelete(request, httpServletRequest, sessionId, conversationList);

        // 응답 추출
        String responseMessage = getResponseMessage(messages);

        // ai의 답변 또한 리스트에 저장한다.
        String answerMsg = saveResponseToListAndConvertListToString(conversationList, messages, responseMessage);

        // 상담 내역 리스트에 표시될 제목은 8자로 자름
        String title = getTitle(request);

        String counselMode = request.getCounselMode();

        CounselHistory counselHistory = new CounselHistory(title, request.getMessage(), answerMsg, findMember, chatSequenceNumber, counselMode);

        counselHistoryRepository.save(counselHistory);

        return counselHistoryRepository.findByChatSequenceNumber(chatSequenceNumber);
    }

    @NotNull
    private static String saveResponseToListAndConvertListToString(List<ChatMessage> conversationList, List<String> messages, String responseMessage) {
        conversationList.add(new ChatMessage("assistant", responseMessage));

        // List를 String 으로 변환
        return getAnswerToString(messages);
    }

    private void registerPrompt(GPTCompletionChatRequest request, List<ChatMessage> conversationList) {
        String counselMode = request.getCounselMode();
        String prompt = getPromptMode(counselMode);

        conversationList.add(new ChatMessage("system", prompt));
    }

    private void registerPrompt(String counselModes, List<ChatMessage> conversationList) {
        String prompt = getPromptMode(counselModes);
        conversationList.add(new ChatMessage("system", prompt));
    }

    @NotNull
    private static String getTitle(GPTCompletionChatRequest request) {
        String title;
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

        //식별자 기준으로 중복 제거
        return rawList.stream()
                .filter(distinctByKey(CounselHistory::getChatSequenceNumber))
                .collect(Collectors.toList());
    }

    // 주어진 키에 따라 중복을 제거하는 유틸리티 메서드
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    public List<CounselHistory> getCounselListDetail(Long chatSequenceNumberId) {

        ChatSequenceNumber chatSequenceNumber = chatSequenceNumberRepository.findById(chatSequenceNumberId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 대화내역"));
        return counselHistoryRepository.findByChatSequenceNumber(chatSequenceNumber); // db에서 대화 내역 찾아서, 리스트로 만들고 
        // completionChat에 던져주면 과거 대화에서 이어나갈 수 있을 것 같은데
        // 그 전에 db에 대화모드를 저장해야함
    }

    public List<CounselHistory> completionChatContinue(Long chatSequenceNumberId, GPTCompletionChatRequest request, HttpServletRequest httpServletRequest) {

        // 1. 일단 과거 대화 내역 리스트를 가져와서 필요한 정보를 추출한다.
        // 2. 새로운 리스트에 다음 순서대로 저장한다. 1)system, prompt 2)과거 대화(질문, 답변)
        // 3. 사용자의 새로운 요청을 리스트에 담는다.
        // 4. 요청을 보낸다.
        // 5. 응답을 받아서 리스트에 저장한다.
        // 6. db에 저장하고 식별자로 구분하여 리스트 리턴

        Member findMember = getMemberByRequestId(request);

        String sessionId = getSessionId(httpServletRequest);

        ChatSequenceNumber findSeq = chatSequenceNumberRepository.findById(chatSequenceNumberId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 대화내역"));

        List<CounselHistory> findCounselList = counselHistoryRepository.findByChatSequenceNumber(findSeq);

        String counselMode = "";

        //위 리스트에서 몇 가지를 추출해야함(1.counselMode, 2.질문, 3.답변)
        for (CounselHistory counselHistory : findCounselList) {
            if (!counselHistory.getCounselMode().equals("")) {
                counselMode = counselHistory.getCounselMode();
            }
        }

        // 기존의 대화 내역은 삭제(채팅을 하다가 과거 채팅 내역으로 가서 다시 채팅 이어가는 경우)
        List<ChatMessage> conversationList = conversationHistory.get(sessionId);

        // 시스템에 역할 부여, 대화 내역 삭제
        if (!(conversationList ==null)) {
            conversationList.clear();
            registerPrompt(counselMode, conversationList);
        }

        // 시스템에 역할 부여
        if (conversationList == null) {
            conversationList = new ArrayList<>();
            registerPrompt(counselMode, conversationList);
        }

        // 리스트에 과거 대화 내역 저장
        for (CounselHistory counselHistory : findCounselList) {
            conversationList.add(new ChatMessage("user", counselHistory.getQuestion()));
            conversationList.add(new ChatMessage("assistant", counselHistory.getAnswer()));
        }

        // 사용자의 새로운 메시지 저장
        List<String> messages = requestChatAndDelete(request, httpServletRequest, sessionId, conversationList);

        String responseMessage = getResponseMessage(messages);

        // ai의 답변 또한 리스트에 저장한다.
        String answerMsg = saveResponseToListAndConvertListToString(conversationList, messages, responseMessage);

        // 상담 내역 리스트에 표시될 제목은 8자로 자름
        String title = getTitle(request);

        CounselHistory counselHistory = new CounselHistory(title, request.getMessage(), answerMsg, findMember, findSeq, counselMode);

        counselHistoryRepository.save(counselHistory);

        return counselHistoryRepository.findByChatSequenceNumber(findSeq);

    }

    private static String getResponseMessage(List<String> messages) {
        String responseMessage = "";
        for (String message : messages) {
            responseMessage = message;
        }
        return responseMessage;
    }

    @NotNull
    private List<String> requestChatAndDelete(GPTCompletionChatRequest request, HttpServletRequest httpServletRequest, String sessionId, List<ChatMessage> conversationList) {
        conversationList.add(new ChatMessage(request.getRole(), request.getMessage()));

        conversationHistory.put(sessionId, conversationList);

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
        return messages;
    }

    private static String getSessionId(HttpServletRequest httpServletRequest) {
        HttpSession session = httpServletRequest.getSession();
        return session.getId();
    }

    private Member getMemberByRequestId(GPTCompletionChatRequest request) {
        return memberRepository.findByMemberId(request.getMemberId()).orElseThrow(() -> new IllegalStateException("존재하지 않는 유저"));
    }

    public void deleteCounsel(Long chatSequenceNumberId) {

        ChatSequenceNumber chatSequenceNumber = chatSequenceNumberRepository.findById(chatSequenceNumberId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 시퀀스"));

        List<CounselHistory> counselHistories = counselHistoryRepository.findByChatSequenceNumber(chatSequenceNumber);

        for (CounselHistory counselHistory : counselHistories) {
            counselHistory.setDeleted(true);
        }
    }

    public void updateCounselTitle(Long chatSequenceNumberId, String newTitle) {

        ChatSequenceNumber chatSequenceNumber = chatSequenceNumberRepository.findById(chatSequenceNumberId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 시퀀스"));

        List<CounselHistory> counselHistories = counselHistoryRepository.findByChatSequenceNumber(chatSequenceNumber);

        CounselHistory counselHistory = counselHistories.get(0);
        counselHistory.setTitle(newTitle);

    }
}
