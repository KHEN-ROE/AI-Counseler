package study.counsel.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import study.counsel.entity.CounselHistory;
import study.counsel.service.ChatGPTService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ChatGPTService chatGPTService;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/main")
    public String main(HttpServletRequest request, Model model) {
        List<CounselHistory> counselList = chatGPTService.getCounselList(request);
        model.addAttribute("counselList", counselList);

        return "main";
    }
}
