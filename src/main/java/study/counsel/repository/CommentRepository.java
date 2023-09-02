package study.counsel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.counsel.entity.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByBoardIdOrderByLikeCountDesc(Long BoardId);
}