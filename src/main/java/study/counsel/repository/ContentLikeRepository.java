package study.counsel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.counsel.entity.ContentLike;

import java.util.Optional;

// 이 레포지토리 필요한 이유? 좋아요 한 번만 누를 수 있게 만드려고(아이디 조회해서 좋아요 눌렀는지 확인)
public interface ContentLikeRepository extends JpaRepository<ContentLike, Long> {

    Optional<ContentLike> findByMemberIdAndCommentId(Long memberId, Long commentId);
}
