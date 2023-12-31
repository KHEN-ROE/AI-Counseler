package study.counsel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.counsel.entity.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByMemberId(String id);

    Optional<Member> findByEmail(String email);

    Optional<Member> findByNickname(String nickname);

}
