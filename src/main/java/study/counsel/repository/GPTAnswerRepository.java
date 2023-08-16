package study.counsel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.counsel.entity.GPTAnswer;

public interface GPTAnswerRepository extends JpaRepository<GPTAnswer, Long> {
}
