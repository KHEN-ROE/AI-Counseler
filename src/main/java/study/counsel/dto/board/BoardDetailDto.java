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
    private String userId;

    @NotNull
    private Date date;

    @NotNull
    private List<CommentDto> comments;


}