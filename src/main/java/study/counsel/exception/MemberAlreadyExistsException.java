package study.counsel.exception;

// 사용자 정의 예외
public class MemberAlreadyExistsException extends RuntimeException {
    public MemberAlreadyExistsException(String message) {
        super(message);
    }
}
