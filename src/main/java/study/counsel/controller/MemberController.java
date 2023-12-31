package study.counsel.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import study.counsel.dto.member.*;
import study.counsel.exception.EmailDuplicateException;
import study.counsel.exception.NicknameDuplicateException;
import study.counsel.exception.UserDuplicateException;
import study.counsel.exception.UserNotFoundException;
import study.counsel.service.MemberService;

import javax.servlet.http.HttpServletRequest;


@Slf4j
@Controller
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberservice;

    @GetMapping("/join")
    public String MemberForm(Model model) {
        model.addAttribute(new MemberFormDto());
        return "members/memberCreateForm";
    }

    @PostMapping("/join")
    public String createMember(@Validated MemberFormDto memberFormDto, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        // 입력값에 오류있으면(검증 실패시) bindingResult에 담아서 다시 form으로 이동
        if (bindingResult.hasErrors()) {
            log.info("error={}", bindingResult.getFieldError());
            return "members/memberCreateForm";
        }

        // 중복회원 검증
        try {
            memberservice.createMember(memberFormDto);
            // 회원가입 성공 메시지 추가
            redirectAttributes.addFlashAttribute("registrationSuccess", true);
        } catch (UserDuplicateException e) {
            log.info("error={}", e.getMessage());
            redirectAttributes.addFlashAttribute("memberIdError", e.getMessage());
            return "redirect:/members/join";
        } catch (NicknameDuplicateException e) {
            log.info("error={}", e.getMessage());
            redirectAttributes.addFlashAttribute("nicknameError", e.getMessage());
            return "redirect:/members/join";
        } catch (EmailDuplicateException e) {
            log.info("error={}", e.getMessage());
            redirectAttributes.addFlashAttribute("emailError", e.getMessage());
            return "redirect:/members/join";
        }
        // 다시 회원가입 폼으로
        return "redirect:/members/join"; // redirect는 url로 이동시킴. return "템플릿명"은 뷰를 리턴
    }

    @GetMapping("/confirm")
    public String confirmPasswordForm() {
        return "members/confirmPasswordForm";
    }

    @PostMapping("/confirm")
    public String confirmPassword(@Validated ConfirmPasswordDto confirmPasswordDto, BindingResult bindingResult, Model model, HttpServletRequest request) {

        log.info("password={}", confirmPasswordDto);

        if (bindingResult.hasErrors()) {
            log.info("error={}", bindingResult.getFieldError());
            return "members/confirmPasswordForm";
        }

        try {
            memberservice.confirmPassword(confirmPasswordDto, request);
            return "redirect:/members/update";

        } catch (Exception e) {
            log.info("error={}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            return "members/confirmPasswordForm";
        }
    }

    @GetMapping("/update")
    public String updateMemberForm(Model model, HttpServletRequest request) {

        String loginMember = (String) request.getSession().getAttribute("loginMember");
        log.info("loginMember={}", loginMember);
        model.addAttribute("memberId", loginMember);
        model.addAttribute(new UpdateMemberFormDto());
        return "/members/memberUpdateForm";
    }

    // "updateMember"는 @ModelAttribute의 속성으로, 이 이름을 사용하여 뷰에서 해당 데이터에 접근 가능
    // model.addAttribute("key", value) 에서 key에 해당
    @PostMapping("/update")
    public String updateMember(@Validated UpdateMemberFormDto memberFormDto, BindingResult bindingResult, Model model, HttpServletRequest request) {

        String loginMember = (String) request.getSession().getAttribute("loginMember");

        if (bindingResult.hasErrors()) {
            log.info("error={}", bindingResult.getFieldError());
            return "members/memberUpdateForm";
        }

        try {
            memberservice.updateMember(memberFormDto, request);
            model.addAttribute("memberId", loginMember);
            return "redirect:/members/logout";
        } catch (NicknameDuplicateException e) {
            log.info("error={}", e.getMessage());
            model.addAttribute("nicknameError", e.getMessage());
            return "/members/memberUpdateForm";
        } catch (EmailDuplicateException e) {
            log.info("error={}", e.getMessage());
            model.addAttribute("emailError", e.getMessage());
            return "/members/memberUpdateForm";
        }
    }

    @GetMapping("/delete")
    public String deleteMemberForm() {
        return "members/deleteMemberForm";
    }

    @PostMapping("/delete")
    public String deleteMember(@Validated DeleteMemberDto deleteMemberDto, BindingResult bindingResult, Model model, HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            log.info("error={}", bindingResult.getFieldError());
            return "members/deleteMemberForm";
        }

        try {
            memberservice.deleteMember(deleteMemberDto, request);
        } catch (Exception e) {
            log.info("error={}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            return "members/deleteMemberForm";
        }

        return "redirect:/";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute(new LoginDto());
        return "members/loginForm";
    }

    @PostMapping("/login")
    public String login(@Validated LoginDto loginDto, BindingResult bindingResult, Model model, HttpServletRequest request) {

        log.info("받은 정보 = {}", loginDto);

        if (bindingResult.hasErrors()) {
            log.info("error={}", bindingResult.getFieldError());
            return "members/loginForm";
        }

        try {
            memberservice.login(loginDto, request);
        } catch (UserNotFoundException e) {
            log.info("error={}", e.getMessage());
            model.addAttribute("userNotFoundException", e.getMessage());
            return "members/loginForm";
        } catch (IllegalStateException e) {
            log.info("error={}", e.getMessage());
            model.addAttribute("illegalStateException", e.getMessage());
            return "members/loginForm";
        }

        return "redirect:/main";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        memberservice.logout(request);
        return "redirect:/";
    }


}
