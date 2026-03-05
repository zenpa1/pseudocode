
**Fixed an error with splitting special symbols** (i.e. ?integer splits the error and the integer into [error=?][token=integer] instead of [error=?integer])
*-Method Changed: scanInvalidLexeme()*

**Fixed an error with reading single line comments** (i.e. pa--ssing gives [error=pa--ssing] instead of [id=pa] while ignoring "--sing")
*-Method Changed: isLexemeBoundary()*

**Fixed an error with reading block comments** (i.e. pa---ssin---g gives [error=pa---ssin---g] instead of [id=pa][id=g])
*-Method Changed: isLexemeBoundary()*

**Fixed an error where unterminated string literals gave the incorrect error line (off by 1)**
*-Method Changed: scanStringLiteral()*

**Fixed an error where unterminated string literals did not display the line of the error**
*-Method Changed: errorAt()*

**Fixed an error(?) where scanner doesnt read any tokens past End.**
*-Method Changed: scanAndPrintFromFile()*

**Fixed an error where numbers ending with periods are accepted as double literals** (i.e. 15. gives [double=15.] instead of [error=15.])
*-Methods Changed: scanNumberLiteral() and scanSignedNumberLiteral()*

**Fixed an error where identifiers/keywords were accepted if they had "." at the end** (i.e. Declaration_Section. gives [id=Declaration_Section. instead of error=Declaration_Section.])
*-Method Changed: scanIdentifierOrKeyword()*
