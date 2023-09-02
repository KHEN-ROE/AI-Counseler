package study.counsel.dto.board;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateBoardDto {

    private String title;
    private String text;

    @NotNull
    private String MemberId;
}