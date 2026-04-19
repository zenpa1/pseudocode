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
        if (args.length > 0 && args[0].equals("--parse")) {
            parseAndPrintFromFile(new File("program.txt"));
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
                System.out.print("[" + "Error: " + token.getErrorMessage() + "]");
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

    private static void parseAndPrintFromFile(File programFile) {
        if (!programFile.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        SymbolTable symbolTable = new SymbolTable();
        Scanner scanner = new Scanner(programFile, symbolTable);
        Parser parser = new Parser(scanner);
        try {
            parser.parse();
        } catch (RuntimeException e) {
            System.out.println("Parse failed: " + e.getMessage());
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

                //Skip noise tokens and continue scanning
                if (!scannedToken.isError() && isNoiseToken(scannedToken.getType())) {
                    continue;
                }

                return scannedToken;
            }
        } catch (IOException e) {
            return errorAtCurrentLocation("I/O error while scanning token: " + e.getMessage());
        }
        return null;
    }

    //Checks if a token type represents a noise token.
    private boolean isNoiseToken(String tokenType) {
        return tokenType.startsWith("NT_");
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
class Parser {
    private final Scanner scanner;
    private Token currentToken;

    public Parser(Scanner scanner) {
        this.scanner = scanner;
        advance(); // prime the first token
    }

    private void advance() {
        currentToken = scanner.getNextToken();
        // skip past any remaining noise tokens if your scanner doesn't already filter them
    }

    private boolean check(String type) {
        return currentToken != null && currentToken.getType().equals(type);
    }

    private void expect(String type) {
        if (!check(type)) {
            throw new RuntimeException(
                "Parse error: expected [" + type + "] but found [" +
                (currentToken == null ? "EOF" : currentToken.getType() + 
                " '" + currentToken.getLexeme() + "'") + "]"
            );
        }
        advance();
    }

    private void error(String message) {
        throw new RuntimeException("Syntax error at '" +
            (currentToken == null ? "EOF" : currentToken.getLexeme()) + "': " + message);
    }

    private void error() {
        error("Unexpected token");
    }

    //<program> ::= "Program" <identifier> <decl_section> <stmt_section> "End."
    private void parseProgram() {
        expect("TK_PROG");
        parseId();
        parseDeclSection();
        parseStmtSection();
        expect("TK_END");
    }

    // <decl_section> ::= "Declaration_Section" <decl_list>
    private void parseDeclSection() {
        expect("TK_DECSEC");
        parseDeclList();
    }   

    // <decl_list> ::= <declaration> <decl_list> | ε
    private void parseDeclList() {
        if (check("TK_INT") || check("TK_DOUBLE") || check("TK_STRING") || check("TK_LIST") || check("TK_BOOL") || check("TK_ARRAY") || check("TK_CONST") || check("TK_DEFINE")) {
            parseDecl();
            parseDeclList();
        }
    }

    // <declaration> ::= <type> <id_list>
    // | "constant" <type> <identifier> "is" <literal>
    // | "define" <identifier> "as" <type>
    private void parseDecl() {
        if (check("TK_CONST")) {
            expect("TK_CONST");
            parseType();
            parseId();
            expect("TK_IS");
            parseLiteral();
        } else if (check("TK_DEFINE")) {
            expect("TK_DEFINE");
            parseId();
            expect("TK_AS");
            parseType();
        } else if (check("TK_INT") || check("TK_DOUBLE") || check("TK_STRING") || check("TK_LIST") || check("TK_BOOL") || check("TK_ARRAY")) {
            parseType();
            parseIdList();
        } else {
            error();
        }
    }

    // <stmt_section> ::= "Statement_Section" <stmt_list>
    private void parseStmtSection(){
        expect("TK_STATESEC");
        parseStmtList();
    }

    // <stmt_list> ::= <statement> <stmt_list> | ε
    private void parseStmtList(){
        if (check("NT_THE") || check("NT_A") || check("NT_AN") || check("NT_PLEASE") || check("TK_SET") || check("TK_SAY") || check("TK_READ") || check("TK_IF") || check("TK_WHILE") || check("TK_REPEAT") || check("TK_FOREVERY") || check("TK_CONSIDER") || check("TK_SCOPE") || check("TK_BREAK") || check("TK_CONTINUE")) {
            parseStmt();
            parseStmtList();
        }
    }

    // <statement> ::= <opt_noise> <action_stmt>
    private void parseStmt(){
        parseOptNoise();
        parseActStmt();
    }
    
    // <opt_noise> ::= <noise> | ε
    private void parseOptNoise(){
        if (check("NT_THE") || check("NT_A") || check("NT_AN") || check("NT_PLEASE")) {
            parseNoise();
        }
    }

    // <noise> ::= "the" | "a" | "an" | "please"
    private void parseNoise(){
        if (check("NT_THE")) {
            expect("NT_THE");
        } else if (check("NT_A")) {
            expect("NT_A");
        } else if (check("NT_AN")) {
            expect("NT_AN");
        } else if (check("NT_PLEASE")) {
            expect("NT_PLEASE");
        } else {
            error();
        }
    }

    // <action_stmt> ::= <assignment>
    // | <io_stmt>
    // | <conditional>
    // | <loop>
    // | <switch>
    // | <scope_statement>
    // | <comment>
    private void parseActStmt(){
        if (check("TK_SET")) {
            parseAss();
        } else if (check("TK_SAY") || check("TK_READ")) {
            parseIoStmt();
        } else if (check("TK_IF")) {
            parseCond();
        } else if (check("TK_WHILE") || check("TK_REPEAT") || check("TK_FOREVERY")) {
            parseLoop();
        } else if (check("TK_CONSIDER")) {
            parseSwitch();
        } else if (check("TK_SCOPE")) {
            parseScopeStmt();
        } else if (check("TK_BREAK")) {
            advance();
        } else if (check("TK_CONTINUE")) {
            advance();
        } else {
            error();
        }
    }

    // <scope_statement> ::= "scope" "block" <stmt_list> "done"
    private void parseScopeStmt(){
        expect("TK_SCOPE");
        expect("TK_BLOCK");
        parseStmtList();
        expect("TK_DONE");
    }

    // <assignment> ::= "set" <identifier> "to" <expression> <opt_typecast>
    // | "set" "item" <expression> "of" <identifier> "to" <expression>
    private void parseAss(){
        expect("TK_SET");
        if(check("TK_ITEM")){
            expect("TK_ITEM");
            parseExpr();
            expect("TK_OF");
            parseId();
            expect("TK_TO");
            parseExpr();
        } else if (check("TK_ID")) {
            parseId();
            expect("TK_TO");
            parseExpr();
            parseOptTypecast();
        } else {
            error();
        }
    }

    // <opt_typecast> ::= "as" <type> | ε
    private void parseOptTypecast(){
        if (check("TK_AS")) {
            expect("TK_AS");
            parseType();
        }
    }
    
    // <io_stmt> ::= "say" <expression_list>
    // | "read" <id_list>
    private void parseIoStmt(){
        if(check("TK_SAY")){
            expect("TK_SAY");
            parseExprList();
        } else if (check("TK_READ")){
            expect("TK_READ");
            parseIdList();
        } else {
            error();
        }
    }
    
    // <expression_list> ::= <expression> <expr_list_tail>
    private void parseExprList(){
        parseExpr();
        parseExprListTail();
    }

    // <expr_list_tail> ::= "," <expression> <expr_list_tail> | ε
    private void parseExprListTail(){
        if(check("TK_COMMA")){
            expect("TK_COMMA");
            parseExpr();
            parseExprListTail();
        }
    }

    // <conditional> ::= "if" <expression> "then" <stmt_list> <else_if_chain> <else_clause> "done"
    private void parseCond(){
        expect("TK_IF");
        parseExpr();
        expect("TK_THEN");
        parseStmtList();
        parseElseIfChain();
        parseElseClause();
        expect("TK_DONE");
    }

    // <else_if_chain> ::= "else_if" <expression> "then" <stmt_list> <else_if_chain> | ε
    private void parseElseIfChain(){
        if(check("TK_ELSEIF")){
            expect("TK_ELSEIF");
            parseExpr();
            expect("TK_THEN");
            parseStmtList();
            parseElseIfChain();
        }
    }

    // <else_clause> ::= "else" <stmt_list> | ε
    private void parseElseClause(){
        if(check("TK_ELSE")){
            expect("TK_ELSE");
            parseStmtList();
        }
    }

    // <loop> ::= <while_loop> | <for_loop> | <repeat_loop>
    private void parseLoop(){
        if(check("TK_WHILE")){
            parseWhileLoop();
        } else if (check("TK_FOREVERY")) {
            parseForLoop();
        } else if (check("TK_REPEAT")) {
            parseRepLoop();
        } else {
            error();
        }
    }

    // <while_loop> ::= "while" <expression> "do" <stmt_list> "done"
    private void parseWhileLoop(){
        expect("TK_WHILE");
        parseExpr();
        expect("TK_DO");
        parseStmtList();
        expect("TK_DONE");
    }

    // <for_loop> ::= "for_every" <identifier> "in" <range_spec> "do" <stmt_list> "done"
    private void parseForLoop(){
        expect("TK_FOREVERY");
        parseId();
        expect("TK_IN");
        parseRangeSpec();
        expect("TK_DO");
        parseStmtList();
        expect("TK_DONE");
    }

    // <repeat_loop> ::= "repeat" <stmt_list> "until" <expression>
    private void parseRepLoop(){
        expect("TK_REPEAT");
        parseStmtList();
        expect("TK_UNTIL");
        parseExpr();
    }

    // <range_spec> ::= "range" <expression> "to" <expression>
    // | <identifier>
    private void parseRangeSpec(){
        if (check("TK_RANGE")) {
            expect("TK_RANGE");
            parseExpr();
            expect("TK_TO");
            parseExpr();
        } else if (check("TK_ID")) {
            parseId();
        } else {
            error();
        }
    }

    // <switch> ::= "consider" <expression> <case_list> <opt_otherwise> "done"
    private void parseSwitch(){
        expect("TK_CONSIDER");
        parseExpr();
        parseCaseList();
        parseOptOther();
        expect("TK_DONE");
    }

    // <case_list> ::= "case" <literal> "then" <stmt_list> <case_list> | ε
    private void parseCaseList(){
        if (check("TK_CASE")) {
            expect("TK_CASE");
            parseLiteral();
            expect("TK_THEN");
            parseStmtList();
            parseCaseList();
        }
    }
    
    // <opt_otherwise> ::= "otherwise" <stmt_list> | ε
    private void parseOptOther(){
        if(check("TK_OTHERWISE")){
            expect("TK_OTHERWISE");
            parseStmtList();
        }
    }

    // <expression> ::= <logic_or>
    private void parseExpr(){
        parseLogicOr();
    }

    // <logic_or> ::= <logic_and> <logic_or_tail>
    private void parseLogicOr() {
        parseLogicAnd();
        parseLogicOrTail();
    }

    // <logic_or_tail> ::= "or" <logic_and> <logic_or_tail> | ε
    private void parseLogicOrTail(){
        if(check("TK_OR")){
            expect("TK_OR");
            parseLogicAnd();
            parseLogicOrTail();
        }
    }

    // <logic_and> ::= <logic_xor> <logic_and_tail>
    private void parseLogicAnd(){
        parseLogicXor();
        parseLogicAndTail();
    }

    // <logic_and_tail> ::= "and" <logic_xor> <logic_and_tail> | ε
    private void parseLogicAndTail(){
        if(check("TK_AND")){
            expect("TK_AND");
            parseLogicXor();
            parseLogicAndTail();
        }
    }

    // <logic_xor> ::= <logic_not> <logic_xor_tail>
    private void parseLogicXor(){
        parseNot();
        parseLogicXorTail();
    }

    // <logic_xor_tail> ::= "xor" <logic_not> <logic_xor_tail> | ε
    private void parseLogicXorTail(){
        if(check("TK_XOR")){
            expect("TK_XOR");
            parseNot();
            parseLogicXorTail();
        }
    }

    // <logic_not> ::= "not" <logic_not> | <relational>
    private void parseNot(){
        if(check("TK_NOT")){
            expect("TK_NOT");
            parseNot();
        } else {
            parseRel();
        }
    }
    
    // <relational> ::= <arithmetic_expr> <rel_tail>
    private void parseRel(){
        parseArithExpr();
        parseRelTail();
    }

    // <rel_tail> ::= <rel_op> <arithmetic_expr> | ε
    private void parseRelTail(){
        if(check("TK_IS") || check("TK_ISNOT") || check("TK_EQTO") || check("TK_NOTEQTO") || check("TK_GREATERTHAN") || check("TK_LESSTHAN") || check("TK_GREATERTHANOREQTO") || check("TK_LESSTHANOREQTO")){
            parseRelOp();
            parseArithExpr();
        }
    }

    // <rel_op> ::= "is"
    // | "is_not"
    // | "equal_to"
    // | "not_equal_to"
    // | "greater_than"
    // | "less_than"
    // | "greater_than_or_equal_to"
    // | "less_than_or_equal_to"
    private void parseRelOp(){
        if(check("TK_IS")){
            expect("TK_IS");
        } else if (check("TK_ISNOT")) {
            expect("TK_ISNOT");
        } else if (check("TK_EQTO")) {
            expect("TK_EQTO");
        } else if (check("TK_NOTEQTO")) {
            expect("TK_NOTEQTO");
        } else if (check("TK_GREATERTHAN")) {
            expect("TK_GREATERTHAN");
        } else if (check("TK_LESSTHAN")) {
            expect("TK_LESSTHAN");
        } else if (check("TK_GREATERTHANOREQTO")) {
            expect("TK_GREATERTHANOREQTO");
        } else if (check("TK_LESSTHANOREQTO")) {
            expect("TK_LESSTHANOREQTO");
        } else {
            error();
        }
    }

    // <arithmetic_expr> ::= <term> <arith_tail>
    private void parseArithExpr(){
        parseTerm();
        parseArithTail();
    }

    // <arith_tail> ::= <add_op> <term> <arith_tail> | ε
    private void parseArithTail(){
        if(check("TK_PLUS") || check("TK_MINUS")){
            parseAddOp();
            parseTerm();
            parseArithTail();
        }
    }

    // <add_op> ::= "plus" | "minus"
    private void parseAddOp(){
        if(check("TK_PLUS")){
            expect("TK_PLUS");
        } else if (check("TK_MINUS")) {
            expect("TK_MINUS");
        } else {
            error();
        }
    }

    // <term> ::= <power> <term_tail>
    private void parseTerm(){
        parsePow();
        parseTermTail();
    }

    // <term_tail> ::= <mult_op> <power> <term_tail> | ε
    private void parseTermTail(){
        if(check("TK_TIMES") || check("TK_DIV") || check("TK_MOD")){
            parseMultOp();
            parsePow();
            parseTermTail();
        }
    }

    // <mult_op> ::= "times" | "divided_by" | "modulo"
    private void parseMultOp(){
        if(check("TK_TIMES")){
            expect("TK_TIMES");
        } else if (check("TK_DIV")) {
            expect("TK_DIV");
        } else if (check("TK_MOD")) {
            expect("TK_MOD");
        } else {
            error();
        }
    }

    // <power> ::= <primary> <power_tail>
    private void parsePow(){
        parsePrimary();
        parsePowTail();
    }

    // <power_tail> ::= "raised_to" <power> | ε
    private void parsePowTail(){
        if(check("TK_EXP")){
            expect("TK_EXP");
            parsePow();
        }            
    }
    
    // <primary> ::= "(" <expression> ")"
    // | <opt_noise> <identifier>
    // | <literal>
    // | <opt_noise> "item" <expression> "of" <special_list>
    private void parsePrimary() {
        if (check("TK_LEFT_PAREN")) {
            expect("TK_LEFT_PAREN");
            parseExpr();
            expect("TK_RIGHT_PAREN");
        } else if (isNoiseToken()) {
            parseOptNoise();
            if (check("TK_ITEM")) {
                expect("TK_ITEM");
                parseExpr();
                expect("TK_OF");
                parseSpecList(); 
            }
            else { parseId(); }
        } else if (check("TK_ITEM")) {
            expect("TK_ITEM");
            parseExpr();
            expect("TK_OF");
            parseSpecList();
        } else if (check("TK_ID")) {  
            parseId();
        } else {                        
            parseLiteral();
        }
    }

    private void parseSpecList() {
        parseId();
    }
    // <literal> ::= <list_literal>
    // | <num_literal>
    // | <string_literal>
    // | <boolean_literal>
    // | <special_literal>
    private void parseLiteral(){
        if(check("TK_LEFT_BRACKET")){
            parseListLiteral();
        } else if (check("TK_INT_LIT") || check("TK_DOUBLE_LIT")) {
            parseNumLit();
        } else if (check("TK_STR_LIT")) {
            parseStrLit();
        } else if (check("TK_TRUE") || check("TK_FALSE")) {
            parseBoolLit();
        } else if (check("TK_LENGTHOF") || check("TK_FIND") || check("TK_JOIN") || check("TK_ITEM")) {
            parseSpecLiteral();
        } else {
            error();
        }
    }

    // <list_literal> ::= "[" <list_elements> "]"
    private void parseListLiteral(){
        expect("TK_LEFT_BRACKET");
        parseListElem();
        expect("TK_RIGHT_BRACKET");
    }

    // <list_elements> ::= <expression> <list_tail> | ε
    private void parseListElem() {
        if (check("TK_LEFT_PAREN") || check("NT_THE") || check("NT_A") || check("NT_AN") 
            || check("NT_PLEASE") || check("TK_ITEM") || check("TK_ID") 
            || check("TK_LEFT_BRACKET") || check("TK_INT_LIT") || check("TK_DOUBLE_LIT") 
            || check("TK_STR_LIT") || check("TK_TRUE") || check("TK_FALSE") 
            || check("TK_LENGTHOF") || check("TK_FIND") || check("TK_JOIN")
            || check("TK_NOT")) {
            parseExpr();
            parseListTail();
        }
    }

    // <list_tail> ::= "," <expression> <list_tail> | ε
    private void parseListTail(){
        if(check("TK_COMMA")){
            expect("TK_COMMA");
            parseExpr();
            parseListTail();
        }
    }

    // <special_literal> ::= <length_op>
    // | <find_op>
    // | <string_op>
    // | <item_op>
    private void parseSpecLiteral(){
        if(check("TK_LENGTHOF")){
            parseLengthOp();
        } else if (check("TK_FIND")) {
            parseFindOp();
        } else if (check("TK_JOIN")) {
            parseStrOp();
        } else if (check("TK_ITEM")) {
            parseItemop();
        } else {
            error();
        }
    }

    // <length_op> ::= "length_of" <special_string>
    private void parseLengthOp(){
        expect("TK_LENGTHOF");
        parseSpecStr();
    }

    // <find_op> ::= "find" <special_string> "in" <special_string>
    private void parseFindOp() {
        expect("TK_FIND");
        parseSpecStr();
        expect("TK_IN");
        parseSpecStr();
    }

    // <string_op> ::= "join" <special_string> "with" <special_string>
    private void parseStrOp(){
        expect("TK_JOIN");
        parseSpecStr();
        expect("TK_WITH");
        parseSpecStr();
    }

    // <item_op> ::= "item" <expression> "of" <identifier>
    private void parseItemop(){
        expect("TK_ITEM");
        parseExpr();
        expect("TK_OF");
        parseId();
    }

    // <special_string> ::= <string_literal> | <identifier>
    private void parseSpecStr(){
        if (check("TK_STRING_LIT")) {
            parseStrLit();
        } else if (check("TK_ID")) {
            parseId();
        } else {
            error();
        }
    }

    // <id_list> ::= <identifier> <id_list_tail>
    private void parseIdList() {
        parseId();
        parseIdListTail();
    }

    // <id_tail_list> ::= "," <identifier> <id_list_tail> | ε
    private void parseIdListTail(){
        if(check("TK_COMMA")){
            expect("TK_COMMA");
            parseId();
            parseIdListTail();
        }
    }

    // <identifier> ::= <letter> <id_tail>
    private void parseId() {
        expect("TK_ID");
    }

    // <id_tail> ::= <letter> <id_tail>
    // | <digit> <id_tail>
    // | "_" <id_tail>
    // | ε
    // private void parseIdTail(){
    //     if(check("TK_LETTER")){
    //         parseLetter();
    //         parseIdTail();
    //     } else if(check("TK_DIGIT")){
    //         parseDigit();
    //         parseIdTail();
    //     } else if(check("TK_UNDERSCORE")){
    //         parseUnderscore();
    //         parseIdTail();
    //     }
    // }

    // <num_literal> ::= <integer_literal> | <double_literal>
    private void parseNumLit() {
        if (check("TK_INT_LIT")) {
            expect("TK_INT_LIT");
        } else if (check("TK_DOUBLE_LIT")) {
            expect("TK_DOUBLE_LIT");
        } else {
            error();
        }
    }

    // <double_literal> ::= <integer_literal> "." <digit_seq>
    // private void parseDblLit(){
    //     parseIntLit();
    //     expect("TK_DOT");
    //     parseDigitSeq();
    // }

    // <integer_literal> ::= <digit_seq> | "-" <digit_seq>
    // private void parseIntLit(){
    //     if(check("TK_MINUS")){
    //         expect("TK_MINUS");
    //         parseDigitSeq();
    //     } else if (check("TK_INT")) {
    //         parseDigitSeq();
    //     } else {
    //         error();
    //     }
    // }

    // <digit_seq> ::= <digit> <digit_seq> | <digit>
    // private void parseDigitSeq() {
    //     if (!check("TK_DIGIT")) {
    //         error("Expected digit");
    //     }

    //     do {
    //         parseDigit();
    //     } while (check("TK_DIGIT"));
    // }

    // <string_literal> ::= "\"" <string_content> "\""
    private void parseStrLit() {
        expect("TK_STR_LIT");
    }

    // <string_content> ::= <string_char> <string_content> | ε
    // private void parseStringCont(){
    //     if(check("TK_STR_CHAR")){
    //         parseStrChar();
    //         parseStringCont();
    //     }
    // }

    // <boolean_literal> ::= "true" | "false"
    private void parseBoolLit(){
        if(check("TK_TRUE")){
            expect("TK_TRUE");
        } else if (check("TK_FALSE")) {
            expect("TK_FALSE");
        } else {
            error();
        }
    }

    // <comment> ::= <single_line_comment> | <multi_line_comment>
    // private void parseComm(){
    //     if(check("TK_COMMENT_SINGLE")){
    //         parseSingleLineComm();
    //     } else if (check("TK_COMMENT_MULTI")) {
    //         parseMultiLineComm();
    //     } else {
    //         error();
    //     }
    // }

    // <single_line_comment> ::= "--" <comment_chars> <newline>
    // private void parseSingleLineComm(){
    //     expect("TK_COMMENT_SINGLE");
    //     parseCommChars();
    //     parseNewline();
    // }

    // <multi_line_comment> ::= "---" <comment_chars_multi> "---"
    // private void parseMultiLineComm(){
    //     expect("TK_COMMENT_MULTI");            
    //     parseCommChars();
    //     expect("TK_COMMENT_MULTI");
    // }

    // <comment_chars> ::= <comment_char> <comment_chars> | ε
    // private void parseCommChars(){
    //     if(check("TK_COMM_CHAR")){
    //         parseCommChar();
    //         parseCommChars();
    //     }
    // }

    // <comment_char> ::= <letter>
    //                 | <digit>
    //                 | " "
    //                 | "."
    //                 | ","
    //                 | ":"
    //                 | ";"
    //                 | "!"
    //                 | "?"
    // private void parseCommChar(){
    //     if(check("TK_LETTER")){
    //         parseLetter();
    //     } else if (check("TK_DIGIT")) {
    //         parseDigit();
    //     } else if (check("TK_SPACE") || check("TK_DOT") || check("TK_COMMA") || check("TK_COLON") || check("TK_SEMICOLON") || check("TK_EXCLAMATION") || check("TK_QUESTION")) {
    //         advance(); // consume the comment char
    //     } else {
    //         error();
    //     }
    // }

    //<newline> ::= "\n"
    // private void parseNewline(){
    //     expect("TK_NEWLINE");
    // }

    // <type> ::= "integer"
    //  | "double"
    //  | "string"
    //  | "boolean"
    //  | "list"
    private void parseType(){
        if(check("TK_INT")){
            expect("TK_INT");
        } else if (check("TK_DOUBLE")) {
            expect("TK_DOUBLE");
        } else if (check("TK_STRING")) {
            expect("TK_STRING");
        } else if (check("TK_BOOL")) {
            expect("TK_BOOL");
        } else if (check("TK_LIST")) {
            expect("TK_LIST");
        } else if (check("TK_ARRAY")) {
            expect("TK_ARRAY");
        } else {
            error();
        }
    }

    // <string_char> ::= <letter>
    //         | <digit>
    //         | " "
    //         | "."
    //         | ","
    //         | ":"
    //         | ";"
    //         | "!"
    //         | "?"
    //         | "_"
    //         | "-"
    //         | "+"
    //         | "*"
    //         | "/"
    //         | "("
    //         | ")"
    // private void parseStrChar(){ 
    //     if(check("TK_LETTER")){
    //         parseLetter();
    //     } else if (check("TK_DIGIT")) {
    //         parseDigit();
    //     } else if (check("TK_SPACE") || check("TK_DOT") || check("TK_COMMA") || check("TK_COLON") || check("TK_SEMICOLON") || check("TK_EXCLAMATION") || check("TK_QUESTION") || check("TK_UNDERSCORE") || check("TK_MINUS") || check("TK_PLUS") || check("TK_TIMES") || check("TK_DIV") || check("TK_LEFT_PAREN") || check("TK_RIGHT_PAREN")) {
    //         advance();
    //     } else {
    //         error();
    //     }

    // }

    // <letter> ::= <lowercase_letter> | <uppercase_letter>
    // private void parseLetter(){
    //     if(check("TK_LOWER_LETTER")){
    //         parseLowerCLetter();
    //     } else if (check("TK_UPPER_LETTER")) {
    //         parseUpperCLetter();
    //     } else {
    //         error();
    //     }
    // }

    // <lowercase_letter> ::= "a" | "b" | "c" | "d" | "e" | "f" | "g"
    //                     | "h" | "i" | "j" | "k" | "l" | "m" | "n"
    //                     | "o" | "p" | "q" | "r" | "s" | "t" | "u"
    //                     | "v" | "w" | "x" | "y" | "z"
    // private void parseLowerCLetter(){
    //     if(check("TK_LOWER_LETTER")){
    //         expect("TK_LOWER_LETTER");
    //     } else {
    //         error();
    //     }
    // }

    // <uppercase_letter> ::= "A" | "B" | "C" | "D" | "E" | "F" | "G"
    //                     | "H" | "I" | "J" | "K" | "L" | "M" | "N"
    //                     | "O" | "P" | "Q" | "R" | "S" | "T" | "U"
    //                     | "V" | "W" | "X" | "Y" | "Z"
    // private void parseUpperCLetter(){
    //     if(check("TK_UPPER_LETTER")){
    //         expect("TK_UPPER_LETTER");
    //     } else {
    //         error();
    //     }
    // }

    // <digit> ::= "0" | "1" | "2" | "3" | "4"
    //   | "5" | "6" | "7" | "8" | "9"
    // private void parseDigit(){
    //     if(check("TK_DIGIT")){
    //         expect("TK_DIGIT");
    //     } else {
    //         error();
    //     }
    // }

    private boolean isNoiseToken() {
        return check("NT_THE") || check("NT_A") || check("NT_AN") || check("NT_PLEASE");
    }
    // Entry point — matches <program>
    public void parse() {
        parseProgram();
        if (currentToken != null) {
            throw new RuntimeException("Unexpected token after End.: " + currentToken);
        }
        System.out.println("Parse successful.");
    }
}