package pseudocode_compiler;

/**
 * Exception thrown to continue to the next iteration of a loop.
 * Used internally by the Interpreter to handle continue statements.
 */
public class ContinueException extends RuntimeException {
    /**
     * Constructs a ContinueException.
     */
    public ContinueException() {
        super("continue");
    }
}
