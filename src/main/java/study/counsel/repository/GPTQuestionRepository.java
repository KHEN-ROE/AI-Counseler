package study.counsel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.counsel.entity.GPTAnswer;
import study.counsel.entity.GPTQuestion;

public interface GPTQuestionRepository extends JpaRepository<GPTQuestion, Long> {
}
