package pseudocode_compiler;

import java.util.HashMap;
import java.util.Map;

public enum GrammarRule {
    //Core program/declaration/statement skeleton.
    PROGRAM("1", "program", 5),
    DECL_SECTION("2", "decl_section", 2),
    DECL_LIST_RECURSIVE("3a", "decl_list", 2),
    DECL_LIST_EMPTY("3b", "decl_list", 0),
    DECLARATION_TYPE_ID_LIST("4a", "declaration", 2),
    DECLARATION_CONSTANT("4b", "declaration", 5),
    DECLARATION_DEFINE("4c", "declaration", 4),
    STMT_SECTION("5", "stmt_section", 2),
    STMT_LIST_RECURSIVE("6a", "stmt_list", 2),
    STMT_LIST_SINGLE("6b", "stmt_list", 1),
    STATEMENT_ACTION("7", "statement", 1),

    //Action statements.
    ACTION_ASSIGNMENT("8a", "action_stmt", 1),
    ACTION_IO("8b", "action_stmt", 1),
    ACTION_CONDITIONAL("8c", "action_stmt", 1),
    ACTION_LOOP("8d", "action_stmt", 1),
    ACTION_SWITCH("8e", "action_stmt", 1),
    ACTION_SCOPE("8f", "action_stmt", 1),
    ACTION_LOOP_CONTROL("8g", "action_stmt", 1),
    SCOPE_STATEMENT("8.5", "scope_statement", 4),
    ASSIGNMENT_IDENTIFIER("9a", "assignment", 5),
    ASSIGNMENT_ITEM_OF("9b", "assignment", 7),
    OPT_AS_TYPE("10a", "opt_as_type", 2),
    OPT_AS_TYPE_EMPTY("10b", "opt_as_type", 0),
    IO_SAY("11a", "io_stmt", 2),
    IO_READ("11b", "io_stmt", 2),
    EXPRESSION_LIST_SINGLE("12a", "expression_list", 1),
    EXPRESSION_LIST_RECURSIVE("12b", "expression_list", 3),
    CONDITIONAL_IF("13", "conditional", 5),
    CONDITIONAL_IF_TAIL_ELIF("13.5a", "if_tail", 1),
    CONDITIONAL_IF_TAIL_ELSE("13.5b", "if_tail", 1),
    CONDITIONAL_IF_TAIL_DONE("13.5c", "if_tail", 1),
    ELSE_IF_CLAUSE("14", "else_if_clause", 5),
    ELSE_CLAUSE("15", "else_clause", 3),

    //Loops/switch.
    LOOP_WHILE("16a", "loop", 1),
    LOOP_FOR("16b", "loop", 1),
    LOOP_REPEAT("16c", "loop", 1),
    WHILE_LOOP("17", "while_loop", 5),
    FOR_LOOP("18", "for_loop", 7),
    REPEAT_LOOP("19", "repeat_loop", 4),
    RANGE_SPEC_RANGE("20a", "range_spec", 4),
    RANGE_SPEC_IDENTIFIER("20b", "range_spec", 1),
    LOOP_CONTROL_BREAK("21a", "loop_control", 1),
    LOOP_CONTROL_CONTINUE("21b", "loop_control", 1),
    SWITCH_STMT("22", "switch", 5),
    CASE_LIST_RECURSIVE("23a", "case_list", 5),
    CASE_LIST_EMPTY("23b", "case_list", 0),
    OPT_OTHERWISE_PRESENT("24a", "opt_otherwise", 2),
    OPT_OTHERWISE_EMPTY("24b", "opt_otherwise", 0),

    //Expression tower.
    EXPRESSION_ROOT("25", "expression", 1),
    LOGIC_OR_RECURSIVE("26a", "logic_or_expr", 3),
    LOGIC_OR_BASE("26b", "logic_or_expr", 1),
    LOGIC_AND_RECURSIVE("27a", "logic_and_expr", 3),
    LOGIC_AND_BASE("27b", "logic_and_expr", 1),
    LOGIC_XOR_RECURSIVE("28a", "logic_xor_expr", 3),
    LOGIC_XOR_BASE("28b", "logic_xor_expr", 1),
    LOGIC_NAND_RECURSIVE("29a", "logic_nand_expr", 3),
    LOGIC_NAND_BASE("29b", "logic_nand_expr", 1),
    LOGIC_NOR_RECURSIVE("30a", "logic_nor_expr", 3),
    LOGIC_NOR_BASE("30b", "logic_nor_expr", 1),
    LOGIC_NOT_PREFIX("31a", "logic_not_expr", 2),
    LOGIC_NOT_BASE("31b", "logic_not_expr", 1),
    RELATIONAL_BINARY("32a", "relational", 3),
    RELATIONAL_SINGLE("32b", "relational", 1),

    //Relational/arithmetic operators.
    REL_OP_IS("33a", "rel_op", 1),
    REL_OP_IS_NOT("33b", "rel_op", 1),
    REL_OP_EQUAL("33c", "rel_op", 1),
    REL_OP_NOT_EQUAL("33d", "rel_op", 1),
    REL_OP_GT("33e", "rel_op", 1),
    REL_OP_LT("33f", "rel_op", 1),
    REL_OP_GTE("33g", "rel_op", 1),
    REL_OP_LTE("33h", "rel_op", 1),
    ARITH_RECURSIVE("34a", "arithmetic_expr", 3),
    ARITH_BASE("34b", "arithmetic_expr", 1),
    ADD_OP_PLUS("35a", "add_op", 1),
    ADD_OP_MINUS("35b", "add_op", 1),
    TERM_RECURSIVE("36a", "term", 3),
    TERM_BASE("36b", "term", 1),
    MULT_OP_TIMES("37a", "mult_op", 1),
    MULT_OP_DIV("37b", "mult_op", 1),
    MULT_OP_MOD("37c", "mult_op", 1),
    POWER_RIGHT_ASSOC("38a", "power", 3),
    POWER_BASE("38b", "power", 1),

    //Primary/literals and helpers.
    PRIMARY_GROUPED("39a", "primary", 3),
    PRIMARY_IDENTIFIER("39b", "primary", 1),
    PRIMARY_LITERAL("39c", "primary", 1),
    PRIMARY_ITEM_ACCESS("39d", "primary", 4),
    LIST_LITERAL("40", "list_literal", 3),
    LIST_ELEMENTS_NON_EMPTY("41a", "list_elements", 2),
    LIST_ELEMENTS_EMPTY("41b", "list_elements", 0),
    LIST_TAIL_RECURSIVE("42a", "list_tail", 3),
    LIST_TAIL_EMPTY("42b", "list_tail", 0),
    LITERAL_LIST("43a", "literal", 1),
    LITERAL_NUMBER("43b", "literal", 1),
    LITERAL_STRING("43c", "literal", 1),
    LITERAL_BOOLEAN("43d", "literal", 1),
    LITERAL_SPECIAL("43e", "literal", 1),
    SPECIAL_LITERAL_LENGTH("44a", "special_literal", 1),
    SPECIAL_LITERAL_FIND("44b", "special_literal", 1),
    SPECIAL_LITERAL_STRING_OP("44c", "special_literal", 1),
    LENGTH_OP("45", "length_op", 2),
    FIND_OP("46", "find_op", 4),
    STRING_OP("47", "string_op", 4),
    SPECIAL_STRING_LITERAL("48a", "special_string", 1),
    SPECIAL_STRING_IDENTIFIER("48b", "special_string", 1),
    SPECIAL_LIST_IDENTIFIER("49a", "special_list", 1),
    SPECIAL_LIST_LITERAL("49b", "special_list", 1),
    ID_LIST_SINGLE("50a", "id_list", 1),
    ID_LIST_RECURSIVE("50b", "id_list", 3),
    IDENTIFIER_LETTER_TAIL("51", "identifier", 2),
    ID_TAIL_LETTER("52a", "id_tail", 2),
    ID_TAIL_DIGIT("52b", "id_tail", 2),
    ID_TAIL_UNDERSCORE("52c", "id_tail", 2),
    ID_TAIL_EMPTY("52d", "id_tail", 0),
    NUM_LITERAL_INTEGER("53a", "num_literal", 1),
    NUM_LITERAL_DOUBLE("53b", "num_literal", 1),
    DOUBLE_LITERAL("54", "double_literal", 3),
    INTEGER_LITERAL_POSITIVE("55a", "integer_literal", 1),
    INTEGER_LITERAL_NEGATIVE("55b", "integer_literal", 2),
    DIGIT_SEQ_RECURSIVE("56a", "digit_seq", 2),
    DIGIT_SEQ_SINGLE("56b", "digit_seq", 1),
    STRING_LITERAL("57", "string_literal", 3),
    STRING_CONTENT_RECURSIVE("58a", "string_content", 2),
    STRING_CONTENT_EMPTY("58b", "string_content", 0),
    STRING_ELEMENT_CHAR("59a", "string_element", 1),
    STRING_ELEMENT_ESCAPE("59b", "string_element", 1),
    ESCAPE_NEWLINE("60a", "escape_seq", 1),
    ESCAPE_TAB("60b", "escape_seq", 1),
    ESCAPE_BACKSLASH("60c", "escape_seq", 1),
    ESCAPE_QUOTE("60d", "escape_seq", 1),
    STRING_CHAR("61", "string_char", 1),
    BOOLEAN_TRUE("62a", "boolean_literal", 1),
    BOOLEAN_FALSE("62b", "boolean_literal", 1),
    TYPE_INTEGER("63a", "type", 1),
    TYPE_DOUBLE("63b", "type", 1),
    TYPE_STRING("63c", "type", 1),
    TYPE_BOOLEAN("63d", "type", 1),
    TYPE_LIST("63e", "type", 1),
    LETTER("64", "letter", 1),
    DIGIT("65", "digit", 1);

    private static final Map<String, GrammarRule> BY_RULE_ID = new HashMap<>();
    private static final Map<String, GrammarRule> RULE_ID_ALIASES = new HashMap<>();

    static {
        for (GrammarRule rule : values()) {
            BY_RULE_ID.put(rule.ruleId, rule);
        }

        //Aliases required by the generated parsing_table.csv reduce IDs.
        RULE_ID_ALIASES.put("4", DECLARATION_TYPE_ID_LIST);
        RULE_ID_ALIASES.put("60a", ID_LIST_SINGLE);
    }

    private final String ruleId;
    private final String lhs;
    private final int popCount;

    GrammarRule(String ruleId, String lhs, int popCount) {
        this.ruleId = ruleId;
        this.lhs = lhs;
        this.popCount = popCount;
    }

    public String getRuleId() {
        return ruleId;
    }

    public String getLhs() {
        return lhs;
    }

    public int getPopCount() {
        return popCount;
    }

    public static GrammarRule fromRuleId(String ruleId) {
        GrammarRule rule = RULE_ID_ALIASES.get(ruleId);
        if (rule == null) {
            rule = BY_RULE_ID.get(ruleId);
        }
        if (rule == null) {
            throw new RuntimeException("No GrammarRule mapping found for reduce target " + ruleId);
        }
        return rule;
    }
}