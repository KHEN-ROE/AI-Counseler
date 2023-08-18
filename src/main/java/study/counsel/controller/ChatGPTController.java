package study.counsel.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import study.counsel.dto.gpt.GPTCompletionChatRequest;
import study.counsel.dto.gpt.GPTCompletionChatResponse;
import study.counsel.service.ChatGPTService;

import javax.validation.Valid;


@Slf4j
@RestController // 나중에 Controller로 바꾸자
@RequestMapping("/api/chatgpt/rest")
@RequiredArgsConstructor
public class ChatGPTController {

    private final ChatGPTService chatGPTService;

    // 전송 구조를 바꿔야함. 이전의 대화 내용을 모두 전송해줘야 함...
    @PostMapping("/completion/chat")
    public GPTCompletionChatResponse completionChat(final @RequestBody @Valid GPTCompletionChatRequest gptCompletionChatRequest) {
        return chatGPTService.completionChat(gptCompletionChatRequest);
    }
}
