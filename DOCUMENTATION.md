# Pseudocode Compiler Scanner Documentation

## 1) Purpose of this project
This project currently implements the **scanner (lexer)** phase of a pseudocode compiler in Java.

At a high level, it:
- Reads a source file (`program.txt` by default)
- Splits source text into tokens (keywords, identifiers, literals, separators)
- Reports lexical errors with line + character position
- Builds a symbol table for user-defined identifiers
- Includes built-in regression tests (`--test`) for known bug fixes

---

## 2) Source layout
Main implementation file:
- `Pseudocode_Compiler/src/pseudocode_compiler/Pseudocode_Compiler.java`

Key classes inside that file:
- `Pseudocode_Compiler` — entry point + test harness
- `Scanner` — lexical analyzer (core logic)
- `SymbolTable` — identifier storage
- `Token` — token/error container
- `TokenHashMap` — reserved-word lookup table

---

## 3) Execution modes
## Normal mode
When no arguments are passed, `main` scans `program.txt`.

Flow:
1. Validate file exists
2. Create `Scanner`
3. Pull tokens using `getNextToken()` until EOF
4. Print tokens line-by-line
5. Print symbol table when `TK_END` is encountered

## Test mode
When `--test` is passed, `runScannerTests()` executes embedded sample programs.

Why this matters:
- Regression tests live beside scanner logic
- Bug fixes can be validated quickly
- You can extend tests without external framework setup

---

## 4) Class-by-class explanation

## `Pseudocode_Compiler`
### `main(String[] args)`
Chooses between:
- `--test` => run embedded scanner tests
- default => scan `program.txt`

### `scanAndPrintFromFile(File programFile)`
Responsible for scanner output presentation:
- Prints formatted tokens
- Adds line breaks when scanner line changes
- Distinguishes regular token output vs error output
- Prints the symbol table at `TK_END`

### `runScannerTests()`
Defines self-contained program strings used to verify scanner behavior.

Current suite includes:
- Baseline tokenization
- Operators/literals
- Comments
- Control-flow keywords
- Built-ins/noise tokens
- Reserved word coverage
- All tracked bug fixes (invalid lexemes, invalid numbers, multiline commas, list recovery, trailing commas, unterminated strings, negative numbers, block comments, leading-dot doubles)

### `runSingleScannerTest(String testName, String source)`
Creates a temp file, writes source string, scans it, then deletes file.

Why temp files were used (instead of in-memory strings):
- Keeps scanner execution path identical to real usage (`FileReader` + `BufferedReader`)
- Avoids introducing scanner-only test code paths

---

## `Scanner` (core lexer)
The scanner operates as a single-pass, character-driven state machine.

### Core state fields
- `reader` — source input stream
- `ch` — current character (as int)
- `currentLine` — 1-based line counter
- `currentPosition` — 1-based absolute character position
- `currentLineBuffer` / `lastCompletedLine` — full-line snapshots for better error diagnostics
- `pendingError` — deferred lexical errors (e.g., block comment handler)
- `tokenHashMap` — reserved keyword map
- `symbolTable` — user identifiers

### `readChar()`
Centralized character consumption method.

What it does:
- Reads one character
- Increments `currentPosition`
- Maintains line buffers for diagnostic output

Why this works better than direct `reader.read()` everywhere:
- Consistent position tracking
- Consistent line snapshot behavior
- Fewer bugs from mixed read paths

### Lookahead helpers
- `peek()` — look 1 character ahead
- `peekNext(int num)` — look N characters ahead

Used for comment delimiter detection and signed/leading-dot number checks.

### Comment handlers
- `consumeSingleLineComment()` for `-- ... \n`
- `consumeMultiLineComment()` for `--- ... ---`
- `whiteSpaceAndCommentHandler()` to repeatedly skip whitespace/comments before scanning tokens

Important behavior:
- Block comment closing now consumes exactly `---` and preserves the next character
- Unterminated block comment returns a lexical error token

### Lexeme boundary model
`isLexemeBoundary(char current)` defines where tokens may legally end.

This is critical for bug fixes like:
- `abc@123` => one invalid lexeme
- `7s5` => one invalid lexeme
- `.5abc` => one invalid lexeme

### Invalid lexeme consumption
`consumeUntilBoundary()` gathers the rest of a malformed contiguous sequence.

Why this works:
- Prevents misleading token splitting
- Produces clearer diagnostics with complete offending text

### Token scanners

#### `scanIdentifierOrKeyword()`
Scans `[A-Za-z][A-Za-z0-9_]*` (+ optional trailing `.` as currently implemented).
Then:
- If in reserved map => reserved token
- Else => `TK_ID` + register in symbol table

Also handles invalid mixed forms (`abc@123`) as a single error.

#### `scanNumberLiteral()`
Scans unsigned integers and doubles.
Examples:
- `42` => `TK_INT_LIT`
- `3.14` => `TK_DOUBLE_LIT`
- `7s5` => invalid lexeme

#### `scanSignedNumberLiteral()`
Scans `-` followed by digits (and optional decimal fraction).
Examples:
- `-5` => `TK_INT_LIT`
- `-2.5` => `TK_DOUBLE_LIT`

#### `scanLeadingDotDoubleLiteral()`
Scans doubles with no leading zero.
Example:
- `.5` => `TK_DOUBLE_LIT`

#### `scanStringLiteral()`
Scans `"..."`.
If unterminated:
- Reports error with line + position
- Includes **full source line** in message

#### `scanListLiteral()`
Scans `[ ... ]`.
Special rules added:
- Unterminated at newline => reports error and recovers to continue scanning next tokens
- Trailing comma before `]` (e.g., `[1,2,]`) => explicit error

#### `scanCommaToken()`
Returns `TK_COMMA` and consumes comma.
Needed for variable/argument lists over same or multiple lines.

#### `scanInvalidLexeme()`
Fallback when no valid token rule matches.
Reports quoted offending lexeme with line + position.

### `getNextToken()` dispatcher
Order of scan decisions:
1. Skip whitespace/comments
2. Return pending deferred errors
3. Detect and scan token type by current character
4. Apply stall guard (safety net)

Safety guard:
- If scanner fails to consume input in a cycle, it force-consumes one char and emits an invalid-lexeme error.
- Prevents endless loops on malformed/unexpected input.

---

## `SymbolTable`
Stores discovered identifiers as `lexeme -> TK_ID`.

Implementation detail:
- Uses `LinkedHashMap` to preserve insertion order for stable output.

---

## `Token`
Represents either:
- Normal token (`type`, `lexeme`)
- Error token (`type=ERROR`, `errorMessage`)

`isError()` tells printer whether to use normal or error format.

---

## `TokenHashMap`
Reserved lexeme dictionary (`String -> token kind`).

Contains keywords/operators/functions/noise/comment markers, e.g.:
- `Program`, `Declaration_Section`, `Statement_Section`, `End.`
- `set`, `to`, `if`, `else_if`, `done`, `while`, `repeat`, etc.
- `plus`, `minus`, `times`, `divided_by`, `modulo`, `raised_to`
- `length_of`, `find`, `item`, `join`, `with`
- Noise tokens: `the`, `a`, `an`, `please`

---

## 5) Why key bug fixes now work

1. **Endless loop on special character**
- Progress guard in `getNextToken()` ensures each cycle consumes input.

2. **Invalid lexeme value not shown**
- `visibleLexeme()` and consolidated invalid-lexeme paths include exact offending text.

3. **Identifier + special split incorrectly**
- Boundary validation + `consumeUntilBoundary()` emits one invalid lexeme (e.g., `abc@123`).

4. **Invalid number literal not rejected (`7s5`)**
- Number scanner now validates post-number boundary and rejects mixed forms.

5. **Multiline/comma tokens not registered**
- Explicit `TK_COMMA` scanning supports comma-separated values across lines.

6. **Unterminated list swallowed remaining tokens**
- List scanner newline recovery emits error then resumes scanning next line.

7. **Trailing comma in list accepted (`[1,2,]`)**
- Tracks last significant char; comma before `]` triggers error.

8. **Unterminated string did not show full line**
- Line-buffer snapshots included in string error message.

9. **Negative integer issues (`-5`)**
- Signed number scanner handles `-<digits>` and `-<digits>.<digits>`.

10. **Block comments broken**
- Block close now consumes exactly `---` and preserves following token.

11. **Leading-dot doubles (`.5`)**
- Dedicated scanner path tokenizes leading-dot doubles.

---

## 6) How to run (recommended on your machine)
Because your default `java` is older than your compiler JDK, use explicit JDK 21 binaries.

From `Pseudocode_Compiler` folder:

```powershell
& "C:\Program Files\Java\jdk-21\bin\javac.exe" -d build\classes src\pseudocode_compiler\Pseudocode_Compiler.java
& "C:\Program Files\Java\jdk-21\bin\java.exe" -cp build\classes pseudocode_compiler.Pseudocode_Compiler --test
```

Normal mode (uses `program.txt`):

```powershell
& "C:\Program Files\Java\jdk-21\bin\java.exe" -cp build\classes pseudocode_compiler.Pseudocode_Compiler
```

---

## 7) How to create and run your own sample programs

## Option A: Edit `program.txt` (quick manual test)
1. Open `Pseudocode_Compiler/program.txt`
2. Paste your pseudocode sample
3. Run normal mode command
4. Inspect token stream + symbol table

Example:
```text
Program Demo
Declaration_Section
double value
Statement_Section
set value to .5
say value
End.
```

## Option B: Add a named regression sample in code
1. Open `runScannerTests()` in `Pseudocode_Compiler.java`
2. Add a new `runSingleScannerTest("Your Test Name", "...")`
3. Recompile and run `--test`

Template:
```java
runSingleScannerTest("My custom case",
        "Program Custom\n"
        + "Declaration_Section\n"
        + "integer x\n"
        + "Statement_Section\n"
        + "set x to 10\n"
        + "End.");
```

When to choose which:
- `program.txt` => fast one-off experiments
- `runScannerTests()` => persistent regression for future verification

---

## 8) Practical troubleshooting

## “UnsupportedClassVersionError”
Cause: running classes compiled by JDK 21 with Java 8 runtime.

Fix: run explicit Java 21 binary as shown above.

## “File does not exist.”
Cause: normal mode expects `program.txt` in current working directory.

Fix: run from `Pseudocode_Compiler` folder, or adjust file path in code.

## Weird token breaks on malformed input
Use `--test` mode and add a focused test case so behavior is reproducible and trackable.

---

## 9) Extension notes (for parser phase later)
When adding parser integration, keep scanner contracts stable:
- Keep `Token` shape unchanged (`type`, `lexeme`, `isError`, `errorMessage`)
- Keep deterministic token order
- Keep scanner recovery behavior (error + continue) to maximize parser diagnostics

Suggested next parser-ready improvements:
- Add token line/position fields directly in `Token` object (instead of message-only)
- Replace string token kinds with enum constants
- Move test programs to external fixtures for large-scale testing

---

## 10) Summary
This scanner implementation now supports:
- Reserved keywords/operators/functions
- Identifier symbol tracking
- Numeric variants (`42`, `3.14`, `-5`, `.5`)
- Strings and list literals
- Comma-separated multiline tokenization
- Robust lexical diagnostics and recovery
- Embedded regression suite tied to every tracked scanner bug
