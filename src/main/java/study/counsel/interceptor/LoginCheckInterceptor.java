package study.counsel.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import study.counsel.entity.Member;
import study.counsel.repository.MemberRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginCheckInterceptor implements HandlerInterceptor {

    private final MemberRepository memberRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) throws Exception {

        String requestURI = request.getRequestURI();

        log.info("인증 체크 인터셉터 실행 : {}", requestURI);

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loginMember") == null) {
            log.info("미인증 사용자 요청");
            log.info("{}", handler);

            // 세션 만료되면 로그아웃시킴
            response.sendRedirect("/members/logout");
            return false;
        }

        String loginMember = (String) session.getAttribute("loginMember");

        Member member = memberRepository.findByMemberId(loginMember).orElseThrow(() -> new IllegalStateException("유효하지 않은 세션"));

        if (member.isDeleted()) {
            log.info("탈퇴한 회원");
            log.info("{}", handler);
            response.sendRedirect("/");

            return false;
        }
        return true;
    }
}
