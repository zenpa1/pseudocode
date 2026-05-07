package pseudocode_compiler;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Symbol table for managing variables during interpretation.
 * Supports nested scoping with a stack of HashMaps representing each scope level.
 * The bottom of the stack is the global scope, and new scopes are pushed when
 * entering blocks (functions, if statements, loops, etc.).
 */
public class SymbolTable {

    // Stack of scopes; each scope is a map of variable names to their records
    private final Stack<Map<String, VariableRecord>> scopeStack;
    
    // Legacy LinkedHashMap for backward compatibility with scanning phase
    private final LinkedHashMap<String, String> identifiers;

    /**
     * Constructs a SymbolTable with a global scope.
     */
    public SymbolTable() {
        this.scopeStack = new Stack<>();
        this.identifiers = new LinkedHashMap<>();
        // Initialize with global scope
        enterScope();
    }

    /**
     * Enters a new scope (e.g., when entering a block or function).
     */
    public void enterScope() {
        scopeStack.push(new HashMap<>());
    }

    /**
     * Exits the current scope (e.g., when exiting a block or function).
     * Throws an exception if attempting to exit the global scope.
     *
     * @throws IllegalStateException if trying to exit the global scope
     */
    public void exitScope() {
        if (scopeStack.size() <= 1) {
            throw new IllegalStateException("Cannot exit the global scope");
        }
        scopeStack.pop();
    }

    /**
     * Declares a variable in the current scope with the given data type.
     * Initializes the variable's value to null.
     *
     * @param name     the variable name
     * @param dataType the variable's data type
     * @throws IllegalArgumentException if the variable is already declared in the current scope
     */
    public void declareVariable(String name, String dataType) {
        declareVariable(name, dataType, null);
    }

    /**
     * Declares a variable in the current scope with the given data type and initial value.
     *
     * @param name     the variable name
     * @param dataType the variable's data type
     * @param value    the initial value
     * @throws IllegalArgumentException if the variable is already declared in the current scope
     */
    public void declareVariable(String name, String dataType, Object value) {
        Map<String, VariableRecord> currentScope = scopeStack.peek();
        
        if (currentScope.containsKey(name)) {
            throw new IllegalArgumentException(
                "Variable '" + name + "' is already declared in the current scope");
        }
        
        currentScope.put(name, new VariableRecord(dataType, value));
    }

    /**
     * Assigns a value to an existing variable.
     * Searches from the current scope down to the global scope.
     *
     * @param name  the variable name
     * @param value the new value
     * @throws IllegalArgumentException if the variable is not found in any scope
     */
    public void assignVariable(String name, Object value) {
        // Search from the top of the stack (current scope) down to the bottom (global)
        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            Map<String, VariableRecord> scope = scopeStack.get(i);
            if (scope.containsKey(name)) {
                scope.get(name).setValue(value);
                return;
            }
        }
        
        throw new IllegalArgumentException("Variable '" + name + "' not found in any scope");
    }

    /**
     * Looks up a variable and returns its VariableRecord.
     * Searches from the current scope down to the global scope.
     *
     * @param name the variable name
     * @return the VariableRecord if found
     * @throws IllegalArgumentException if the variable is not found in any scope
     */
    public VariableRecord lookupVariable(String name) {
        // Search from the top of the stack (current scope) down to the bottom (global)
        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            Map<String, VariableRecord> scope = scopeStack.get(i);
            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }
        
        throw new IllegalArgumentException("Variable '" + name + "' not found in any scope");
    }

    /**
     * Checks if a variable exists in any scope.
     *
     * @param name the variable name
     * @return true if the variable exists, false otherwise
     */
    public boolean variableExists(String name) {
        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            if (scopeStack.get(i).containsKey(name)) {
                return true;
            }
        }
        return false;
    }

    // ========== Legacy methods for backward compatibility with scanning phase ==========

    /**
     * Registers a lexeme as an identifier symbol (legacy method for scanning phase).
     *
     * @param lexeme the lexeme to register
     */
    public void addIdentifier(String lexeme) {
        identifiers.put(lexeme, "TK_ID");
    }

    /**
     * Checks if a lexeme has already been declared as an identifier symbol (legacy method).
     *
     * @param lexeme the lexeme to check
     * @return true if the lexeme is registered, false otherwise
     */
    public boolean containsIdentifier(String lexeme) {
        return identifiers.containsKey(lexeme);
    }

    /**
     * Returns all identifier entries for reporting/debug output (legacy method).
     *
     * @return an iterable of identifier entries
     */
    public Iterable<Map.Entry<String, String>> entries() {
        return identifiers.entrySet();
    }

    /**
     * Gets the current depth of the scope stack.
     *
     * @return the number of scopes currently on the stack
     */
    public int getScopeDepth() {
        return scopeStack.size();
    }
}
