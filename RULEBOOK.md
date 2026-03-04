# Pseudocode Language Specification (CS-ELEC2A)

## 1. Program Anatomy

[cite_start]Every program must follow a strict sequential structure[cite: 1377]:

- [cite_start]**Header**: Begins with `Program <identifier>`[cite: 43].
- **Declaration Section**: Marked by `Declaration_Section`. [cite_start]All variables and constants must be explicitly declared here before use[cite: 51, 1368].
- **Statement Section**: Marked by `Statement_Section`. [cite_start]Contains all executable logic[cite: 51].
- [cite_start]**Footer**: Terminated physically with `End.` (note the period)[cite: 53].

## 2. Character Set & Syntax Rules

- **Case-Sensitivity**: The language is strictly case-sensitive. Keywords are entirely lowercase (e.g., `say`). [cite_start]Casing variations (e.g., `Say`) are treated as identifiers[cite: 15, 16, 17].
- [cite_start]**Whitespaces**: Spaces, tabs, and newlines act as the primary delimiters[cite: 98]. [cite_start]Tokens can span multiple lines[cite: 1453].
- [cite_start]**Separators**: The language does **not** use semicolons or periods as statement separators[cite: 1189].
- **Comments**:
  - [cite_start]Single-line: `--` ignores all characters until a newline[cite: 1410].
  - [cite_start]Multi-line (Block): `---` opens the block and another `---` closes it[cite: 1411].
- [cite_start]**Noise Tokens**: Optional keywords used purely for human readability: `the`, `a`, `an`, `please`[cite: 1474].

## 3. Data Types & Literals

[cite_start]The language supports four main data types[cite: 1069]:

- [cite_start]`integer`: Whole numbers (e.g., `42`)[cite: 61, 1073].
- [cite_start]`double`: Floating-point numbers (e.g., `3.14`)[cite: 61, 1073]. [cite_start]Unified under a 64-bit IEEE 754 standard behind the scenes[cite: 1482].
- [cite_start]`boolean`: `true` or `false`[cite: 61, 1074].
- [cite_start]`string`: Characters enclosed in double quotes (e.g., `"Hello"`)[cite: 61, 1075].
- [cite_start]`list`: Heterogeneous collections enclosed in brackets (e.g., `["Alice", 25]`)[cite: 61, 1496].

## 4. Identifiers & Variables

- **Naming Rules**: Must begin with a letter (A-Z, a-z). [cite_start]Subsequent characters can be letters, digits (0-9), or underscores (\_)[cite: 1079].
- [cite_start]**Assignments**: Use the `set ... to` keywords (e.g., `set count to 5`)[cite: 1275].
- [cite_start]**Constants**: Declared with the `constant` keyword and assigned using `is` (e.g., `constant double PI is 3.14`)[cite: 1089]. [cite_start]Cannot be reassigned[cite: 1370].
- [cite_start]**Type Aliasing**: Types can be aliased using `define <NewType> as <BaseType>`[cite: 1091].
- [cite_start]**Type Casting**: Explicit conversions use the `as` keyword (e.g., `set myString to 50 as string`)[cite: 1264].

## 5. Operators & Operator Precedence

[cite_start]Standard mathematical precedence is followed, overriding with parentheses `()`[cite: 1347].

1.  [cite_start]**Grouping**: `()` [cite: 1364]
2.  [cite_start]**Access**: `of`, `length_of`, `item` (Right-to-Left) [cite: 1364]
3.  [cite_start]**Exponentiation**: `raised_to` (Right-to-Left) [cite: 1364]
4.  [cite_start]**Multiplicative**: `times`, `divided_by`, `modulo` (Left-to-Right) [cite: 1364]
5.  [cite_start]**Additive/String**: `plus`, `minus`, `join`, `with` (Left-to-Right) [cite: 1364]
6.  [cite_start]**Relational**: `greater_than`, `less_than`, `equal_to`, `not_equal_to`, `greater_than_or_equal_to`, `less_than_or_equal_to`, `is`, `is_not` [cite: 1364]
7.  [cite_start]**Logical NOT**: `not` (Right-to-Left) [cite: 1364]
8.  [cite_start]**Logical AND/NAND**: `and`, `nand` (Left-to-Right) [cite: 1366]
9.  [cite_start]**Logical XOR**: `xor` (Left-to-Right) [cite: 1366]
10. [cite_start]**Logical OR/NOR**: `or`, `nor` (Left-to-Right) [cite: 1366]

[cite_start]_Note: The unary minus operator does not exist; negative numbers must be handled via negative literals or subtraction[cite: 1555]. [cite_start]Complex logic expressions must be grouped with parentheses to avoid ambiguity[cite: 1374]._

## 6. Control Flow & Looping

[cite_start]Blocks are started with descriptive keywords and strictly terminated with `done`[cite: 1196].

- [cite_start]**Conditional (if)**: `if <cond> then ... else_if <cond> then ... else ... done`[cite: 1290, 1291, 1292, 1293, 1294, 1295].
- [cite_start]**Switch (consider)**: `consider <id> \n case <val> then ... otherwise ... done`[cite: 1311, 1312, 1313, 1318, 1319]. [cite_start](No fall-through [cite: 1324]).
- [cite_start]**Indefinite Loop (while)**: `while <cond> do ... done`[cite: 1331].
- [cite_start]**Post-Condition Loop (repeat)**: `repeat ... until <cond>`[cite: 1334]. (Note: Does not end in `done`).
- [cite_start]**Definite Loop (for_every)**: `for_every <id> in range <start> to <end> do ... done`[cite: 1337].
- [cite_start]**Collection Loop**: `for_every <item> in <list> do ... done`[cite: 1341].
- [cite_start]**Loop Control**: `break` and `continue` are supported[cite: 1544].
- [cite_start]**Local Scope**: Defined using `scope ... done`[cite: 1228, 1229].

## 7. Input / Output & Built-in Functions

- [cite_start]**Output**: `say <expression>` (supports comma-separated lists)[cite: 1272].
- [cite_start]**Input**: `read <identifier>` (supports comma-separated lists)[cite: 1266, 1619].
- **String/List Functions**:
  - [cite_start]`length_of <string/list>`: Returns size[cite: 1594].
  - [cite_start]`find <substring> in <string>`: Returns index[cite: 1595].
  - [cite_start]`item <index> of <list>`: Accesses element[cite: 1514].
  - [cite_start]`join <string1> with <string2>`: Concatenation[cite: 1592].

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
