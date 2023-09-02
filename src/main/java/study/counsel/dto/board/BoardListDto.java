package study.counsel.dto.board;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@AllArgsConstructor
public class BoardListDto {

    @NotNull
    private Long id;

    @NotNull
    private String title;

    @NotNull
    private String MemberId;

    @NotNull
    private Date date;

}