param(
    [ValidateSet("test", "normal")]
    [string]$Mode = "test"
)

$ErrorActionPreference = "Stop"

# Resolve project paths relative to this script so it can be run from any directory.
$repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$compilerRoot = Join-Path $repoRoot "Pseudocode_Compiler"
$sourceFile = Join-Path $compilerRoot "src\pseudocode_compiler\Pseudocode_Compiler.java"
$classOutput = Join-Path $compilerRoot "build\classes"

# Use explicit JDK 21 binaries to avoid runtime mismatch with system Java.
$javac = "C:\Program Files\Java\jdk-21\bin\javac.exe"
$java = "C:\Program Files\Java\jdk-21\bin\java.exe"

Push-Location $compilerRoot
try {
    & $javac -d $classOutput $sourceFile

    if ($Mode -eq "normal") {
        & $java -cp "build\classes" pseudocode_compiler.Pseudocode_Compiler
    }
    else {
        & $java -cp "build\classes" pseudocode_compiler.Pseudocode_Compiler --test
    }
}
finally {
    Pop-Location
}
