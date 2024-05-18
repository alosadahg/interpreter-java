package interpreter;

public class Token {
    final TokenType type;
    final String lexeme;
    private Object literal;
    final int line;
    public Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public void setLiteral(Object literal) {
        this.literal = literal;
    }

    @Override
    public String toString() {
        if(type == TokenType.NEW_LINE){
            return "\n";
        }
        return "Token [type: " + type + " | lexeme: " + lexeme + " | literal: " + literal + " | line: " + line + "]";
    }


    public TokenType getType() {
        return type;
    }


    public String getLexeme() {
        return lexeme;
    }


    public Object getLiteral() {
        return literal;
    }


    public int getLine() {
        return line;
    }

    
}
