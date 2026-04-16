package pseudocode_compiler;

import java.io.File;

/**
 * Small driver for running the LALR(1) parser against the current program.
 */
public class ParserTester {

    public static void main(String[] args) {
        ParsingTable table = new ParsingTable();
        table.loadCSV("Pseudocode_Compiler/parser_tools/parsing_table.csv");

        try {
            Scanner scanner = new Scanner(new File("Pseudocode_Compiler/program.txt"), new SymbolTable());
            Parser parser = new Parser(scanner, table);
            ASTNode root = parser.parse();

            System.out.println("Parsing successful! Generated AST:");
            root.printTree("", true);
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
        }
    }
}