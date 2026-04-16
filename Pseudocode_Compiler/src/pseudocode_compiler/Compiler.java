package pseudocode_compiler;

import java.io.*;
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

