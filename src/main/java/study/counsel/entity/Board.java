package study.counsel.entity;

import lombok.*;
import study.counsel.common.BaseEntity;
import study.counsel.dto.board.AddBoardDto;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Board extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "boardId")
    private Long id;

    @Setter
    private String title;

    @Setter
    @Column(length = 1000)
    private String text;

    @Setter
    private Date date;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId")
    private Member member;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContentLike> contentLikes;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    @Setter
    private boolean isDeleted;

    public Board(String title, String text, Date date, Member member) {

        this.title = title;
        this.text = text;
        this.date = date;
        this.member = member;
    }

    public static Board addBoard(AddBoardDto addBoardDto, Member member) {
        return new Board(addBoardDto.getTitle(), addBoardDto.getText(), new Date(), member);
    }
}
