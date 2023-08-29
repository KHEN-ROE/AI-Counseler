package study.counsel.dto.gpt;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GPTCompletionChatRequest {

    private String model;

    private String role;

    @NotNull
    private String message;

    private String memberId;

    @NotNull
    private String counselMode;

    private Integer maxTokens;

    public static ChatCompletionRequest of(GPTCompletionChatRequest request, Map<String, List<ChatMessage>> conversationHistory, HttpServletRequest httpServletRequest) {


        HttpSession session = httpServletRequest.getSession();
        String sessionId = session.getId();

        return ChatCompletionRequest.builder()
                .model(request.getModel())
//                .messages(convertChatMessage(request)) // message에 role, content 포함됨.
                .messages(conversationHistory.get(sessionId)) // map에서 list를 꺼낸다
                .maxTokens(request.getMaxTokens())
                .build();
    }

    // new ChatMessage에 이전 시스템의 역할과 이전 대화내역을 넣으면 되지 않을까
    // 이거 필요 없을듯?
//    private static List<ChatMessage> convertChatMessage(GPTCompletionChatRequest request) {
//        return List.of(new ChatMessage("system", "당신은 웨이트 트레이닝 전문가입니다. 20대 여성처럼 친근하게 답변해주세요.")
//                ,new ChatMessage(request.getRole(), request.getMessage()));
//    }
}
