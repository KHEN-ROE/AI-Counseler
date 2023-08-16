package study.counsel.service;

import com.theokanning.openai.completion.chat.ChatCompletionResult;
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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatGPTService {

    private final OpenAiService openAiService;

    private final GPTQuestionRepository gptQuestionRepository;

    private final GPTAnswerRepository gptAnswerRepository;

    public GPTCompletionChatResponse completionChat(GPTCompletionChatRequest gptCompletionChatRequest) {

        ChatCompletionResult chatCompletion = openAiService.createChatCompletion(GPTCompletionChatRequest.of(gptCompletionChatRequest));

        GPTCompletionChatResponse response = GPTCompletionChatResponse.of(chatCompletion);

        List<String> messages = response.getMessages().stream()
                .map(GPTCompletionChatResponse.Message::getMessage)
                .collect(Collectors.toList());

        GPTAnswer gptAnswer = saveAnswer(messages);

        saveQuestion(gptCompletionChatRequest.getMessage(), gptAnswer);

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
