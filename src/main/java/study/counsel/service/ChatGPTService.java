package study.counsel.service;

import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.counsel.dto.gpt.GPTCompletionChatRequest;
import study.counsel.dto.gpt.GPTCompletionChatResponse;
import study.counsel.entity.GPTAnswer;
import study.counsel.entity.GPTQuestion;
import study.counsel.repository.GPTAnswerRepository;
import study.counsel.repository.GPTQuestionRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatGPTService {

    private final OpenAiService openAiService;

    private final GPTQuestionRepository gptQuestionRepository;

    private final GPTAnswerRepository gptAnswerRepository;

    // 동시성 문제 해결과 데이터의 순서 유지를 위해 이 구현체 사용
    Map<Object, List<ChatMessage>> conversationHistory = Collections.synchronizedMap(new LinkedHashMap<>()); // 식별자, conversationList(키, 값)

    // SYSTEM에 역할 부여
    String prompt = "당신은 이제 사용자의 여자친구입니다. " +
            " 20대 여성처럼 친근하게 답변해주세요. " +
            " 인공지능이 아닌 사람처럼 말하고 행동하세요. "
            + "그리고 당신의 이름은 이제 연수입니다.";

    public GPTCompletionChatResponse completionChat(GPTCompletionChatRequest request, HttpServletRequest httpServletRequest) {

        // 대화내역 리스트를 만든다. 그 안에 리스트를 넣는다(역할과 content로 구성된 ChatMessage 인터페이스 쓰면 됨),
        // 맨 처음 요소로는 system, content가 온다.
        // 그래서, 바로 아래 of()의 파라미터로 대화내역 리스트, request를 준다,
        // 대화 내역이 이제 기억되기는 하는데, 각 사용자 별로 conversationList가 있어야 하는 거 아닐까?
        // 여러명이 동시에 요청하면? -> 대화 내용이 섞이게 됨.
        // 따라서 사용자 별 세션을 이용하여 구분해야 함
        // Map에 사용자의 식별자, 대화기록을 넣는다.

        // 세션에서 식별자 가져옴
        Object loginMember = httpServletRequest.getSession().getAttribute("loginMember");

        if (loginMember == null) {
            throw new IllegalStateException("로그인하지 않은 사용자");
        }

        // 기존의 대화 내용을 가져옴
        List<ChatMessage> conversationList = conversationHistory.get(loginMember); // "role", "content"만 들어감

        // 대화 내용이 없다면 새 리스트 생성
        // 그리고 시스템의 역할 부여
        if (conversationList == null) {
            conversationList = new ArrayList<>();
            conversationList.add(new ChatMessage("system", prompt));
        }
        
        // 리스트에 사용자의 요청 저장
        conversationList.add(new ChatMessage(request.getRole(), request.getMessage()));

        log.info("대화 내역={}", conversationList);

        // 업데이트된 대화 내용을 다시 저장
        conversationHistory.put(loginMember, conversationList);

        log.info("Map에 있는 내용={}", conversationHistory);

        // open AI 서버에 요청 전송
        ChatCompletionResult chatCompletion = openAiService.createChatCompletion(GPTCompletionChatRequest.of(request, conversationHistory, httpServletRequest));

        GPTCompletionChatResponse response = GPTCompletionChatResponse.of(chatCompletion);

        // message만 추출
        List<String> messages = response.getMessages().stream()
                .map(GPTCompletionChatResponse.Message::getMessage)
                .collect(Collectors.toList());

        String responseMessage = "";
        for (String message : messages) {
            responseMessage = message;
        }

        log.info("받은 메시지={}", responseMessage);

        // ai의 답변 또한 리스트에 저장한다.
        conversationList.add(new ChatMessage("assistant", responseMessage));

        GPTAnswer gptAnswer = saveAnswer(messages);

        saveQuestion(request.getMessage(), gptAnswer);

        return response;
    }

    private void saveQuestion(String question, GPTAnswer answer) {
        GPTQuestion questionEntity = new GPTQuestion(question, answer);
        gptQuestionRepository.save(questionEntity);
    }

    private GPTAnswer saveAnswer(List<String> response) {

        String answer = response.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.joining());

        return gptAnswerRepository.save(new GPTAnswer(answer));
    }

}
