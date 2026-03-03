package pseudocode_compiler;
import java.util.HashMap;
import java.io.*;

public class Pseudocode_Compiler { 
    
    public static void main(String[] args) {
        HashMap<String, String> symbolTable = new HashMap<>();
        File program = new File("program.txt");
        Token token;
        
        //check if file exists
        if(!program.exists()){
            System.out.println("File does not exist.");
            System.exit(0);
        }
        
        Scanner scanner = new Scanner(program, symbolTable);
        
        int printedLine = 1;
        
        while(true){
            token = scanner.getNextToken();
            
            if (token == null) {
                System.out.println("\nEnd of file reached.");
                break;
            }

            //adds newline if current line is higher than previous
            if (scanner.getCurrentLine() > printedLine) {
                System.out.println(); 
                printedLine = scanner.getCurrentLine();
            }

            if (token.isError()) {
                System.out.print("[" + "Error: " + token.getErrorMessage() + "]");
            } else {
                System.out.print("[" + token.getType() + ", " + token.getLexeme() + "] ");
            }
            
            if (token.getType().equals("TK_END")){
                System.out.println();
                System.out.println("\n--- Symbol Table ---");
                for (HashMap.Entry<String, String> entry : symbolTable.entrySet()) {
                    System.out.println("Lexeme: " + entry.getKey() + " | Type: " + entry.getValue());
                }
                break;
            }
        }
    }
}

class Scanner{
    private BufferedReader reader;
    private int ch;
    private LookupTable lookupTable = new LookupTable();
    private HashMap<String, String> symbolTable; 
    private Token token;
    private int currentLine = 1;
    private Token pendingError = null;
    
    public Scanner(File inputFile, HashMap symbolTable){
        this.symbolTable = symbolTable;
        try {
            reader = new BufferedReader(new FileReader(inputFile));
            ch = reader.read();
        } catch (IOException e) {
            System.out.println("Could not open file: " + e.getMessage());
        }
    }
    
    private int peek() throws IOException{
        reader.mark(1);
        int nextChar = reader.read();
        reader.reset();
        return nextChar;
    }
    
    private int peekNext(int num) throws IOException {
        reader.mark(num);
        int nextChar = -1;
        for (int i = 0; i < num; i++) {
            nextChar = reader.read(); //read num times to reach that position
        }
        reader.reset();
        return nextChar;
    }
    
    private void consumeSingleLineComment() throws IOException {
        while (ch != '\n' && ch != -1) {
            ch = reader.read();
        }
        if (ch == '\n') {
            currentLine++;
            ch = reader.read();
        }
    }
    
    private Token consumeMultiLineComment() throws IOException {
        while (true) {
            if (ch == -1) {
                return new Token("[" + "Error: unterminated multiline comment at line " + currentLine + "]");
            }
            if (ch == '\n') 
                currentLine++;
            
            if (ch == '-' && peek() == '-' && peekNext(2) == '-') {
                ch = reader.read(); // consume first '-'
                ch = reader.read(); // consume second '-'
                ch = reader.read(); // consume third '-'
                ch = reader.read();
                return null;
            }
            ch = reader.read();
        }
    }
    
    private void whiteSpaceAndCommentHandler() {
        try {
            
            char current = (char) ch;

            while (Character.isWhitespace(current)) {
                if (current == '\n') 
                    currentLine++;
                ch = reader.read();
                current = (char) ch; //update current each iteration
            }

            //check for comments after exiting whitespace loop
            if (current == '-' && peek() == '-') {
                ch = reader.read(); //consume first '-'
                ch = reader.read(); //consume second '-'

                if (ch == '-') {
                    ch = reader.read(); //consume third '-'  
                    Token error = consumeMultiLineComment();
                    if (error != null) pendingError = error;
                } else {
                    consumeSingleLineComment();
                }
                whiteSpaceAndCommentHandler();
            }

        } catch (IOException e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }
    
    public int getCurrentLine() { return currentLine; }
    
    public Token getNextToken(){
        try { 
            whiteSpaceAndCommentHandler();
            
            if (pendingError != null) { //check for pending error first 
                Token error = pendingError;
                pendingError = null;
                return error;
            }
            
            while (ch != -1) { //check if there are chars to be read
                char current = (char) ch;
                current = (char) ch;

                if (Character.isLetter(current)) { //identifiers/keywords--------------------------------------
                    StringBuilder string = new StringBuilder();

                    while (Character.isLetterOrDigit(current) || current == '_') {
                        //concat current char to lexeme
                        string.append(current);
                        ch = reader.read(); // read next char
                        current = (char) ch;
                    }
                    
                    if (current == '.') {
                        string.append(current);
                        ch = reader.read();
                        current = (char) ch;
                    }

                    String lexeme = string.toString();

                    if (lookupTable.contains(lexeme) == true) //check if token is a reserved word
                    {
                        return token = new Token(lookupTable.lookup(lexeme), lexeme);
                    } else {
                        //add token to symbolTable as identifier instead
                        if (!symbolTable.containsKey(lexeme)) {
                            symbolTable.put(lexeme, "TK_ID");
                        }
                        return token = new Token("TK_ID", lexeme);
                    }
                } else if (Character.isDigit(current)) { //digits--------------------------------------
                    StringBuilder string = new StringBuilder();

                    while (Character.isDigit(current)) {
                        string.append(current);
                        ch = reader.read(); //read next char
                        current = (char) ch;
                    }

                    if (current == '.') {
                        string.append(current);
                        ch = reader.read();
                        current = (char) ch;
                        while (Character.isDigit(current)) {
                            string.append(current);
                            ch = reader.read();
                            current = (char) ch;
                        }
                        String lexeme = string.toString();
                        return token = new Token("TK_DOUBLE_LIT", lexeme);
                    }
                    String lexeme = string.toString();
                    return token = new Token("TK_INT_LIT", lexeme);
                } else if (current == '"') { //string literals--------------------------------------
                    StringBuilder string = new StringBuilder();
                    ch = reader.read(); //read opening quote
                    current = (char) ch;

                    while (current != '"' && current != '\n' && ch != -1) {
                        string.append(current);
                        ch = reader.read();
                        current = (char) ch;
                    }

                    if (current == '"') {
                        ch = reader.read();
                        String lexeme = string.toString();
                        return token = new Token("TK_STR_LIT", lexeme);
                    } else if (current == '\n') {
                        //string broken by a newline
                        currentLine++;
                        ch = reader.read();
                        String lexeme = string.toString();
                        return token = new Token("Unterminated string literal at line " + currentLine + " near: " + lexeme);
                    } else {
                        //hit EOF
                        String lexeme = string.toString();
                        return token = new Token("Unterminated string literal at " + currentLine + "near " + lexeme);
                    }
                } else if (current == '[') { //list literals--------------------------------------
                    StringBuilder string = new StringBuilder();
                    string.append(current);
                    ch = reader.read(); //read opening bracket
                    current = (char) ch;
                    
                    while (current != ']' && ch != -1) {
                        whiteSpaceAndCommentHandler();
                        current = (char) ch;
                        if (current == ']') break;
                        string.append(current);
                        ch = reader.read();   
                        current = (char) ch;
                    } 
                    
                    if (current == ']'){
                        string.append(current);
                        ch = reader.read();
                        String lexeme = string.toString();
                        return token = new Token("TK_LIST_LIT", lexeme);
                    } else
                        return token = new Token("Unterminated list literal near: " + string.toString() + " at line " + currentLine);
                } else{
                    StringBuilder badString = new StringBuilder();
                    badString.append(current);
                    ch = reader.read();
                    current = (char) ch;
                    while (ch != -1 && !Character.isWhitespace(current)
                            && !Character.isLetter(current)
                            && !Character.isDigit(current)
                            && current != '"'
                            && current != '[') {
                        badString.append(current);
                        ch = reader.read(); // read next char
                        current = (char) ch;
                    }
                    
                    String badToken = badString.toString();
                    return token = new Token("Invalid lexeme " + badToken + " at line " + currentLine);
                }
            }
        } catch (IOException e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
        return null;
    }
}

class Token{
    private String type;
    private String lexeme;
    private boolean isError;
    private String errorMessage;
    
    public Token(String type, String lexeme){
        this.type = type;
        this.lexeme = lexeme;
        this.isError = false;
        this.errorMessage = null;
    }
    
    public Token(String errorMessage) {
        this.type = "ERROR";
        this.lexeme = "";
        this.isError = true;
        this.errorMessage = errorMessage;
    }
    
    public boolean isError() { return isError; }
    public String getErrorMessage() { return errorMessage; }
    public String getType(){ return type; }
    public String getLexeme(){ return lexeme; }
    
    public String toString(){
        if (isError) return "(ERROR: " + errorMessage + ")";
        return "(" + type + ", " + lexeme + ")";
    }
}

class LookupTable{
    private HashMap<String, String> table;
    
    public LookupTable(){
        table = new HashMap<>();
        initialize();
    }
    
    private void initialize(){
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
        table.put("is", "TK_IS");
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
        table.put("a", "NT_A");
        table.put("an", "NT_AN");
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
    
    public String lookup(String lexeme){
        if (table.containsKey(lexeme)) {
            return table.get(lexeme);
        }
        return null;
    }
    
    public boolean contains(String lexeme) {
        return table.containsKey(lexeme);
    }
}