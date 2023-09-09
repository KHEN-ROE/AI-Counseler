package study.counsel.exception;

// 사용자 정의 예외
public class UserDuplicateException extends IllegalStateException {

    public UserDuplicateException() {
        super();
    }

    public UserDuplicateException(String s) {
        super(s);
    }

    public UserDuplicateException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserDuplicateException(Throwable cause) {
        super(cause);
    }
}
