package pseudocode_compiler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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
        while (true) {
            String tokenType = getCurrentTokenType();
            Action action = table.getAction(stateStack.peek(), tokenType);

            switch (action.type) {
                case SHIFT:
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
                    if (semanticStack.isEmpty()) {
                        throw new RuntimeException("Parser accepted with empty semantic stack.");
                    }
                    //The root AST should be the only semantic value that remains.
                    return semanticStack.pop();

                case NULL:
                default:
                    String unexpectedLexeme = currentToken == null ? "$" : currentToken.getLexeme();
                    //Prefer token-level line number; fallback handles end-of-input cases.
                    int lineNum = currentToken == null ? scanner.getCurrentLine() : currentToken.getLineNum();
                    throw new RuntimeException(
                            "Syntax Error at line " + lineNum + ": unexpected token " + unexpectedLexeme);
            }
        }
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

        return currentToken.getType();
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
}