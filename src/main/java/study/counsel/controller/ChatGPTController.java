package study.counsel.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import study.counsel.dto.gpt.GPTCompletionChatRequest;
import study.counsel.entity.CounselHistory;
import study.counsel.service.ChatGPTService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;


@Slf4j
@Controller // 나중에 Controller로 바꾸자
@RequestMapping("/chatGPT")
@RequiredArgsConstructor
public class ChatGPTController {

    private final ChatGPTService chatGPTService;

    // 이전의 대화 내용을 모두 전송해줘야 함.
    // 1. 모든 대화 내역을 리스트나 큐에 저장한다.
    // 2. 토큰 수를 체크 한다.
    // 3. 총 토큰 수가 4097을 초과하면 그 이하가 될 때까지 오래된 것부터 제거한다.
    // 4. 위 조건을 만족하면 open AI 서버에 요청을 보낸다.
    // 현재 빠뜨린 점 : 이전 대화 내역을 사용자가 확인하기 위해서 상담번호, 질문, 답변 모두를 포함한 엔티티가 필요할 듯 - 질문, 답변을 나눠서 저장할 필요 없다
    // 로그인할 때마다 JSESSIONID 는 동일한가? 새로운 브라우저 세션 내에서 로그인하면 달라진다. 그러나 동일한 세션 내에서 로갓했다가 로긴하면 같을 수 있음.
    // 지우기 버튼을 만들어서 LIST에 있는 대화 내역을 지워야할 듯.
    // 상담 모드 만들고

    @PostMapping("/completion/chat")
    public String completionChat(final @ModelAttribute @Valid GPTCompletionChatRequest request, HttpServletRequest httpServletRequest, Model model) {

        log.info("받은 정보={}", request);

        request.setModel("gpt-3.5-turbo");
        request.setRole("user");

        Object loginMember = httpServletRequest.getSession().getAttribute("loginMember");
        request.setMemberId(loginMember.toString()); // 타입 변환 필요

        List<CounselHistory> conversationList = chatGPTService.completionChat(request, httpServletRequest);
        List<CounselHistory> counselList = chatGPTService.getCounselList(httpServletRequest);

        model.addAttribute("counselList", counselList);
        model.addAttribute("conversationList", conversationList);

        return "chatView";
    }

    @GetMapping("/completion/chat")
    public String getCounselList(HttpServletRequest request, Model model) {

        List<CounselHistory> counselList = chatGPTService.getCounselList(request);

        model.addAttribute("counselList", counselList);

        return "chatView";
    }

    @GetMapping("/completion/chat/{JSESSIONID}")
    public String getCounselListDetail(@PathVariable String JSESSIONID, HttpServletRequest request, Model model) {

        List<CounselHistory> conversationList = chatGPTService.getCounselListDetail(JSESSIONID);
        List<CounselHistory> counselList = chatGPTService.getCounselList(request);

        model.addAttribute("counselList", counselList);
        model.addAttribute("conversationList", conversationList);

        return "chatView";
    }

}
