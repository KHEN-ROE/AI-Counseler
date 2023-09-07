package study.counsel.entity;

import lombok.*;
import study.counsel.common.BaseEntity;
import study.counsel.dto.comment.AddAndUpdateCommentDto;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    private String text;

    @Setter
    private Date date;

    @Setter
    private Long likeCount = 0L;

    @Setter
    private boolean isDeleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId")
    private Member member;

    // 어느 게시글에서 쓴 건지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boardId")
    private Board board;

    @OneToMany(mappedBy = "comment")
    private List<ContentLike> contentLikes;

    public Comment(String text, Date date, Member member, Board board) {
        this.text = text;
        this.date = date;
        this.member = member;
        this.board = board;
    }

    public static Comment addComment(AddAndUpdateCommentDto addCommentDto, Member member, Board board) {
        return new Comment(addCommentDto.getText(), new Date(), member, board);
    }
}
