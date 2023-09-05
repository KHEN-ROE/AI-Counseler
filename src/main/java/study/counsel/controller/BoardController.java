package study.counsel.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import study.counsel.dto.board.*;
import study.counsel.dto.comment.AddAndUpdateCommentDto;
import study.counsel.dto.comment.CommentDto;
import study.counsel.service.BoardService;
import study.counsel.service.CommentService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;


@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;
    private final CommentService commentService;

    @GetMapping("/view")
    public String getList(@RequestParam(name="page", defaultValue="0") int page, @RequestParam(name="size", defaultValue="10") int size, Model model) { // 요청된 페이지 번호, 한 페이지에 보여줄 게시글 개수
        log.info("page : " + page + "," + "size : " + size);
        Pageable pageable = PageRequest.of(page, size); //page와 size를 기반으로 한 Pageable 객체 생성

        try {
            Page<BoardListDto> list = boardService.getList(pageable);
            model.addAttribute("list", list);
        } catch (Exception e) {
            log.info("error={}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "board/boardList";
    }

    @GetMapping("/view/{id}")
    public String getBoard(@PathVariable Long id, Model model) {
        log.info("글 번호 : " + id);
        try {
            BoardDetailDto board = boardService.getBoard(id);
            model.addAttribute("board", board);

            List<CommentDto> comments = commentService.getComment(id);
            model.addAttribute("comments", comments);
            model.addAttribute("addAndUpdateCommentDto", new AddAndUpdateCommentDto());
        } catch (Exception e) {
            log.info("error={}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
        }

        return "board/boardDetail";
    }

    @GetMapping("/add")
    public String addBoardForm(Model model) {
        model.addAttribute(new AddBoardDto());
        return "board/addBoardForm";
    }

    @PostMapping("/add")
    public String addBoard(@Valid AddBoardDto addBoardDto, BindingResult bindingResult, Model model, HttpServletRequest request) {
        log.info("받은 게시글 정보 : {}", addBoardDto);

        if (bindingResult.hasErrors()) {
            model.addAttribute("error", bindingResult.getFieldError());
        }

        try {
            boardService.addBoard(addBoardDto, request);
        } catch (Exception e) {
            model.addAttribute("error={}", e.getMessage());
        }
        return "redirect:/board/view";
    }

    @GetMapping("/update/{id}")
    public String updateBoardForm(@PathVariable Long id, Model model) {
        BoardDetailDto board = boardService.getBoard(id);
        model.addAttribute("board", board);

        return "board/updateBoardForm";
    }

    @PostMapping("/update/{id}")
    public String updateBoard(@PathVariable Long id, @Valid UpdateBoardDto updateBoardDto, BindingResult bindingResult, Model model, HttpServletRequest request) {
        log.info("받은 정보 : " + id + "," + updateBoardDto);

        if (bindingResult.hasErrors()) {
            model.addAttribute("error", bindingResult.getFieldError());
        }

        try {
            boardService.updateBoard(id, updateBoardDto, request);
            BoardDetailDto updatedBoard = boardService.getBoard(id);
            model.addAttribute("board", updatedBoard); // 수정한 게시글 정보 모델에 추가
            model.addAttribute("addAndUpdateCommentDto", new AddAndUpdateCommentDto());


        } catch (Exception e) {
            model.addAttribute("error={}", e.getMessage());
        }

        return "redirect:/board/view/" + id;
    }

    @PostMapping("/delete/{id}")
    public String deleteBoard(@PathVariable Long id, HttpServletRequest request, Model model) {
        try {
            boardService.deleteBoard(id, request);
        } catch (Exception e) {
            model.addAttribute("error={}", e.getMessage());
        }

        return "redirect:/board/view";
    }

}
