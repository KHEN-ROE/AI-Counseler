package study.counsel.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import study.counsel.dto.board.BoardDetailDto;
import study.counsel.dto.comment.AddAndUpdateCommentDto;
import study.counsel.dto.comment.CommentDto;
import study.counsel.dto.comment.DeleteCommentDto;
import study.counsel.service.BoardService;
import study.counsel.service.CommentService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {


    private final CommentService commentService;
    private final BoardService boardService;

    @GetMapping("/get/{boardId}")
    public String getComment(@PathVariable Long boardId, Model model, HttpServletRequest request) {
        log.info("글번호 : " + boardId);

        String loginMember = (String) request.getSession().getAttribute("loginMember");

        try {
            List<CommentDto> comments = commentService.getComment(boardId);
            model.addAttribute("comments", comments);
            model.addAttribute("loginMember", loginMember);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
        }

        return "/board/boardDetail";
    }

    @PostMapping("/add")
    public String addComment(@Valid AddAndUpdateCommentDto addCommentDto, BindingResult bindingResult, Model model, HttpServletRequest request) {

        log.info("addCommentDto={}", addCommentDto);

        if (bindingResult.hasErrors()) {
            model.addAttribute("error",bindingResult.getFieldError());
        }

        try {
            commentService.addComment(addCommentDto, request);
            List<CommentDto> comments = commentService.getComment(addCommentDto.getBoardId());

            model.addAttribute("comments", comments);

            BoardDetailDto board = boardService.getBoard(addCommentDto.getBoardId());
            model.addAttribute("board", board);
        } catch (Exception e) {
            log.info("errorMessage={}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
        }

        Long boardId = addCommentDto.getBoardId();

        return "redirect:/board/view/" + boardId;
    }

    @PostMapping("/update")
    public String updateComment(@Valid AddAndUpdateCommentDto updateCommentDto, BindingResult bindingResult, Model model, HttpServletRequest request) {
        log.info("받은 댓글 정보={}", updateCommentDto);

        if (bindingResult.hasErrors()) {
            model.addAttribute("error", bindingResult.getFieldError());
        }

        try {
            commentService.updateComment(updateCommentDto, request);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
        }

        Long boardId = updateCommentDto.getBoardId();

        return "redirect:/board/view/" + boardId;

    }

    @PostMapping("/delete")
    public String deleteComment(@Valid DeleteCommentDto deleteCommentDto, HttpServletRequest request, Model model) {
        try {
            commentService.deleteComment(deleteCommentDto, request);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
        }

        Long boardId = deleteCommentDto.getBoardId();

        return "redirect:/board/view/" + boardId;
    }
}