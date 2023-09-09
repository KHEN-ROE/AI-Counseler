package study.counsel.exception;

public class EmailDuplicateException extends IllegalStateException {
    public EmailDuplicateException() {
        super();
    }

    public EmailDuplicateException(String s) {
        super(s);
    }

    public EmailDuplicateException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmailDuplicateException(Throwable cause) {
        super(cause);
    }
}
