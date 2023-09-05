package study.counsel.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddAndUpdateCommentDto {

    @NotNull
    private String MemberId;

    @NotNull
    private String text;

    @NotNull
    private Long boardId;
}
