package study.counsel.config;

import com.theokanning.openai.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Chat GPT 라이브러리를 사용하기전, 해당 서비스에 토큰 주입을 하기위한 Config
 */
@Slf4j
@Configuration
public class ChatGPTConfig {

    @Value("${gpt.token}")
    private String token;

    @Bean
    public OpenAiService openAiService() {
        log.info("token : {}을 활용한 OpenAIService 생성", token);
        return new OpenAiService(token, Duration.ofSeconds(60));
    }
}
