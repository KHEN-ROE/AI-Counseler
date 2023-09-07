package study.counsel.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.counsel.dto.board.*;
import study.counsel.dto.comment.CommentDto;
import study.counsel.entity.Board;
import study.counsel.entity.Member;
import study.counsel.repository.BoardRepository;
import study.counsel.repository.MemberRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final CommentService commentService;

    public Page<BoardListDto> getList(Pageable pageable) {

        // 엔티티 조회
        Page<Board> findAll = boardRepository.findAll(pageable);

        // 엔티티 -> dto 변환
        List<BoardListDto> collect = findAll.stream()
                .map(b -> new BoardListDto(b.getId(), b.getTitle(), b.getMember().getMemberId(), b.getDate(), b.isDeleted()))
                .collect(Collectors.toList());

        return new PageImpl<>(collect, pageable, findAll.getTotalElements());

    }

    public BoardDetailDto getBoard(Long id) {
        Board board = boardRepository.findById(id).orElseThrow(() -> new IllegalStateException("존재하지 않는 게시글"));

        // 필요한 정보를 BoardDetailDto에 담기
        String title = board.getTitle();
        String text = board.getText();
        String nickname = board.getMember().getNickname();
        String memberId = board.getMember().getMemberId();
        Date date = board.getDate();
        List<CommentDto> comments = commentService.getComment(id);
        Long likeCount = board.getLikeCount();

        return new BoardDetailDto(id, title, text, nickname, memberId, date, comments, likeCount);
    }

    public void addBoard(AddBoardDto addBoardDto, HttpServletRequest request) {

        String loginMember = (String) request.getSession().getAttribute("loginMember");
        log.info("loginMember={}", loginMember);
        Member findMember = memberRepository.findByMemberId(loginMember).orElseThrow(() -> new IllegalStateException("존재하지 않는 회원"));

        Board board = Board.addBoard(addBoardDto, findMember);

        boardRepository.save(board);

    }

    public void updateBoard(Long id, UpdateBoardDto updateBoardDto, HttpServletRequest request) {
        Board findBoard = boardRepository.findById(id).orElseThrow(() -> new IllegalStateException("존재하지 않는 게시글"));

        String loginMember = (String) request.getSession().getAttribute("loginMember");

        if (!findBoard.getMember().getMemberId().equals(loginMember)) {
            throw new IllegalArgumentException("일치하지 않는 사용자");
        }

        findBoard.setTitle(updateBoardDto.getTitle());
        findBoard.setText(updateBoardDto.getText());

    }

    public void deleteBoard(Long id, HttpServletRequest request) {

        Board findBoard = boardRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글"));

        String loginMember = (String) request.getSession().getAttribute("loginMember");

        if (!findBoard.getMember().getMemberId().equals(loginMember)) {
            throw new IllegalArgumentException("일치하지 않는 사용자");
        }

        findBoard.setDeleted(true);

    }
}
