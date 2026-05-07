package pseudocode_compiler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Base class for all AST nodes.
 *
 * <p>
 * Every node stores children in source order so tree visualization and later
 * semantic passes can traverse a predictable structure.
 */
public abstract class ASTNode {

    //Ordered children of this AST node.
    protected final List<ASTNode> children = new ArrayList<>();
    
    //Line number in source file for error reporting.
    protected int lineNumber = -1;

    //Adds one child if non-null.
    protected final void addChild(ASTNode child) {
        if (child != null) {
            children.add(child);
        }
    }

    //Adds multiple children while preserving collection order.
    protected final void addChildren(Collection<? extends ASTNode> nodes) {
        if (nodes == null) {
            return;
        }

        for (ASTNode node : nodes) {
            addChild(node);
        }
    }

    //Common tree-printer used by all subclasses.
    protected void printSelfAndChildren(String prefix, boolean isTail, String label) {
        String branch = isTail ? "`-- " : "|-- ";
        System.out.println(prefix + branch + label);

        String childPrefix = prefix + (isTail ? "    " : "|   ");
        for (int i = 0; i < children.size(); i++) {
            children.get(i).printTree(childPrefix, i == children.size() - 1);
        }
    }

    /**
     * Gets the line number where this node appears in the source file.
     *
     * @return the line number, or -1 if not set
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Sets the line number for this node.
     *
     * @param lineNumber the line number in the source file
     */
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public abstract void printTree(String prefix, boolean isTail);
}

//Root program node.
class ProgramNode extends ASTNode {

    public ProgramNode(DeclSectionNode declarationSection, StmtSectionNode statementSection) {
        addChild(declarationSection);
        addChild(statementSection);
    }

    public ProgramNode(List<? extends ASTNode> nodes) {
        addChildren(nodes);
    }

    @Override
    public void printTree(String prefix, boolean isTail) {
        printSelfAndChildren(prefix, isTail, "ProgramNode");
    }
}

//Container for declarations.
class DeclSectionNode extends ASTNode {

    public DeclSectionNode(List<? extends ASTNode> declarations) {
        addChildren(declarations);
    }

    @Override
    public void printTree(String prefix, boolean isTail) {
        printSelfAndChildren(prefix, isTail, "DeclSectionNode");
    }
}

//Container for executable statements.
class StmtSectionNode extends ASTNode {

    public StmtSectionNode(List<? extends ASTNode> statements) {
        addChildren(statements);
    }

    @Override
    public void printTree(String prefix, boolean isTail) {
        printSelfAndChildren(prefix, isTail, "StmtSectionNode");
    }
}

//Assignment statement node.
class AssignmentNode extends ASTNode {

    public AssignmentNode(IdentifierNode identifier, ASTNode expression) {
        addChild(identifier);
        addChild(expression);
    }

    public AssignmentNode(List<? extends ASTNode> nodes) {
        addChildren(nodes);
    }

    @Override
    public void printTree(String prefix, boolean isTail) {
        printSelfAndChildren(prefix, isTail, "AssignmentNode");
    }
}

//Conditional statement node.
class IfNode extends ASTNode {

    public IfNode(ASTNode condition, StmtSectionNode thenBranch, StmtSectionNode elseBranch) {
        addChild(condition);
        addChild(thenBranch);
        addChild(elseBranch);
    }

    public IfNode(List<? extends ASTNode> nodes) {
        addChildren(nodes);
    }

    @Override
    public void printTree(String prefix, boolean isTail) {
        printSelfAndChildren(prefix, isTail, "IfNode");
    }
}

//While-loop statement node.
class WhileLoopNode extends ASTNode {

    public WhileLoopNode(ASTNode condition, StmtSectionNode body) {
        addChild(condition);
        addChild(body);
    }

    public WhileLoopNode(List<? extends ASTNode> nodes) {
        addChildren(nodes);
    }

    @Override
    public void printTree(String prefix, boolean isTail) {
        printSelfAndChildren(prefix, isTail, "WhileLoopNode");
    }
}

//Repeat-until loop statement node.
class RepeatUntilNode extends ASTNode {

    public RepeatUntilNode(StmtSectionNode body, ASTNode condition) {
        addChild(body);
        addChild(condition);
    }

    public RepeatUntilNode(List<? extends ASTNode> nodes) {
        addChildren(nodes);
    }

    @Override
    public void printTree(String prefix, boolean isTail) {
        printSelfAndChildren(prefix, isTail, "RepeatUntilNode");
    }
}

//For-loop statement node.
class ForLoopNode extends ASTNode {

    public ForLoopNode(IdentifierNode iterator, ASTNode startExpression, ASTNode endExpression, StmtSectionNode body) {
        addChild(iterator);
        addChild(startExpression);
        addChild(endExpression);
        addChild(body);
    }

    public ForLoopNode(List<? extends ASTNode> nodes) {
        addChildren(nodes);
    }

    @Override
    public void printTree(String prefix, boolean isTail) {
        printSelfAndChildren(prefix, isTail, "ForLoopNode");
    }
}

//Binary expression node (operator plus left/right children).
class BinaryExprNode extends ASTNode {

    private final String operator;

    public BinaryExprNode(ASTNode leftExpression, String operator, ASTNode rightExpression) {
        this.operator = operator;
        addChild(leftExpression);
        addChild(rightExpression);
    }

    public String getOperator() {
        return operator;
    }

    @Override
    public void printTree(String prefix, boolean isTail) {
        printSelfAndChildren(prefix, isTail, "BinaryExprNode(" + operator + ")");
    }
}

//Literal leaf node.
class LiteralNode extends ASTNode {

    private final String value;

    public LiteralNode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public void printTree(String prefix, boolean isTail) {
        printSelfAndChildren(prefix, isTail, "LiteralNode(" + value + ")");
    }
}

//Identifier leaf node.
class IdentifierNode extends ASTNode {

    private final String name;

    public IdentifierNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public void printTree(String prefix, boolean isTail) {
        printSelfAndChildren(prefix, isTail, "IdentifierNode(" + name + ")");
    }
}

//Generic terminal leaf used for shifted tokens without dedicated node classes.
class TerminalNode extends ASTNode {

    private final String tokenType;
    private final String lexeme;

    public TerminalNode(String tokenType, String lexeme) {
        this.tokenType = tokenType;
        this.lexeme = lexeme;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getLexeme() {
        return lexeme;
    }

    @Override
    public void printTree(String prefix, boolean isTail) {
        printSelfAndChildren(prefix, isTail, "TerminalNode(" + tokenType + ", " + lexeme + ")");
    }
}

//Generic non-terminal node used when there is no specialized concrete class.
class NonTerminalNode extends ASTNode {

    private final String lhs;

    public NonTerminalNode(String lhs, List<? extends ASTNode> nodes) {
        this.lhs = lhs;
        addChildren(nodes);
    }

    public String getLhs() {
        return lhs;
    }

    @Override
    public void printTree(String prefix, boolean isTail) {
        printSelfAndChildren(prefix, isTail, "NonTerminalNode(" + lhs + ")");
    }
}

//Say statement node (output/print statement).
class SayNode extends ASTNode {

    public SayNode(ASTNode expression) {
        addChild(expression);
    }

    public SayNode(List<? extends ASTNode> expressions) {
        addChildren(expressions);
    }

    @Override
    public void printTree(String prefix, boolean isTail) {
        printSelfAndChildren(prefix, isTail, "SayNode");
    }
}

//Read statement node (input/read statement).
class ReadNode extends ASTNode {

    private final String variableName;

    public ReadNode(String variableName) {
        this.variableName = variableName;
    }

    public ReadNode(IdentifierNode identifier) {
        this.variableName = identifier.getName();
    }

    public String getVariableName() {
        return variableName;
    }

    @Override
    public void printTree(String prefix, boolean isTail) {
        printSelfAndChildren(prefix, isTail, "ReadNode(" + variableName + ")");
    }
}

//Break statement node.
class BreakNode extends ASTNode {

    @Override
    public void printTree(String prefix, boolean isTail) {
        printSelfAndChildren(prefix, isTail, "BreakNode");
    }
}

//Continue statement node.
class ContinueNode extends ASTNode {

    @Override
    public void printTree(String prefix, boolean isTail) {
        printSelfAndChildren(prefix, isTail, "ContinueNode");
    }
}

//Consider (switch) statement node.
class ConsiderNode extends ASTNode {

    public ConsiderNode(ASTNode expression, List<? extends ASTNode> cases) {
        addChild(expression);
        addChildren(cases);
    }

    public ConsiderNode(List<? extends ASTNode> nodes) {
        addChildren(nodes);
    }

    @Override
    public void printTree(String prefix, boolean isTail) {
        printSelfAndChildren(prefix, isTail, "ConsiderNode");
    }
}

//Case statement node (part of consider block).
class CaseNode extends ASTNode {

    private final Object caseValue;  // The value to match (null for otherwise)

    public CaseNode(Object caseValue, StmtSectionNode body) {
        this.caseValue = caseValue;
        addChild(body);
    }

    public CaseNode(List<? extends ASTNode> nodes) {
        this.caseValue = null;
        addChildren(nodes);
    }

    public Object getCaseValue() {
        return caseValue;
    }

    @Override
    public void printTree(String prefix, boolean isTail) {
        String label = caseValue == null ? "CaseNode(otherwise)" : "CaseNode(" + caseValue + ")";
        printSelfAndChildren(prefix, isTail, label);
    }
}

//Scope block statement node.
class ScopeBlockNode extends ASTNode {

    public ScopeBlockNode(StmtSectionNode body) {
        addChild(body);
    }

    public ScopeBlockNode(List<? extends ASTNode> statements) {
        addChildren(statements);
    }

    @Override
    public void printTree(String prefix, boolean isTail) {
        printSelfAndChildren(prefix, isTail, "ScopeBlockNode");
    }
}