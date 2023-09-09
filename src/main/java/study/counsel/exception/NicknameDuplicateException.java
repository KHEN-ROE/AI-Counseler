package study.counsel.exception;

public class NicknameDuplicateException extends IllegalStateException {

    public NicknameDuplicateException() {
        super();
    }

    public NicknameDuplicateException(String s) {
        super(s);
    }

    public NicknameDuplicateException(String message, Throwable cause) {
        super(message, cause);
    }

    public NicknameDuplicateException(Throwable cause) {
        super(cause);
    }
}
