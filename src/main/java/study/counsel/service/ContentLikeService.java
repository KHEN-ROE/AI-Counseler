package study.counsel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.counsel.dto.like.AddLikeDto;
import study.counsel.dto.like.LikeDeleteDto;
import study.counsel.entity.Board;
import study.counsel.entity.Comment;
import study.counsel.entity.ContentLike;
import study.counsel.entity.Member;
import study.counsel.repository.BoardRepository;
import study.counsel.repository.CommentRepository;
import study.counsel.repository.ContentLikeRepository;
import study.counsel.repository.MemberRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ContentLikeService {

    private final ContentLikeRepository contentLikeRepository;

    private final MemberRepository memberRepository;

    private final BoardRepository boardRepository;

    private final CommentRepository commentRepository;

    public Long getBoardLike(Long boardId) {
        Board findBoard = boardRepository.findById(boardId).orElseThrow(() -> new IllegalStateException("존재하지 않는 게시글"));
        return findBoard.getLikeCount();
    }

    public Long getCommentLike(Long commentId) {
        Comment findComment = commentRepository.findById(commentId).orElseThrow(() -> new IllegalStateException("존재하지 않는 댓글"));
        return findComment.getLikeCount();
    }

    public void addBoardLike(Long boardId, HttpServletRequest request) {

        String loginMember = (String) request.getSession().getAttribute("loginMember");

        Member findMember = memberRepository.findByMemberId(loginMember).orElseThrow(() -> new IllegalStateException("존재하지 않는 회원"));
        Long memberId = findMember.getId();

        Optional<ContentLike> findLike = contentLikeRepository.findByMemberIdAndBoardId(memberId, boardId);

        if (findLike.isPresent()) {
            throw new IllegalStateException("좋아요는 한 번만 가능");
        }

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 게시판"));

        ContentLike contentLike = ContentLike.addBoardLike(findMember, board);
        contentLikeRepository.save(contentLike);

        board.setLikeCount(board.getLikeCount() + 1);
        boardRepository.save(board);

    }

    public void addCommentLike(AddLikeDto addLikeDto, HttpServletRequest request) {

        String loginMember = (String) request.getSession().getAttribute("loginMember");

        Member findMember = memberRepository.findByMemberId(loginMember).orElseThrow(() -> new IllegalStateException("존재하지 않는 회원"));
        Long memberId = findMember.getId();

        Optional<ContentLike> findLike = contentLikeRepository.findByMemberIdAndCommentId(memberId, addLikeDto.getCommentId());

        if (findLike.isPresent()) {
            throw new IllegalStateException("좋아요는 한 번만 가능");
        }

        Board board = boardRepository.findById(addLikeDto.getBoardId())
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 게시판"));

        Comment comment = commentRepository.findById(addLikeDto.getCommentId())
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 댓글"));

        ContentLike contentLike = ContentLike.addCommentLike(findMember, board, comment);
        contentLikeRepository.save(contentLike);

        comment.setLikeCount(comment.getLikeCount() + 1);
        commentRepository.save(comment);
    }

    public void deleteLike(LikeDeleteDto LikeDeleteDto) {
        Comment comment = commentRepository.findById(LikeDeleteDto.getCommentId()).orElseThrow(() -> new IllegalStateException("존재하지 않는 댓글"));
        comment.setLikeCount(comment.getLikeCount() - 1);
    }



}