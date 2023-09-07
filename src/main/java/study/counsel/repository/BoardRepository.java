package study.counsel.repository;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import study.counsel.entity.Board;

import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {

    @Query("SELECT b FROM Board b JOIN FETCH b.member m JOIN FETCH b.comments c WHERE b.id = :id")
    Optional<Board> findWithMemberAndComments(@Param("id") Long id);

    @NotNull
    Page<Board> findAll(@NotNull Pageable pageable);
}