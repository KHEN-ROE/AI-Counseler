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
import javax.servlet.http.HttpSession;
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
    // 문제점 : 사용자가 새로운 대화를 시작하면 리스트를 비워야함. - 리스트 건드리지 말고, 맵에 새로운 리스트를 만들면 될듯
    // 그리고 같은 세션 내에서 새로운 대화를 계속해도 하나의 스레드 안에 대화가 다 들어감
    // 대화 페이지를 벗어나거나, new chat을 누르면 리스트를 비운다? jsessionid 도 바꿔야할 것 같은데. 이걸 바꾸기보다, 
    // 식별자를 다른 걸로 교체하자. auto increment 안 걸고 하면 되지 않을까
    // 과거 대화 내역에서 채팅을 계속 이어가려면 어떻게 해야 하는가? 일단 엔티티에 대화 모드도 저장해야 할듯
    // chatVIew에 새채팅 버튼 만든다. 서버에 별도의 컨트롤러 만들고 이거 클릭하면 식별자 +1 시킨다. 식별자만 completionChat에 전달
    // main.html의 상담 요청 버튼도 클릭 시 식별자 +1 시킨다.
    // 과거 대화내역에서 채팅 이어나가기 기능 -> 식별자를 전달하면 안 될까?

    @PostMapping("/completion/chat")
    public String completionChat(final @Valid GPTCompletionChatRequest request, HttpServletRequest httpServletRequest, Model model) {

        log.info("받은 정보={}", request);

        request.setModel("gpt-3.5-turbo");
        request.setRole("user");

        Object loginMember = httpServletRequest.getSession().getAttribute("loginMember");
        request.setMemberId(loginMember.toString()); // 타입 변환 필요

        List<CounselHistory> counselList = chatGPTService.getCounselList(httpServletRequest);
        List<CounselHistory> conversationList = chatGPTService.completionChat(request, httpServletRequest);

        model.addAttribute("counselList", counselList);
        model.addAttribute("conversationList", conversationList);

        return "/chat/chatView";
    }

    @GetMapping("/completion/chat")
    public String getCounselList(HttpServletRequest request, Model model) {

        List<CounselHistory> counselList = chatGPTService.getCounselList(request);

        log.info("counselList={}", counselList);

        model.addAttribute("counselList", counselList);

        return "/chat/chatView";
    }

    @GetMapping("/completion/chat/{chatSequenceNumberId}")
    public String getCounselListDetail(@PathVariable Long chatSequenceNumberId, HttpServletRequest request, Model model) {

        List<CounselHistory> counselList = chatGPTService.getCounselList(request);
        List<CounselHistory> conversationList = chatGPTService.getCounselListDetail(chatSequenceNumberId);

        // 특정 대화 내역 클릭하면 대화 식별자를 세션에 저장
        HttpSession session = request.getSession();
        session.setAttribute("chatSequenceNumberId", chatSequenceNumberId);

        model.addAttribute("counselList", counselList);
        model.addAttribute("conversationList", conversationList);

        return "/chat/chatViewHistory";
    }

    // 과거 대화에서 다시 채팅
    @PostMapping("/completion/chat/{chatSequenceNumberId}")
    public String completionChatContinue(@PathVariable Long chatSequenceNumberId, @Valid GPTCompletionChatRequest request, HttpServletRequest httpServletRequest, Model model) {

        log.info("path={}", chatSequenceNumberId);
        log.info("request={}", request);

        request.setModel("gpt-3.5-turbo");
        request.setRole("user");

        Object loginMember = httpServletRequest.getSession().getAttribute("loginMember");
        request.setMemberId(loginMember.toString()); // 타입 변환 필요

        List<CounselHistory> counselList = chatGPTService.getCounselList(httpServletRequest);
        List<CounselHistory> conversationList = chatGPTService.completionChatContinue(chatSequenceNumberId, request, httpServletRequest);

        model.addAttribute("counselList", counselList);
        model.addAttribute("conversationList", conversationList);
        model.addAttribute("chatSequenceNumberId", chatSequenceNumberId);

        return "/chat/chatViewHistory";

    }

    @PostMapping("/updateTitle/{chatSequenceNumberId}")
    public String updateCounselTitle(@PathVariable Long chatSequenceNumberId, @RequestParam String newTitle , Model model) {

        log.info("번호={}", chatSequenceNumberId, "새제목={}", newTitle);

        try {
            chatGPTService.updateCounselTitle(chatSequenceNumberId, newTitle);
        } catch (Exception e) {
            model.addAttribute("errorMessage={}", e.getMessage());
        }
        return "chat/chatView";
    }

    @PostMapping("/delete/{chatSequenceNumberId}")
    public String deleteCounsel(@PathVariable Long chatSequenceNumberId, Model model) {

        try {
            chatGPTService.deleteCounsel(chatSequenceNumberId);
        } catch (Exception e) {
            model.addAttribute("errorMessage={}", e.getMessage());
        }
        return "chat/chatView";
    }
}
