package study.counsel.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import study.counsel.common.BaseEntity;

import javax.persistence.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GPTQuestion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long id;

    @Column(name = "gpt_question", length = 1500, nullable = false)
    private String question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id")
    private GPTAnswer answer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    public GPTQuestion(String question, GPTAnswer answer, Member member) {
        this.question = question;
        this.answer = answer;
        this.member = member;
    }
}
