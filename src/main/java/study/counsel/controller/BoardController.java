package study.counsel.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import study.counsel.dto.board.*;
import study.counsel.service.BoardService;

import javax.validation.Valid;


@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;

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
    public BoardDetailDto getBoard(@PathVariable Long id) {
        log.info("글 번호 : " + id);
        return boardService.getBoard(id);
    }

    @PostMapping("/add")
    public void addBoard(@RequestBody @Valid AddBoardDto addBoardDto) {
        log.info("받은 게시글 정보 : {}", addBoardDto);
        boardService.addBoard(addBoardDto);
    }

    @PostMapping("/update/{id}")
    public void updateBoard(@PathVariable Long id, @RequestBody @Valid UpdateBoardDto updateBoardDto) {
        log.info("받은 정보 : " + id + "," + updateBoardDto);
        boardService.updateBoard(id, updateBoardDto);
    }

    @PostMapping("/delete/{id}")
    public void deleteBoard(@PathVariable Long id, @RequestBody @Valid DeleteBoardDto deleteBoardDto) {
        boardService.deleteBoard(id, deleteBoardDto);
    }

}
