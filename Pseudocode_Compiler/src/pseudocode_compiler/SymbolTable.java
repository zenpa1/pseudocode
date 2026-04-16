package pseudocode_compiler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Symbol table for user-defined identifiers discovered during scanning.
 */
class SymbolTable {

    //LinkedHashMap preserves insertion order when printing the table.
    private final LinkedHashMap<String, String> identifiers = new LinkedHashMap<>();

    //Registers a lexeme as an identifier symbol.
    public void addIdentifier(String lexeme) {
        identifiers.put(lexeme, "TK_ID");
    }

    //Checks if a lexeme has already been declared as an identifier symbol.
    public boolean containsIdentifier(String lexeme) {
        return identifiers.containsKey(lexeme);
    }

    //Returns all identifier entries for reporting/debug output.
    public Iterable<Map.Entry<String, String>> entries() {
        return identifiers.entrySet();
    }
}
