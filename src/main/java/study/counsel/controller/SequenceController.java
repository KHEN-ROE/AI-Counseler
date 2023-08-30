package study.counsel.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import study.counsel.entity.ChatSequenceNumber;
import study.counsel.service.SequenceService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Slf4j
@Controller
@RequiredArgsConstructor
public class SequenceController {

    private final SequenceService sequenceService;

    @PostMapping("/increaseSeq")
    public String increaseSeq(HttpServletRequest request) {
        ChatSequenceNumber chatSequenceNumber = sequenceService.increaseSeq();

        HttpSession session = request.getSession();
        session.setAttribute("chatSequenceNumber", chatSequenceNumber);

        return "chatView";
    }

    @PostMapping("/increaseSeqAndShowMain")
    public String increaseSeqAndShowMain() {
        sequenceService.increaseSeq();
        return "main";
    }
}
