package study.counsel.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import study.counsel.dto.board.BoardDetailDto;
import study.counsel.dto.comment.AddAndUpdateCommentDto;
import study.counsel.dto.comment.CommentDto;
import study.counsel.dto.like.AddLikeDto;
import study.counsel.dto.like.LikeDeleteDto;
import study.counsel.entity.Board;
import study.counsel.repository.BoardRepository;
import study.counsel.service.CommentService;
import study.counsel.service.ContentLikeService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Date;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/like")
@RequiredArgsConstructor
public class ContentLikeController {

    private final ContentLikeService contentLikeService;
    private final CommentService commentService;
    private final BoardRepository boardRepository;

    @GetMapping("/getBoardLike/{boardId}")
    @ResponseBody
    public ResponseEntity<?> getBoardLike(@PathVariable Long boardId) {

        log.info("boardId={}", boardId);

        try {
            Long like = contentLikeService.getBoardLike(boardId);
            return ResponseEntity.ok(like);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/getCommentLike/{commentId}")
    @ResponseBody
    public ResponseEntity<?> getCommentLike(@PathVariable Long commentId) {
        try {
            Long like = contentLikeService.getCommentLike(commentId);
            return ResponseEntity.ok(like);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/addBoardLike/{boardId}")
    public String addBoardLike(@PathVariable Long boardId, Model model, HttpServletRequest request) {

        log.info("boardId={}", boardId);

        Board findBoard = boardRepository.findById(boardId).orElseThrow(() -> new IllegalStateException("존재하지 않는 게시글"));
        // 필요한 정보를 BoardDetailDto에 담기
        String title = findBoard.getTitle();
        String text = findBoard.getText();
        String nickname = findBoard.getMember().getNickname();
        String memberId = findBoard.getMember().getMemberId();
        Date date = findBoard.getDate();
        List<CommentDto> comments = commentService.getComment(boardId);
        Long likeCount = findBoard.getLikeCount();

        BoardDetailDto board = new BoardDetailDto(boardId, title, text, nickname, memberId, date, comments, likeCount);

        try {
            contentLikeService.addBoardLike(boardId, request);
            model.addAttribute("board", board);
            model.addAttribute("comments,", comments);
            model.addAttribute("addAndUpdateCommentDto", new AddAndUpdateCommentDto());

        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
        }

        return "/board/boardDetail";
    }

    @PostMapping("/addCommentLike")
    public String addCommentLike(@Valid AddLikeDto addLikeDto, BindingResult bindingResult, Model model, HttpServletRequest request) {
        log.info("받은 Like 정보 : " + addLikeDto);

        if (bindingResult.hasErrors()) {
            model.addAttribute("error={}", bindingResult.getFieldError());
        }

        Board findBoard = boardRepository.findById(addLikeDto.getBoardId()).orElseThrow(() -> new IllegalStateException("존재하지 않는 게시글"));
        // 필요한 정보를 BoardDetailDto에 담기
        Long id = findBoard.getId();
        String title = findBoard.getTitle();
        String text = findBoard.getText();
        String nickname = findBoard.getMember().getNickname();
        String memberId = findBoard.getMember().getMemberId();
        Date date = findBoard.getDate();
        List<CommentDto> comments = commentService.getComment(addLikeDto.getBoardId());
        Long likeCount = findBoard.getLikeCount();

        BoardDetailDto board = new BoardDetailDto(id, title, text, nickname, memberId, date, comments, likeCount);

        try {
            contentLikeService.addCommentLike(addLikeDto, request);
            model.addAttribute("board", board);
            model.addAttribute("comments,", comments);
            model.addAttribute("addAndUpdateCommentDto", new AddAndUpdateCommentDto());

        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
        }

        return "/board/boardDetail";
    }

    @PostMapping("/delete")
    public String deleteLike(@Valid LikeDeleteDto LikeDeleteDto) {
        contentLikeService.deleteLike(LikeDeleteDto);

        return "/board/boardDetail";
    }
}
