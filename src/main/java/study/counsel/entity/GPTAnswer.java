package study.counsel.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.eclipse.jdt.internal.compiler.parser.RecoveredPackageVisibilityStatement;
import study.counsel.common.BaseEntity;

import javax.persistence.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GPTAnswer extends BaseEntity {

    // Member와 조인 필요
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Long id;

    @Column(name = "gpt_answer", length = 1500, nullable = false)
    private String answer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId")
    private Member member;

    public GPTAnswer(String answer, Member member) {
        this.answer = answer;
        this.member = member;
    }
}
