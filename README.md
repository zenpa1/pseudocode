# Pseudocode Scanner Project

This repository contains a Java-based **scanner (lexer)** for a custom pseudocode language.

The scanner reads source files, tokenizes lexemes, reports lexical errors with line/position diagnostics, and builds a symbol table for user-defined identifiers.

## What this project currently includes

- A file-based scanner implementation in Java
- Reserved-token lookup table for language keywords/operators/functions/noise tokens
- Symbol table support for identifiers
- Robust lexical error handling and recovery
- Embedded regression suite (`--test`) covering scanner goals and bug fixes

## Repository layout

- `Pseudocode_Compiler/src/pseudocode_compiler/Pseudocode_Compiler.java`  
	Main scanner implementation and test harness.
- `Pseudocode_Compiler/program.txt`  
	Default input program for normal execution mode.
- `RULEBOOK.md`  
	Language spec + project tracker used for implementation order.
- `DOCUMENTATION.md`  
	Full technical documentation (architecture, logic, bug-fix rationale, and examples).

## Execution modes

## 1) Normal mode
Scans `program.txt` and prints tokens + symbol table.

## 2) Test mode (`--test`)
Runs embedded regression programs that validate scanner behavior and bug-fix coverage.

## Build and run (Windows PowerShell)

> Use explicit JDK 21 binaries to avoid Java runtime version mismatch.

From `Pseudocode_Compiler`:

```powershell
& "C:\Program Files\Java\jdk-21\bin\javac.exe" -d build\classes src\pseudocode_compiler\Pseudocode_Compiler.java
& "C:\Program Files\Java\jdk-21\bin\java.exe" -cp build\classes pseudocode_compiler.Pseudocode_Compiler --test
```

Normal mode (scan `program.txt`):

```powershell
& "C:\Program Files\Java\jdk-21\bin\java.exe" -cp build\classes pseudocode_compiler.Pseudocode_Compiler
```

## Convenience script (`runpseudo.ps1`)

You can run compile + execute in one command from the repo root:

```powershell
.\runpseudo.ps1
```

Supported modes:

```powershell
.\runpseudo.ps1 -Mode test    # compile + run --test suite (default)
.\runpseudo.ps1 -Mode normal  # compile + run program.txt
```

If PowerShell execution policy blocks scripts, run once in your terminal:

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
```

## Helper snippet (what it is and why use it)

If you run scripts often, PowerShell normally requires typing `./` (or `.\`) before script files.
The helper snippet creates a **small command alias** named `runpseudo` so you can run:

```powershell
runpseudo
```

instead of:

```powershell
.\runpseudo.ps1
```

### One-time (current terminal session only)

```powershell
Set-Alias runpseudo "$PWD\runpseudo.ps1"
```

What this does:
- `Set-Alias` creates a shortcut command name
- `runpseudo` becomes a shortcut to your script path
- it lasts only until you close that terminal

### Persistent (every new PowerShell terminal)

Add this line to your PowerShell profile (`$PROFILE`):

```powershell
Set-Alias runpseudo "C:\Users\zenpa1\Desktop\Main\Development\my-projects\main-projects\pseudocode\runpseudo.ps1"
```

Then restart your terminal. After that, `runpseudo` works in every new PowerShell session.

## Quick workflow for your own sample programs

1. Edit `Pseudocode_Compiler/program.txt`
2. Put your pseudocode sample in that file
3. Run normal mode command above
4. Check token output and error diagnostics

For persistent regression checks, add new samples in `runScannerTests()` inside `Pseudocode_Compiler.java`.

## Detailed docs

For complete method-by-method explanation and all scanner rules, see:

- `DOCUMENTATION.md`

## Current Status

Scanner objectives and bug-fix tracker status (from `RULEBOOK.md`):

### Completed goals

- [x] Create tester methods
- [x] Create scanner methods
- [x] Create symbol table for identifiers
- [x] Create hashmap for all tokens
- [x] Indicate the line number of invalid characters/errors
- [x] Indicate the position of invalid characters/errors
- [x] Troubleshooting showcase with sample programs covering token categories

### Completed scanner/parser bug fixes

- [x] Endless loop with special characters outside literals
- [x] Invalid lexeme value now shown in errors
- [x] Identifier + special characters without whitespace now treated as one invalid lexeme
- [x] Invalid number literals like `7s5` now throw lexical errors
- [x] Multiline and comma-separated token registration fixed
- [x] Unterminated list literal no longer swallows remaining tokens
- [x] Lists ending with trailing comma now throw error (e.g., `[5, 4, 2,]`)
- [x] Unterminated string literal errors now include the full source line
- [x] Negative numeric literals (e.g., `-5`) are tokenized
- [x] Block comments (`---`) fixed, including inline close behavior
- [x] Leading-dot doubles (e.g., `.5`) are tokenized

### Last Verified

- Date: 2026-03-04
- Command: `runpseudo`
- Result: full scanner regression suite passed

## Branching reminder

Continue working on feature branches (not `main`) when adding new scanner/parser work.
