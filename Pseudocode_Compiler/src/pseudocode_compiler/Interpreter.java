package pseudocode_compiler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Interpreter for executing the abstract syntax tree (AST) of a pseudocode
 * program. Uses a visitor pattern to route different node types to specialized
 * execution methods. Maintains a symbol table for managing variables and scopes
 * during execution.
 */
public class Interpreter {

    private final SymbolTable symbolTable;
    private final Scanner inputScanner;  // For read statements

    /**
     * Constructs an Interpreter with a given symbol table.
     *
     * @param symbolTable the symbol table to use for variable management
     */
    public Interpreter(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.inputScanner = new Scanner(System.in);
    }

    /**
     * Constructs an Interpreter with a new symbol table.
     */
    public Interpreter() {
        this.symbolTable = new SymbolTable();
        this.inputScanner = new Scanner(System.in);
    }

    /**
     * Interprets the given AST root node. Entry point for interpretation;
     * initializes execution with the global scope.
     *
     * @param root the root node of the AST
     * @throws InterpreterException if an error occurs during interpretation
     */
    public void interpret(ASTNode root) throws InterpreterException {
        if (root == null) {
            throw new InterpreterException("Root node cannot be null", -1);
        }
        evaluate(root);
    }

    /**
     * Evaluates an AST node using a visitor pattern. Routes the node to the
     * appropriate execution method based on its type.
     *
     * @param node the node to evaluate
     * @return the result of evaluation (may be null for statements)
     * @throws InterpreterException if an error occurs during evaluation
     */
    private Object evaluate(ASTNode node) throws InterpreterException {
        if (node == null) {
            return null;
        }

        // Visitor pattern: dispatch based on node type
        if (node instanceof ProgramNode) {
            return executeProgramNode((ProgramNode) node);
        } else if (node instanceof DeclSectionNode) {
            return executeDeclSectionNode((DeclSectionNode) node);
        } else if (node instanceof StmtSectionNode) {
            return executeStmtSectionNode((StmtSectionNode) node);
        } else if (node instanceof AssignmentNode) {
            return executeAssignmentNode((AssignmentNode) node);
        } else if (node instanceof SayNode) {
            return executeSayNode((SayNode) node);
        } else if (node instanceof ReadNode) {
            return executeReadNode((ReadNode) node);
        } else if (node instanceof IfNode) {
            return executeIfNode((IfNode) node);
        } else if (node instanceof WhileLoopNode) {
            return executeWhileLoopNode((WhileLoopNode) node);
        } else if (node instanceof RepeatUntilNode) {
            return executeRepeatUntilNode((NonTerminalNode) node);
        } else if (node instanceof ForLoopNode) {
            return executeForLoopNode((ForLoopNode) node);
        } else if (node instanceof BreakNode) {
            return executeBreakNode((BreakNode) node);
        } else if (node instanceof ContinueNode) {
            return executeContinueNode((ContinueNode) node);
        } else if (node instanceof ConsiderNode) {
            return executeConsiderNode((NonTerminalNode) node);
        } else if (node instanceof ScopeBlockNode) {
            return executeScopeBlockNode((NonTerminalNode) node);
        } else if (node instanceof BinaryExprNode) {
            return evaluateBinaryExprNode((BinaryExprNode) node);
        } else if (node instanceof LiteralNode) {
            return evaluateLiteralNode((LiteralNode) node);
        } else if (node instanceof IdentifierNode) {
            return evaluateIdentifierNode((IdentifierNode) node);
        } else if (node instanceof TerminalNode) {
            return evaluateTerminalNode((TerminalNode) node);
        } else if (node instanceof NonTerminalNode) {
            return evaluateNonTerminalNode((NonTerminalNode) node);
        } else {
            throw new InterpreterException(
                    "Unknown node type: " + node.getClass().getName(), node);
        }
    }

    // ========== Program Structure Execution Methods ==========
    /**
     * Executes a ProgramNode, processing declarations then statements.
     *
     * @param node the ProgramNode to execute
     * @return null
     * @throws InterpreterException if an error occurs
     */
    private Object executeProgramNode(ProgramNode node) throws InterpreterException {
        for (ASTNode child : node.children) {
            if (child instanceof TerminalNode || child instanceof IdentifierNode) {
                continue;
            }
            if (child instanceof DeclSectionNode || child instanceof StmtSectionNode) {
                evaluate(child);
            }
        }
        return null;
    }

    /**
     * Executes a DeclSectionNode, processing all variable declarations.
     * Extracts variable names and types from the AST and registers them in the
     * symbol table.
     *
     * @param node the DeclSectionNode to execute
     * @return null
     * @throws InterpreterException if an error occurs
     */
    private Object executeDeclSectionNode(DeclSectionNode node) throws InterpreterException {
        for (ASTNode child : node.children) {
            if (child instanceof TerminalNode) {
                continue;
            }
            processDeclarationList(child);
        }
        return null;
    }

    /**
     * Recursively processes declaration list nodes to extract variable
     * declarations.
     */
    private void processDeclarationList(ASTNode listNode) throws InterpreterException {
        if (!(listNode instanceof NonTerminalNode)) {
            return;
        }

        for (ASTNode child : listNode.children) {
            if (child instanceof NonTerminalNode) {
                NonTerminalNode nt = (NonTerminalNode) child;
                String label = nt.getLhs();

                if ("declaration".equals(label) || "decl".equals(label)) {
                    extractVariablesFromDeclaration(child);
                } else if ("decl_list".equals(label) || "decl_section".equals(label)) {
                    processDeclarationList(child);
                }
            }
        }
    }

    /**
     * Extracts variable declarations from a declaration node and registers them
     * in the symbol table.
     */
    private void extractVariablesFromDeclaration(ASTNode declNode) throws InterpreterException {
        String dataType = null;
        boolean isConstant = false;
        String constName = null;
        ASTNode constValueNode = null;
        boolean seenIs = false;
        boolean isDefine = false;
        String defineName = null;
        boolean seenAs = false;

        for (ASTNode child : declNode.children) {
            if (child instanceof TerminalNode) {
                TerminalNode tn = (TerminalNode) child;
                String tokenType = tn.getTokenType();
                if ("TK_INT".equals(tokenType)) {
                    dataType = "INTEGER";
                } else if ("TK_DEFINE".equals(tokenType)) {
                    isDefine = true;
                } else if ("TK_AS".equals(tokenType)) {
                    seenAs = true;
                } else if ("TK_DOUBLE".equals(tokenType)) {
                    dataType = "REAL";
                } else if ("TK_STRING".equals(tokenType)) {
                    dataType = "STRING";
                } else if ("TK_BOOL".equals(tokenType)) {
                    dataType = "BOOLEAN";
                } else if ("TK_ID".equals(tokenType)) {
                    String varName = tn.getLexeme();
                    if (dataType != null) {
                        try {
                            symbolTable.declareVariable(varName, dataType);
                        } catch (RuntimeException e) {
                        }
                    }
                } else if ("TK_CONST".equals(tokenType)) {
                    isConstant = true;
                } else if ("TK_IS".equals(tokenType)) {
                    seenIs = true;
                }
            } else if (child instanceof IdentifierNode) {
                String varName = ((IdentifierNode) child).getName();
                if (isConstant) {
                    constName = varName;
                } else if (isDefine) {
                    defineName = varName;  // dataType is null here but that's ok, set later
                } else if (dataType != null) {
                    try {
                        symbolTable.declareVariable(varName, dataType);
                    } catch (RuntimeException e) {
                    }
                }
            } else if (child instanceof NonTerminalNode) {                     // <-- top-level now
                String label = ((NonTerminalNode) child).getLhs();
                if ("type".equals(label) || "base_type".equals(label)) {
                    dataType = extractTypeFromNode(child);
                } else if ("id_list".equals(label) || "identifier_list".equals(label)) {
                    extractIdentifiersFromList(child, dataType);
                } else if (seenIs && ("literal".equals(label) || "num_literal".equals(label))) {
                    constValueNode = child;
                }
            }
        }

        if (isConstant && constName != null && dataType != null) {
            Object constValue = constValueNode != null ? evaluate(constValueNode) : null;
            try {
                symbolTable.declareVariable(constName, dataType, constValue);
            } catch (RuntimeException e) {
            }
        }
        if (isDefine && defineName != null && dataType != null) {
            try {
                symbolTable.declareVariable(defineName, dataType);
            } catch (RuntimeException e) {
            }
        }
    }

    /**
     * Extracts data type from a type NonTerminalNode.
     */
    private String extractTypeFromNode(ASTNode typeNode) {
        for (ASTNode child : typeNode.children) {
            if (child instanceof TerminalNode) {
                String tokenType = ((TerminalNode) child).getTokenType();
                switch (tokenType) {
                    case "TK_INT":
                        return "INTEGER";
                    case "TK_DOUBLE":
                        return "REAL";
                    case "TK_STRING":
                        return "STRING";
                    case "TK_BOOL":
                        return "BOOLEAN";
                    case "TK_LIST":
                        return "LIST";
                }
            }
        }
        return null;
    }

    /**
     * Extracts identifiers from an id_list and declares them with the given
     * type.
     */
    private void extractIdentifiersFromList(ASTNode listNode, String dataType) throws InterpreterException {
        if (dataType == null) {
            return;
        }

        for (ASTNode child : listNode.children) {
            if (child instanceof TerminalNode) {
                TerminalNode tn = (TerminalNode) child;
                if ("TK_ID".equals(tn.getTokenType())) {
                    String varName = tn.getLexeme();
                    try {
                        symbolTable.declareVariable(varName, dataType);
                    } catch (RuntimeException e) {
                        // Variable might already be declared, continue
                    }
                }
            } else if (child instanceof IdentifierNode) {
                String varName = ((IdentifierNode) child).getName();
                try {
                    symbolTable.declareVariable(varName, dataType);
                } catch (RuntimeException e) {
                    // Variable might already be declared, continue
                }
            } else if (child instanceof NonTerminalNode) {
                extractIdentifiersFromList(child, dataType);
            }
        }
    }

    /**
     * Executes a StmtSectionNode, processing all statements in sequence.
     *
     * @param node the StmtSectionNode to execute
     * @return null
     * @throws InterpreterException if an error occurs
     */
    private Object executeStmtSectionNode(StmtSectionNode node) throws InterpreterException {
        for (ASTNode child : node.children) {
            if (child instanceof TerminalNode) {
                continue;
            }
            evaluate(child);
        }
        return null;
    }

    // ========== Statement Execution Methods ==========
    /**
     * Executes an AssignmentNode. Evaluates the right-hand expression and
     * assigns it to the identifier. Performs type checking: verifies the value
     * is compatible with the variable's declared type.
     *
     * @param node the AssignmentNode to execute
     * @return null
     * @throws InterpreterException if the variable doesn't exist or type
     * mismatch occurs
     */
    private Object executeItemAssignment(AssignmentNode node) throws InterpreterException {
        ASTNode indexExpr = null;
        String listName = null;
        ASTNode valueExpr = null;
        boolean seenOf = false;
        boolean seenTo = false;

        for (ASTNode child : node.children) {
            if (child instanceof TerminalNode) {
                String tt = ((TerminalNode) child).getTokenType();
                if ("TK_OF".equals(tt)) {
                    seenOf = true;
                    continue;
                }
                if ("TK_TO".equals(tt)) {
                    seenTo = true;
                    continue;
                }
                continue; // skip TK_SET, TK_ITEM
            }
            if (child instanceof IdentifierNode) {
                listName = ((IdentifierNode) child).getName();
                continue;
            }
            // NonTerminalNode expressions
            if (!seenOf && indexExpr == null) {
                indexExpr = child;   // first expression = index
            } else if (seenTo) {
                valueExpr = child;   // expression after "to" = new value
            }
        }

        if (listName == null || indexExpr == null || valueExpr == null) {
            throw new InterpreterException(
                    "Malformed item assignment statement", node);
        }

        if (!symbolTable.variableExists(listName)) {
            throw new InterpreterException(
                    "Variable '" + listName + "' has not been declared", node);
        }

        VariableRecord record = symbolTable.lookupVariable(listName);
        Object listObj = record.getValue();
        if (!(listObj instanceof List)) {
            throw new InterpreterException(
                    "Variable '" + listName + "' is not a list", node);
        }

        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) listObj;

        int index = toInteger(evaluate(indexExpr));
        if (index < 1 || index > list.size()) {
            throw new InterpreterException(
                    "Index " + index + " out of bounds for list '"
                    + listName + "' of size " + list.size(), node);
        }

        Object newValue = evaluate(valueExpr);
        list.set(index - 1, newValue); // 1-based indexing, mutate in place

        return null;
    }

    private Object executeAssignmentNode(AssignmentNode node) throws InterpreterException {
        IdentifierNode identifierNode = null;
        ASTNode expressionNode = null;
        String castType = null;
        //System.err.println("DEBUG AssignmentNode children:");

        for (ASTNode child : node.children) {
            if (child instanceof TerminalNode
                    && "TK_ITEM".equals(((TerminalNode) child).getTokenType())) {
                return executeItemAssignment(node);
            }
        }

        for (ASTNode child : node.children) {
            /*System.err.println("  " + child.getClass().getSimpleName()
                    + (child instanceof TerminalNode ? " [" + ((TerminalNode) child).getTokenType() + "]"
                            : child instanceof NonTerminalNode ? " [" + ((NonTerminalNode) child).getLhs() + "]"
                                    : child instanceof IdentifierNode ? " [" + ((IdentifierNode) child).getName() + "]" : ""));*/

            if (child instanceof IdentifierNode && identifierNode == null) {
                identifierNode = (IdentifierNode) child;
            } else if (identifierNode != null && expressionNode == null) {
                if (child instanceof TerminalNode) {
                    String tt = ((TerminalNode) child).getTokenType();
                    if (tt.equals("TK_TRUE") || tt.equals("TK_FALSE")
                            || tt.equals("TK_INT_LIT") || tt.equals("TK_REAL_LIT")
                            || tt.equals("TK_STR_LIT")) {
                        expressionNode = child;
                    }
                    // else it's a keyword like TK_TO or TK_SET skip it
                } else {
                    expressionNode = child;
                }
            } else if (identifierNode != null && expressionNode != null) {
                // ADD THIS BLOCK: capture opt_as_type
                if (child instanceof NonTerminalNode
                        && "opt_as_type".equals(((NonTerminalNode) child).getLhs())) {
                    castType = extractCastType((NonTerminalNode) child);
                }
            }
        }

        if (identifierNode == null) {
            throw new InterpreterException(
                    "Left side of assignment must be an identifier", node);
        }

        if (expressionNode == null) {
            throw new InterpreterException(
                    "Assignment requires an expression on the right side", node);
        }

        String variableName = identifierNode.getName();
        Object value = evaluate(expressionNode);

        if (value instanceof List && castType != null) {
            value = castListElements((List<?>) value, castType, node);
        }

        try {
            if (!symbolTable.variableExists(variableName)) {
                throw new InterpreterException(
                        "Variable '" + variableName + "' has not been declared", node);
            }

            VariableRecord record = symbolTable.lookupVariable(variableName);
            String declaredType = record.getDataType();

            if (declaredType.equals("INTEGER")) {
                if (!(value instanceof Number)) {
                    throw new InterpreterException(
                            "Type error: cannot assign " + getTypeName(value) + " to variable '"
                            + variableName + "' of type INTEGER", node);
                }
                double temp = ((Number) value).doubleValue();
                if (temp > 2147483647 || temp < -2147483647) {
                    throw new InterpreterException(
                            "Value error: Value " + value + " of "
                            + variableName + " out of the 32-bit range of integers", node);
                }
            }

            if (!isTypeCompatible(declaredType, value)) {
                if (declaredType.equals("INTEGER") && (value instanceof Double)) {
                    value = ((Number) value).intValue();
                } else if (declaredType.equals("STRING") && ((value instanceof Double) || (value instanceof Integer))) {
                    value = value.toString();
                } else {
                    throw new InterpreterException(
                            "Type error: cannot assign " + getTypeName(value) + " to variable '"
                            + variableName + "' of type " + declaredType, node);
                }
            }

            symbolTable.assignVariable(variableName, value);
        } catch (IllegalArgumentException e) {
            throw new InterpreterException(e.getMessage(), node);
        }

        return null;
    }

    private String extractCastType(NonTerminalNode optAsType) {
        for (ASTNode child : optAsType.children) {
            // Direct terminal (if AST collapses type node)
            if (child instanceof TerminalNode) {
                String tt = ((TerminalNode) child).getTokenType();
                switch (tt) {
                    case "TK_INT":
                        return "INTEGER";
                    case "TK_DOUBLE":
                        return "REAL";
                    case "TK_STRING":
                        return "STRING";
                    case "TK_BOOL":
                        return "BOOLEAN";
                }
            }
            // Wrapped in a <type> non-terminal
            if (child instanceof NonTerminalNode) {
                String lhs = ((NonTerminalNode) child).getLhs();
                if ("type".equals(lhs)) {
                    return extractTypeFromNode(child); // your existing method handles terminals inside
                }
            }
        }
        return null;
    }

    private List<Object> castListElements(List<?> list, String castType, ASTNode node)
            throws InterpreterException {
        List<Object> result = new ArrayList<>();
        for (Object element : list) {
            switch (castType.toUpperCase()) {
                case "INTEGER":
                    if (element instanceof Number) {
                        result.add(((Number) element).intValue());
                    } else {
                        throw new InterpreterException(
                                "Cannot cast list element '" + element + "' to INTEGER", node);
                    }
                    break;
                case "REAL":
                    if (element instanceof Number) {
                        result.add(((Number) element).doubleValue());
                    } else {
                        throw new InterpreterException(
                                "Cannot cast list element '" + element + "' to REAL", node);
                    }
                    break;
                case "STRING":
                    result.add(element.toString());
                    break;
                default:
                    result.add(element);
            }
        }
        return result;
    }

    /**
     * Executes a SayNode. Outputs the evaluated expression(s) to System.out
     * with implicit string conversion. All types are converted to strings
     * before printing.
     *
     * @param node the SayNode to execute
     * @return null
     * @throws InterpreterException if evaluation fails
     */
    private Object executeSayNode(SayNode node) throws InterpreterException {
        //System.err.println("DEBUG SayNode children: " + node.children.size());
        for (ASTNode child : node.children) {
            System.err.println("  " + child.getClass().getSimpleName()
                    + (child instanceof TerminalNode ? " [" + ((TerminalNode) child).getTokenType() + "]"
                            : child instanceof NonTerminalNode ? " [" + ((NonTerminalNode) child).getLhs() + "]" : ""));
            Object value = evaluate(child);
            String output = valueToString(value);
            System.out.print(output);
        }
        System.out.println();
        return null;
    }

    /**
     * Executes a ReadNode. Reads a line of input from System.in using Scanner
     * and stores it in the variable. The input is stored as a String initially;
     * type conversion is handled during assignment.
     *
     * @param node the ReadNode to execute
     * @return null
     * @throws InterpreterException if the variable doesn't exist or input
     * reading fails
     */
    private Object executeReadNode(ReadNode node) throws InterpreterException {
        String variableName = node.getVariableName();

        if (!symbolTable.variableExists(variableName)) {
            throw new InterpreterException(
                    "Variable '" + variableName + "' has not been declared", node);
        }

        try {
            if (!inputScanner.hasNextLine()) {
                throw new InterpreterException(
                        "Error reading input: no input available", node);
            }

            String inputLine = inputScanner.nextLine();
            VariableRecord record = symbolTable.lookupVariable(variableName);
            String declaredType = record.getDataType();

            Object convertedValue = convertStringToType(inputLine, declaredType, node);

            symbolTable.assignVariable(variableName, convertedValue);
        } catch (IllegalArgumentException e) {
            throw new InterpreterException(e.getMessage(), node);
        }

        return null;
    }

    /**
     * Executes an IfNode. Evaluates the condition and executes the appropriate
     * branch. Supports if/else_if/else chains with proper type checking on
     * conditions.
     *
     * @param node the IfNode to execute
     * @return null
     * @throws InterpreterException if an error occurs during evaluation
     */
    private Object executeIfNode(IfNode node) throws InterpreterException, BreakException, ContinueException {

        //System.err.println("DEBUG: op exists = " + symbolTable.variableExists("op"));
        // Execute pre-if statements (read op)
        for (ASTNode child : node.children) {
            if (child instanceof TerminalNode
                    && "TK_IF".equals(((TerminalNode) child).getTokenType())) {
                break;
            }
            if (!(child instanceof TerminalNode)) {
                evaluate(child);
            }
        }

        // Collect post-TK_IF children
        ASTNode conditionNode = null;
        ASTNode thenBranch = null;
        ASTNode ifTailClause = null;

        boolean seenIf = false;
        boolean seenThen = false;

        for (ASTNode child : node.children) {
            if (child instanceof TerminalNode) {
                String tt = ((TerminalNode) child).getTokenType();
                if ("TK_IF".equals(tt)) {
                    seenIf = true;
                    continue;
                }
                if ("TK_THEN".equals(tt)) {
                    //System.err.println("DEBUG seenThen=true, conditionNode at this point = " + conditionNode);
                    seenThen = true;
                    continue;
                }
                continue;
            }
            if (!seenIf) {
                continue;
            }

            if (conditionNode == null) {
                conditionNode = child;
            } else if (seenThen && thenBranch == null) {
                if (child instanceof NonTerminalNode) {
                    String childLhs = ((NonTerminalNode) child).getLhs();
                    if ("if_tail".equals(childLhs)) {
                        ifTailClause = child;
                    } else if ("stmt_list".equals(childLhs)) {
                        thenBranch = child;
                    }
                }
            } else if (child instanceof NonTerminalNode) {
                String childLhs = ((NonTerminalNode) child).getLhs();
                if ("if_tail".equals(childLhs)) {
                    ifTailClause = child;
                }
            }
        }

        String tailPrefix = (!(ifTailClause.children.get(0) instanceof TerminalNode)) ? 
                ((TerminalNode) ifTailClause.children.get(0).children.get(0)).getLexeme() :
                "done";
        
        // Evaluate condition
        //System.err.println("DEBUG conditionNode = " + (conditionNode == null ? "NULL" : conditionNode.getClass().getSimpleName()
        //        + (conditionNode instanceof NonTerminalNode ? " [" + ((NonTerminalNode) conditionNode).getLhs() + "]" : "")));
        Object conditionValue = evaluate(conditionNode);
        //System.err.println("DEBUG conditionValue = [" + conditionValue + "]");
        if (toBoolean(conditionValue)) {
            //System.err.println("DEBUG condition is TRUE, thenBranch=" + thenBranch + ", elseIfClause=" + elseIfClause);
            // then-body is either a direct stmt_list or first stmt_list in else_if_clause
            if (thenBranch != null) {
                symbolTable.enterScope();
                try {
                    evaluate(thenBranch);
                } catch(BreakException | ContinueException e) {
                    throw e;
                }
                finally {
                    symbolTable.exitScope();
                }
            } else if (tailPrefix.equals("else_if")) {
                //System.err.println("DEBUG calling executeThenBodyFromElseIf");
                // execute only the then-body portion of else_if_clause
                executeThenBodyFromElseIf(ifTailClause.children.get(0));
            }
        }

        // Condition was false — try else_if chain
        if (tailPrefix.equals("else_if")) {
            if (evaluateElseIfChain(ifTailClause.children.get(0))) {
                return null;
            }
        }

        if (tailPrefix.equals("else")) {
            evaluate(ifTailClause.children.get(0));
        }
        return null;
    }

    private void executeThenBodyFromElseIf(ASTNode elseIfClause) throws InterpreterException, BreakException, ContinueException {
        symbolTable.enterScope();
        try {
            for (ASTNode child : elseIfClause.children) {
                if (child instanceof TerminalNode) {
                    continue;
                }
                if (child instanceof NonTerminalNode
                        && "stmt_list".equals(((NonTerminalNode) child).getLhs())) {
                    // Add this:
                    //System.err.println("DEBUG then-body stmt_list children: " + child.children.size());
                    for (ASTNode c : child.children) {
                        //System.err.println("  " + c.getClass().getSimpleName()
                        //+ (c instanceof NonTerminalNode ? " [" + ((NonTerminalNode) c).getLhs() + "]" : ""));
                    }
                    evaluate(child);
                    return;
                }
            }
        }catch(ContinueException | BreakException e) { 
                    throw e;
        } finally {
            symbolTable.exitScope();
        }
    }

    private boolean evaluateElseIfChain(ASTNode elseIfClause) throws InterpreterException, BreakException, ContinueException {
        if (!(elseIfClause instanceof NonTerminalNode)) {
            return false;
        }
        if (!hasNonTerminalChildren(elseIfClause)) {
            return false;
        }

        // Find TK_ELSEIF position to split the children correctly
        /*int elseIfIndex = -1;
        List<ASTNode> children = elseIfClause.children;
        for (int i = 0; i < children.size(); i++) {
            ASTNode child = children.get(i);
            if (child instanceof TerminalNode
                    && "TK_ELSEIF".equals(((TerminalNode) child).getTokenType())) {
                elseIfIndex = i;
                break;
            }
        }*/

        // No TK_ELSEIF found — this is an empty/epsilon else_if_clause
        if (elseIfClause.children.size() < 0) {
            return false;
        }

        // Find TK_THEN after TK_ELSEIF
        /*int thenIndex = -1;
        for (int i = elseIfIndex + 1; i < children.size(); i++) {
            ASTNode child = children.get(i);
            if (child instanceof TerminalNode
                    && "TK_THEN".equals(((TerminalNode) child).getTokenType())) {
                thenIndex = i;
                break;
            }
        }*/

        TerminalNode thenToken = (TerminalNode) elseIfClause.children.get(2);
        if (!thenToken.getTokenType().equals("TK_THEN")) {
            System.out.println(thenToken.getTokenType());
            return false;
        }

        // Condition is the first non-terminal between TK_ELSEIF and TK_THEN
        ASTNode condition = elseIfClause.children.get(1);
        /*for (int i = elseIfIndex + 1; i < thenIndex; i++) {
            if (!(children.get(i) instanceof TerminalNode)) {
                condition = children.get(i);
                break;
            }
        }*/

        // Then-body is the first stmt_list after TK_THEN
        ASTNode thenBody = null;
        ASTNode ifTailClause = null;
        for (int i = 4; i < elseIfClause.children.size(); i++) {
            ASTNode child = elseIfClause.children.get(i);
            if (child instanceof TerminalNode) {
                continue;
            }
            if (child instanceof NonTerminalNode) {
                String lhs = ((NonTerminalNode) child).getLhs();
                if ("stmt_list".equals(lhs) && thenBody == null) {
                    thenBody = child;
                } else if ("if_tail".equals(lhs)) {
                    ifTailClause = child;
                }
            }
        }

        if (condition == null) {
            return false;
        }
        
        String tailPrefix = (!(ifTailClause.children.get(0) instanceof TerminalNode)) ? 
                ((TerminalNode) ifTailClause.children.get(0).children.get(0)).getLexeme() :
                "done";

        //System.err.println("DEBUG elseif condition node = " + condition.getClass().getSimpleName()
        //        + (condition instanceof NonTerminalNode ? " [" + ((NonTerminalNode) condition).getLhs() + "]" : ""));
        Object condResult = evaluate(condition);
        //System.err.println("DEBUG elseif conditionValue = [" + condResult + "]");

        
        if (toBoolean(condResult)) {
            symbolTable.enterScope();
            try {
                if (elseIfClause.children.get(3) != null) {
                    evaluate(elseIfClause.children.get(3));
                }
            } catch(BreakException | ContinueException e) { throw e; }
            finally {
                symbolTable.exitScope();
            }
            return true;
        }

        if (tailPrefix.equals("else_if") && evaluateElseIfChain(ifTailClause.children.get(0))) {
            return true;
        }

        if (tailPrefix.equals("else")) {
            evaluate(ifTailClause.children.get(0));
            return true;
        }

        return false;
    }

    /**
     * Executes a WhileLoopNode. Repeatedly evaluates the body while the
     * condition is true. Supports break and continue statements via custom
     * exceptions.
     *
     * @param node the WhileLoopNode to execute
     * @return null
     * @throws InterpreterException if an error occurs during evaluation
     */
    private Object executeWhileLoopNode(WhileLoopNode node) throws InterpreterException {
        // Filter out keyword terminals (while, do, done, etc.)
        List<ASTNode> semanticChildren = new ArrayList<>();
        for (ASTNode child : node.children) {
            if (!(child instanceof TerminalNode)) {
                semanticChildren.add(child);
            }
        }

        if (semanticChildren.size() < 2) {
            throw new InterpreterException(
                    "WhileLoopNode requires exactly 2 children (condition and body)", node);
        }

        ASTNode conditionNode = semanticChildren.get(0);
        ASTNode body = semanticChildren.get(1);

        while (true) {
            try {
                Object conditionValue = evaluate(conditionNode);
                if (!toBoolean(conditionValue)) {
                    break;
                }

                symbolTable.enterScope();
                try {
                    evaluate(body);
                } finally {
                    symbolTable.exitScope();
                }
            } catch (BreakException e) {
                break;
            } catch (ContinueException e) {
                continue;
            }
        }

        return null;
    }

    /**
     * Executes a RepeatUntilNode. Executes the body once, then repeats while
     * the condition is false (until it becomes true). Supports break and
     * continue statements via custom exceptions.
     *
     * @param node the RepeatUntilNode to execute
     * @return null
     * @throws InterpreterException if an error occurs during evaluation
     */
    private Object executeRepeatUntilNode(NonTerminalNode node) throws InterpreterException {
        // Filter out keyword terminals (repeat, until, etc.)
        List<ASTNode> semanticChildren = new ArrayList<>();
        for (ASTNode child : node.children) {
            if (!(child instanceof TerminalNode)) {
                semanticChildren.add(child);
            }
        }

        if (semanticChildren.size() < 2) {
            throw new InterpreterException(
                    "RepeatUntilNode requires exactly 2 children (body and condition)", node);
        }

        ASTNode body = semanticChildren.get(0);
        ASTNode conditionNode = semanticChildren.get(1);

        do {
            try {
                symbolTable.enterScope();
                try {
                    evaluate(body);
                } finally {
                    symbolTable.exitScope();
                }
            } catch (BreakException e) {
                break;
            } catch (ContinueException e) {
                // Continue to next iteration (which evaluates condition)
            }

            Object conditionValue = evaluate(conditionNode);
            if (toBoolean(conditionValue)) {
                break;
            }
        } while (true);

        return null;
    }

    /**
     * Executes a ForLoopNode. Initializes the loop variable, then iterates from
     * start to end value. Supports break and continue statements via custom
     * exceptions.
     *
     * @param node the ForLoopNode to execute
     * @return null
     * @throws InterpreterException if an error occurs during evaluation
     */
    private Object executeForLoopNode(ForLoopNode node) throws InterpreterException {
        // Filter out keyword terminals (for, from, to, do, done, etc.)
        List<ASTNode> semanticChildren = new ArrayList<>();
        for (ASTNode child : node.children) {
            if (!(child instanceof TerminalNode)) {
                semanticChildren.add(child);
            }
        }

        if (semanticChildren.size() != 3) {
            throw new InterpreterException(
                    "ForLoopNode requires exactly 3 children (iterator, range, body)", node);
        }

        ASTNode iteratorNode = semanticChildren.get(0);
        ASTNode rangeNode = semanticChildren.get(1);
        ASTNode bodyNode = semanticChildren.get(2);

        if (!(iteratorNode instanceof IdentifierNode)) {
            throw new InterpreterException(
                    "For-loop iterator must be an identifier", iteratorNode);
        }

        List<ASTNode> rangeChildren = new ArrayList<>();
        for (ASTNode child : rangeNode.children) {
            if (!(child instanceof TerminalNode)) {
                rangeChildren.add(child);
            }
        }

        String iteratorName = ((IdentifierNode) iteratorNode).getName();

        if (rangeChildren.size() == 2) {
            ASTNode startNode = rangeChildren.get(0);
            ASTNode endNode = rangeChildren.get(1);

            Object startValue = evaluate(startNode);
            Object endValue = evaluate(endNode);

            int start = toInteger(startValue);
            int end = toInteger(endValue);

            for (int i = start; i <= end; i++) {
                try {
                    symbolTable.enterScope();
                    try {
                        symbolTable.declareVariable(iteratorName, "INTEGER", i);
                        evaluate(bodyNode);
                    } finally {
                        symbolTable.exitScope();
                    }
                } catch (BreakException e) {
                    break;
                } catch (ContinueException e) {
                    continue;
                }
            }
        } else {
            ASTNode listNode = rangeChildren.get(0);
            Object listValue = evaluate(listNode);
            List<?> list = (List<?>) listValue;
            String datatype;

            for (Object val : list) {
                datatype = getTypeName(val);

                try {
                    symbolTable.enterScope();
                    try {
                        symbolTable.declareVariable(iteratorName, datatype, val);
                        evaluate(bodyNode);
                    } finally {
                        symbolTable.exitScope();
                    }
                } catch (BreakException e) {
                    break;
                } catch (ContinueException e) {
                    continue;
                }
            }
        }

        return null;
    }

    /**
     * Executes a BreakNode. Throws a BreakException to exit the innermost loop.
     *
     * @param node the BreakNode to execute
     * @return never returns normally
     * @throws BreakException always
     */
    private Object executeBreakNode(BreakNode node) throws BreakException {
        throw new BreakException();
    }

    /**
     * Executes a ContinueNode. Throws a ContinueException to skip to the next
     * loop iteration.
     *
     * @param node the ContinueNode to execute
     * @return never returns normally
     * @throws ContinueException always
     */
    private Object executeContinueNode(ContinueNode node) throws ContinueException {
        throw new ContinueException();
    }

    /**
     * Executes a ConsiderNode (switch-case construct). Evaluates the expression
     * once, then compares it to each case value. Executes the body of the first
     * matching case and stops (no fall-through). If no case matches, executes
     * the otherwise branch (if present).
     *
     * @param node the ConsiderNode to execute
     * @return null
     * @throws InterpreterException if an error occurs during evaluation
     */
    private Object executeConsiderNode(NonTerminalNode node) throws InterpreterException {
        if (node.children.size() < 1) {
            throw new InterpreterException(
                    "ConsiderNode requires at least an expression", node);
        }

        ASTNode expressionNode = node.children.get(1);
        Object switchValue = evaluate(expressionNode);

        ASTNode currentCase = node.children.get(2);

        while (currentCase != null && currentCase instanceof NonTerminalNode && (currentCase.children.size() > 0)) {
            NonTerminalNode caseNode = (NonTerminalNode) currentCase;
            String type = ((TerminalNode) caseNode.children.get(0)).getLexeme();

            // Handle "case"
            if (type.equals("case")) {
                Object caseValue = evaluate(caseNode.children.get(1));

                if (valuesEqual(switchValue, caseValue)) {
                    return executeCaseBody(caseNode, 3);
                }
            }

            currentCase = (caseNode.children.size() > 4) ? caseNode.children.get(4) : null;
        }

        NonTerminalNode otherwiseCase = (NonTerminalNode) node.children.get(3);

        if (otherwiseCase.children.size() > 0) {
            return executeCaseBody(otherwiseCase, 1);
        }

        return null;

        /*for (int i = 2; i < node.children.size() - 1; i++) {
            ASTNode child = node.children.get(i);
            
            String caseCheck = ((TerminalNode) child.children.get(0)).getLexeme();

            if (!(caseCheck.equals("case") || caseCheck.equals("otherwise"))) {
                throw new InterpreterException(
                        "ConsiderNode should only contain CaseNode children", child);
            }

            Object caseValue = (caseCheck.equals("case")) ?
                    evaluate(child.children.get(1)) :
                    null;
            
            System.out.println("iteration #" + i + ": " + caseValue + " - " + caseCheck);

            if (caseValue == null || caseCheck.equals("otherwise")) {
                otherwiseCase = (NonTerminalNode) child;
                continue;
            }

            if (valuesEqual(switchValue, caseValue)) {
                symbolTable.enterScope();
                try {
                    for (ASTNode stmt : child.children.get(3).children) {
                        evaluate(stmt);
                    }
                } finally {
                    symbolTable.exitScope();
                }
                return null;
            }
        }

        if (otherwiseCase != null) {
            symbolTable.enterScope();
            try {
                evaluate(otherwiseCase.children.get(1));
            } finally {
                symbolTable.exitScope();
            }
        }

        return null;*/
    }

    private Object executeCaseBody(NonTerminalNode caseNode, int bodyIndex) throws InterpreterException {
        symbolTable.enterScope();
        try {
            for (ASTNode stmt : caseNode.children.get(bodyIndex).children) {
                evaluate(stmt);
            }
        } catch (BreakException | ContinueException e) {
            throw e; // re-propagate so the enclosing loop can handle it
        } finally {
            symbolTable.exitScope();
        }
        return null;
    }

    /**
     * Executes a ScopeBlockNode. Creates a new scope, executes the block's
     * statements, then exits the scope. Ensures scope is properly cleaned up
     * even if an error occurs.
     *
     * @param node the ScopeBlockNode to execute
     * @return null
     * @throws InterpreterException if an error occurs during evaluation
     */
    private Object executeScopeBlockNode(NonTerminalNode node) throws InterpreterException {      
        Map<String, Object> snapshot = symbolTable.snapshotVisibleValues();
        symbolTable.enterScope();
        try {
            for (ASTNode child : node.children.get(2).children) {
                evaluate(child);
            }
        } catch (BreakException | ContinueException e) {
            throw e; // re-propagate so the enclosing loop can handle it
        } finally {
            symbolTable.exitScope();
            symbolTable.restoreValues(snapshot);
        }
        return null;
    }

    // ========== Expression Evaluation Methods ==========
    /**
     * Evaluates a BinaryExprNode. Supports arithmetic, logical, and comparison
     * operators. Applies strict type checking to prevent invalid operations.
     *
     * @param node the BinaryExprNode to evaluate
     * @return the result of the binary operation
     * @throws InterpreterException if operands are invalid or operator is
     * unsupported
     */
    private Object evaluateBinaryExprNode(BinaryExprNode node) throws InterpreterException {
        // Filter out operator terminals so positional indexing only sees operands
        List<ASTNode> semanticChildren = new ArrayList<>();
        for (ASTNode child : node.children) {
            if (!(child instanceof TerminalNode)) {
                semanticChildren.add(child);
            }
        }

        if (semanticChildren.size() < 2) {
            throw new InterpreterException(
                    "BinaryExprNode requires exactly 2 children", node);
        }

        Object left = evaluate(semanticChildren.get(0));
        Object right = evaluate(semanticChildren.get(1));
        String operator = node.getOperator();

        switch (operator) {
            // Arithmetic operators
            case "plus":
                return evaluatePlus(left, right, node);
            case "minus":
                return evaluateMinus(left, right, node);
            case "times":
                return evaluateTimes(left, right, node);
            case "divided_by":
                return evaluateDividedBy(left, right, node);
            case "modulo":
                return evaluateModulo(left, right, node);
            case "raised_to":
                return evaluateRaisedTo(left, right, node);

            // Logical operators
            case "and":
                return evaluateLogicalAnd(left, right, node);
            case "or":
                return evaluateLogicalOr(left, right, node);
            case "xor":
                return evaluateLogicalXor(left, right, node);
            case "nand":
                return evaluateLogicalNand(left, right, node);
            case "nor":
                return evaluateLogicalNor(left, right, node);

            // Relational operators
            case "equal_to":
                return evaluateEqual(left, right, node);
            case "not_equal_to":
                return evaluateNotEqual(left, right, node);
            case "is":
                return evaluateIs(left, right, node);
            case "is_not":
                return evaluateIsNot(left, right, node);
            case "greater_than":
                return evaluateGreaterThan(left, right, node);
            case "less_than":
                return evaluateLessThan(left, right, node);
            case "greater_than_or_equal":
                return evaluateGreaterThanOrEqual(left, right, node);
            case "less_than_or_equal":
                return evaluateLessThanOrEqual(left, right, node);

            default:
                throw new InterpreterException(
                        "Unknown operator: " + operator, node);
        }
    }

    /**
     * Evaluates addition (numeric only; no string concatenation).
     *
     * @param left the left operand
     * @param right the right operand
     * @param node the node for error reporting
     * @return the sum as Integer or Double
     * @throws InterpreterException if operands are not numeric
     */
    private Object evaluatePlus(Object left, Object right, ASTNode node)
            throws InterpreterException {
        if (left instanceof Integer && right instanceof Integer) {
            return (Integer) left + (Integer) right;
        } else if (left instanceof Number && right instanceof Number) {
            return ((Number) left).doubleValue() + ((Number) right).doubleValue();
        } else {
            throw new InterpreterException(
                    "Type error: cannot add " + getTypeName(left) + " and "
                    + getTypeName(right) + " (plus operator requires numeric types)", node);
        }
    }

    /**
     * Evaluates subtraction (numeric only).
     *
     * @param left the left operand
     * @param right the right operand
     * @param node the node for error reporting
     * @return the difference as Integer or Double
     * @throws InterpreterException if operands are not numeric
     */
    private Object evaluateMinus(Object left, Object right, ASTNode node)
            throws InterpreterException {
        if (left instanceof Integer && right instanceof Integer) {
            return (Integer) left - (Integer) right;
        } else if (left instanceof Number && right instanceof Number) {
            return ((Number) left).doubleValue() - ((Number) right).doubleValue();
        } else {
            throw new InterpreterException(
                    "Type error: cannot subtract " + getTypeName(right) + " from "
                    + getTypeName(left) + " (minus operator requires numeric types)", node);
        }
    }

    /**
     * Evaluates multiplication (numeric only).
     *
     * @param left the left operand
     * @param right the right operand
     * @param node the node for error reporting
     * @return the product as Integer or Double
     * @throws InterpreterException if operands are not numeric
     */
    private Object evaluateTimes(Object left, Object right, ASTNode node)
            throws InterpreterException {
        if (left instanceof Integer && right instanceof Integer) {
            return (Integer) left * (Integer) right;
        } else if (left instanceof Number && right instanceof Number) {
            return ((Number) left).doubleValue() * ((Number) right).doubleValue();
        } else {
            throw new InterpreterException(
                    "Type error: cannot multiply " + getTypeName(left) + " and "
                    + getTypeName(right) + " (times operator requires numeric types)", node);
        }
    }

    /**
     * Evaluates division (numeric only). Result is always a Double. Throws an
     * exception on division by zero.
     *
     * @param left the left operand (dividend)
     * @param right the right operand (divisor)
     * @param node the node for error reporting
     * @return the quotient as Double
     * @throws InterpreterException if operands are not numeric or divisor is
     * zero
     */
    private Object evaluateDividedBy(Object left, Object right, ASTNode node)
            throws InterpreterException {
        if (!(left instanceof Number) || !(right instanceof Number)) {
            throw new InterpreterException(
                    "Type error: cannot divide " + getTypeName(left) + " by "
                    + getTypeName(right) + " (divided_by operator requires numeric types)", node);
        }

        double rightValue = ((Number) right).doubleValue();
        if (rightValue == 0.0) {
            throw new InterpreterException(
                    "Runtime error: division by zero", node);
        }

        return ((Number) left).doubleValue() / rightValue;
    }

    /**
     * Evaluates modulo (remainder of integer division). Both operands must be
     * integers.
     *
     * @param left the left operand (dividend)
     * @param right the right operand (divisor)
     * @param node the node for error reporting
     * @return the remainder as Integer
     * @throws InterpreterException if operands are not integers or divisor is
     * zero
     */
    private Object evaluateModulo(Object left, Object right, ASTNode node)
            throws InterpreterException {
        if (!(left instanceof Integer) || !(right instanceof Integer)) {
            throw new InterpreterException(
                    "Type error: modulo operator requires integer operands, got "
                    + getTypeName(left) + " modulo " + getTypeName(right), node);
        }

        int rightValue = (Integer) right;
        if (rightValue == 0) {
            throw new InterpreterException(
                    "Runtime error: modulo by zero", node);
        }

        return (Integer) left % rightValue;
    }

    /**
     * Evaluates exponentiation (raised_to). Both operands are converted to
     * Double for calculation.
     *
     * @param left the base
     * @param right the exponent
     * @param node the node for error reporting
     * @return the result as Double
     * @throws InterpreterException if operands are not numeric
     */
    private Object evaluateRaisedTo(Object left, Object right, ASTNode node)
            throws InterpreterException {
        if (!(left instanceof Number) || !(right instanceof Number)) {
            throw new InterpreterException(
                    "Type error: cannot raise " + getTypeName(left) + " to the power of "
                    + getTypeName(right) + " (raised_to operator requires numeric types)", node);
        }

        double base = ((Number) left).doubleValue();
        double exponent = ((Number) right).doubleValue();
        double result = Math.pow(base, exponent);

        // Return Integer if both operands were integers and result is whole
        if (left instanceof Integer && right instanceof Integer
                && result == Math.floor(result) && !Double.isInfinite(result)) {
            return (int) result;
        }

        return result;
    }

    // ========== Logical Operator Evaluation Methods ==========
    /**
     * Evaluates logical AND.
     */
    private Object evaluateLogicalAnd(Object left, Object right, ASTNode node)
            throws InterpreterException {
        return toBoolean(left) && toBoolean(right);
    }

    /**
     * Evaluates logical OR.
     */
    private Object evaluateLogicalOr(Object left, Object right, ASTNode node)
            throws InterpreterException {
        return toBoolean(left) || toBoolean(right);
    }

    /**
     * Evaluates exclusive OR (XOR).
     */
    private Object evaluateLogicalXor(Object left, Object right, ASTNode node)
            throws InterpreterException {
        return toBoolean(left) ^ toBoolean(right);
    }

    /**
     * Evaluates NAND (NOT AND).
     */
    private Object evaluateLogicalNand(Object left, Object right, ASTNode node)
            throws InterpreterException {
        return !(toBoolean(left) && toBoolean(right));
    }

    /**
     * Evaluates NOR (NOT OR).
     */
    private Object evaluateLogicalNor(Object left, Object right, ASTNode node)
            throws InterpreterException {
        return !(toBoolean(left) || toBoolean(right));
    }

    // ========== Relational Operator Evaluation Methods ==========
    /**
     * Evaluates equality (equal_to).
     */
    private Object evaluateEqual(Object left, Object right, ASTNode node)
            throws InterpreterException {
        if (left == null && right == null) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        return left.equals(right);
    }

    /**
     * Evaluates inequality (not_equal_to).
     */
    private Object evaluateNotEqual(Object left, Object right, ASTNode node)
            throws InterpreterException {
        return !(Boolean) evaluateEqual(left, right, node);
    }

    /**
     * Evaluates identity check (is).
     */
    private Object evaluateIs(Object left, Object right, ASTNode node)
            throws InterpreterException {
        if (left == null && right == null) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        return left == right;
    }

    /**
     * Evaluates negated identity check (is_not).
     */
    private Object evaluateIsNot(Object left, Object right, ASTNode node)
            throws InterpreterException {
        return !(Boolean) evaluateIs(left, right, node);
    }

    /**
     * Evaluates greater-than comparison (greater_than).
     */
    private Object evaluateGreaterThan(Object left, Object right, ASTNode node)
            throws InterpreterException {
        if (left instanceof Number && right instanceof Number) {
            return ((Number) left).doubleValue() > ((Number) right).doubleValue();
        }
        if (left instanceof String && right instanceof String) {
            return ((String) left).compareTo((String) right) > 0;
        }
        throw new InterpreterException(
                "Type error: cannot compare " + getTypeName(left) + " greater_than "
                + getTypeName(right) + " (greater_than requires both operands to be numeric or both to be strings)",
                node);
    }

    /**
     * Evaluates less-than comparison (less_than).
     */
    private Object evaluateLessThan(Object left, Object right, ASTNode node)
            throws InterpreterException {
        if (left instanceof Number && right instanceof Number) {
            return ((Number) left).doubleValue() < ((Number) right).doubleValue();
        }
        if (left instanceof String && right instanceof String) {
            return ((String) left).compareTo((String) right) < 0;
        }
        throw new InterpreterException(
                "Type error: cannot compare " + getTypeName(left) + " less_than "
                + getTypeName(right) + " (less_than requires both operands to be numeric or both to be strings)",
                node);
    }

    /**
     * Evaluates greater-than-or-equal comparison (greater_than_or_equal).
     */
    private Object evaluateGreaterThanOrEqual(Object left, Object right, ASTNode node)
            throws InterpreterException {
        if (left instanceof Number && right instanceof Number) {
            return ((Number) left).doubleValue() >= ((Number) right).doubleValue();
        }
        if (left instanceof String && right instanceof String) {
            return ((String) left).compareTo((String) right) >= 0;
        }
        throw new InterpreterException(
                "Type error: cannot compare " + getTypeName(left) + " greater_than_or_equal "
                + getTypeName(right) + " (greater_than_or_equal requires both operands to be numeric or both to be strings)",
                node);
    }

    /**
     * Evaluates less-than-or-equal comparison (less_than_or_equal).
     */
    private Object evaluateLessThanOrEqual(Object left, Object right, ASTNode node)
            throws InterpreterException {
        if (left instanceof Number && right instanceof Number) {
            return ((Number) left).doubleValue() <= ((Number) right).doubleValue();
        }
        if (left instanceof String && right instanceof String) {
            return ((String) left).compareTo((String) right) <= 0;
        }
        throw new InterpreterException(
                "Type error: cannot compare " + getTypeName(left) + " less_than_or_equal "
                + getTypeName(right) + " (less_than_or_equal requires both operands to be numeric or both to be strings)",
                node);
    }

    /**
     * Evaluates a LiteralNode. Determines the literal type from the string
     * value and returns an appropriate Object.
     *
     * @param node the LiteralNode to evaluate
     * @return the literal value as an Integer, Double, String, or Boolean
     * @throws InterpreterException if the literal format is invalid
     */
    private Object evaluateLiteralNode(LiteralNode node) throws InterpreterException {
        String value = node.getValue();

        if (value == null || value.isEmpty()) {
            throw new InterpreterException("Literal value cannot be empty", node);
        }

        // Quoted string — strip quotes
        if ((value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }

        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }

        //Only string literals can have whitespaces in them
        if (value.contains(" ")) {
            return value;
        }

        if (!value.contains(".")) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
            }
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
        }

        // Already-unquoted string — return as-is
        return value;
    }

    /**
     * Evaluates an IdentifierNode. Looks up the variable in the symbol table
     * and returns its current value.
     *
     * @param node the IdentifierNode to evaluate
     * @return the variable's current value
     * @throws InterpreterException if the variable doesn't exist
     */
    private Object evaluateIdentifierNode(IdentifierNode node) throws InterpreterException {
        String variableName = node.getName();
        try {
            VariableRecord record = symbolTable.lookupVariable(variableName);
            //System.err.println("DEBUG: " + variableName + " = " + record.getValue());
            return record.getValue();
        } catch (IllegalArgumentException e) {
            throw new InterpreterException(e.getMessage(), node);
        }
    }

    /**
     * Evaluates a TerminalNode. Only handles value-carrying terminals
     * (literals, booleans). Keyword terminals should never reach this method —
     * they are filtered out upstream by all execute/evaluate methods.
     *
     * @param node the TerminalNode to evaluate
     * @return the terminal's value
     * @throws InterpreterException if the token type is not a value-carrying
     * terminal
     */
    private Object evaluateTerminalNode(TerminalNode node) throws InterpreterException {
        String tokenType = node.getTokenType();
        String lexeme = node.getLexeme();

        switch (tokenType) {
            case "TK_INT":
                try {
                    return Integer.parseInt(lexeme);
                } catch (NumberFormatException e) {
                    throw new InterpreterException(
                            "Invalid integer literal: " + lexeme, node);
                }
            case "TK_REAL":
                try {
                    return Double.parseDouble(lexeme);
                } catch (NumberFormatException e) {
                    throw new InterpreterException(
                            "Invalid real literal: " + lexeme, node);
                }
            case "TK_STRING":
                if ((lexeme.startsWith("\"") && lexeme.endsWith("\""))
                        || (lexeme.startsWith("'") && lexeme.endsWith("'"))) {
                    return lexeme.substring(1, lexeme.length() - 1);
                }
                return lexeme;
            case "TK_TRUE":
                return true;
            case "TK_FALSE":
                return false;
            default:
                throw new InterpreterException(
                        "Cannot evaluate terminal node of type: " + tokenType, node);
        }
    }

    /**
     * Evaluates a NonTerminalNode. Generic evaluation for non-terminals without
     * specialized classes. Evaluates all children and returns the last child's
     * value.
     *
     * @param node the NonTerminalNode to evaluate
     * @return the result of evaluation (value of last child)
     * @throws InterpreterException if evaluation fails
     */
    private Object evaluateNonTerminalNode(NonTerminalNode node) throws InterpreterException, BreakException, ContinueException {
        //System.err.println("DEBUG NonTerminalNode: " + node.getLhs());
        String label = node.getLhs();

        switch (label) {
            // Binary expression non-terminals
            case "relational":
            case "logic_and_expr":
            case "logic_or_expr":
            case "logic_xor_expr":
            case "logic_nand_expr":
            case "logic_nor_expr":
            case "arithmetic_expr":
            case "term":
            case "power":
                return evaluateBinaryNonTerminal(node);

            case "length_op":
                for (ASTNode child : node.children) {
                    if (!(child instanceof TerminalNode)) {
                        Object string = evaluate(child);

                        if (string instanceof String) {
                            return string.toString().length();
                        } else {
                            throw new InterpreterException(
                                    "Cannot find the length of non-string identifier " + string, node);
                        }
                    }
                }
                return null;

            case "string_op":
                List<ASTNode> semanticChildren = new ArrayList<>();
                for (ASTNode child : node.children) {
                    if (!(child instanceof TerminalNode)) {
                        semanticChildren.add(child);
                    }
                }

                if (semanticChildren.size() != 2) {
                    throw new InterpreterException(
                            "Requires two separate string values for join_with", node);
                }

                Object value1 = evaluate(semanticChildren.get(0)),
                 value2 = evaluate(semanticChildren.get(1));

                if (!(value1 instanceof String)) {
                    throw new InterpreterException(
                            "Invalid identifier value " + value1 + ". Only strings can be used for join_with", node);
                }
                if (!(value2 instanceof String)) {
                    throw new InterpreterException(
                            "Invalid identifier value " + value2 + ". Only strings can be used for join_with", node);
                }

                String string1 = value1.toString(),
                 string2 = value2.toString();

                return string1 + string2;

            // Statement structure pass-throughs
            case "stmt_list":
            case "statement":
            case "action_stmt":
                Object stmtResult = null;
                for (ASTNode child : node.children) {
                    if (!(child instanceof TerminalNode)) {
                        try {
                            stmtResult = evaluate(child);
                        } catch (BreakException | ContinueException e) {
                            throw e; // must propagate up to the enclosing loop
                        }
                    }
                }
                return stmtResult;

            // IO statement (say/read)
            case "io_stmt": {
                boolean isSay = false;
                boolean isRead = false;
                for (ASTNode child : node.children) {
                    if (child instanceof TerminalNode) {
                        String tt = ((TerminalNode) child).getTokenType();
                        if ("TK_SAY".equals(tt)) {
                            isSay = true;
                        } else if ("TK_READ".equals(tt)) {
                            isRead = true;
                        }
                        continue;
                    }
                    if (isSay) {
                        printExpressionList(child);
                    } else if (isRead) {
                        readIdList(child);  // <-- new
                    }
                }
                if (isSay) {
                    System.out.println();
                }
                return null;
            }

            case "switch":
                return executeConsiderNode(node);

            // Expression pass-through wrappers
            case "expression_list":
            case "expression":
            case "logic_not_expr": {
                boolean hasNot = false;
                ASTNode operand = null;
                for (ASTNode child : node.children) {
                    if (child instanceof TerminalNode
                            && "TK_NOT".equals(((TerminalNode) child).getTokenType())) {
                        hasNot = true;
                    } else if (!(child instanceof TerminalNode)) {
                        operand = child;
                    }
                }
                Object result = evaluate(operand);
                return hasNot ? !toBoolean(result) : result;
            }
            case "primary": {
                // Check for item <expr> of <list> pattern
                boolean hasItem = false;
                for (ASTNode child : node.children) {
                    if (child instanceof TerminalNode
                            && "TK_ITEM".equals(((TerminalNode) child).getTokenType())) {
                        hasItem = true;
                        break;
                    }
                }

                if (hasItem) {
                    ASTNode indexExpr = null;
                    ASTNode listNode = null;
                    for (ASTNode child : node.children) {
                        if (child instanceof TerminalNode) {
                            continue; // skip TK_ITEM, TK_OF
                        }
                        if (indexExpr == null) {
                            indexExpr = child;    // first non-terminal: the index
                        } else {
                            listNode = child;                        // second: special_list
                        }
                    }
                    Object indexValue = evaluate(indexExpr);
                    int index = toInteger(indexValue);

                    // Evaluate special_list to get the list identifier
                    Object listValue = evaluate(listNode);
                    if (!(listValue instanceof List)) {
                        throw new InterpreterException("item...of requires a list variable", node);
                    }
                    List<?> list = (List<?>) listValue;
                    if (index < 1 || index > list.size()) {
                        throw new InterpreterException(
                                "Index " + index + " out of bounds for list of size " + list.size(), node);
                    }
                    return list.get(index - 1); // 1-based indexing
                }

                // Original pass-through for normal primary expressions
                Object result = null;
                for (ASTNode child : node.children) {
                    if (child instanceof TerminalNode) {
                        String tt = ((TerminalNode) child).getTokenType();
                        if (tt.equals("TK_TRUE") || tt.equals("TK_FALSE")
                                || tt.equals("TK_INT_LIT") || tt.equals("TK_REAL_LIT")
                                || tt.equals("TK_STR_LIT")) {
                            result = evaluate(child);
                        }
                    } else {
                        result = evaluate(child);
                    }
                }
                return result;
            }
            case "literal":
            case "num_literal": {
                Object result = null;
                for (ASTNode child : node.children) {
                    if (child instanceof TerminalNode) {
                        String tt = ((TerminalNode) child).getTokenType();
                        if (tt.equals("TK_TRUE") || tt.equals("TK_FALSE")
                                || tt.equals("TK_INT_LIT") || tt.equals("TK_REAL_LIT")
                                || tt.equals("TK_STR_LIT")) {
                            result = evaluate(child);
                        }
                    } else {
                        result = evaluate(child);
                    }
                }
                return result;
            }
            case "conditional": {
                // Structure from trace Rule 13: if <expression> then <stmt_list> <else_if_clause> <else_clause> done
                // Filter into: condition, then-body, and optional else/else-if parts
                ASTNode conditionNode = null;
                ASTNode thenBody = null;
                ASTNode elseIfClause = null;
                ASTNode elseClause = null;

                for (ASTNode child : node.children) {
                    if (child instanceof TerminalNode) {
                        continue; // skip if/then/done
                    }
                    if (conditionNode == null) {
                        conditionNode = child; // first non-terminal is the condition
                    } else if (thenBody == null) {
                        thenBody = child;     // second is the then body (stmt_list)
                    } else if (child instanceof NonTerminalNode) {
                        String childLabel = ((NonTerminalNode) child).getLhs();
                        if ("else_if_clause".equals(childLabel)) {
                            elseIfClause = child;
                        } else if ("else_clause".equals(childLabel)) {
                            elseClause = child;
                        }
                    }
                }

                Object conditionValue = evaluate(conditionNode);
                if (toBoolean(conditionValue)) {
                    symbolTable.enterScope();
                    try {
                        evaluate(thenBody);
                    } catch (BreakException | ContinueException e) {
                        throw e; // re-propagate so the enclosing loop can handle it
                    }
                    finally {
                        symbolTable.exitScope();
                    }
                } else if (elseIfClause != null && hasNonTerminalChildren(elseIfClause)) {
                    try {
                        evaluate(elseIfClause);
                    } catch (BreakException | ContinueException e) {
                        throw e; // re-propagate so the enclosing loop can handle it
                    }
                } else if (elseClause != null) {
                    try {
                        evaluate(elseClause);
                    }
                    catch (BreakException | ContinueException e) {
                        throw e; // re-propagate so the enclosing loop can handle it
                    }
                }
                return null;
            }

            case "else_if_clause":
                // Pass-through — contains nested conditionals if present
                for (ASTNode child : node.children) {
                    try {
                        if (!(child instanceof TerminalNode)) {
                            evaluate(child);
                        }
                    }
                    catch (BreakException | ContinueException e) {
                        throw e; // re-propagate so the enclosing loop can handle it
                    }
                }
                return null;

            case "else_clause": {
                // Structure: else <stmt_list>
                symbolTable.enterScope();
                try {
                    for (ASTNode child : node.children) {
                        if (child instanceof TerminalNode) {
                            continue; // skip TK_ELSE
                        }
                        evaluate(child);
                    }
                } catch (BreakException | ContinueException e) {
                        throw e; // re-propagate so the enclosing loop can handle it
                    }
                finally {
                    symbolTable.exitScope();
                }
                return null;
            }

            case "list_literal": {
                List<Object> list = new ArrayList<>();
                for (ASTNode child : node.children) {
                    if (child instanceof TerminalNode) {
                        continue; // skip [ and ]
                    }
                    collectListElements(child, list);
                }
                return list;
            }

            case "list_tail":
            case "list_elements": {
                List<Object> list = new ArrayList<>();
                collectListElements(node, list);
                return list;
            }

            case "find_op": {
                ASTNode needleNode = null;
                ASTNode haystackNode = null;
                for (ASTNode child : node.children) {
                    if (child instanceof TerminalNode) {
                        continue; // skip TK_FIND, TK_IN
                    }
                    if (needleNode == null) {
                        needleNode = child;    // first non-terminal: the substring to find
                    } else {
                        haystackNode = child;  // second: the string to search in
                    }
                }

                if (needleNode == null || haystackNode == null) {
                    throw new InterpreterException("find_op requires two operands", node);
                }

                Object needle = evaluate(needleNode);
                Object haystack = evaluate(haystackNode);

                if (!(needle instanceof String)) {
                    throw new InterpreterException(
                            "find requires a string to search for, got " + getTypeName(needle), node);
                }
                if (!(haystack instanceof String)) {
                    throw new InterpreterException(
                            "find requires a string to search in, got " + getTypeName(haystack), node);
                }

                int idx = ((String) haystack).indexOf((String) needle);
                if (idx == -1) {
                    throw new InterpreterException(
                            "Substring \"" + needle + "\" not found in \"" + haystack + "\"", node);
                }
                return idx + 1; // 1-based indexing
            }
            
            case "repeat_loop":
                return executeRepeatUntilNode(node);
                
            case "scope_statement":
                return executeScopeBlockNode(node);
                
            case "loop_control":
                ASTNode temp = node.children.get(0);
                if(((TerminalNode) temp).getLexeme().equals("break")) {
                    throw new BreakException();
                }
                else {
                    throw new ContinueException();
                }

            default: {
                Object result = null;
                for (ASTNode child : node.children) {
                    if (child instanceof TerminalNode) {
                        String tt = ((TerminalNode) child).getTokenType();
                        if (tt.equals("TK_TRUE") || tt.equals("TK_FALSE")
                                || tt.equals("TK_INT_LIT") || tt.equals("TK_REAL_LIT")
                                || tt.equals("TK_STR_LIT")) {
                            result = evaluate(child);
                        }
                    } else {
                        result = evaluate(child);
                    }
                }
                return result;
            }
        }
    }

    private void readIdList(ASTNode node) throws InterpreterException {
        if (node instanceof IdentifierNode) {
            String varName = ((IdentifierNode) node).getName();
            if (!symbolTable.variableExists(varName)) {
                throw new InterpreterException(
                        "Variable '" + varName + "' has not been declared", node);
            }
            VariableRecord record = symbolTable.lookupVariable(varName);
            String inputLine = inputScanner.nextLine();
            //System.err.println("DEBUG read [" + varName + "] = [" + inputLine + "]");
            Object value = convertStringToType(inputLine, record.getDataType(), node);
            symbolTable.assignVariable(varName, value);
        } else if (node instanceof NonTerminalNode) {
            for (ASTNode child : node.children) {
                if (child instanceof TerminalNode) {
                    continue; // skip commas
                }
                readIdList(child);
            }
        }
    }

    private void collectListElements(ASTNode node, List<Object> list) throws InterpreterException {
        if (!(node instanceof NonTerminalNode)) {
            return;
        }
        for (ASTNode child : node.children) {
            if (child instanceof TerminalNode) {
                continue; // skip commas
            }
            String childLabel = child instanceof NonTerminalNode
                    ? ((NonTerminalNode) child).getLhs() : "";
            if ("expression".equals(childLabel)) {
                list.add(evaluate(child));
            } else if ("list_tail".equals(childLabel)) {
                collectListElements(child, list); // recurse for next elements
            }
        }
    }

    private void printExpressionList(ASTNode node) throws InterpreterException {
        if (node instanceof NonTerminalNode
                && "expression_list".equals(((NonTerminalNode) node).getLhs())) {
            for (ASTNode child : node.children) {
                if (child instanceof TerminalNode) {
                    continue; // skip commas
                }
                printExpressionList(child); // recurse for nested expression_lists
            }
        } else {
            System.out.print(valueToString(evaluate(node)));
        }
    }

    private boolean hasNonTerminalChildren(ASTNode node) {
        for (ASTNode child : node.children) {
            if (!(child instanceof TerminalNode)) {
                return true;
            }
        }
        return false;
    }

    private Object evaluateBinaryNonTerminal(NonTerminalNode node) throws InterpreterException {
        // Collect semantic children (non-terminals and identifiers/literals)
        // and find the operator terminal in the middle
        List<ASTNode> operands = new ArrayList<>();
        String operator = null;

        for (ASTNode child : node.children) {
            if (child instanceof TerminalNode) {
                // Direct operator terminal (e.g. TK_AND, TK_PLUS)
                String op = tokenTypeToOperator(((TerminalNode) child).getTokenType());
                if (op != null) {
                    operator = op;
                }
            } else if (child instanceof NonTerminalNode) {
                NonTerminalNode nt = (NonTerminalNode) child;
                // Check if this is an operator wrapper node like <rel_op>
                String op = extractOperatorFromWrapper(nt);
                if (op != null) {
                    operator = op;
                } else {
                    operands.add(child);
                }
            } else {
                operands.add(child);
            }
        }

        // Pass-through reduction (Rule *b): single operand, no operator
        if (operands.size() == 1 && operator == null) {
            return evaluate(operands.get(0));
        }

        if (operands.size() != 2 || operator == null) {
            throw new InterpreterException(
                    "Malformed binary expression node: " + node.getLhs(), node);
        }

        Object left = evaluate(operands.get(0));
        Object right = evaluate(operands.get(1));

        // Reuse the existing operator dispatch
        switch (operator) {
            case "plus":
                return evaluatePlus(left, right, node);
            case "minus":
                return evaluateMinus(left, right, node);
            case "times":
                return evaluateTimes(left, right, node);
            case "divided_by":
                return evaluateDividedBy(left, right, node);
            case "modulo":
                return evaluateModulo(left, right, node);
            case "raised_to":
                return evaluateRaisedTo(left, right, node);
            case "and":
                return evaluateLogicalAnd(left, right, node);
            case "or":
                return evaluateLogicalOr(left, right, node);
            case "xor":
                return evaluateLogicalXor(left, right, node);
            case "nand":
                return evaluateLogicalNand(left, right, node);
            case "nor":
                return evaluateLogicalNor(left, right, node);
            case "greater_than":
                return evaluateGreaterThan(left, right, node);
            case "less_than":
                return evaluateLessThan(left, right, node);
            case "greater_than_or_equal_to":
                return evaluateGreaterThanOrEqual(left, right, node);
            case "less_than_or_equal_to":
                return evaluateLessThanOrEqual(left, right, node);
            case "equal_to":
                return evaluateEqual(left, right, node);
            case "not_equal_to":
                return evaluateNotEqual(left, right, node);
            case "is":
                return evaluateIs(left, right, node);
            case "is_not":
                return evaluateIsNot(left, right, node);
            default:
                throw new InterpreterException("Unknown operator: " + operator, node);
        }
    }

    private String extractOperatorFromWrapper(NonTerminalNode node) {
        // Operator wrappers contain a single terminal that maps to an operator
        if (node.children.size() == 1 && node.children.get(0) instanceof TerminalNode) {
            String op = tokenTypeToOperator(
                    ((TerminalNode) node.children.get(0)).getTokenType());
            return op;  // null if the terminal isn't an operator token
        }
        return null;
    }

    private String tokenTypeToOperator(String tokenType) {
        switch (tokenType) {
            case "TK_PLUS":
                return "plus";
            case "TK_MINUS":
                return "minus";
            case "TK_TIMES":
                return "times";
            case "TK_DIV":
                return "divided_by";
            case "TK_MOD":
                return "modulo";
            case "TK_EXP":
                return "raised_to";
            case "TK_AND":
                return "and";
            case "TK_OR":
                return "or";
            case "TK_XOR":
                return "xor";
            case "TK_NAND":
                return "nand";
            case "TK_NOR":
                return "nor";
            case "TK_GREATERTHAN":
                return "greater_than";
            case "TK_LESSTHAN":
                return "less_than";
            case "TK_GREATERTHANOREQTO":
                return "greater_than_or_equal_to";
            case "TK_LESSTHANOREQTO":
                return "less_than_or_equal_to";
            case "TK_EQTO":
                return "equal_to";
            case "TK_NOTEQTO":
                return "not_equal_to";
            case "TK_IS":
                return "is";
            case "TK_ISNOT":
                return "is_not";
            default:
                return null;
        }
    }

    // ========== Helper Methods ==========
    /**
     * Converts an Object to a boolean value.
     */
    private boolean toBoolean(Object value) throws InterpreterException {
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof Integer) {
            return ((Integer) value) != 0;
        } else if (value instanceof Double) {
            return ((Double) value) != 0.0;
        } else if (value instanceof String) {
            return !((String) value).isEmpty();
        } else {
            throw new InterpreterException("Cannot convert " + value + " to boolean", -1);
        }
    }

    /**
     * Converts an Object to an integer value.
     */
    private int toInteger(Object value) throws InterpreterException {
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Double) {
            return ((Double) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                throw new InterpreterException("Cannot convert \"" + value + "\" to integer", -1);
            }
        } else {
            throw new InterpreterException("Cannot convert " + value + " to integer", -1);
        }
    }

    /**
     * Returns a human-readable type name for an Object.
     */
    private String getTypeName(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof Integer) {
            return "INTEGER";
        } else if (value instanceof Double) {
            return "REAL";
        } else if (value instanceof String) {
            return "STRING";
        } else if (value instanceof Boolean) {
            return "BOOLEAN";
        } else {
            return value.getClass().getSimpleName();
        }
    }

    /**
     * Checks if a value is type-compatible with a declared type.
     */
    private boolean isTypeCompatible(String declaredType, Object value) {
        if (value == null) {
            return true;
        }

        switch (declaredType.toUpperCase()) {
            case "INTEGER":
                return value instanceof Integer;
            case "REAL":
                return value instanceof Integer || value instanceof Double;
            case "STRING":
                return value instanceof String;
            case "BOOLEAN":
                return value instanceof Boolean;
            case "LIST":
                return value instanceof List;
            default:
                return true;
        }
    }

    /**
     * Converts a value to a string representation for output.
     */
    private String valueToString(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof Boolean) {
            return (Boolean) value ? "true" : "false";
        } else {
            return value.toString();
        }
    }

    /**
     * Converts a string input to the appropriate type based on the declared
     * type.
     */
    private Object convertStringToType(String inputString, String declaredType, ASTNode node)
            throws InterpreterException {
        switch (declaredType.toUpperCase()) {
            case "INTEGER":
                try {
                    return Integer.parseInt(inputString.trim());
                } catch (NumberFormatException e) {
                    throw new InterpreterException(
                            "Input error: \"" + inputString + "\" cannot be converted to INTEGER", node);
                }
            case "REAL":
                try {
                    return Double.parseDouble(inputString.trim());
                } catch (NumberFormatException e) {
                    throw new InterpreterException(
                            "Input error: \"" + inputString + "\" cannot be converted to REAL", node);
                }
            case "STRING":
                return inputString.trim();
            case "BOOLEAN":
                String trimmed = inputString.trim().toLowerCase();
                if ("true".equals(trimmed) || "1".equals(trimmed) || "yes".equals(trimmed)) {
                    return true;
                } else if ("false".equals(trimmed) || "0".equals(trimmed) || "no".equals(trimmed)) {
                    return false;
                } else {
                    throw new InterpreterException(
                            "Input error: \"" + inputString + "\" cannot be converted to BOOLEAN "
                            + "(valid values: true, false, yes, no, 0, 1)", node);
                }
            default:
                return inputString;
        }
    }

    /**
     * Compares two values for equality in case statements.
     */
    private boolean valuesEqual(Object value1, Object value2) {
        if (value1 == null && value2 == null) {
            return true;
        }
        if (value1 == null || value2 == null) {
            return false;
        }
        if (value1 instanceof Number && value2 instanceof Number) {
            return ((Number) value1).doubleValue() == ((Number) value2).doubleValue();
        }
        return value1.equals(value2);
    }

    /**
     * Gets the symbol table used by this interpreter.
     */
    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    /**
     * Closes the input scanner.
     */
    public void close() {
        if (inputScanner != null) {
            inputScanner.close();
        }
    }
}
