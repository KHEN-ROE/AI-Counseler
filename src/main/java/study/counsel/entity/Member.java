package study.counsel.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import study.counsel.common.BaseEntity;
import study.counsel.dto.member.MemberFormDto;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {


    @Id @GeneratedValue
    private Long id;

    private String memberId;

    @Setter
    private String password;

    @Setter
    private String username;

    @Setter
    private String nickname;

    @Setter
    private String email;

    @Setter
    private boolean isDeleted = false;

    public Member(String memberId, String password, String username, String nickname, String email) {
        this.memberId = memberId;
        this.password = password;
        this.username = username;
        this.nickname = nickname;
        this.email = email;
    }

    public static Member createMember(MemberFormDto memberFormDto, String encryptedPassword) {
        return new Member(memberFormDto.getMemberId(), encryptedPassword,
                memberFormDto.getUsername(), memberFormDto.getNickname(), memberFormDto.getEmail());
    }






}
