package pseudocode_compiler;

/**
 * Exception thrown to break out of a loop.
 * Used internally by the Interpreter to handle break statements.
 */
public class BreakException extends RuntimeException {
    /**
     * Constructs a BreakException.
     */
    public BreakException() {
        super("break");
    }
}
