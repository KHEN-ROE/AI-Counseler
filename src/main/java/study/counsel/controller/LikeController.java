package study.counsel.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import study.counsel.dto.like.AddLikeDto;
import study.counsel.dto.like.LikeDeleteDto;
import study.counsel.service.LikeService;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/like")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @GetMapping("/get/{commentId}")
    public Long getLike(@PathVariable Long commentId) {
        return likeService.getLike(commentId);
    }

    @PostMapping("/add")
    public void addLike(@RequestBody @Valid AddLikeDto addLikeDto) {
        log.info("받은 Like 정보 : " + addLikeDto);
        likeService.addLike(addLikeDto);
    }

    @PostMapping("/delete")
    public void deleteLike(@RequestBody @Valid LikeDeleteDto LikeDeleteDto) {
        likeService.deleteLike(LikeDeleteDto);
    }
}
