package pseudocode_compiler;

import java.util.Scanner;

/**
 * Interpreter for executing the abstract syntax tree (AST) of a pseudocode program.
 * Uses a visitor pattern to route different node types to specialized execution methods.
 * Maintains a symbol table for managing variables and scopes during execution.
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
     * Interprets the given AST root node.
     * Entry point for interpretation; initializes execution with the global scope.
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
     * Evaluates an AST node using a visitor pattern.
     * Routes the node to the appropriate execution method based on its type.
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
            return executeRepeatUntilNode((RepeatUntilNode) node);
        } else if (node instanceof ForLoopNode) {
            return executeForLoopNode((ForLoopNode) node);
        } else if (node instanceof BreakNode) {
            return executeBreakNode((BreakNode) node);
        } else if (node instanceof ContinueNode) {
            return executeContinueNode((ContinueNode) node);
        } else if (node instanceof ConsiderNode) {
            return executeConsiderNode((ConsiderNode) node);
        } else if (node instanceof ScopeBlockNode) {
            return executeScopeBlockNode((ScopeBlockNode) node);
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
        // Process semantic children (DeclSectionNode and StmtSectionNode)
        // Skip keyword terminals and the program identifier
        for (ASTNode child : node.children) {
            // Skip keyword terminals like "Program" and "End.", and the program name identifier
            if (child instanceof TerminalNode || child instanceof IdentifierNode) {
                continue;
            }
            // Only process DeclSectionNode and StmtSectionNode
            if (child instanceof DeclSectionNode || child instanceof StmtSectionNode) {
                evaluate(child);
            }
        }
        return null;
    }

    /**
     * Executes a DeclSectionNode, processing all variable declarations.
     * Extracts variable names and types from the AST and registers them in the symbol table.
     *
     * @param node the DeclSectionNode to execute
     * @return null
     * @throws InterpreterException if an error occurs
     */
    private Object executeDeclSectionNode(DeclSectionNode node) throws InterpreterException {
        // Extract all variable declarations from the declaration section
        // The structure is: DeclSectionNode -> decl_list -> declaration -> (type, id_list)
        
        for (ASTNode child : node.children) {
            if (child instanceof TerminalNode) {
                // Skip keyword terminals like "Declaration_Section"
                continue;
            }
            // Process all non-terminal children (typically decl_list nodes)
            processDeclarationList(child);
        }
        return null;
    }

    /**
     * Recursively processes declaration list nodes to extract variable declarations.
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
                    // Process a single declaration
                    extractVariablesFromDeclaration(child);
                } else if ("decl_list".equals(label) || "decl_section".equals(label)) {
                    // Recursively process nested decl_list
                    processDeclarationList(child);
                }
            }
        }
    }

    /**
     * Extracts variable declarations from a declaration node and registers them in the symbol table.
     */
    private void extractVariablesFromDeclaration(ASTNode declNode) throws InterpreterException {
        // Declaration structure: type identifier id_list
        // Need to extract type from type node and identifiers from id_list
        
        String dataType = null;
        
        for (ASTNode child : declNode.children) {
            if (child instanceof TerminalNode) {
                // Type keywords are terminals
                TerminalNode tn = (TerminalNode) child;
                String tokenType = tn.getTokenType();
                
                // Map token types to data type strings
                if ("TK_INT".equals(tokenType)) {
                    dataType = "INTEGER";
                } else if ("TK_DOUBLE".equals(tokenType)) {
                    dataType = "REAL";
                } else if ("TK_STRING".equals(tokenType)) {
                    dataType = "STRING";
                } else if ("TK_BOOL".equals(tokenType)) {
                    dataType = "BOOLEAN";
                } else if ("TK_ID".equals(tokenType)) {
                    // Identifier terminal
                    String varName = tn.getLexeme();
                    if (dataType != null) {
                        try {
                            symbolTable.declareVariable(varName, dataType);
                        } catch (RuntimeException e) {
                            // Variable might already be declared, continue
                        }
                    }
                }
            } else if (child instanceof IdentifierNode && dataType != null) {
                // Found an identifier to declare with the extracted type
                String varName = ((IdentifierNode) child).getName();
                try {
                    symbolTable.declareVariable(varName, dataType);
                } catch (RuntimeException e) {
                    // Variable might already be declared, continue
                }
            } else if (child instanceof NonTerminalNode) {
                NonTerminalNode nt = (NonTerminalNode) child;
                String label = nt.getLhs();
                
                if ("type".equals(label) || "base_type".equals(label)) {
                    // Extract type from type node
                    dataType = extractTypeFromNode(child);
                } else if ("id_list".equals(label) || "identifier_list".equals(label)) {
                    // Extract all identifiers from id_list with the previously determined type
                    extractIdentifiersFromList(child, dataType);
                }
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
                if ("TK_INT".equals(tokenType)) {
                    return "INTEGER";
                } else if ("TK_DOUBLE".equals(tokenType)) {
                    return "REAL";
                } else if ("TK_STRING".equals(tokenType)) {
                    return "STRING";
                } else if ("TK_BOOL".equals(tokenType)) {
                    return "BOOLEAN";
                }
            }
        }
        return null;
    }

    /**
     * Extracts identifiers from an id_list and declares them with the given type.
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
                // Recursively process nested id_list nodes
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
        // Execute all statements in order (skip keyword terminals)
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
     * Executes an AssignmentNode.
     * Evaluates the right-hand expression and assigns it to the identifier.
     * Performs type checking: verifies the value is compatible with the variable's declared type.
     *
     * @param node the AssignmentNode to execute
     * @return null
     * @throws InterpreterException if the variable doesn't exist or type mismatch occurs
     */
    private Object executeAssignmentNode(AssignmentNode node) throws InterpreterException {
        // Find the identifier and expression nodes among the children
        // (may include keyword terminals like "set" and "to")
        IdentifierNode identifierNode = null;
        ASTNode expressionNode = null;
        
        for (ASTNode child : node.children) {
            if (child instanceof IdentifierNode && identifierNode == null) {
                identifierNode = (IdentifierNode) child;
            } else if (!(child instanceof TerminalNode) && identifierNode != null && expressionNode == null) {
                // After finding the identifier, the first non-terminal that isn't a terminal keyword is the expression
                expressionNode = child;
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

        // Verify variable exists and perform type checking
        try {
            if (!symbolTable.variableExists(variableName)) {
                throw new InterpreterException(
                    "Variable '" + variableName + "' has not been declared", node);
            }

            VariableRecord record = symbolTable.lookupVariable(variableName);
            String declaredType = record.getDataType();

            // Type compatibility check
            if (!isTypeCompatible(declaredType, value)) {
                throw new InterpreterException(
                    "Type error: cannot assign " + getTypeName(value) + " to variable '" + 
                    variableName + "' of type " + declaredType, node);
            }

            // Assign the value
            symbolTable.assignVariable(variableName, value);
        } catch (IllegalArgumentException e) {
            throw new InterpreterException(e.getMessage(), node);
        }

        return null;
    }

    /**
     * Executes a SayNode.
     * Outputs the evaluated expression(s) to System.out with implicit string conversion.
     * All types are converted to strings before printing.
     *
     * @param node the SayNode to execute
     * @return null
     * @throws InterpreterException if evaluation fails
     */
    private Object executeSayNode(SayNode node) throws InterpreterException {
        // Process all expressions in the say statement
        for (ASTNode child : node.children) {
            Object value = evaluate(child);
            String output = valueToString(value);
            System.out.print(output);
        }
        System.out.println();  // Print newline at the end
        return null;
    }

    /**
     * Executes a ReadNode.
     * Reads a line of input from System.in using Scanner and stores it in the variable.
     * The input is stored as a String initially; type conversion is handled during assignment.
     *
     * @param node the ReadNode to execute
     * @return null
     * @throws InterpreterException if the variable doesn't exist or input reading fails
     */
    private Object executeReadNode(ReadNode node) throws InterpreterException {
        String variableName = node.getVariableName();

        // Verify variable exists
        if (!symbolTable.variableExists(variableName)) {
            throw new InterpreterException(
                "Variable '" + variableName + "' has not been declared", node);
        }

        try {
            // Read input from System.in
            if (!inputScanner.hasNextLine()) {
                throw new InterpreterException(
                    "Error reading input: no input available", node);
            }

            String inputLine = inputScanner.nextLine();
            VariableRecord record = symbolTable.lookupVariable(variableName);
            String declaredType = record.getDataType();

            // Convert input to the appropriate type based on variable's declared type
            Object convertedValue = convertStringToType(inputLine, declaredType, node);

            // Assign the converted value
            symbolTable.assignVariable(variableName, convertedValue);
        } catch (IllegalArgumentException e) {
            throw new InterpreterException(e.getMessage(), node);
        }

        return null;
    }

    /**
     * Executes an IfNode.
     * Evaluates the condition and executes the appropriate branch.
     * Supports if/else_if/else chains with proper type checking on conditions.
     *
     * @param node the IfNode to execute
     * @return null
     * @throws InterpreterException if an error occurs during evaluation
     */
    private Object executeIfNode(IfNode node) throws InterpreterException {
        // Expected structure: child[0] = condition, child[1] = then branch, 
        // child[2], child[3], ... = else_if/else branches (optional)
        if (node.children.size() < 2) {
            throw new InterpreterException(
                "IfNode requires at least 2 children (condition and then-branch)", node);
        }

        ASTNode conditionNode = node.children.get(0);
        ASTNode thenBranch = node.children.get(1);
        Object conditionValue = evaluate(conditionNode);

        // Type check: condition must evaluate to a boolean or truthy value
        try {
            boolean condition = toBoolean(conditionValue);

            if (condition) {
                symbolTable.enterScope();
                try {
                    evaluate(thenBranch);
                } finally {
                    symbolTable.exitScope();
                }
                return null;
            }

            // Handle else_if and else branches
            for (int i = 2; i < node.children.size(); i++) {
                ASTNode branch = node.children.get(i);

                // Check if this is an else_if (another IfNode)
                if (branch instanceof IfNode) {
                    // Recursively evaluate the else_if
                    executeIfNode((IfNode) branch);
                    return null;
                } else {
                    // This is the else branch (StmtSectionNode)
                    symbolTable.enterScope();
                    try {
                        evaluate(branch);
                    } finally {
                        symbolTable.exitScope();
                    }
                    return null;
                }
            }
        } catch (InterpreterException e) {
            throw e;
        } catch (Exception e) {
            throw new InterpreterException(
                "Type error: if condition must evaluate to a boolean value", node);
        }

        return null;
    }

    /**
     * Executes a WhileLoopNode.
     * Repeatedly evaluates the body while the condition is true.
     * Supports break and continue statements via custom exceptions.
     *
     * @param node the WhileLoopNode to execute
     * @return null
     * @throws InterpreterException if an error occurs during evaluation
     */
    private Object executeWhileLoopNode(WhileLoopNode node) throws InterpreterException {
        // Expected structure: child[0] = condition, child[1] = body
        if (node.children.size() < 2) {
            throw new InterpreterException(
                "WhileLoopNode requires exactly 2 children (condition and body)", node);
        }

        ASTNode conditionNode = node.children.get(0);
        ASTNode body = node.children.get(1);

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
                // Break out of the loop
                break;
            } catch (ContinueException e) {
                // Continue to next iteration
                continue;
            }
        }

        return null;
    }

    /**
     * Executes a RepeatUntilNode.
     * Executes the body once, then repeats while the condition is false (until it becomes true).
     * Supports break and continue statements via custom exceptions.
     *
     * @param node the RepeatUntilNode to execute
     * @return null
     * @throws InterpreterException if an error occurs during evaluation
     */
    private Object executeRepeatUntilNode(RepeatUntilNode node) throws InterpreterException {
        // Expected structure: child[0] = body, child[1] = condition
        if (node.children.size() < 2) {
            throw new InterpreterException(
                "RepeatUntilNode requires exactly 2 children (body and condition)", node);
        }

        ASTNode body = node.children.get(0);
        ASTNode conditionNode = node.children.get(1);

        do {
            try {
                symbolTable.enterScope();
                try {
                    evaluate(body);
                } finally {
                    symbolTable.exitScope();
                }
            } catch (BreakException e) {
                // Break out of the loop
                break;
            } catch (ContinueException e) {
                // Continue to next iteration (which evaluates condition)
                // No special action needed; the loop will naturally continue
            }

            // Check the until condition (exit when true)
            Object conditionValue = evaluate(conditionNode);
            if (toBoolean(conditionValue)) {
                break;
            }
        } while (true);

        return null;
    }

    /**
     * Executes a ForLoopNode.
     * Initializes the loop variable, then iterates from start to end value.
     * Supports break and continue statements via custom exceptions.
     *
     * @param node the ForLoopNode to execute
     * @return null
     * @throws InterpreterException if an error occurs during evaluation
     */
    private Object executeForLoopNode(ForLoopNode node) throws InterpreterException {
        // Expected structure: child[0] = iterator, child[1] = start, child[2] = end, child[3] = body
        if (node.children.size() < 4) {
            throw new InterpreterException(
                "ForLoopNode requires exactly 4 children (iterator, start, end, body)", node);
        }

        ASTNode iteratorNode = node.children.get(0);
        ASTNode startNode = node.children.get(1);
        ASTNode endNode = node.children.get(2);
        ASTNode bodyNode = node.children.get(3);

        if (!(iteratorNode instanceof IdentifierNode)) {
            throw new InterpreterException(
                "For-loop iterator must be an identifier", iteratorNode);
        }

        String iteratorName = ((IdentifierNode) iteratorNode).getName();
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
                // Break out of the loop
                break;
            } catch (ContinueException e) {
                // Continue to next iteration
                continue;
            }
        }

        return null;
    }

    /**
     * Executes a BreakNode.
     * Throws a BreakException to exit the innermost loop.
     *
     * @param node the BreakNode to execute
     * @return never returns normally
     * @throws BreakException always
     */
    private Object executeBreakNode(BreakNode node) throws BreakException {
        throw new BreakException();
    }

    /**
     * Executes a ContinueNode.
     * Throws a ContinueException to skip to the next loop iteration.
     *
     * @param node the ContinueNode to execute
     * @return never returns normally
     * @throws ContinueException always
     */
    private Object executeContinueNode(ContinueNode node) throws ContinueException {
        throw new ContinueException();
    }

    /**
     * Executes a ConsiderNode (switch-case construct).
     * Evaluates the expression once, then compares it to each case value.
     * Executes the body of the first matching case and stops (no fall-through).
     * If no case matches, executes the otherwise branch (if present).
     *
     * @param node the ConsiderNode to execute
     * @return null
     * @throws InterpreterException if an error occurs during evaluation
     */
    private Object executeConsiderNode(ConsiderNode node) throws InterpreterException {
        // Expected structure: child[0] = expression, child[1...n] = case/otherwise nodes
        if (node.children.size() < 1) {
            throw new InterpreterException(
                "ConsiderNode requires at least an expression", node);
        }

        ASTNode expressionNode = node.children.get(0);
        Object switchValue = evaluate(expressionNode);

        // Iterate through all case nodes
        CaseNode otherwiseCase = null;
        for (int i = 1; i < node.children.size(); i++) {
            ASTNode child = node.children.get(i);

            if (!(child instanceof CaseNode)) {
                throw new InterpreterException(
                    "ConsiderNode should only contain CaseNode children", child);
            }

            CaseNode caseNode = (CaseNode) child;

            // Check if this is the otherwise case
            if (caseNode.getCaseValue() == null) {
                otherwiseCase = caseNode;
                continue;  // Process otherwise last
            }

            // Compare the switch value with the case value
            if (valuesEqual(switchValue, caseNode.getCaseValue())) {
                // Execute this case and exit (no fall-through)
                symbolTable.enterScope();
                try {
                    for (ASTNode stmt : caseNode.children) {
                        evaluate(stmt);
                    }
                } finally {
                    symbolTable.exitScope();
                }
                return null;
            }
        }

        // If no case matched, execute the otherwise case
        if (otherwiseCase != null) {
            symbolTable.enterScope();
            try {
                for (ASTNode stmt : otherwiseCase.children) {
                    evaluate(stmt);
                }
            } finally {
                symbolTable.exitScope();
            }
        }

        return null;
    }

    /**
     * Executes a ScopeBlockNode.
     * Creates a new scope, executes the block's statements, then exits the scope.
     * Ensures scope is properly cleaned up even if an error occurs.
     *
     * @param node the ScopeBlockNode to execute
     * @return null
     * @throws InterpreterException if an error occurs during evaluation
     */
    private Object executeScopeBlockNode(ScopeBlockNode node) throws InterpreterException {
        symbolTable.enterScope();
        try {
            // Execute all statements in the block
            for (ASTNode child : node.children) {
                evaluate(child);
            }
        } finally {
            symbolTable.exitScope();
        }
        return null;
    }

    // ========== Expression Evaluation Methods ==========

    /**
     * Evaluates a BinaryExprNode.
     * Supports arithmetic, logical, and comparison operators.
     * Applies strict type checking to prevent invalid operations.
     *
     * @param node the BinaryExprNode to evaluate
     * @return the result of the binary operation
     * @throws InterpreterException if operands are invalid or operator is unsupported
     */
    private Object evaluateBinaryExprNode(BinaryExprNode node) throws InterpreterException {
        // Expected structure: child[0] = left operand, child[1] = right operand
        if (node.children.size() < 2) {
            throw new InterpreterException(
                "BinaryExprNode requires exactly 2 children", node);
        }

        Object left = evaluate(node.children.get(0));
        Object right = evaluate(node.children.get(1));
        String operator = node.getOperator();

        // Arithmetic operators: require numeric types
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
     * @param left  the left operand
     * @param right the right operand
     * @param node  the node for error reporting
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
                "Type error: cannot add " + getTypeName(left) + " and " + 
                getTypeName(right) + " (plus operator requires numeric types)", node);
        }
    }

    /**
     * Evaluates subtraction (numeric only).
     *
     * @param left  the left operand
     * @param right the right operand
     * @param node  the node for error reporting
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
                "Type error: cannot subtract " + getTypeName(right) + " from " + 
                getTypeName(left) + " (minus operator requires numeric types)", node);
        }
    }

    /**
     * Evaluates multiplication (numeric only).
     *
     * @param left  the left operand
     * @param right the right operand
     * @param node  the node for error reporting
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
                "Type error: cannot multiply " + getTypeName(left) + " and " + 
                getTypeName(right) + " (times operator requires numeric types)", node);
        }
    }

    /**
     * Evaluates division (numeric only).
     * Result is always a Double. Throws an exception on division by zero.
     *
     * @param left  the left operand (dividend)
     * @param right the right operand (divisor)
     * @param node  the node for error reporting
     * @return the quotient as Double
     * @throws InterpreterException if operands are not numeric or divisor is zero
     */
    private Object evaluateDividedBy(Object left, Object right, ASTNode node) 
            throws InterpreterException {
        if (!(left instanceof Number) || !(right instanceof Number)) {
            throw new InterpreterException(
                "Type error: cannot divide " + getTypeName(left) + " by " + 
                getTypeName(right) + " (divided_by operator requires numeric types)", node);
        }

        double rightValue = ((Number) right).doubleValue();
        if (rightValue == 0.0) {
            throw new InterpreterException(
                "Runtime error: division by zero", node);
        }

        return ((Number) left).doubleValue() / rightValue;
    }

    /**
     * Evaluates modulo (remainder of integer division).
     * Both operands must be integers.
     *
     * @param left  the left operand (dividend)
     * @param right the right operand (divisor)
     * @param node  the node for error reporting
     * @return the remainder as Integer
     * @throws InterpreterException if operands are not integers or divisor is zero
     */
    private Object evaluateModulo(Object left, Object right, ASTNode node) 
            throws InterpreterException {
        if (!(left instanceof Integer) || !(right instanceof Integer)) {
            throw new InterpreterException(
                "Type error: modulo operator requires integer operands, got " + 
                getTypeName(left) + " modulo " + getTypeName(right), node);
        }

        int rightValue = (Integer) right;
        if (rightValue == 0) {
            throw new InterpreterException(
                "Runtime error: modulo by zero", node);
        }

        return (Integer) left % rightValue;
    }

    /**
     * Evaluates exponentiation (raised_to).
     * Both operands are converted to Double for calculation.
     *
     * @param left  the base
     * @param right the exponent
     * @param node  the node for error reporting
     * @return the result as Double
     * @throws InterpreterException if operands are not numeric
     */
    private Object evaluateRaisedTo(Object left, Object right, ASTNode node) 
            throws InterpreterException {
        if (!(left instanceof Number) || !(right instanceof Number)) {
            throw new InterpreterException(
                "Type error: cannot raise " + getTypeName(left) + " to the power of " + 
                getTypeName(right) + " (raised_to operator requires numeric types)", node);
        }

        double base = ((Number) left).doubleValue();
        double exponent = ((Number) right).doubleValue();
        return Math.pow(base, exponent);
    }

    // ========== Logical Operator Evaluation Methods ==========

    /**
     * Evaluates logical AND.
     * Both operands are converted to boolean; result is true only if both are true.
     *
     * @param left  the left operand
     * @param right the right operand
     * @param node  the node for error reporting
     * @return true if both operands are true, false otherwise
     * @throws InterpreterException if operands cannot be converted to boolean
     */
    private Object evaluateLogicalAnd(Object left, Object right, ASTNode node) 
            throws InterpreterException {
        boolean leftBool = toBoolean(left);
        boolean rightBool = toBoolean(right);
        return leftBool && rightBool;
    }

    /**
     * Evaluates logical OR.
     * Both operands are converted to boolean; result is true if either is true.
     *
     * @param left  the left operand
     * @param right the right operand
     * @param node  the node for error reporting
     * @return true if either operand is true, false otherwise
     * @throws InterpreterException if operands cannot be converted to boolean
     */
    private Object evaluateLogicalOr(Object left, Object right, ASTNode node) 
            throws InterpreterException {
        boolean leftBool = toBoolean(left);
        boolean rightBool = toBoolean(right);
        return leftBool || rightBool;
    }

    /**
     * Evaluates exclusive OR (XOR).
     * Both operands are converted to boolean; result is true if exactly one is true.
     *
     * @param left  the left operand
     * @param right the right operand
     * @param node  the node for error reporting
     * @return true if exactly one operand is true, false otherwise
     * @throws InterpreterException if operands cannot be converted to boolean
     */
    private Object evaluateLogicalXor(Object left, Object right, ASTNode node) 
            throws InterpreterException {
        boolean leftBool = toBoolean(left);
        boolean rightBool = toBoolean(right);
        return leftBool ^ rightBool;
    }

    /**
     * Evaluates NAND (NOT AND).
     * Both operands are converted to boolean; result is false only if both are true.
     *
     * @param left  the left operand
     * @param right the right operand
     * @param node  the node for error reporting
     * @return false if both operands are true, true otherwise
     * @throws InterpreterException if operands cannot be converted to boolean
     */
    private Object evaluateLogicalNand(Object left, Object right, ASTNode node) 
            throws InterpreterException {
        boolean leftBool = toBoolean(left);
        boolean rightBool = toBoolean(right);
        return !(leftBool && rightBool);
    }

    /**
     * Evaluates NOR (NOT OR).
     * Both operands are converted to boolean; result is true only if both are false.
     *
     * @param left  the left operand
     * @param right the right operand
     * @param node  the node for error reporting
     * @return true if both operands are false, false otherwise
     * @throws InterpreterException if operands cannot be converted to boolean
     */
    private Object evaluateLogicalNor(Object left, Object right, ASTNode node) 
            throws InterpreterException {
        boolean leftBool = toBoolean(left);
        boolean rightBool = toBoolean(right);
        return !(leftBool || rightBool);
    }

    // ========== Relational Operator Evaluation Methods ==========

    /**
     * Evaluates equality (equal_to).
     * Works for all types; uses .equals() for object comparison.
     *
     * @param left  the left operand
     * @param right the right operand
     * @param node  the node for error reporting
     * @return true if the operands are equal, false otherwise
     * @throws InterpreterException if evaluation fails
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
     * Works for all types; negation of equal_to.
     *
     * @param left  the left operand
     * @param right the right operand
     * @param node  the node for error reporting
     * @return true if the operands are not equal, false otherwise
     * @throws InterpreterException if evaluation fails
     */
    private Object evaluateNotEqual(Object left, Object right, ASTNode node) 
            throws InterpreterException {
        Object result = evaluateEqual(left, right, node);
        return !(Boolean) result;
    }

    /**
     * Evaluates identity check (is).
     * Checks if both operands reference the same object or are numerically/lexically equal.
     *
     * @param left  the left operand
     * @param right the right operand
     * @param node  the node for error reporting
     * @return true if operands are identical, false otherwise
     * @throws InterpreterException if evaluation fails
     */
    private Object evaluateIs(Object left, Object right, ASTNode node) 
            throws InterpreterException {
        // For primitives, check value equality; for objects, check reference equality
        if (left == null && right == null) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        // Use equals() for value comparison
        return left.equals(right);
    }

    /**
     * Evaluates negated identity check (is_not).
     * Negation of is operator.
     *
     * @param left  the left operand
     * @param right the right operand
     * @param node  the node for error reporting
     * @return true if operands are not identical, false otherwise
     * @throws InterpreterException if evaluation fails
     */
    private Object evaluateIsNot(Object left, Object right, ASTNode node) 
            throws InterpreterException {
        Object result = evaluateIs(left, right, node);
        return !(Boolean) result;
    }

    /**
     * Evaluates greater-than comparison (greater_than).
     * Works for Numbers (numeric comparison) and Strings (lexicographical comparison).
     *
     * @param left  the left operand
     * @param right the right operand
     * @param node  the node for error reporting
     * @return true if left > right, false otherwise
     * @throws InterpreterException if operands are incompatible types
     */
    private Object evaluateGreaterThan(Object left, Object right, ASTNode node) 
            throws InterpreterException {
        // Both numeric
        if (left instanceof Number && right instanceof Number) {
            return ((Number) left).doubleValue() > ((Number) right).doubleValue();
        }
        // Both strings
        if (left instanceof String && right instanceof String) {
            return ((String) left).compareTo((String) right) > 0;
        }
        // Type mismatch
        throw new InterpreterException(
            "Type error: cannot compare " + getTypeName(left) + " greater_than " + 
            getTypeName(right) + " (greater_than requires both operands to be numeric or both to be strings)", 
            node);
    }

    /**
     * Evaluates less-than comparison (less_than).
     * Works for Numbers (numeric comparison) and Strings (lexicographical comparison).
     *
     * @param left  the left operand
     * @param right the right operand
     * @param node  the node for error reporting
     * @return true if left < right, false otherwise
     * @throws InterpreterException if operands are incompatible types
     */
    private Object evaluateLessThan(Object left, Object right, ASTNode node) 
            throws InterpreterException {
        // Both numeric
        if (left instanceof Number && right instanceof Number) {
            return ((Number) left).doubleValue() < ((Number) right).doubleValue();
        }
        // Both strings
        if (left instanceof String && right instanceof String) {
            return ((String) left).compareTo((String) right) < 0;
        }
        // Type mismatch
        throw new InterpreterException(
            "Type error: cannot compare " + getTypeName(left) + " less_than " + 
            getTypeName(right) + " (less_than requires both operands to be numeric or both to be strings)", 
            node);
    }

    /**
     * Evaluates greater-than-or-equal comparison (greater_than_or_equal).
     * Works for Numbers (numeric comparison) and Strings (lexicographical comparison).
     *
     * @param left  the left operand
     * @param right the right operand
     * @param node  the node for error reporting
     * @return true if left >= right, false otherwise
     * @throws InterpreterException if operands are incompatible types
     */
    private Object evaluateGreaterThanOrEqual(Object left, Object right, ASTNode node) 
            throws InterpreterException {
        // Both numeric
        if (left instanceof Number && right instanceof Number) {
            return ((Number) left).doubleValue() >= ((Number) right).doubleValue();
        }
        // Both strings
        if (left instanceof String && right instanceof String) {
            return ((String) left).compareTo((String) right) >= 0;
        }
        // Type mismatch
        throw new InterpreterException(
            "Type error: cannot compare " + getTypeName(left) + " greater_than_or_equal " + 
            getTypeName(right) + " (greater_than_or_equal requires both operands to be numeric or both to be strings)", 
            node);
    }

    /**
     * Evaluates less-than-or-equal comparison (less_than_or_equal).
     * Works for Numbers (numeric comparison) and Strings (lexicographical comparison).
     *
     * @param left  the left operand
     * @param right the right operand
     * @param node  the node for error reporting
     * @return true if left <= right, false otherwise
     * @throws InterpreterException if operands are incompatible types
     */
    private Object evaluateLessThanOrEqual(Object left, Object right, ASTNode node) 
            throws InterpreterException {
        // Both numeric
        if (left instanceof Number && right instanceof Number) {
            return ((Number) left).doubleValue() <= ((Number) right).doubleValue();
        }
        // Both strings
        if (left instanceof String && right instanceof String) {
            return ((String) left).compareTo((String) right) <= 0;
        }
        // Type mismatch
        throw new InterpreterException(
            "Type error: cannot compare " + getTypeName(left) + " less_than_or_equal " + 
            getTypeName(right) + " (less_than_or_equal requires both operands to be numeric or both to be strings)", 
            node);
    }

    /**
     * Evaluates a LiteralNode.
     * Determines the literal type from the string value and returns an appropriate Object.
     * Types: Integer, Double, String (quoted), Boolean (true/false).
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

        // Check for string literal (quoted)
        if ((value.startsWith("\"") && value.endsWith("\"")) ||
            (value.startsWith("'") && value.endsWith("'"))) {
            // Remove quotes and return the string content
            return value.substring(1, value.length() - 1);
        }

        // Check for boolean literals
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }

        // Try to parse as Integer
        if (!value.contains(".")) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                // Not an integer, try double below
            }
        }

        // Try to parse as Double
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new InterpreterException(
                "Invalid literal value: \"" + value + "\" (expected number, string, or boolean)", node);
        }
    }

    /**
     * Evaluates an IdentifierNode.
     * Looks up the variable in the symbol table and returns its current value.
     *
     * @param node the IdentifierNode to evaluate
     * @return the variable's current value
     * @throws InterpreterException if the variable doesn't exist
     */
    private Object evaluateIdentifierNode(IdentifierNode node) throws InterpreterException {
        String variableName = node.getName();

        try {
            VariableRecord record = symbolTable.lookupVariable(variableName);
            return record.getValue();
        } catch (IllegalArgumentException e) {
            throw new InterpreterException(e.getMessage(), node);
        }
    }

    /**
     * Evaluates a TerminalNode.
     * Processes terminal symbols (tokens that were directly shifted onto the parse stack).
     * Attempts to interpret the lexeme as a literal value.
     *
     * @param node the TerminalNode to evaluate
     * @return the terminal's value
     * @throws InterpreterException if evaluation fails
     */
    private Object evaluateTerminalNode(TerminalNode node) throws InterpreterException {
        String tokenType = node.getTokenType();
        String lexeme = node.getLexeme();

        // Try to interpret as a literal based on token type
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
                // Remove quotes if present
                if ((lexeme.startsWith("\"") && lexeme.endsWith("\"")) ||
                    (lexeme.startsWith("'") && lexeme.endsWith("'"))) {
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
     * Evaluates a NonTerminalNode.
     * Generic evaluation for non-terminals without specialized classes.
     * Currently routes to evaluate all children and returns the last child's value.
     *
     * @param node the NonTerminalNode to evaluate
     * @return the result of evaluation (value of last child)
     * @throws InterpreterException if evaluation fails
     */
    private Object evaluateNonTerminalNode(NonTerminalNode node) throws InterpreterException {
        // For generic non-terminals, evaluate all children and return the last result
        Object result = null;
        for (ASTNode child : node.children) {
            result = evaluate(child);
        }
        return result;
    }

    // ========== Helper Methods ==========

    /**
     * Converts an Object to a boolean value.
     * Used for conditional evaluation.
     *
     * @param value the value to convert
     * @return the boolean interpretation of the value
     * @throws InterpreterException if the value cannot be converted to boolean
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
     * Used for loop bounds and arithmetic.
     *
     * @param value the value to convert
     * @return the integer value
     * @throws InterpreterException if the value cannot be converted to integer
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
     * Used in error messages.
     *
     * @param value the object
     * @return the type name as a String
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
     * Allows implicit conversions where appropriate (e.g., INTEGER to REAL).
     *
     * @param declaredType the declared type as a String (e.g., "INTEGER", "STRING")
     * @param value        the value to check
     * @return true if the value is compatible with the declared type
     */
    private boolean isTypeCompatible(String declaredType, Object value) {
        if (value == null) {
            return true;  // null is assignable to any type
        }

        switch (declaredType.toUpperCase()) {
            case "INTEGER":
                return value instanceof Integer;
            case "REAL":
                // REAL can accept both Integer and Double
                return value instanceof Integer || value instanceof Double;
            case "STRING":
                return value instanceof String;
            case "BOOLEAN":
                return value instanceof Boolean;
            default:
                return true;  // Unknown types are assumed compatible
        }
    }

    /**
     * Converts a value to a string representation for output.
     * All types are converted with their standard Java string representation.
     *
     * @param value the value to convert
     * @return the string representation
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
     * Converts a string input to the appropriate type based on the declared type.
     * Used when reading user input from System.in.
     *
     * @param inputString  the string input from the user
     * @param declaredType the declared type to convert to
     * @param node         the node for error reporting
     * @return the converted value
     * @throws InterpreterException if conversion fails or type is unsupported
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
                return inputString;
            case "BOOLEAN":
                String trimmed = inputString.trim().toLowerCase();
                if ("true".equals(trimmed) || "1".equals(trimmed) || "yes".equals(trimmed)) {
                    return true;
                } else if ("false".equals(trimmed) || "0".equals(trimmed) || "no".equals(trimmed)) {
                    return false;
                } else {
                    throw new InterpreterException(
                        "Input error: \"" + inputString + "\" cannot be converted to BOOLEAN " +
                        "(valid values: true, false, yes, no, 0, 1)", node);
                }
            default:
                return inputString;  // Default to string for unknown types
        }
    }

    /**
     * Compares two values for equality in case statements.
     * Handles numeric, string, and boolean comparisons.
     *
     * @param value1 the first value
     * @param value2 the second value
     * @return true if the values are equal, false otherwise
     */
    private boolean valuesEqual(Object value1, Object value2) {
        if (value1 == null && value2 == null) {
            return true;
        }
        if (value1 == null || value2 == null) {
            return false;
        }

        // If both are numbers, compare numerically
        if (value1 instanceof Number && value2 instanceof Number) {
            return ((Number) value1).doubleValue() == ((Number) value2).doubleValue();
        }

        // Otherwise, use equals()
        return value1.equals(value2);
    }

    /**
     * Gets the symbol table used by this interpreter.
     *
     * @return the symbol table
     */
    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    /**
     * Closes the input scanner.
     * Should be called when the interpreter is no longer needed to free resources.
     */
    public void close() {
        if (inputScanner != null) {
            inputScanner.close();
        }
    }
}
