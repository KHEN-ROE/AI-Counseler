package study.counsel.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.counsel.config.PasswordEncrypter;
import study.counsel.dto.member.*;
import study.counsel.entity.Member;
import study.counsel.exception.EmailDuplicateException;
import study.counsel.exception.NicknameDuplicateException;
import study.counsel.exception.UserDuplicateException;
import study.counsel.exception.UserNotFoundException;
import study.counsel.repository.MemberRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncrypter passwordEncrypter;

    public void createMember(MemberFormDto memberFormDto) {

        // 중복 회원 검증. 중복이면 409에러(conflict)
        memberRepository.findByMemberId(memberFormDto.getMemberId())
                .ifPresent(member ->
                        {
                            throw new UserDuplicateException("이미 존재하는 아이디");
                        }
                );

        memberRepository.findByEmail(memberFormDto.getEmail())
                .ifPresent(member ->
                        {
                            throw new EmailDuplicateException("이미 존재하는 이메일");
                        }
                );

        memberRepository.findByNickname(memberFormDto.getNickname())
                .ifPresent(member ->
                        {
                            throw new NicknameDuplicateException("이미 존재하는 별명");
                        }
                );

        // 비밀번호 암호화
        String encryptedPassword = passwordEncrypter.encrypt(memberFormDto.getPassword());

        // 중복아니면 신규가입
        Member member = Member.createMember(memberFormDto, encryptedPassword);
        memberRepository.save(member);
    }

    public void updateMember(UpdateMemberFormDto memberFormDto, HttpServletRequest request) {

        String loginMember = (String) request.getSession().getAttribute("loginMember");

        Member member = memberRepository.findByMemberId(loginMember)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 회원"));

        memberRepository.findByNickname(memberFormDto.getNickname()).ifPresent(m -> {
            if (!m.getMemberId().equals(member.getMemberId())) { // db에 있는 id와, 현재 로그인한 회원의 id가 같은지 체크
                throw new NicknameDuplicateException("이미 존재하는 닉네임");
            }
        });

        memberRepository.findByEmail(memberFormDto.getEmail()).ifPresent(m -> {
            if (!m.getMemberId().equals(member.getMemberId())) {
                throw new EmailDuplicateException("이미 존재하는 이메일");
            }
        });

        String encryptedPassword = passwordEncrypter.encrypt(memberFormDto.getPassword());

        member.setPassword(encryptedPassword);
        member.setNickname(memberFormDto.getNickname());
        member.setUsername(memberFormDto.getUsername());
        member.setEmail(memberFormDto.getEmail());

    }

    public void confirmPassword(ConfirmPasswordDto confirmPasswordDto, HttpServletRequest request) {

        String loginMember = (String) request.getSession().getAttribute("loginMember");

        Member findMember = memberRepository.findByMemberId(loginMember)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 회원"));

        String encryptedPassword = passwordEncrypter.encrypt(confirmPasswordDto.getPassword());

        if (!findMember.getPassword().equals(encryptedPassword)) {
            throw new IllegalStateException("비밀번호 불일치");
        }
    }

    public void deleteMember(DeleteMemberDto deleteMemberDto, HttpServletRequest request) {

        String loginMember = (String) request.getSession().getAttribute("loginMember");

        Member findMember = memberRepository.findByMemberId(loginMember)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 사용자"));

        String encryptedPwd = passwordEncrypter.encrypt(deleteMemberDto.getPassword());
        if (findMember.getPassword().equals(encryptedPwd)) {
            findMember.setDeleted(true); // 논리적 삭제
        } else {
            throw new IllegalStateException("비밀번호 불일치");
        }
    }

    public void login(LoginDto loginDto, HttpServletRequest request) {

        Member findMember = memberRepository.findByMemberId(loginDto.getMemberId()).orElseThrow(() -> new UserNotFoundException("존재하지 않는 계정입니다."));
        String encryptedPwd = passwordEncrypter.encrypt(loginDto.getPassword());

        if (findMember.getPassword().equals(encryptedPwd)) {
            // 로그인 성공시 쿠키에 JSESSIONID 저장
            HttpSession session = request.getSession();
            // 세션에 사용자 id 저장
            session.setAttribute("loginMember", loginDto.getMemberId());
        } else {
            throw new IllegalStateException("비밀번호가 일치하지 않습니다.");
        }

    }

    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession();

        if (session != null) {
            session.invalidate();
        }
    }
}
