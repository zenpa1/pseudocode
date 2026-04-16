package pseudocode_compiler;

/**
 * Token data transfer object used by scanner output.
 */
class Token {

    private final String type;
    private final String lexeme;
    private final boolean isError;
    private final String errorMessage;
    private int lineNum;

    public Token(String type, String lexeme) {
        this.type = type;
        this.lexeme = lexeme;
        this.isError = false;
        this.errorMessage = null;
        this.lineNum = -1;
    }

    public Token(String errorMessage) {
        this.type = "ERROR";
        this.lexeme = "";
        this.isError = true;
        this.errorMessage = errorMessage;
        this.lineNum = -1;
    }

    public boolean isError() {
        return isError;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getType() {
        return type;
    }

    public String getLexeme() {
        return lexeme;
    }

    public int getLineNum() {
        return lineNum;
    }

    public void setLineNum(int lineNum) {
        this.lineNum = lineNum;
    }

    @Override
    public String toString() {
        if (isError) {
            return "(ERROR: " + errorMessage + ")";
        }
        return "(" + type + ", " + lexeme + ")";
    }
}
