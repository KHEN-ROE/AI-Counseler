package study.counsel.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@AllArgsConstructor
public class CommentDto {

    @NotNull
    private Long id;

    @NotNull
    private String text;

    @NotNull
    private Date date;

    @NotNull
    private Long LikeCount = 0L;

    // 작성자
    @NotNull
    private String MemberId;

    @NotNull
    private Long boardId;

}