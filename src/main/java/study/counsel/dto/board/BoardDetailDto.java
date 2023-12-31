package study.counsel.dto.board;

import lombok.AllArgsConstructor;
import lombok.Data;
import study.counsel.dto.comment.CommentDto;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
public class BoardDetailDto {

    @NotNull
    private Long id;

    @NotNull
    private String title;

    @NotNull
    private String text;

    @NotNull
    private String nickname;

    @NotNull
    private String memberId;

    @NotNull
    private Date date;

    @NotNull
    private List<CommentDto> comments;

    @NotNull
    private Long likeCount = 0L;


}