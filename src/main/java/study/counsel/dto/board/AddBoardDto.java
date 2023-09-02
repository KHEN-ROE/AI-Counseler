package study.counsel.dto.board;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddBoardDto {

    @NotNull(message = "제목은 필수 값")
    private String title;

    @NotNull(message = "내용은 필수 값")
    private String text;

    @NotNull(message = "id는 필수 값")
    private String memberId;

    @NotNull
    private Date date;


}