package pseudocode_compiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Lexical scanner for the pseudocode language.
 *
 * <p>
 * This scanner performs single-pass tokenization directly from a character
 * stream. It is intentionally explicit (rather than regex-heavy) to make
 * lexical edge cases, recovery behavior, and diagnostics easier to reason about
 * in a teaching context.
 */
public class Scanner {

    private BufferedReader reader;
    private int ch;
    //Central token hashmap for fixed/reserved lexemes.
    private TokenHashMap tokenHashMap = new TokenHashMap();
    //Reference to identifier symbol table used by the scanner.
    private SymbolTable symbolTable;
    private int currentLine = 1;
    //Absolute 1-based character position in the input stream.
    private int currentPosition = 0;
    //Tracks characters in the current physical source line for richer diagnostics.
    private StringBuilder currentLineBuffer = new StringBuilder();
    //Stores the most recently completed physical source line.
    private String lastCompletedLine = "";
    private Token pendingError = null;

    Scanner(File inputFile, SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        try {
            reader = new BufferedReader(new FileReader(inputFile));
            ch = readChar();
        } catch (IOException e) {
            System.out.println("Could not open file: " + e.getMessage());
        }
    }

    /**
     * Reads one character from the source stream while updating scanner
     * diagnostics state.
     */
    private int readChar() throws IOException {
        int nextChar = reader.read();
        if (nextChar != -1) {
            currentPosition++;

            //Keep source-line snapshots updated so error messages can show full lines.
            char read = (char) nextChar;
            if (read == '\n') {
                lastCompletedLine = currentLineBuffer.toString();
                currentLineBuffer.setLength(0);
            } else {
                currentLineBuffer.append(read);
            }
        }
        return nextChar;
    }

    //Builds a scanner error token with both line number and character position.
    private Token errorAt(String details, int position) {
        return new Token(details.trim() + " at line " + currentLine + ", position " + position);
    }

    //Builds a scanner error token at the scanner's current location.
    private Token errorAtCurrentLocation(String details) {
        return errorAt(details, currentPosition);
    }

    //Formats potentially problematic text so invalid lexeme values are always visible.
    private String visibleLexeme(String rawLexeme) {
        return "'" + rawLexeme.replace("\n", "\\n").replace("\t", "\\t") + "'";
    }

    //Returns a full-line snapshot for unterminated-string diagnostics.
    private String lineSnapshotForStringError(boolean endedByNewline) {
        if (endedByNewline) {
            return lastCompletedLine;
        }
        return currentLineBuffer.toString();
    }

    /**
     * Checks whether the current character represents a token boundary.
     */
    private boolean isLexemeBoundary(char current) throws IOException {
        return ch == -1
                || Character.isWhitespace(current)
                || current == '"'
                || current == '['
                || current == ']'
                || current == ','
                || current == '('
                || current == ')'
                || (current == '-' && peek() == '-')
                || (current == '-' && peek() == '-' && peekNext(2) == '-');

    }

    //Consumes the remainder of an invalid mixed lexeme until the next boundary.
    private String consumeUntilBoundary() throws IOException {
        StringBuilder tail = new StringBuilder();
        char current = (char) ch;

        while (ch != -1 && !isLexemeBoundary(current)) {
            tail.append(current);
            ch = readChar();
            current = (char) ch;
        }

        return tail.toString();
    }

    //Looks one character ahead without consuming it.
    private int peek() throws IOException {
        reader.mark(1);
        int nextChar = reader.read();
        reader.reset();
        return nextChar;
    }

    //Looks ahead by N characters and returns the Nth character.
    private int peekNext(int num) throws IOException {
        reader.mark(num);
        int nextChar = -1;
        for (int i = 0; i < num; i++) {
            nextChar = reader.read();
        }
        reader.reset();
        return nextChar;
    }

    //Consumes one single-line comment that starts with "--".
    private void consumeSingleLineComment() throws IOException {
        while (ch != '\n' && ch != -1) {
            ch = readChar();
        }
        if (ch == '\n') {
            currentLine++;
            ch = readChar();
        }
    }

    //Consumes one multi-line comment block "--- ... ---".
    private Token consumeMultiLineComment() throws IOException {
        int commentStartPosition = currentPosition;

        while (true) {
            if (ch == -1) {
                return errorAt("Unterminated multiline comment ", commentStartPosition);
            }

            if (ch == '\n') {
                currentLine++;
            }

            if (ch == '-' && peek() == '-' && peekNext(2) == '-') {
                //Consume exactly the closing delimiter and leave the next character intact.
                ch = readChar();
                ch = readChar();
                ch = readChar();
                return null;
            }
            ch = readChar();
        }
    }

    /**
     * Consumes ignorable syntax: whitespace, single-line comments, and block comments.
     */
    private void whiteSpaceAndCommentHandler() {
        try {
            char current = (char) ch;

            while (Character.isWhitespace(current)) {
                if (current == '\n') {
                    currentLine++;
                }
                ch = readChar();
                current = (char) ch;
            }

            if (current == '-' && peek() == '-') {
                ch = readChar();
                ch = readChar();

                if (ch == '-') {
                    ch = readChar();
                    Token error = consumeMultiLineComment();
                    if (error != null) {
                        pendingError = error;
                    }
                } else {
                    consumeSingleLineComment();
                }
                whiteSpaceAndCommentHandler();
            }

        } catch (IOException e) {
            pendingError = errorAtCurrentLocation("I/O error while handling whitespace/comments: " + e.getMessage());
        }
    }

    //Scans an identifier or reserved keyword token.
    private Token scanIdentifierOrKeyword() throws IOException {
        StringBuilder string = new StringBuilder();
        int identifierStartPosition = currentPosition;
        char current = (char) ch;

        while (Character.isLetterOrDigit(current) || current == '_') {
            string.append(current);
            ch = readChar();
            current = (char) ch;
        }

        //Accepts 'End.' as the only keyword that can use a special symbol
        if (string.toString().equals("End") && current == '.') {
            string.append(current);
            ch = readChar();
            current = (char) ch;
        }

        //If an identifier is immediately followed by special characters, treat the
        //whole sequence as one invalid lexeme instead of splitting it into tokens.
        if (!isLexemeBoundary(current)) {
            String mixedLexeme = string.toString() + consumeUntilBoundary();
            return errorAt("Invalid lexeme " + visibleLexeme(mixedLexeme), identifierStartPosition);
        }

        String lexeme = string.toString();

        if (tokenHashMap.contains(lexeme)) {
            return new Token(tokenHashMap.lookup(lexeme), lexeme);
        }

        if (!symbolTable.containsIdentifier(lexeme)) {
            symbolTable.addIdentifier(lexeme);
        }
        return new Token("TK_ID", lexeme);
    }

    /**
     * Scans unsigned numeric literals.
     */
    private Token scanNumberLiteral() throws IOException {
        StringBuilder string = new StringBuilder();
        int numberStartPosition = currentPosition;
        char current = (char) ch;

        while (Character.isDigit(current)) {
            string.append(current);
            ch = readChar();
            current = (char) ch;
        }

        if (current == '.') {
            string.append(current);
            ch = readChar();
            current = (char) ch;

            while (Character.isDigit(current)) {
                string.append(current);
                ch = readChar();
                current = (char) ch;
            }

            //A numeric literal followed by non-boundary characters is invalid (e.g., 3.14abc).
            if (!isLexemeBoundary(current)) {
                String invalidLexeme = string.toString() + consumeUntilBoundary();
                return errorAt("Invalid lexeme " + visibleLexeme(invalidLexeme), numberStartPosition);
            }

            if (string.charAt(string.length() - 1) == '.') {
                return errorAt("Invalid lexeme " + visibleLexeme(string.toString()), numberStartPosition);
            }

            return new Token("TK_DOUBLE_LIT", string.toString());
        }

        //A numeric literal followed by non-boundary characters is invalid (e.g., 7s5).
        if (!isLexemeBoundary(current)) {
            String invalidLexeme = string.toString() + consumeUntilBoundary();
            return errorAt("Invalid lexeme " + visibleLexeme(invalidLexeme), numberStartPosition);
        }

        return new Token("TK_INT_LIT", string.toString());
    }

    //Scans negative numeric literals that begin with '-' followed by digits.
    private Token scanSignedNumberLiteral() throws IOException {
        StringBuilder string = new StringBuilder();
        int numberStartPosition = currentPosition;

        //Consume and record the sign.
        string.append((char) ch);
        ch = readChar();
        char current = (char) ch;

        while (Character.isDigit(current)) {
            string.append(current);
            ch = readChar();
            current = (char) ch;
        }

        if (current == '.') {
            string.append(current);
            ch = readChar();
            current = (char) ch;

            while (Character.isDigit(current)) {
                string.append(current);
                ch = readChar();
                current = (char) ch;
            }

            if (!isLexemeBoundary(current)) {
                String invalidLexeme = string.toString() + consumeUntilBoundary();
                return errorAt("Invalid lexeme " + visibleLexeme(invalidLexeme), numberStartPosition);
            }

            if (string.charAt(string.length() - 1) == '.') {
                return errorAt("Invalid lexeme " + visibleLexeme(string.toString()), numberStartPosition);
            }

            return new Token("TK_DOUBLE_LIT", string.toString());
        }

        if (!isLexemeBoundary(current)) {
            String invalidLexeme = string.toString() + consumeUntilBoundary();
            return errorAt("Invalid lexeme " + visibleLexeme(invalidLexeme), numberStartPosition);
        }

        return new Token("TK_INT_LIT", string.toString());
    }

    //Scans double literals that start with a dot (e.g., .5).
    private Token scanLeadingDotDoubleLiteral() throws IOException {
        StringBuilder string = new StringBuilder();
        int numberStartPosition = currentPosition;

        //Consume dot and then all following digits.
        string.append((char) ch);
        ch = readChar();
        char current = (char) ch;

        while (Character.isDigit(current)) {
            string.append(current);
            ch = readChar();
            current = (char) ch;
        }

        //Reject malformed forms such as .5abc.
        if (!isLexemeBoundary(current)) {
            String invalidLexeme = string.toString() + consumeUntilBoundary();
            return errorAt("Invalid lexeme " + visibleLexeme(invalidLexeme), numberStartPosition);
        }

        return new Token("TK_DOUBLE_LIT", string.toString());
    }

    //Scans a string literal enclosed in double quotes.
    private Token scanStringLiteral() throws IOException {
        StringBuilder string = new StringBuilder();
        int stringStartPosition = currentPosition;
        ch = readChar();
        char current = (char) ch;

        while (current != '"' && current != '\n' && ch != -1) {
            // handle escape sequences
            if (current == '\\') {
                ch = readChar();
                current = (char) ch;

                switch (current) {
                    case '"':
                        string.append('"');   // \" -> "
                        break;
                    case '\\':
                        string.append('\\');  // \\ -> \
                        break;
                    case 'n':
                        string.append('\n');  // \n -> newline
                        break;
                    case 't':
                        string.append('\t');  // \t -> tab
                        break;
                    default:
                        //unrecognized escape sequence -> error
                        Token escapeError = errorAt("Invalid escape sequence: \\" + current, currentPosition);

                        //keep consuming the rest of the string first
                        ch = readChar();
                        current = (char) ch;
                        while (current != '"' && current != '\n' && ch != -1) {
                            if (current == '\\') {
                                ch = readChar(); //consume the escaped char too
                                current = (char) ch;
                            }
                            ch = readChar();
                            current = (char) ch;
                        }

                        //consume the closing quote if present
                        if (current == '"') {
                            ch = readChar();
                        }

                        //returns error
                        return escapeError;
                }
            } else {
                string.append(current); // normal character
            }
            ch = readChar();
            current = (char) ch;
        }

        if (current == '"') {
            ch = readChar();
            return new Token("TK_STR_LIT", string.toString());
        } else if (current == '\n') {
            String fullLine = lineSnapshotForStringError(true);
            Token error = errorAt("Unterminated string literal in line: " + fullLine, stringStartPosition);

            currentLine++;
            ch = readChar();

            return error;
        }

        String fullLine = lineSnapshotForStringError(false);
        return errorAt("Unterminated string literal in line: " + visibleLexeme(fullLine), stringStartPosition);
    }

    private Token scanLeftBracket() throws IOException {
        ch = readChar();
        return new Token("TK_LEFT_BRACKET", "[");
    }

    private Token scanLeftParen() throws IOException {
        ch = readChar();
        return new Token("TK_LEFT_PAREN", "(");
    }

    private Token scanRightParen() throws IOException {
        ch = readChar();
        return new Token("TK_RIGHT_PAREN", ")");
    }

    private Token scanRightBracket() throws IOException {
        ch = readChar();
        return new Token("TK_RIGHT_BRACKET", "]");
    }

    //scans a fallback invalid lexeme token when no valid rule matches.
    private Token scanInvalidLexeme() throws IOException {
        StringBuilder badString = new StringBuilder();
        int invalidStartPosition = currentPosition;
        badString.append((char) ch);
        ch = readChar();
        char current = (char) ch;

        while (ch != -1 && !Character.isWhitespace(current)) {
            badString.append(current);
            ch = readChar();
            current = (char) ch;
        }

        return errorAt("Invalid lexeme " + visibleLexeme(badString.toString()), invalidStartPosition);
    }

    //Scans a comma separator token used in variable and argument lists.
    private Token scanCommaToken() throws IOException {
        ch = readChar();
        return new Token("TK_COMMA", ",");
    }

    /**
     * Exposes current physical line for display formatting in the caller.
     */
    public int getCurrentLine() {
        return currentLine;
    }

    /**
     * Returns the next token or {@code null} at end-of-file.
     */
    public Token getNextToken() {
        try {
            whiteSpaceAndCommentHandler();

            if (pendingError != null) { //check for pending error first
                Token error = pendingError;
                pendingError = null;
                return annotateTokenLine(error);
            }

            while (ch != -1) {
                //Capture current position to ensure each scan cycle consumes input.
                int tokenStartPosition = currentPosition;
                char current = (char) ch;
                Token scannedToken;

                if (Character.isLetter(current)) {
                    scannedToken = scanIdentifierOrKeyword();
                    if(scannedToken.getLexeme().equals("please") || scannedToken.getLexeme().equals("the")) {
                        whiteSpaceAndCommentHandler();
                        continue;
                    }  
                } else if (Character.isDigit(current)) {
                    scannedToken = scanNumberLiteral();
                } else if (current == '-' && peek() != -1 && Character.isDigit((char) peek())) {
                    //Treat '-<digits>' as a signed numeric literal.
                    scannedToken = scanSignedNumberLiteral();
                } else if (current == '.' && peek() != -1 && Character.isDigit((char) peek())) {
                    //Treat '.<digits>' as a leading-dot double literal.
                    scannedToken = scanLeadingDotDoubleLiteral();
                } else if (current == '"') {
                    scannedToken = scanStringLiteral();
                } else if (current == '[') {
                    scannedToken = scanLeftBracket();
                } else if (current == ']') {
                    scannedToken = scanRightBracket();
                } else if (current == '(') {
                    scannedToken = scanLeftParen();
                } else if (current == ')') {
                    scannedToken = scanRightParen();
                } else if (current == ',') {
                    scannedToken = scanCommaToken();
                } else {
                    scannedToken = scanInvalidLexeme();
                }

                //if scanner did not advance, force-consume one character and emit error.
                if (currentPosition == tokenStartPosition && ch != -1) {
                    int stalledChar = ch;
                    ch = readChar();
                    return annotateTokenLine(errorAt("Invalid lexeme " + visibleLexeme(String.valueOf((char) stalledChar)), tokenStartPosition));
                }

                //Skip noise tokens and continue scanning
                if (!scannedToken.isError() && isNoiseToken(scannedToken.getType())) {
                    continue;
                }

                return annotateTokenLine(scannedToken);
            }
        } catch (IOException e) {
            return annotateTokenLine(errorAtCurrentLocation("I/O error while scanning token: " + e.getMessage()));
        }
        return null;
    }

    //Attaches the current scanner line to outgoing tokens for parser diagnostics.
    private Token annotateTokenLine(Token token) {
        if (token != null) {
            token.setLineNum(currentLine);
        }
        return token;
    }

    //Checks if a token type represents a noise token.
    private boolean isNoiseToken(String tokenType) {
        return tokenType.startsWith("NT_");
    }

    public static void main(String[] args) {
        Compiler.main(args);
    }
}
