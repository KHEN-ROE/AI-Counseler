package study.counsel.repository;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import study.counsel.entity.Board;

public interface BoardRepository extends JpaRepository<Board, Long> {
    @NotNull
    Page<Board> findAll(@NotNull Pageable pageable);
}