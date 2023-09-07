package study.counsel.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import study.counsel.config.PasswordEncrypter;
import study.counsel.dto.member.DeleteMemberDto;
import study.counsel.dto.member.MemberFormDto;
import study.counsel.entity.Member;
import study.counsel.exception.MemberAlreadyExistsException;
import study.counsel.repository.MemberRepository;
import study.counsel.service.MemberService;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberControllerTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    MemberService memberService;

    @Autowired
    PasswordEncrypter passwordEncrypter;

    // 회원가입 테스트
    @Test
    void createMember() {
        // given
        MemberFormDto memberFormDto = createForm();

        //when
        memberService.createMember(memberFormDto);

        // then
        Member member = memberRepository.findByMemberId(memberFormDto.getMemberId()).get();
        assertThat(member.getMemberId()).isEqualTo(memberFormDto.getMemberId());

    }

    // 중복확인 테스트
    @Test
    void duplicateMember() {
        //given
        MemberFormDto memberFormDto = createForm();
        MemberFormDto memberFormDto2 = createForm();

        //when
        memberService.createMember(memberFormDto);
        Throwable e = assertThrows(MemberAlreadyExistsException.class, () -> memberService.createMember(memberFormDto2));

        //then
        assertEquals("이미 존재하는 아이디", e.getMessage());
    }

    // 비번 확인 테스트
    @Test
    void confirmPassword() {
        //given
        MemberFormDto memberFormDto = createForm();

        //when
        memberService.createMember(memberFormDto);
        Member member = memberRepository.findByMemberId(memberFormDto.getMemberId()).get();

        String pwd = passwordEncrypter.encrypt("test");
        //then
        assertThat(member.getPassword()).isEqualTo(pwd);

    }

    // 회원수정 테스트
    @Test
    void updateMember() {
        //given
        MemberFormDto memberFormDto = createForm();
        memberService.createMember(memberFormDto);

        //when
        Member member = memberRepository.findByMemberId(memberFormDto.getMemberId()).get();
        member.setPassword("test2");
        member.setUsername("test2");
        member.setNickname("test2");
        member.setEmail("bbb@bbb.bbb");

        //then
        assertThat(member.getPassword()).isEqualTo("test2");
        assertThat(member.getUsername()).isEqualTo("test2");
        assertThat(member.getNickname()).isEqualTo("test2");
        assertThat(member.getEmail()).isEqualTo("bbb@bbb.bbb");

    }

    // 회원삭제 테스트
    @Test
    void deleteMember() {
        MemberFormDto memberFormDto = createForm();
        memberService.createMember(memberFormDto);

        Member findMember = memberRepository.findByMemberId(memberFormDto.getMemberId()).orElseThrow(() -> new IllegalStateException("존재하지 않는 회원"));

        String memberId = findMember.getMemberId();
        String password = findMember.getPassword();

        DeleteMemberDto deleteMemberDto = new DeleteMemberDto();

        deleteMemberDto.setMemberId(memberId);
        deleteMemberDto.setPassword(password);

        memberService.deleteMember(deleteMemberDto);

        assertThat(findMember.isDeleted()).isTrue();
    }

    public MemberFormDto createForm() {
        MemberFormDto memberFormDto = new MemberFormDto();
        memberFormDto.setMemberId("test");
        memberFormDto.setPassword("test");
        memberFormDto.setUsername("test");
        memberFormDto.setNickname("test");
        memberFormDto.setEmail("aaa@aaa.aaa");

        return memberFormDto;
    }



}