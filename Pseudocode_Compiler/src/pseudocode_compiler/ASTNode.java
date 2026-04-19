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

    @Override
    public String toTreeExpression() {
        return "[ProgramNode # " + getChildrenExpression() + "]";
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

    @Override
    public String toTreeExpression() {
        return "[DeclSectionNode # " + getChildrenExpression() + "]";
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

    @Override
    public String toTreeExpression() {
        return "[StmtSectionNode # " + getChildrenExpression() + "]";
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

    @Override
    public String toTreeExpression() {
        return "[AssignmentNode # " + getChildrenExpression() + "]";
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

    @Override
    public String toTreeExpression() {
        return "[IfNode # " + getChildrenExpression() + "]";
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

    @Override
    public String toTreeExpression() {
        return "[WhileLoopNode # " + getChildrenExpression() + "]";
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

    @Override
    public String toTreeExpression() {
        return "[ForLoopNode # " + getChildrenExpression() + "]";
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

    public LiteralNode(String value) {
        this.value = value;
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

    public IdentifierNode(String name) {
        this.name = name;
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

    public TerminalNode(String tokenType, String lexeme) {
        this.tokenType = tokenType;
        this.lexeme = lexeme;
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

    public NonTerminalNode(String lhs, List<? extends ASTNode> nodes) {
        this.lhs = lhs;
        addChildren(nodes);
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
