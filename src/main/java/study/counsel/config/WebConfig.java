package study.counsel.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import study.counsel.interceptor.LoginCheckInterceptor;
import study.counsel.repository.MemberRepository;

import javax.servlet.http.HttpServletRequest;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LoginCheckInterceptor loginCheckInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(loginCheckInterceptor)
                .order(1)
                .addPathPatterns("/**") // 모든 경로에 적용
                .excludePathPatterns("/", "/members/join", "/members/login", "/members/logout", "/error", "/favicon.ico", "/**.css"); // 미적용
    }
}
