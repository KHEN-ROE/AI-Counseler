package study.counsel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.counsel.dto.comment.AddAndUpdateCommentDto;
import study.counsel.dto.comment.CommentDto;
import study.counsel.dto.comment.DeleteCommentDto;
import study.counsel.entity.Board;
import study.counsel.entity.Comment;
import study.counsel.entity.Member;
import study.counsel.repository.BoardRepository;
import study.counsel.repository.CommentRepository;
import study.counsel.repository.MemberRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;

    private final MemberRepository memberRepository;

    private final BoardRepository boardRepository;

    // 댓글은 모든 사람이 다 볼 수 있어야 함. 근데 비밀 댓글인 경우?(이건 나중에) + 대댓글은?
    public List<CommentDto> getComment(Long boardId) {
        List<Comment> findAll = commentRepository.findAllByBoardIdOrderByLikeCountDesc(boardId);
        return findAll.stream()
                .map(comment -> new CommentDto(comment.getId(), comment.getText(), comment.getDate(), comment.getLikeCount(), comment.getMember().getNickname(), comment.getMember().getMemberId(), comment.getBoard().getId(), comment.isDeleted()))
                .collect(Collectors.toList());
    }

    public void addComment(AddAndUpdateCommentDto addCommentDto, HttpServletRequest request) {
        // 댓글 작성자가 db에 있는 회원인지 확인

        String loginMember = (String) request.getSession().getAttribute("loginMember");

        Member findMember = memberRepository.findByMemberId(loginMember).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원"));
        Board findBoard = boardRepository.findById(addCommentDto.getBoardId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글"));
        Comment comment = Comment.addComment(addCommentDto, findMember, findBoard);
        commentRepository.save(comment);
    }

    public void updateComment(AddAndUpdateCommentDto updateCommentDto, HttpServletRequest request) {

        String loginMember = (String) request.getSession().getAttribute("loginMember");

        Board findBoard = boardRepository.findById(updateCommentDto.getBoardId()).orElseThrow(() -> new IllegalStateException("존재하지 않는 게시글"));
        Comment findComment = commentRepository.findById(updateCommentDto.getCommentId()).orElseThrow(() -> new IllegalStateException("존재하지 않는 댓글"));

        if (findComment.getMember().getMemberId().equals(loginMember)) {
            findComment.setText(updateCommentDto.getText());
            findComment.setDate(new Date());
        } else {
            throw new IllegalStateException("일치하지 않는 사용자");
        }

    }

    public void deleteComment(DeleteCommentDto deleteCommentDto, HttpServletRequest request) {

        String loginMember = (String) request.getSession().getAttribute("loginMember");

        Comment findComment = commentRepository.findById(deleteCommentDto.getCommentId())
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 댓글"));

        if (findComment.getMember().getMemberId().equals(loginMember)) {
            findComment.setDeleted(true);
        } else {
            throw new IllegalStateException("일치하지 않는 사용자");
        }
    }
}