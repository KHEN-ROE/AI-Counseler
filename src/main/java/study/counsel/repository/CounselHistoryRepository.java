package study.counsel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.counsel.entity.CounselHistory;

import java.util.List;

public interface CounselHistoryRepository extends JpaRepository<CounselHistory, Long> {

    List<CounselHistory> findByJSESSIONID(String JSESSIONID);

    List<CounselHistory> findByMemberId(Object loginMember);
}
