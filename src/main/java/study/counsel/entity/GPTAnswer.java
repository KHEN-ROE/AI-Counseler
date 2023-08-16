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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gpt_answer", length = 1500, nullable = false)
    private String answer;

    public GPTAnswer(String answer) {
        this.answer = answer;
    }
}
