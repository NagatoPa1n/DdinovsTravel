package uz.nagato.touragency.common.exception;

/** A downstream service the request depended on could not be reached. */
public class ServiceUnavailableException extends RuntimeException {

    public ServiceUnavailableException(String message) {
        super(message);
    }
}
