package study.counsel.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.counsel.config.PasswordEncrypter;
import study.counsel.dto.ConfirmPasswordDto;
import study.counsel.dto.DeleteMemberDto;
import study.counsel.dto.LoginDto;
import study.counsel.dto.MemberFormDto;
import study.counsel.entity.Member;
import study.counsel.exception.MemberAlreadyExistsException;
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
                            throw new MemberAlreadyExistsException("이미 존재하는 아이디");
                        }
                );

        memberRepository.findByEmail(memberFormDto.getEmail())
                .ifPresent(member ->
                        {
                            throw new MemberAlreadyExistsException("이미 존재하는 이메일");
                        }
                );

        // 비밀번호 암호화
        String encryptedPassword = passwordEncrypter.encrypt(memberFormDto.getPassword());

        // 중복아니면 신규가입
        Member member = Member.createMember(memberFormDto, encryptedPassword);
        memberRepository.save(member);
    }

    public void updateMember(MemberFormDto memberFormDto) {

        Member member = memberRepository.findByMemberId(memberFormDto.getMemberId())
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 회원"));

        member.setPassword(memberFormDto.getPassword());
        member.setNickname(memberFormDto.getNickname());
        member.setUsername(memberFormDto.getUsername());
        member.setEmail(memberFormDto.getEmail());

    }

    public void confirmPassword(ConfirmPasswordDto confirmPasswordDto) {

        Member findMember = memberRepository.findByMemberId(confirmPasswordDto.getMemberId())
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 회원"));

        String encryptedPassword = passwordEncrypter.encrypt(confirmPasswordDto.getPassword());

        try {
            if (findMember.getPassword().equals(encryptedPassword)) {
                log.info("패스워드 일치");
            }
        } catch (Exception e) {
            throw new IllegalStateException("비밀번호 불일치");
        }
    }

    public void deleteMember(DeleteMemberDto deleteMemberDto) {

        Member findMember = memberRepository.findByMemberId(deleteMemberDto.getMemberId())
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 사용자"));

        String encryptedPwd = passwordEncrypter.encrypt(deleteMemberDto.getPassword());
        if (findMember.getPassword().equals(encryptedPwd)) {
            findMember.setDeleted(true); // 논리적 삭제
        }
    }

    public void login(LoginDto loginDto, HttpServletRequest request) {

        Member findMember = memberRepository.findByMemberId(loginDto.getMemberId()).orElseThrow(() -> new IllegalStateException("존재하지 않는 유저"));
        String encryptedPwd = passwordEncrypter.encrypt(loginDto.getPassword());

        if (findMember.getPassword().equals(encryptedPwd)) {
            // 로그인 성공시 쿠키에 JSESSIONID 저장
            HttpSession session = request.getSession();
            // 세션에 사용자 id 저장
            session.setAttribute("loginMember", loginDto.getMemberId());
        } else {
            throw new IllegalStateException("비밀번호 불일치"); // 나중에 예외처리
        }

    }

    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession();

        if (session != null) {
            session.invalidate();
        }
    }
}
