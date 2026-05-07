package pseudocode_compiler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Core LALR(1) shift-reduce parser.
 *
 * <p>
 * The parser keeps a state stack for LR automaton states and a semantic stack
 * for AST fragments. SHIFT pushes one state plus one leaf node. REDUCE pops
 * both stacks according to the selected grammar rule and then pushes the GOTO
 * state with a newly synthesized parent AST node.
 *
 * <p>
 * Conceptual model:
 * <ul>
 * <li>The state stack answers: "where am I in the automaton?"</li>
 * <li>The semantic stack answers: "what AST fragments have I built so far?"</li>
 * <li>The current token is the one-token lookahead used to choose parser action.</li>
 * </ul>
 */
public class Parser {

        private static final List<String> SYNCHRONIZATION_TOKENS = Arrays.asList(
            "TK_DONE",
            "TK_END_DOT",
            "TK_END",
            "TK_UNTIL",
            "TK_ELSE",
            "TK_ELSEIF");

        private static final List<String> SYNCHRONIZATION_TABLE_SYMBOLS = Arrays.asList(
            "done",
            "End.",
            "until",
            "else",
            "else_if");

    private final Scanner scanner;
    private final ParsingTable table;
    private Token currentToken;
    private final Stack<Integer> stateStack = new Stack<>();
    private final Stack<ASTNode> semanticStack = new Stack<>();

    /**
     * Creates a parser and eagerly fetches the first lookahead token.
     */
    public Parser(Scanner scanner, ParsingTable table) {
        this.scanner = scanner;
        this.table = table;
        this.currentToken = scanner.getNextToken();
        //LALR parsers conventionally start at automaton state 0.
        stateStack.push(0);
    }

    /**
     * Executes the shift-reduce loop until ACCEPT or syntax failure.
     *
     * <p>
     * Algorithm summary:
     * <ol>
     * <li>Read action from parsing table using (topState, lookaheadSymbol)</li>
     * <li>SHIFT: consume token and move to target state</li>
     * <li>REDUCE: collapse RHS nodes into one LHS node, then apply GOTO</li>
     * <li>ACCEPT: return AST root</li>
     * <li>NULL: report syntax error with precise token context</li>
     * </ol>
     */
    public ASTNode parse() {
        System.out.println("=== LALR(1) PARSER TRACE ==============================================================================");
        System.out.printf("%-45s | %-20s | %-18s | %s%n", "STACK", "LOOKAHEAD", "ACTION", "DETAILS");
        System.out.println("-------------------------------------------------------------------------------------------------------");
        while (true) {
            String currentStack = stateStack.toString();
            String lookahead = currentToken == null
                    ? "$ ($)"
                    : currentToken.getType() + " (" + currentToken.getLexeme() + ")";
            if (lookahead.length() > 18) {
                lookahead = lookahead.substring(0, 15) + "...";
            }

            String tokenType = getCurrentTokenType();
            Action action = table.getAction(stateStack.peek(), tokenType);

            switch (action.type) {
                case SHIFT:
                    System.out.printf("%-45s | %-20s | SHIFT to State %-3s | Consumed: %s (\"%s\")%n",
                            currentStack, lookahead, action.target, currentToken.getType(), currentToken.getLexeme());
                    //Move to the target parser state and remember the consumed token.
                    stateStack.push(Integer.parseInt(action.target));
                    //A shifted terminal becomes one AST leaf on the semantic stack.
                    semanticStack.push(createLeafNode(currentToken));
                    //Advance lookahead only on SHIFT.
                    currentToken = scanner.getNextToken();
                    break;

                case REDUCE:
                    //Resolve reduce target (for example "3b") into a concrete grammar rule.
                    GrammarRule rule = GrammarRule.fromRuleId(action.target);
                    int popCount = rule.getPopCount();
                    String rhs;
                    if (popCount == 0) {
                        rhs = "epsilon";
                    } else if (popCount == 1) {
                        rhs = "1 symbol";
                    } else {
                        rhs = popCount + " symbols";
                    }
                    System.out.printf("%-45s | %-20s | REDUCE by Rule %-3s | Applied:  <%s> -> %s%n",
                            currentStack, lookahead, action.target, rule.getLhs(), rhs);

                    //Pop RHS states from the state stack.
                    for (int i = 0; i < popCount; i++) {
                        if (stateStack.isEmpty()) {
                            throw new RuntimeException("Parser state stack underflow during reduce " + action.target);
                        }
                        //Each popped state corresponds to one consumed RHS grammar symbol.
                        stateStack.pop();
                    }

                    //Pop RHS semantic values from the semantic stack.
                    List<ASTNode> reducedNodes = new ArrayList<>();
                    for (int i = 0; i < popCount; i++) {
                        if (semanticStack.isEmpty()) {
                            throw new RuntimeException("Parser semantic stack underflow during reduce " + action.target);
                        }
                        reducedNodes.add(semanticStack.pop());
                    }
                    //Pop order is right-to-left, so reverse to rebuild left-to-right AST children.
                    Collections.reverse(reducedNodes);

                    //Build parent node that corresponds to the rule's LHS non-terminal.
                    ASTNode parentNode = createNodeForLhs(rule.getLhs(), reducedNodes);

                    //Compute LR GOTO using the uncovered state and the reduced LHS symbol.
                    Action gotoAction = table.getAction(stateStack.peek(), rule.getLhs());
                    if (gotoAction.type == ActionType.NULL || gotoAction.target == null || gotoAction.target.isEmpty()) {
                        //This means the table is inconsistent with grammar or rule mapping.
                        throw new RuntimeException("Missing GOTO for state " + stateStack.peek() + " and lhs " + rule.getLhs());
                    }

                    stateStack.push(Integer.parseInt(gotoAction.target));
                    semanticStack.push(parentNode);
                    //Do not consume lookahead on REDUCE.
                    break;

                case ACCEPT:
                    System.out.printf("%-45s | %-20s | ACCEPT             | Syntax successfully validated!%n", currentStack, lookahead);
                    if (semanticStack.isEmpty()) {
                        throw new RuntimeException("Parser accepted with empty semantic stack.");
                    }
                    //The root AST should be the only semantic value that remains.
                    return semanticStack.pop();

                case NULL:
                default:
                    int lineNum = currentToken == null ? scanner.getCurrentLine() : currentToken.getLineNum();
                    String unexpectedLexeme = currentToken == null ? "$" : currentToken.getLexeme();
                    System.err.println("Syntax Error at line " + lineNum + ": unexpected token '" + unexpectedLexeme + "'");

                    if (currentToken == null) {
                        throw new RuntimeException("Unrecoverable syntax error at line " + lineNum);
                    }

                    while (true) {
                        if (stateStack.isEmpty() || semanticStack.isEmpty()) {
                            throw new RuntimeException("Unrecoverable syntax error at line " + lineNum);
                        }

                        int recoveryState = stateStack.peek();
                        if (hasSynchronizationTransition(recoveryState)) {
                            break;
                        }

                        stateStack.pop();
                        semanticStack.pop();
                    }

                    while (currentToken != null && !isSynchronizationToken(currentToken.getType())) {
                        currentToken = scanner.getNextToken();
                    }

                    //If recovery stops on a synchronization token that still has no action,
                    //consume one token to guarantee forward progress.
                    if (currentToken != null) {
                        Action postRecoveryAction = table.getAction(stateStack.peek(), getCurrentTokenType());
                        if (postRecoveryAction.type == ActionType.NULL) {
                            currentToken = scanner.getNextToken();
                        }
                    }
                    break;
            }
        }
    }

    private boolean hasSynchronizationTransition(int state) {
        for (String synchronizationToken : SYNCHRONIZATION_TABLE_SYMBOLS) {
            Action action = table.getAction(state, synchronizationToken);
            if (action.type != ActionType.NULL) {
                return true;
            }
        }
        return false;
    }

    private boolean isSynchronizationToken(String tokenType) {
        return SYNCHRONIZATION_TOKENS.contains(tokenType);
    }

    /**
     * Converts scanner token to parser-table terminal symbol.
     */
    private String getCurrentTokenType() {
        if (currentToken == null) {
            //EOF sentinel used by the parsing table.
            return "$";
        }

        if (currentToken.isError()) {
            //Scanner errors are treated as parser-error terminals.
            return "ERROR";
        }

        return mapTokenToTableSymbol(currentToken);
    }

    private String mapTokenToTableSymbol(Token token) {
        String type = token.getType();

        switch (type) {
            case "TK_PROG":
                return "Program";
            case "TK_DECSEC":
                return "Declaration_Section";
            case "TK_STATESEC":
                return "Statement_Section";
            case "TK_END":
                return "End.";
            case "TK_ID":
                return "identifier";
            case "TK_INT_LIT":
                return "INT_LIT";
            case "TK_DOUBLE_LIT":
                return "DOUBLE_LIT";
            case "TK_STR_LIT":
                return "STR_LIT";
            case "TK_TRUE":
            case "TK_FALSE":
                return "BOOL_LIT";
            case "TK_COMMA":
                return ",";
            case "TK_LEFT_PAREN":
                return "(";
            case "TK_RIGHT_PAREN":
                return ")";
            case "TK_LEFT_BRACKET":
                return "[";
            case "TK_RIGHT_BRACKET":
                return "]";
            default:
                //Most table terminals are represented by literal keyword lexemes.
                return token.getLexeme();
        }
    }

    /**
     * Wraps a shifted token into the smallest suitable AST leaf.
        *
        * <p>
        * Why this normalization matters:
        * <ul>
        * <li>Keeps semantic stack compact and meaningful</li>
        * <li>Allows parser reductions to compose nodes predictably</li>
        * <li>Preserves original token details for debugging and tree printing</li>
        * </ul>
     */
    private ASTNode createLeafNode(Token token) {
        if (token == null) {
            return new TerminalNode("$", "$");
        }

        if (token.isError()) {
            throw new RuntimeException(token.getErrorMessage());
        }

        String type = token.getType();
        if ("TK_ID".equals(type)) {
            return new IdentifierNode(token.getLexeme());
        }

        if (type.contains("LIT")) {
            return new LiteralNode(token.getLexeme());
        }

        return new TerminalNode(type, token.getLexeme());
    }

    /**
     * Chooses a concrete AST class for key non-terminals and uses a generic
     * wrapper for everything else.
        *
        * <p>
        * This keeps the parser robust while the grammar evolves. New non-terminals
        * can still be represented in the AST immediately, even before a dedicated
        * node class is introduced.
     */
    private ASTNode createNodeForLhs(String lhs, List<ASTNode> nodes) {
        switch (lhs) {
            case "program":
                return new ProgramNode(nodes);
            case "decl_section":
                return new DeclSectionNode(nodes);
            case "stmt_section":
                return new StmtSectionNode(nodes);
            case "assignment":
                return new AssignmentNode(nodes);
            case "conditional":
                return new IfNode(nodes);
            case "while_loop":
                return new WhileLoopNode(nodes);
            case "for_loop":
                return new ForLoopNode(nodes);
            default:
                return new NonTerminalNode(lhs, nodes);
        }
    }
}

enum ActionType {
    //Consume lookahead and move to explicit target state.
    SHIFT,
    //Apply grammar rule and synthesize one non-terminal.
    REDUCE,
    //Successful parse completion.
    ACCEPT,
    //No valid table entry for current parser configuration.
    NULL
}

class Action {

    final ActionType type;
    final String target;

    Action(ActionType type, String target) {
        this.type = type;
        this.target = target;
    }

    static Action nullAction() {
        return new Action(ActionType.NULL, "");
    }

    static Action fromEncoded(String encodedAction) {
        if (encodedAction == null) {
            return nullAction();
        }

        String normalized = encodedAction.trim();
        if (normalized.isEmpty()) {
            return nullAction();
        }

        if (normalized.equalsIgnoreCase("Accept")) {
            return new Action(ActionType.ACCEPT, "");
        }

        if (normalized.startsWith("R ")) {
            //Format: "R 3b" -> REDUCE with target "3b".
            return new Action(ActionType.REDUCE, normalized.substring(2).trim());
        }

        if (normalized.startsWith("S/G ")) {
            //Format: "S/G 42" -> SHIFT/GOTO with target state 42.
            return new Action(ActionType.SHIFT, normalized.substring(4).trim());
        }

        //Unknown action encoding is treated as a missing table action.
        return nullAction();
    }
}

class ParsingTable {

    private static final List<String> DECLARATION_STARTER_SYMBOLS = Arrays.asList(
            "integer",
            "double",
            "string",
            "boolean",
            "list",
            "constant",
            "define");

    private final Map<Integer, Map<String, Action>> table = new HashMap<>();

    public void putAction(int state, String symbol, Action action) {
        //Each state owns a symbol->action map.
        table.computeIfAbsent(state, key -> new HashMap<>()).put(symbol, action);
    }

    public Action getAction(int state, String symbol) {
        Map<String, Action> row = table.get(state);
        if (row == null) {
            return Action.nullAction();
        }

        Action action = row.get(symbol);
        if (action != null) {
            return action;
        }

        //Workaround for extracted table gap: state 7 should include declaration parsing transitions.
        if (state == 7 && (DECLARATION_STARTER_SYMBOLS.contains(symbol)
                || "type".equals(symbol)
                || "declaration".equals(symbol))) {
            Map<String, Action> declarationRow = table.get(4);
            if (declarationRow != null) {
                Action declarationAction = declarationRow.get(symbol);
                if (declarationAction != null) {
                    return declarationAction;
                }
            }
        }

        if (state == 7) {
            Map<String, Action> declarationRow = table.get(4);
            if (declarationRow != null) {
                Action declarationDefaultReduce = declarationRow.get("REDUCE_DEFAULT");
                if (declarationDefaultReduce != null) {
                    return declarationDefaultReduce;
                }
            }
        }

        //Some table rows use IDENTIFIER while others use identifier.
        if ("identifier".equals(symbol)) {
            Action identifierUppercase = row.get("IDENTIFIER");
            if (identifierUppercase != null) {
                return identifierUppercase;
            }
        } else if ("IDENTIFIER".equals(symbol)) {
            Action identifierLowercase = row.get("identifier");
            if (identifierLowercase != null) {
                return identifierLowercase;
            }
        }

        //Workaround for extracted table gaps: some expression states only keep "expression"
        //goto but miss closure transitions. Reuse state 34's expression closure entries.
        if (row.containsKey("expression")) {
            Map<String, Action> expressionClosureRow = table.get(34);
            if (expressionClosureRow != null) {
                Action expressionAction = expressionClosureRow.get(symbol);
                if (expressionAction == null && "identifier".equals(symbol)) {
                    expressionAction = expressionClosureRow.get("IDENTIFIER");
                }
                if (expressionAction != null) {
                    return expressionAction;
                }
            }
        }

        //Special code block for multiple identifiers (parser does not read comma otherwise)
        if (state == 20 && ",".equals(symbol)) {
            Action commaAction = new Action(ActionType.SHIFT, "48");
            return commaAction;
        }

        //Custom workaround for detecting expression lists (expressions separated by commas, (i.e. say "hello", name)
        if ((state == 55) && ",".equals(symbol)) {
            Action commaAction = new Action(ActionType.SHIFT, "98");
            return commaAction;
        }
        
        //Custom workaround for detecting multiple list items
        if ((state == 124 || state == 171) && ",".equals(symbol)) {
            Action commaAction = new Action(ActionType.SHIFT, "152");
            return commaAction;
        }

        //Table supports explicit reduce-default entries for lookahead-agnostic reductions.
        Action defaultReduce = row.get("REDUCE_DEFAULT");
        if (defaultReduce != null) {
            return defaultReduce;
        }

        return Action.nullAction();
    }

    public static ParsingTable fromCsv(String csvPath) {
        ParsingTable parsingTable = new ParsingTable();

        try (BufferedReader reader = new BufferedReader(new FileReader(csvPath))) {
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 3);
                if (parts.length < 3) {
                    continue;
                }

                int state = Integer.parseInt(parts[0].trim());
                String symbol = parts[1].trim();
                String encodedAction = parts[2].trim();

                //Rows include both ACTION and GOTO style entries under one format.
                parsingTable.putAction(state, symbol, Action.fromEncoded(encodedAction));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load parsing table from CSV: " + csvPath, e);
        }

        return parsingTable;
    }

    public void loadCSV(String csvPath) {
        ParsingTable loadedTable = fromCsv(csvPath);
        table.clear();
        table.putAll(loadedTable.table);
    }
}
