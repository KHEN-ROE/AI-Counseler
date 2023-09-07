package study.counsel.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import study.counsel.dto.member.ConfirmPasswordDto;
import study.counsel.dto.member.DeleteMemberDto;
import study.counsel.dto.member.LoginDto;
import study.counsel.dto.member.MemberFormDto;
import study.counsel.exception.MemberAlreadyExistsException;
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
        } catch (MemberAlreadyExistsException e) {
            log.info("error={}", e.getMessage());
            redirectAttributes.addAttribute("errorMessage", e.getMessage());
            return "redirect:/members/memberCreateForm"; // 이렇게 하면 409에러 못띄운다. MemberAlreadyExistsException가 사실상 무의미.
        }

        // 회원가입 성공 메시지 추가
        redirectAttributes.addFlashAttribute("registrationSuccess", true);
        // 다시 회원가입 폼으로
        return "redirect:/members/join"; // redirect는 url로 이동시킴. return "템플릿명"은 뷰를 리턴
    }

    @PostMapping("confirm")
    public String confirmPassword(@ModelAttribute("confirmPwd") @Validated ConfirmPasswordDto confirmPasswordDto, BindingResult bindingResult, Model model) {

        if (bindingResult.hasErrors()) {
            log.info("error={}", bindingResult.getFieldError());
            return "members/comfirmPwdForm";
        }

        try {
            memberservice.confirmPassword(confirmPasswordDto);

        } catch (Exception e) {
            log.info("error={}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
        }

        return "member/updateMemberForm";

    }

    // "updateMember"는 @ModelAttribute의 속성으로, 이 이름을 사용하여 뷰에서 해당 데이터에 접근 가능
    // model.addAttribute("key", value) 에서 key에 해당
    @PostMapping("/update")
    public String updateMember(@ModelAttribute("updateMember") @Validated MemberFormDto memberFormDto, BindingResult bindingResult, Model model) {

        if (bindingResult.hasErrors()) {
            log.info("error={}", bindingResult.getFieldError());
            return "members/memberUpdateForm";
        }

        try {
            memberservice.updateMember(memberFormDto);
        } catch (Exception e) {
            log.info("error={}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/";
    }

    @PostMapping("/delete")
    public String deleteMember(@ModelAttribute("deleteMember") @Validated DeleteMemberDto deleteMemberDto, BindingResult bindingResult, Model model) {

        if (bindingResult.hasErrors()) {
            log.info("error={}", bindingResult.getFieldError());
            return "members/deleteMemberForm";
        }

        try {
            memberservice.deleteMember(deleteMemberDto);
        } catch (Exception e) {
            log.info("error={}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
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
        } catch (Exception e) {
            log.info("error={}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
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
