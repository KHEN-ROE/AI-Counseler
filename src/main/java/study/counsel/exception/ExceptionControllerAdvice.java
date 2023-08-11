package study.counsel.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

// 애플리케이션에서 발생하는 모든 예외를 여기서 처리
@ControllerAdvice
public class ExceptionControllerAdvice {

    @ExceptionHandler(MemberAlreadyExistsException.class)
    public ResponseEntity<String> conflictHandler(MemberAlreadyExistsException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
}
