package study.counsel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.counsel.entity.ChatSequenceNumber;

public interface ChatSequenceNumberRepository extends JpaRepository<ChatSequenceNumber, Long> {
}
