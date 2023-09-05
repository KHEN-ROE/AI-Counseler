package study.counsel.dto.board;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddBoardDto {

    @NotNull(message = "제목은 필수 값")
    private String title;

    @NotNull(message = "내용은 필수 값")
    private String text;

}