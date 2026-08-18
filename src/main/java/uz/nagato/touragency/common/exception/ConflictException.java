package uz.nagato.touragency.common.exception;

/** Thrown when a request collides with existing state: duplicate email, slug, and so on. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
