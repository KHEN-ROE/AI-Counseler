package study.counsel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.counsel.dto.like.AddLikeDto;
import study.counsel.dto.like.LikeDeleteDto;
import study.counsel.entity.Board;
import study.counsel.entity.Comment;
import study.counsel.entity.Like;
import study.counsel.entity.Member;
import study.counsel.repository.BoardRepository;
import study.counsel.repository.CommentRepository;
import study.counsel.repository.LikeRepository;
import study.counsel.repository.MemberRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeService {

    private final LikeRepository likeRepository;

    private final MemberRepository memberRepository;

    private final BoardRepository boardRepository;

    private final CommentRepository commentRepository;

    public Long getLike(Long commentId) {
        Comment findComment = commentRepository.findById(commentId).orElseThrow(() -> new IllegalStateException("존재하지 않는 댓글"));
        return findComment.getLikeCount();
    }

    public void addLike(AddLikeDto addLikeDto) {

        Member findMember = memberRepository.findByMemberId(addLikeDto.getMemberId()).orElseThrow(() -> new IllegalStateException("존재하지 않는 회원"));
        Long memberId = findMember.getId();


        Optional<Like> findLike = likeRepository.findByMemberIdAndCommentId(memberId, addLikeDto.getCommentId());

        if (findLike.isPresent()) {
            throw new IllegalStateException("좋아요는 한 번만 가능");
        }

        Board board = boardRepository.findById(addLikeDto.getBoardId())
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 게시판"));

        Comment comment = commentRepository.findById(addLikeDto.getCommentId())
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 댓글"));

        Like like = Like.addLike(findMember, board, comment);
        likeRepository.save(like);

        comment.setLikeCount(comment.getLikeCount() + 1);
        commentRepository.save(comment);
    }

    public void deleteLike(LikeDeleteDto LikeDeleteDto) {
        Comment comment = commentRepository.findById(LikeDeleteDto.getCommentId()).orElseThrow(() -> new IllegalStateException("존재하지 않는 댓글"));
        comment.setLikeCount(comment.getLikeCount() - 1);
    }
}