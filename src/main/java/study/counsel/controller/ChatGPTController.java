package study.counsel.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import study.counsel.dto.gpt.GPTCompletionChatRequest;
import study.counsel.dto.gpt.GPTCompletionChatResponse;
import study.counsel.service.ChatGPTService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;


@Slf4j
@RestController // 나중에 Controller로 바꾸자
@RequestMapping("/api/chatgpt/rest")
@RequiredArgsConstructor
public class ChatGPTController {

    private final ChatGPTService chatGPTService;

    // 이전의 대화 내용을 모두 전송해줘야 함.
    // 1. 모든 대화 내역을 리스트나 큐에 저장한다.
    // 2. 토큰 수를 체크 한다.
    // 3. 총 토큰 수가 4097을 초과하면 그 이하가 될 때까지 오래된 것부터 제거한다.
    // 4. 위 조건을 만족하면 open AI 서버에 요청을 보낸다.
    // 현재 빠뜨린 점 : 이전 대화 내역을 사용자가 확인하기 위해서 상담번호, 질문, 답변 모두를 포함한 엔티티가 필요할 듯
    // 로그인할 때마다 JSESSIONID 는 동일한가? 그렇다면 이전 대화 내역을 계속 보내게 되는데?
    // 지우기 버튼을 만들어서 LIST에 있는 대화 내역을 지워야할 듯

    @PostMapping("/completion/chat")
    public GPTCompletionChatResponse completionChat(final @RequestBody @Valid GPTCompletionChatRequest request, HttpServletRequest httpServletRequest) {
        return chatGPTService.completionChat(request, httpServletRequest);
    }
}
