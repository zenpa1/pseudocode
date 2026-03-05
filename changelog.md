
**Fixed an error with splitting special symbols** (i.e. ?integer splits the error and the integer into [error=?][token=integer] instead of [error=?integer])
<br>*-Method Changed: scanInvalidLexeme()*
<br><br>

**Fixed an error with reading single line comments** (i.e. pa--ssing gives [error=pa--ssing] instead of [id=pa] while ignoring "--sing")
<br>*-Method Changed: isLexemeBoundary()*
<br><br>


**Fixed an error with reading block comments** (i.e. pa---ssin---g gives [error=pa---ssin---g] instead of [id=pa][id=g])
<br>*-Method Changed: isLexemeBoundary()*
<br><br>


**Fixed an error where unterminated string literals gave the incorrect error line (off by 1)**
<br>*-Method Changed: scanStringLiteral()*
<br><br>


**Fixed an error where unterminated string literals did not display the line of the error**
<br>*-Method Changed: errorAt()*
<br><br>

**Fixed an error(?) where scanner doesnt read any tokens past End.**
<br>*-Method Changed: scanAndPrintFromFile()*
<br><br>

**Fixed an error where numbers ending with periods are accepted as double literals** (i.e. 15. gives [double=15.] instead of [error=15.])
<br>*-Methods Changed: scanNumberLiteral() and scanSignedNumberLiteral()*
<br><br>

**Fixed an error where identifiers/keywords were accepted if they had "." at the end** (i.e. Declaration_Section. gives [id=Declaration_Section. instead of error=Declaration_Section.])
<br>*-Method Changed: scanIdentifierOrKeyword()*
