package pseudocode_compiler;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Entry point for the pseudocode scanner prototype.
 *
 * <p>
 * Historical note:
 * <ul>
 * <li>The class name keeps the original "Compiler" label for compatibility.</li>
 * <li>Its current practical role is lexical scanning/tokenization.</li>
 * <li>You can think of this class as the project's scanner driver.</li>
 * </ul>
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
public class Compiler {

    public static void main(String[] args) {
        //This entrypoint behaves as the scanner executable for the current project stage.
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
