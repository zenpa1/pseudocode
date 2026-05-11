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
    
    public abstract String toTreeExpression();
    
    protected String getChildrenExpression() {
        if (children.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < children.size(); i++) {
            sb.append(children.get(i).toTreeExpression());
            if (i < children.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}

//Root program node.
class ProgramNode extends ASTNode {

    public ProgramNode(DeclSectionNode declarationSection, StmtSectionNode statementSection, int lineNumber) {
        addChild(declarationSection);
        addChild(statementSection);
        this.lineNumber = lineNumber;
    }

    public ProgramNode(List<? extends ASTNode> nodes, int lineNumber) {
        addChildren(nodes);
        this.lineNumber = lineNumber;
    }

    @Override
    public void printTree(String prefix, boolean isTail) {
        printSelfAndChildren(prefix, isTail, "ProgramNode");
    }
    
    @Override
    public String toTreeExpression() {
        return "[ProgramNode # " + getChildrenExpression() + "]";
    }
}

//Container for declarations.
class DeclSectionNode extends ASTNode {

    public DeclSectionNode(List<? extends ASTNode> declarations, int lineNumber) {
        addChildren(declarations);
        this.lineNumber = lineNumber;
    }

    @Override
    public void printTree(String prefix, boolean isTail) {
        printSelfAndChildren(prefix, isTail, "DeclSectionNode");
    }
    
    @Override
    public String toTreeExpression() {
        return "[DeclSectionNode # " + getChildrenExpression() + "]";
    }
}

//Container for executable statements.
class StmtSectionNode extends ASTNode {

    public StmtSectionNode(List<? extends ASTNode> statements, int lineNumber) {
        addChildren(statements);
        this.lineNumber = lineNumber;
    }

    @Override
    public void printTree(String prefix, boolean isTail) {
        printSelfAndChildren(prefix, isTail, "StmtSectionNode");
    }
    
    @Override
    public String toTreeExpression() {
        return "[StmtSectionNode # " + getChildrenExpression() + "]";
    }
}

//Assignment statement node.
class AssignmentNode extends ASTNode {

    public AssignmentNode(IdentifierNode identifier, ASTNode expression, int lineNumber) {
        addChild(identifier);
        addChild(expression);
        this.lineNumber = lineNumber;
    }

    public AssignmentNode(List<? extends ASTNode> nodes, int lineNumber) {
        addChildren(nodes);
        this.lineNumber = lineNumber;
    }

    @Override
    public void printTree(String prefix, boolean isTail) {
        printSelfAndChildren(prefix, isTail, "AssignmentNode");
    }
    
    @Override
    public String toTreeExpression() {
        return "[AssignmentNode # " + getChildrenExpression() + "]";
    }
}

//Conditional statement node.
class IfNode extends ASTNode {

    public IfNode(ASTNode condition, StmtSectionNode thenBranch, StmtSectionNode elseBranch, int lineNumber) {
        addChild(condition);
        addChild(thenBranch);
        addChild(elseBranch);
        this.lineNumber = lineNumber;
    }

    public IfNode(List<? extends ASTNode> nodes, int lineNumber) {
        addChildren(nodes);
        this.lineNumber = lineNumber;
    }

    @Override
    public void printTree(String prefix, boolean isTail) {
        printSelfAndChildren(prefix, isTail, "IfNode");
    }
    
    @Override
    public String toTreeExpression() {
        return "[IfNode # " + getChildrenExpression() + "]";
    }
}

//While-loop statement node.
class WhileLoopNode extends ASTNode {

    public WhileLoopNode(ASTNode condition, StmtSectionNode body, int lineNumber) {
        addChild(condition);
        addChild(body);
        this.lineNumber = lineNumber;
    }

    public WhileLoopNode(List<? extends ASTNode> nodes, int lineNumber) {
        addChildren(nodes);
        this.lineNumber = lineNumber;
    }

    @Override
    public void printTree(String prefix, boolean isTail) {
        printSelfAndChildren(prefix, isTail, "WhileLoopNode");
    }
    
    @Override
    public String toTreeExpression() {
        return "[WhileLoopNode # " + getChildrenExpression() + "]";
    }
}

//Repeat-until loop statement node.
class RepeatUntilNode extends ASTNode {

    public RepeatUntilNode(StmtSectionNode body, ASTNode condition, int lineNumber) {
        addChild(body);
        addChild(condition);
        this.lineNumber = lineNumber;
    }

    public RepeatUntilNode(List<? extends ASTNode> nodes, int lineNumber) {
        addChildren(nodes);
        this.lineNumber = lineNumber;
    }

    @Override
    public void printTree(String prefix, boolean isTail) {
        printSelfAndChildren(prefix, isTail, "RepeatUntilNode");
    }
    
    @Override
    public String toTreeExpression() {
        return "[RepeatUntilNode # " + getChildrenExpression() + "]";
    }
}

//For-loop statement node.
class ForLoopNode extends ASTNode {

    public ForLoopNode(IdentifierNode iterator, ASTNode startExpression, ASTNode endExpression, StmtSectionNode body, int lineNumber) {
        addChild(iterator);
        addChild(startExpression);
        addChild(endExpression);
        addChild(body);
        this.lineNumber = lineNumber;
    }

    public ForLoopNode(List<? extends ASTNode> nodes, int lineNumber) {
        addChildren(nodes);
        this.lineNumber = lineNumber;
    }

    @Override
    public void printTree(String prefix, boolean isTail) {
        printSelfAndChildren(prefix, isTail, "ForLoopNode");
    }
    
    @Override
    public String toTreeExpression() {
        return "[ForLoopNode # " + getChildrenExpression() + "]";
    }
}

//Binary expression node (operator plus left/right children).
class BinaryExprNode extends ASTNode {

    private final String operator;

    public BinaryExprNode(ASTNode leftExpression, String operator, ASTNode rightExpression, int lineNumber) {
        this.operator = operator;
        addChild(leftExpression);
        addChild(rightExpression);
        this.lineNumber = lineNumber;
    }

    public String getOperator() {
        return operator;
    }

    @Override
    public void printTree(String prefix, boolean isTail) {
        printSelfAndChildren(prefix, isTail, "BinaryExprNode(" + operator + ")");
    }
    
    @Override
    public String toTreeExpression() {
        return "[BinaryExprNode(" + operator + ") # " + getChildrenExpression() + "]";
    }
}

//Literal leaf node.
class LiteralNode extends ASTNode {

    private final String value;

    public LiteralNode(String value, int lineNumber) {
        this.value = value;
        this.lineNumber = lineNumber;
    }

    public String getValue() {
        return value;
    }

    @Override
    public void printTree(String prefix, boolean isTail) {
        printSelfAndChildren(prefix, isTail, "LiteralNode(" + value + ")");
    }
    
    @Override
    public String toTreeExpression() {
        // Leaf nodes still need the '#' and empty '[]' for the grtree parser
        return "[LiteralNode(" + value + ") # []]"; 
    }
}

//Identifier leaf node.
class IdentifierNode extends ASTNode {

    private final String name;

    public IdentifierNode(String name, int lineNumber) {
        this.name = name;
        this.lineNumber = lineNumber;
    }

    public String getName() {
        return name;
    }

    @Override
    public void printTree(String prefix, boolean isTail) {
        printSelfAndChildren(prefix, isTail, "IdentifierNode(" + name + ")");
    }
    
    @Override
    public String toTreeExpression() {
        // Leaf nodes still need the '#' and empty '[]' for the grtree parser
        return "[IdentifierNode(" + name + ") # []]"; 
    }
}

//Generic terminal leaf used for shifted tokens without dedicated node classes.
class TerminalNode extends ASTNode {

    private final String tokenType;
    private final String lexeme;

    public TerminalNode(String tokenType, String lexeme, int lineNumber) {
        this.tokenType = tokenType;
        this.lexeme = lexeme;
        this.lineNumber = lineNumber;
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
    
    @Override
    public String toTreeExpression() {
        // Leaf nodes still need the '#' and empty '[]' for the grtree parser
        if(lexeme.equals("[") || lexeme.equals("]")) 
            return "[TerminalNode(" + tokenType + ") # []]"; 
        
        return "[TerminalNode(" + tokenType + ", " + lexeme + ") # []]"; 
    }
}

//Generic non-terminal node used when there is no specialized concrete class.
class NonTerminalNode extends ASTNode {

    private final String lhs;

    public NonTerminalNode(String lhs, List<? extends ASTNode> nodes, int lineNumber) {
        this.lhs = lhs;
        addChildren(nodes);
        this.lineNumber = lineNumber;
    }

    public String getLhs() {
        return lhs;
    }

    @Override
    public void printTree(String prefix, boolean isTail) {
        printSelfAndChildren(prefix, isTail, "NonTerminalNode(" + lhs + ")");
    }
    
    @Override
    public String toTreeExpression() {
        // Leaf nodes still need the '#' and empty '[]' for the grtree parser
        return "[NonTerminalNode(" + lhs + ") # +" + getChildrenExpression() + "]"; 
    }
}

//Say statement node (output/print statement).
class SayNode extends ASTNode {

    public SayNode(ASTNode expression, int lineNumber) {
        addChild(expression);
        this.lineNumber = lineNumber;
    }

    public SayNode(List<? extends ASTNode> expressions, int lineNumber) {
        addChildren(expressions);
        this.lineNumber = lineNumber;
    }

    @Override
    public void printTree(String prefix, boolean isTail) {
        printSelfAndChildren(prefix, isTail, "SayNode");
    }
    
    @Override
    public String toTreeExpression() {
        return "[SayNode # " + getChildrenExpression() + "]";
    }
}

//Read statement node (input/read statement).
class ReadNode extends ASTNode {

    private final String variableName;

    public ReadNode(String variableName, int lineNumber) {
        this.variableName = variableName;
        this.lineNumber = lineNumber;
    }

    public ReadNode(IdentifierNode identifier, int lineNumber) {
        this.variableName = identifier.getName();
        this.lineNumber = lineNumber;
    }

    public String getVariableName() {
        return variableName;
    }

    @Override
    public void printTree(String prefix, boolean isTail) {
        printSelfAndChildren(prefix, isTail, "ReadNode(" + variableName + ")");
    }
    
    @Override
    public String toTreeExpression() {
        return "[ReadNode # " + getChildrenExpression() + "]";
    }
}

//Break statement node.
class BreakNode extends ASTNode {

    @Override
    public void printTree(String prefix, boolean isTail) {
        printSelfAndChildren(prefix, isTail, "BreakNode");
    }
    
    @Override
    public String toTreeExpression() {
        return "[BreakNode # " + getChildrenExpression() + "]";
    }
}

//Continue statement node.
class ContinueNode extends ASTNode {

    @Override
    public void printTree(String prefix, boolean isTail) {
        printSelfAndChildren(prefix, isTail, "ContinueNode");
    }
    
    @Override
    public String toTreeExpression() {
        return "[ContinueNode # " + getChildrenExpression() + "]";
    }
}

//Consider (switch) statement node.
class ConsiderNode extends ASTNode {

    public ConsiderNode(ASTNode expression, List<? extends ASTNode> cases, int lineNumber) {
        addChild(expression);
        addChildren(cases);
        this.lineNumber = lineNumber;
    }

    public ConsiderNode(List<? extends ASTNode> nodes, int lineNumber) {
        addChildren(nodes);
        this.lineNumber = lineNumber;
    }

    @Override
    public void printTree(String prefix, boolean isTail) {
        printSelfAndChildren(prefix, isTail, "ConsiderNode");
    }
    
    @Override
    public String toTreeExpression() {
        return "[ConsiderNode # " + getChildrenExpression() + "]";
    }
}

//Case statement node (part of consider block).
class CaseNode extends ASTNode {

    private final Object caseValue;  // The value to match (null for otherwise)

    public CaseNode(Object caseValue, StmtSectionNode body, int lineNumber) {
        this.caseValue = caseValue;
        addChild(body);
        this.lineNumber = lineNumber;
    }

    public CaseNode(List<? extends ASTNode> nodes, int lineNumber) {
        this.caseValue = null;
        addChildren(nodes);
        this.lineNumber = lineNumber;
    }

    public Object getCaseValue() {
        return caseValue;
    }

    @Override
    public void printTree(String prefix, boolean isTail) {
        String label = caseValue == null ? "CaseNode(otherwise)" : "CaseNode(" + caseValue + ")";
        printSelfAndChildren(prefix, isTail, label);
    }
    
    @Override
    public String toTreeExpression() {
        return "[CaseNode # " + getChildrenExpression() + "]";
    }
}

//Scope block statement node.
class ScopeBlockNode extends ASTNode {

    public ScopeBlockNode(StmtSectionNode body, int lineNumber) {
        addChild(body);
        this.lineNumber = lineNumber;
    }

    public ScopeBlockNode(List<? extends ASTNode> statements, int lineNumber) {
        addChildren(statements);
        this.lineNumber = lineNumber;
    }

    @Override
    public void printTree(String prefix, boolean isTail) {
        printSelfAndChildren(prefix, isTail, "ScopeBlockNode");
    }
    
    @Override
    public String toTreeExpression() {
        return "[ScopeBlockNode # " + getChildrenExpression() + "]";
    }
}