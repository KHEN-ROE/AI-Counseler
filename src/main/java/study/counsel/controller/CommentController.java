package study.counsel.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import study.counsel.dto.comment.AddAndUpdateCommentDto;
import study.counsel.dto.comment.CommentDto;
import study.counsel.dto.comment.DeleteCommentDto;
import study.counsel.service.CommentService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {


    private final CommentService commentService;

    @GetMapping("/get/{boardId}")
    public List<CommentDto> getComment(@PathVariable Long boardId) {
        log.info("글번호 : " + boardId);
        return commentService.getComment(boardId);
    }

    @PostMapping("/add")
    public void addComment(@RequestBody @Valid AddAndUpdateCommentDto addCommentDto) {
        commentService.addComment(addCommentDto);
    }

    @PostMapping("/update/{id}")
    public void updateComment(@PathVariable Long id, @RequestBody @Valid AddAndUpdateCommentDto updateCommentDto) {
        log.info("받은 댓글 정보 : " + id + "," + updateCommentDto);
        commentService.updateComment(id, updateCommentDto);
    }

    @PostMapping("/delete/{id}")
    public void deleteComment(@PathVariable Long id, @RequestBody @Valid DeleteCommentDto deleteCommentDto) {
        log.info("받은 정보 : " + id + "," + deleteCommentDto);
        commentService.deleteComment(id, deleteCommentDto);
    }
}