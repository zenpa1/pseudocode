package pseudocode_compiler;

/**
 * Custom exception thrown during interpretation when errors occur.
 * Reports the error message along with the line number from the source file.
 */
public class InterpreterException extends Exception {

    private final int lineNumber;

    /**
     * Constructs an InterpreterException with a message and AST node.
     *
     * @param message the error message
     * @param node    the ASTNode where the error occurred (used to get line number)
     */
    public InterpreterException(String message, ASTNode node) {
        super(message);
        this.lineNumber = node != null ? node.getLineNumber() : -1;
    }

    /**
     * Constructs an InterpreterException with a message and explicit line number.
     *
     * @param message    the error message
     * @param lineNumber the line number in the source file
     */
    public InterpreterException(String message, int lineNumber) {
        super(message);
        this.lineNumber = lineNumber;
    }

    /**
     * Gets the line number where the error occurred.
     *
     * @return the line number, or -1 if not available
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Returns a formatted error message including the line number.
     *
     * @return the error message with line number information
     */
    @Override
    public String toString() {
        if (lineNumber >= 0) {
            return "InterpreterException at line " + lineNumber + ": " + getMessage();
        }
        return "InterpreterException: " + getMessage();
    }
}
