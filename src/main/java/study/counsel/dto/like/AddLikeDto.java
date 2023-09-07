package study.counsel.dto.like;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddLikeDto {

    @NotNull
    private Long boardId;

    @NotNull
    private Long commentId;
}
