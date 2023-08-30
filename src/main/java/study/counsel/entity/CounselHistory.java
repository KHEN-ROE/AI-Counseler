package study.counsel.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import study.counsel.common.BaseEntity;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CounselHistory extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "counselHistory_id")
    private Long id;

    private String title;

    @Column(name = "gpt_question", length = 1500, nullable = false)
    private String question;

    @Column(name = "gpt_answer", length = 1500, nullable = false)
    private String answer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatSequenceNumber_id")
    private ChatSequenceNumber chatSequenceNumber;

    private String counselMode;

    public CounselHistory(String title, String question, String answer, Member member, ChatSequenceNumber chatSequenceNumber, String counselMode) {
        this.title = title;
        this.question = question;
        this.answer = answer;
        this.member = member;
        this.chatSequenceNumber = chatSequenceNumber;
        this.counselMode = counselMode;
    }
}
