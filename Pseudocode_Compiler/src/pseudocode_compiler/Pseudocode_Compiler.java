package pseudocode_compiler;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Entry point for the pseudocode scanner prototype.
 *
 * <p>
 * This class supports two execution modes:
 * <ul>
 * <li>Default mode: scans tokens from {@code program.txt}</li>
 * <li>Test mode ({@code --test}): executes embedded scanner regression
 * programs</li>
 * </ul>
 *
 * <p>
 * The design intentionally keeps scanner validation close to the scanner
 * implementation so language fixes can be verified quickly after each change.
 */
public class Pseudocode_Compiler {

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--test")) {
            /*
            Passing --test in the command line will spin up
            temporary files with hardcoded strings to run the given method.

            Feel free to add or modify these programs to build more unit tests.
             */
            runScannerTests();
            return;
        }

        scanAndPrintFromFile(new File("program.txt"));
    }

    /**
     * Scans one pseudocode source file and prints tokens in stream order.
     *
     * <p>
     * The output is line-aware for readability and prints the collected symbol
     * table after the terminal {@code TK_END} token is reached.
     */
    private static void scanAndPrintFromFile(File programFile) {
        //Dedicated symbol table stores user-defined identifiers discovered by the scanner.
        SymbolTable symbolTable = new SymbolTable();
        Token token;

        if (!programFile.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }

        Scanner scanner = new Scanner(programFile, symbolTable);
        int printedLine = 1;

        while (true) {
            token = scanner.getNextToken();

            if (token == null) {
                System.out.println("\nEnd of file reached.");
                System.out.println();
                System.out.println("\n--- Symbol Table ---");
                for (Map.Entry<String, String> entry : symbolTable.entries()) {
                    System.out.println("Lexeme: " + entry.getKey() + " | Type: " + entry.getValue());
                }
                break;
            }

            if (scanner.getCurrentLine() > printedLine) {
                System.out.println();
                printedLine = scanner.getCurrentLine();
            }

            if (token.isError()) {
                System.out.print("[" + "Error: " + token.getErrorMessage() + "]" + "\n");
            } else {
                System.out.print("[" + token.getType() + ", " + token.getLexeme() + "] ");
            }
        }
    }

    /**
     * Runs scanner regression programs that document expected tokenization
     * behavior.
     *
     * <p>
     * Each embedded sample targets either broad language coverage or a specific
     * bug fix. Keeping these samples executable provides lightweight,
     * repeatable evidence that scanner behavior remains correct after
     * refactors.
     */
    private static void runScannerTests() {
        //Baseline sanity program for scanner smoke-testing.
        runSingleScannerTest("Minimal valid program",
                "Program Demo\n"
                + "Declaration_Section\n"
                + "integer value\n"
                + "Statement_Section\n"
                + "set value to 5\n"
                + "say value\n"
                + "End.");

        //Covers primitive literal tokens, assignment, and arithmetic keyword tokens.
        runSingleScannerTest("Literals and operators",
                "Program Types\n"
                + "Declaration_Section\n"
                + "integer a\n"
                + "double b\n"
                + "string msg\n"
                + "list items\n"
                + "Statement_Section\n"
                + "set a to 42\n"
                + "set b to 3.14\n"
                + "set msg to \"hello\"\n"
                + "set items to [1,2,3]\n"
                + "say a plus 1\n"
                + "End.");

        //Confirms scanner behavior with both single-line and multi-line comments.
        runSingleScannerTest("Comment handling",
                "Program Comments\n"
                + "Declaration_Section\n"
                + "integer x\n"
                + "Statement_Section\n"
                + "-- this is a single line comment\n"
                + "---\n"
                + "this is a\n"
                + "block comment\n"
                + "---\n"
                + "set x to 10\n"
                + "End.");

        //Showcases conditionals, relational/logical operators, and loop-control tokens.
        runSingleScannerTest("Control flow and boolean logic",
                "Program ControlFlow\n"
                + "Declaration_Section\n"
                + "integer count\n"
                + "boolean ok\n"
                + "Statement_Section\n"
                + "set count to 5\n"
                + "set ok to true\n"
                + "if count greater_than 0 and ok then\n"
                + "say \"positive\"\n"
                + "else_if count equal_to 0 or false then\n"
                + "say \"zero\"\n"
                + "else\n"
                + "continue\n"
                + "done\n"
                + "while count greater_than 0 do\n"
                + "set count to count minus 1\n"
                + "break\n"
                + "done\n"
                + "End.");

        //Exercises function-like keywords, access keywords, and common noise tokens.
        runSingleScannerTest("Built-ins and noise tokens",
                "Program Builtins\n"
                + "Declaration_Section\n"
                + "string text\n"
                + "list items\n"
                + "Statement_Section\n"
                + "set text to \"alpha\"\n"
                + "set items to [\"a\",1,true]\n"
                + "say length_of text\n"
                + "say find \"a\" in text\n"
                + "say item 1 of items\n"
                + "say join \"Hello\" with \"World\"\n"
                + "say please join the text with a text\n"
                + "End.");

        //Covers remaining reserved words: type aliasing, constants, scope, repeat/until,
        //for_every/range, consider/case/otherwise, input token, and exponent/modulo tokens.
        runSingleScannerTest("Comprehensive reserved-word coverage",
                "Program FullCoverage\n"
                + "Declaration_Section\n"
                + "define Number as integer\n"
                + "constant double PI is 3.14\n"
                + "array numbers\n"
                + "Statement_Section\n"
                + "scope\n"
                + "set numbers to [1,2,3]\n"
                + "set numbers to numbers\n"
                + "repeat\n"
                + "set numbers to numbers\n"
                + "until false\n"
                + "for_every i in range 1 to 3 do\n"
                + "say i raised_to 2 modulo 2\n"
                + "done\n"
                + "consider i\n"
                + "case 1 then\n"
                + "say \"one\"\n"
                + "otherwise\n"
                + "read i\n"
                + "done\n"
                + "done\n"
                + "End.");

        //Regression test for scanner hangs caused by standalone special characters.
        runSingleScannerTest("Invalid special characters should not hang",
                "Program SpecialChars\n"
                + "Declaration_Section\n"
                + "integer value\n"
                + "Statement_Section\n"
                + "set value to @\n"
                + "say value\n"
                + "set value to #\n"
                + "End.");

        //Regression test to ensure invalid-lexeme errors include the actual offending value.
        runSingleScannerTest("Invalid lexeme value should be shown",
                "Program InvalidValue\n"
                + "Declaration_Section\n"
                + "integer x\n"
                + "Statement_Section\n"
                + "set x to @@!\n"
                + "End.");

        //Regression test to ensure attached identifier+special sequences are not split.
        runSingleScannerTest("Identifier and special chars should be one invalid lexeme",
                "Program MixedLexeme\n"
                + "Declaration_Section\n"
                + "integer value\n"
                + "Statement_Section\n"
                + "set value to abc@123\n"
                + "End.");

        //Regression test to ensure invalid number literals are flagged as one error.
        runSingleScannerTest("Invalid number literal should throw error",
                "Program InvalidNumber\n"
                + "Declaration_Section\n"
                + "integer num\n"
                + "Statement_Section\n"
                + "set num to 7s5\n"
                + "set num to 3.14abc\n"
                + "End.");

        //Regression test for tokens split across lines and comma-separated identifiers.
        runSingleScannerTest("Multiline comma-separated tokens",
                "Program MultiLineComma\n"
                + "Declaration_Section\n"
                + "integer a,b,c\n"
                + "Statement_Section\n"
                + "read a,\n"
                + "b,\n"
                + "c\n"
                + "say a,\n"
                + "b,\n"
                + "c\n"
                + "End.");

        //Regression test to ensure scanner continues after unterminated list errors.
        runSingleScannerTest("Unterminated list should recover and continue",
                "Program ListRecovery\n"
                + "Declaration_Section\n"
                + "list nums\n"
                + "Statement_Section\n"
                + "set nums to [5, 4, 2\n"
                + "say \"still scanning\"\n"
                + "End.");

        //Regression test for invalid list literals that end with a trailing comma.
        runSingleScannerTest("List trailing comma should throw error",
                "Program TrailingComma\n"
                + "Declaration_Section\n"
                + "list nums\n"
                + "Statement_Section\n"
                + "set nums to [5, 4, 2,]\n"
                + "say \"after list\"\n"
                + "End.");

        //Regression test to ensure unterminated-string errors include full source line.
        runSingleScannerTest("Unterminated string should print full line",
                "Program StringLine\n"
                + "Declaration_Section\n"
                + "string msg\n"
                + "Statement_Section\n"
                + "set msg to \"Hello, world!\n"
                + "say msg\n"
                + "End.");

        //Regression test for negative numeric literals.
        runSingleScannerTest("Negative integer literal should tokenize",
                "Program NegativeInt\n"
                + "Declaration_Section\n"
                + "integer value\n"
                + "Statement_Section\n"
                + "set value to -5\n"
                + "End.");

        //Regression test for inline block comments: token after closing --- must remain intact.
        runSingleScannerTest("Inline block comment should preserve next token",
                "Program BlockInline\n"
                + "Declaration_Section\n"
                + "integer x\n"
                + "Statement_Section\n"
                + "set x to 1\n"
                + "---hidden block---say x\n"
                + "End.");

        //Regression test for double literals without leading zero.
        runSingleScannerTest("Leading-dot double literal should tokenize",
                "Program LeadingDot\n"
                + "Declaration_Section\n"
                + "double value\n"
                + "Statement_Section\n"
                + "set value to .5\n"
                + "End.");
    }

    /**
     * Executes one scanner test program by writing it to a temporary file.
     *
     * <p>
     * Using temporary files keeps scanner execution identical to real usage
     * (file-based input) while still allowing self-contained tests inside
     * source code.
     */
    private static void runSingleScannerTest(String testName, String source) {
        File testFile = null;

        try {
            testFile = File.createTempFile("pseudocode_test_", ".txt");
            try (FileWriter writer = new FileWriter(testFile)) {
                writer.write(source);
            }

            System.out.println("\n=== Test: " + testName + " ===");
            scanAndPrintFromFile(testFile);
            System.out.println("=== End Test: " + testName + " ===");
        } catch (IOException e) {
            System.out.println("Test setup failed for '" + testName + "': " + e.getMessage());
        } finally {
            if (testFile != null && testFile.exists()) {
                testFile.delete();
            }
        }
    }
}

/**
 * Lexical scanner for the pseudocode language.
 *
 * <p>
 * This scanner performs single-pass tokenization directly from a character
 * stream. It is intentionally explicit (rather than regex-heavy) to make
 * lexical edge cases, recovery behavior, and diagnostics easier to reason about
 * in a teaching context.
 */
class Scanner {

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

    public Scanner(File inputFile, SymbolTable symbolTable) {
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
     *
     * <p>
     * Why this helper exists:
     * <ul>
     * <li>Guarantees position accounting is centralized and consistent</li>
     * <li>Keeps full-line snapshots available for high-quality error
     * messages</li>
     * <li>Reduces subtle bugs caused by direct reader access in multiple
     * methods</li>
     * </ul>
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
     *
     * <p>
     * This method is a core rule for validating mixed lexemes (e.g., {@code abc@123},
     * {@code 7s5}, {@code .5abc}). If a token candidate is followed by a
     * non-boundary, the scanner upgrades it to one invalid lexeme to avoid
     * misleading token splitting.
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
        int commentStartLine = currentLine;
        int commentStartPosition = currentPosition;

        while (true) {
            if (ch == -1) {
                return errorAt("Unterminated multiline comment ", commentStartPosition);
                //return errorAtCurrentLocation("Unterminated multiline comment");
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
     * Consumes ignorable syntax: whitespace, single-line comments, and block
     * comments.
     *
     * <p>
     * This method recurses after each comment block so consecutive comment
     * regions are fully consumed before token extraction continues.
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
     *
     * <p>
     * Accepted forms:
     * <ul>
     * <li>Integer: {@code 42}</li>
     * <li>Double: {@code 3.14}</li>
     * </ul>
     * Any contiguous non-boundary suffix (e.g., {@code 7s5}) is promoted to a
     * single invalid lexeme for clearer diagnostics.
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
                        string.append('"');   // \" → "
                        break;
                    case '\\':
                        string.append('\\');  // \\ → \
                        break;
                    case 'n':
                        string.append('\n');  // \n → newline
                        break;
                    case 't':
                        string.append('\t');  // \t → tab
                        break;
                    default:
                        //unrecognized escape sequence -> error
                        Token escapeError = errorAt("Invalid escape sequence: \\" + current
                                + " at line " + currentLine, currentPosition);
                        
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

    /**
     * Scans list literals enclosed in square brackets.
     *
     * <p>
     * Includes two targeted recovery/validation rules:
     * <ul>
     * <li>Reports unterminated lists on newline and resumes scanning next
     * tokens</li>
     * <li>Rejects trailing commas before {@code ]}</li>
     * </ul>
     */
    /*private Token scanListLiteral() throws IOException {
        StringBuilder string = new StringBuilder();
        int listStartPosition = currentPosition;
        string.append((char) ch);
        ch = readChar();
        char current = (char) ch;
        //Tracks the last non-whitespace character to detect a trailing comma before ']'.
        char lastSignificantChar = '[';
        int lastSignificantPosition = listStartPosition;

        //Track whether we reached a physical line break before finding ']'.
        boolean hitLineBreakBeforeClose = false;
        while (current != ']' && ch != -1) {
            //If a newline appears before ']', report an unterminated list and recover
            //at the start of the next line so remaining tokens are still scanned.
            if (current == '\n') {
                hitLineBreakBeforeClose = true;
                currentLine++;
                ch = readChar();
                break;
            }

            string.append(current);
            if (!Character.isWhitespace(current)) {
                lastSignificantChar = current;
                lastSignificantPosition = currentPosition;
            }
            ch = readChar();
            current = (char) ch;
        }

        if (current == ']') {
            //Reject lists ending with a trailing comma, e.g., [1, 2, 3,].
            if (lastSignificantChar == ',') {
                string.append(current);
                ch = readChar();
                return errorAt("Invalid trailing comma in list literal near: " + string.toString(), lastSignificantPosition);
            }

            string.append(current);
            ch = readChar();
            return new Token("TK_LIST_LIT", string.toString());
        }

        if (hitLineBreakBeforeClose) {
            return errorAt("Unterminated list literal near: " + string.toString(), listStartPosition);
        }

        return errorAt("Unterminated list literal near: " + string.toString(), listStartPosition);
    }*/
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
     *
     * <p>
     * The method includes a progress safety guard: if a scan cycle fails to
     * consume input, one character is force-consumed and emitted as an invalid
     * lexeme. This prevents non-terminating loops on unexpected input.
     */
    public Token getNextToken() {
        try {
            whiteSpaceAndCommentHandler();

            if (pendingError != null) { //check for pending error first 
                Token error = pendingError;
                pendingError = null;
                return error;
            }

            while (ch != -1) {
                //Capture current position to ensure each scan cycle consumes input.
                int tokenStartPosition = currentPosition;
                char current = (char) ch;
                Token scannedToken;

                if (Character.isLetter(current)) {
                    scannedToken = scanIdentifierOrKeyword();
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
                    return errorAt("Invalid lexeme " + visibleLexeme(String.valueOf((char) stalledChar)), tokenStartPosition);
                }

                return scannedToken;
            }
        } catch (IOException e) {
            return errorAtCurrentLocation("I/O error while scanning token: " + e.getMessage());
        }
        return null;
    }
}

/**
 * Symbol table for user-defined identifiers discovered during scanning.
 */
class SymbolTable {

    //LinkedHashMap preserves insertion order when printing the table.
    private final LinkedHashMap<String, String> identifiers = new LinkedHashMap<>();

    //Registers a lexeme as an identifier symbol.
    public void addIdentifier(String lexeme) {
        identifiers.put(lexeme, "TK_ID");
    }

    //Checks if a lexeme has already been declared as an identifier symbol.
    public boolean containsIdentifier(String lexeme) {
        return identifiers.containsKey(lexeme);
    }

    //Returns all identifier entries for reporting/debug output.
    public Iterable<Map.Entry<String, String>> entries() {
        return identifiers.entrySet();
    }
}

/**
 * Token data transfer object used by scanner output.
 */
class Token {

    private final String type;
    private final String lexeme;
    private final boolean isError;
    private final String errorMessage;

    public Token(String type, String lexeme) {
        this.type = type;
        this.lexeme = lexeme;
        this.isError = false;
        this.errorMessage = null;
    }

    public Token(String errorMessage) {
        this.type = "ERROR";
        this.lexeme = "";
        this.isError = true;
        this.errorMessage = errorMessage;
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

    @Override
    public String toString() {
        if (isError) {
            return "(ERROR: " + errorMessage + ")";
        }
        return "(" + type + ", " + lexeme + ")";
    }
}

/**
 * Reserved-token lookup table.
 *
 * <p>
 * Maps fixed pseudocode lexemes to their token kinds. This is separate from
 * {@link SymbolTable}, which stores only user-defined identifiers.
 */
class TokenHashMap {

    //Stores lexeme-to-token mappings for all reserved/static tokens.
    private final HashMap<String, String> table;

    public TokenHashMap() {
        table = new HashMap<>();
        initialize();
    }

    //Populates the hashmap with all predefined language tokens.
    private void initialize() {
        table.put("Program", "TK_PROG");
        table.put("End.", "TK_END");
        table.put("Declaration_Section", "TK_DECSEC");
        table.put("Statement_Section", "TK_STATESEC");
        table.put("scope", "TK_SCOPE");
        table.put("define", "TK_DEFINE");
        table.put("block", "TK_BLOCK");
        table.put("set", "TK_SET");
        table.put("to", "TK_TO");
        table.put("constant", "TK_CONST");
        table.put("is", "TK_IS");
        table.put("as", "TK_AS");
        table.put("integer", "TK_INT");
        table.put("double", "TK_DOUBLE");
        table.put("string", "TK_STRING");
        table.put("list", "TK_LIST");
        table.put("boolean", "TK_BOOL");
        table.put("array", "TK_ARRAY");
        table.put("say", "TK_SAY");
        table.put("read", "TK_READ");
        table.put("plus", "TK_PLUS");
        table.put("minus", "TK_MINUS");
        table.put("times", "TK_TIMES");
        table.put("divided_by", "TK_DIV");
        table.put("modulo", "TK_MOD");
        table.put("raised_to", "TK_EXP");
        table.put("is_not", "TK_ISNOT");
        table.put("equal_to", "TK_EQTO");
        table.put("not_equal_to", "TK_NOTEQTO");
        table.put("greater_than", "TK_GREATERTHAN");
        table.put("less_than", "TK_LESSTHAN");
        table.put("greater_than_or_equal_to", "TK_GREATERTHANOREQTO");
        table.put("less_than_or_equal_to", "TK_LESSTHANOREQTO");
        table.put("not", "TK_NOT");
        table.put("and", "TK_AND");
        table.put("or", "TK_OR");
        table.put("xor", "TK_XOR");
        table.put("nand", "TK_NAND");
        table.put("nor", "TK_NOR");
        table.put("if", "TK_IF");
        table.put("then", "TK_THEN");
        table.put("else", "TK_ELSE");
        table.put("else_if", "TK_ELSEIF");
        table.put("done", "TK_DONE");
        table.put("while", "TK_WHILE");
        table.put("do", "TK_DO");
        table.put("repeat", "TK_REPEAT");
        table.put("until", "TK_UNTIL");
        table.put("for_every", "TK_FOREVERY");
        table.put("in", "TK_IN");
        table.put("range", "TK_RANGE");
        table.put("break", "TK_BREAK");
        table.put("continue", "TK_CONTINUE");
        table.put("consider", "TK_CONSIDER");
        table.put("case", "TK_CASE");
        table.put("otherwise", "TK_OTHERWISE");
        table.put("true", "TK_TRUE");
        table.put("false", "TK_FALSE");
        table.put("the", "NT_THE");
        table.put("a", "NT_A");
        table.put("an", "NT_AN");
        table.put("please", "NT_PLEASE");
        table.put("join", "TK_JOIN");
        table.put("with", "TK_WITH");
        table.put("length_of", "TK_LENGTHOF");
        table.put("find", "TK_FIND");
        table.put("item", "TK_ITEM");
        table.put("of", "TK_OF");
        table.put("--", "TK_COMMENT_SINGLE");
        table.put("---", "TK_COMMENT_MULTI");
    }

    //Returns token type for a lexeme, or null if the lexeme is not reserved.
    public String lookup(String lexeme) {
        if (table.containsKey(lexeme)) {
            return table.get(lexeme);
        }
        return null;
    }

    //Checks whether a lexeme exists in the predefined token hashmap.
    public boolean contains(String lexeme) {
        return table.containsKey(lexeme);
    }
}
