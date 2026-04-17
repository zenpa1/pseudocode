package pseudocode_compiler;

import java.util.HashMap;

/**
 * Reserved-token lookup table.
 *
 * <p>
 * Maps fixed pseudocode lexemes to their token kinds. This is separate from
 * {@link SymbolTable}, which stores only user-defined identifiers.
 */
class TokenHashMap {

    // Stores lexeme-to-token mappings for all reserved/static tokens.
    private final HashMap<String, String> table;

    public TokenHashMap() {
        table = new HashMap<>();
        initialize();
    }

    // Populates the hashmap with all predefined language tokens.
    private void initialize() {
        table.put("Program", "TK_PROG");
        table.put("End.", "TK_END");
        table.put("Declaration_Section", "TK_DECSEC");
        table.put("Statement_Section", "TK_STATESEC");
        table.put("scope", "TK_SCOPE");
        table.put("define", "TK_DEFINE");
        table.put("block", "TK_BLOCK");
        table.put("set", "TK_SET");
        table.put("to", "TK_TO");
        table.put("constant", "TK_CONST");
        table.put("is", "TK_IS");
        table.put("as", "TK_AS");
        table.put("integer", "TK_INT");
        table.put("double", "TK_DOUBLE");
        table.put("string", "TK_STRING");
        table.put("list", "TK_LIST");
        table.put("boolean", "TK_BOOL");
        table.put("array", "TK_ARRAY");
        table.put("say", "TK_SAY");
        table.put("read", "TK_READ");
        table.put("plus", "TK_PLUS");
        table.put("minus", "TK_MINUS");
        table.put("times", "TK_TIMES");
        table.put("divided_by", "TK_DIV");
        table.put("modulo", "TK_MOD");
        table.put("raised_to", "TK_EXP");
        table.put("is_not", "TK_ISNOT");
        table.put("equal_to", "TK_EQTO");
        table.put("not_equal_to", "TK_NOTEQTO");
        table.put("greater_than", "TK_GREATERTHAN");
        table.put("less_than", "TK_LESSTHAN");
        table.put("greater_than_or_equal_to", "TK_GREATERTHANOREQTO");
        table.put("less_than_or_equal_to", "TK_LESSTHANOREQTO");
        table.put("not", "TK_NOT");
        table.put("and", "TK_AND");
        table.put("or", "TK_OR");
        table.put("xor", "TK_XOR");
        table.put("nand", "TK_NAND");
        table.put("nor", "TK_NOR");
        table.put("if", "TK_IF");
        table.put("then", "TK_THEN");
        table.put("else", "TK_ELSE");
        table.put("else_if", "TK_ELSEIF");
        table.put("done", "TK_DONE");
        table.put("while", "TK_WHILE");
        table.put("do", "TK_DO");
        table.put("repeat", "TK_REPEAT");
        table.put("until", "TK_UNTIL");
        table.put("for_every", "TK_FOREVERY");
        table.put("in", "TK_IN");
        table.put("range", "TK_RANGE");
        table.put("break", "TK_BREAK");
        table.put("continue", "TK_CONTINUE");
        table.put("consider", "TK_CONSIDER");
        table.put("case", "TK_CASE");
        table.put("otherwise", "TK_OTHERWISE");
        table.put("true", "TK_TRUE");
        table.put("false", "TK_FALSE");
        table.put("the", "NT_THE");
        table.put("please", "NT_PLEASE");
        table.put("join", "TK_JOIN");
        table.put("with", "TK_WITH");
        table.put("length_of", "TK_LENGTHOF");
        table.put("find", "TK_FIND");
        table.put("item", "TK_ITEM");
        table.put("of", "TK_OF");
        table.put("--", "TK_COMMENT_SINGLE");
        table.put("---", "TK_COMMENT_MULTI");
    }

    // Returns token type for a lexeme, or null if the lexeme is not reserved.
    public String lookup(String lexeme) {
        if (table.containsKey(lexeme)) {
            return table.get(lexeme);
        }
        return null;
    }

    // Checks whether a lexeme exists in the predefined token hashmap.
    public boolean contains(String lexeme) {
        return table.containsKey(lexeme);
    }
}
