# Pseudocode Language Specification (CS-ELEC2A)

## 1. Program Anatomy

Every program must follow a strict sequential structure:

- **Header**: Begins with `Program <identifier>`.
- **Declaration Section**: Marked by `Declaration_Section`. All variables and constants must be explicitly declared here before use.
- **Statement Section**: Marked by `Statement_Section`. Contains all executable logic.
- **Footer**: Terminated physically with `End.` (note the period).

## 2. Character Set & Syntax Rules

- **Case-Sensitivity**: The language is strictly case-sensitive. Keywords are entirely lowercase (e.g., `say`). Casing variations (e.g., `Say`) are treated as identifiers.
- **Whitespaces**: Spaces, tabs, and newlines act as the primary delimiters. Tokens can span multiple lines.
- **Separators**: The language does **not** use semicolons or periods as statement separators.
- **Comments**:
  - Single-line: `--` ignores all characters until a newline.
  - Multi-line (Block): `---` opens the block and another `---` closes it.
- **Noise Tokens**: Optional keywords used purely for human readability: `the`, `a`, `an`, `please`.

## 3. Data Types & Literals

The language supports four main data types:

- `integer`: Whole numbers (e.g., `42`).
- `double`: Floating-point numbers (e.g., `3.14`). Unified under a 64-bit IEEE 754 standard behind the scenes.
- `boolean`: `true` or `false`.
- `string`: Characters enclosed in double quotes (e.g., `"Hello"`).
- `list`: Heterogeneous collections enclosed in brackets (e.g., `["Alice", 25]`).

## 4. Identifiers & Variables

- **Naming Rules**: Must begin with a letter (A-Z, a-z). Subsequent characters can be letters, digits (0-9), or underscores (\_).
- **Assignments**: Use the `set ... to` keywords (e.g., `set count to 5`).
- **Constants**: Declared with the `constant` keyword and assigned using `is` (e.g., `constant double PI is 3.14`). Cannot be reassigned.
- **Type Aliasing**: Types can be aliased using `define <NewType> as <BaseType>`.
- **Type Casting**: Explicit conversions use the `as` keyword (e.g., `set myString to 50 as string`).

## 5. Operators & Operator Precedence

Standard mathematical precedence is followed, overriding with parentheses `()`.

1.  **Grouping**: `()`
2.  **Access**: `of`, `length_of`, `item` (Right-to-Left)
3.  **Exponentiation**: `raised_to` (Right-to-Left)
4.  **Multiplicative**: `times`, `divided_by`, `modulo` (Left-to-Right)
5.  **Additive/String**: `plus`, `minus`, `join`, `with` (Left-to-Right)
6.  **Relational**: `greater_than`, `less_than`, `equal_to`, `not_equal_to`, `greater_than_or_equal_to`, `less_than_or_equal_to`, `is`, `is_not`
7.  **Logical NOT**: `not` (Right-to-Left)
8.  **Logical AND/NAND**: `and`, `nand` (Left-to-Right)
9.  **Logical XOR**: `xor` (Left-to-Right)
10. **Logical OR/NOR**: `or`, `nor` (Left-to-Right)

_Note: The unary minus operator does not exist; negative numbers must be handled via negative literals or subtraction. Complex logic expressions must be grouped with parentheses to avoid ambiguity._

## 6. Control Flow & Looping

Blocks are started with descriptive keywords and strictly terminated with `done`.

- **Conditional (if)**: `if <cond> then ... else_if <cond> then ... else ... done`.
- **Switch (consider)**: `consider <id> \n case <val> then ... otherwise ... done`. (No fall-through).
- **Indefinite Loop (while)**: `while <cond> do ... done`.
- **Post-Condition Loop (repeat)**: `repeat ... until <cond>`. (Note: Does not end in `done`).
- **Definite Loop (for_every)**: `for_every <id> in range <start> to <end> do ... done`.
- **Collection Loop**: `for_every <item> in <list> do ... done`.
- **Loop Control**: `break` and `continue` are supported.
- **Local Scope**: Defined using `scope ... done`.

## 7. Input / Output & Built-in Functions

- **Output**: `say <expression>` (supports comma-separated lists).
- **Input**: `read <identifier>` (supports comma-separated lists).
- **String/List Functions**:
  - `length_of <string/list>`: Returns size.
  - `find <substring> in <string>`: Returns index.
  - `item <index> of <list>`: Accesses element.
  - `join <string1> with <string2>`: Concatenation.

---

## 8. Compiler Project Objectives & Bug Fixes Tracker

**Goals:**

- [ ] Create tester methods.
- [ ] Create scanner methods.
- [ ] Create symbol table for identifiers.
- [ ] Create hashmap for all tokens.
- [ ] Indicate the line number of invalid characters/errors.
- [ ] Indicate the position of invalid characters/errors.
- [ ] Troubleshooting: Create/reuse and showcase sample programs including all kinds of tokens.

**Specific Scanner/Parser Bugs to Fix:**

- [ ] Endless loop when using a special character (not letter, digit, or underscore) outside of a literal.
- [ ] Program does not show the value of the invalid lexeme.
- [ ] Program separates identifier characters and special characters even when no whitespace separates them.
- [ ] Unthrown error on invalid number literals (e.g., splits `7s5` into an int literal and an id instead of throwing an error).
- [ ] Program fails to register tokens spanning more than one line (and when variables are separated by commas).
- [ ] Program ignores the rest of the tokens when an unterminated list literal is found (e.g., `[5, 4, 2`).
- [ ] Program accepts lists that end in commas instead of throwing an error (e.g., `[5, 4, 2,]`).
- [ ] Program does not print the whole line when an unterminated string literal is found within it (e.g., `"Hello, world!`).
- [ ] Cannot set integers to be negative (setting a number to `-5` causes an endless loop).
- [ ] Block comments (`---`) do not work.
- [ ] Double literals without a leading 0 are undetected (setting something to `.5` causes an endless loop).
