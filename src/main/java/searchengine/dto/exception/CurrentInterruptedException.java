package searchengine.dto.exception;

public class CurrentInterruptedException extends InterruptedException {

    public CurrentInterruptedException(String message) {
        super(message);
    }
}
