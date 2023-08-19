package study.counsel.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import study.counsel.dto.ConfirmPasswordDto;
import study.counsel.dto.DeleteMemberDto;
import study.counsel.dto.LoginDto;
import study.counsel.dto.MemberFormDto;
import study.counsel.exception.MemberAlreadyExistsException;
import study.counsel.service.MemberService;

import javax.servlet.http.HttpServletRequest;
import java.text.Bidi;


@Slf4j
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberservice;

    @GetMapping("/new")
    public String MemberForm(Model model) {
        model.addAttribute(new MemberFormDto());
        return "members/memberCreateForm";
    }

    @PostMapping("/new")
    public String createMember(@Validated @RequestBody MemberFormDto memberFormDto, BindingResult bindingResult, Model model) {

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
            model.addAttribute("errorMessage", e.getMessage());
            return "members/memberCreateForm"; // 이렇게 하면 409에러 못띄운다. MemberAlreadyExistsException가 사실상 무의미.
        }

        // 성공하면 홈으로
        return "redirect:/";
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

        return "model/updateMemberForm";

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

    @PostMapping("/login")
    public String login(@Validated @RequestBody LoginDto loginDto, BindingResult bindingResult, Model model, HttpServletRequest request) {

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
        }

        return "redirect:/";
    }

}
