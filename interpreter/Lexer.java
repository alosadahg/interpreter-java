package interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static interpreter.TokenType.*;

public class Lexer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private static final Map<String, TokenType> keywords;
    private static final List<Character> characters = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line;

    static {
        characters.add('(');
        characters.add(')');
        characters.add('[');
        characters.add(']');
        characters.add(',');
        characters.add(':');
        characters.add('.');
        characters.add('-');
        characters.add('+');
        characters.add('/');
        characters.add('*');
        characters.add('$');
        characters.add('%');
        characters.add('&');
        characters.add('=');
        characters.add('<');
        characters.add('>');
        characters.add('#');
    }

    static {
        keywords = new HashMap<>();
        keywords.put("ELSE", ELSE);
        keywords.put("IF", IF);
        keywords.put("WHILE", WHILE);
        keywords.put("BEGIN", BEGIN);
        keywords.put("END", END);
        keywords.put("CODE", CODE);
        keywords.put("AND", AND);
        keywords.put("OR", OR);
        keywords.put("NOT", NOT);
        keywords.put("FLOAT", FLOAT);
        keywords.put("CHAR", CHAR);
        keywords.put("BOOL", BOOL);
        keywords.put("INT", INT);
        keywords.put("DISPLAY", DISPLAY);
        keywords.put("SCAN", SCAN);
        keywords.put("NULL", NULL);
        keywords.put("STRING", STRING);
    }

    // FLOAT var = 5.6
    public Lexer(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void escapeChar() {
        while(peek() != ']' && !isAtEnd()) {
            if(peek() == '\n') return;
            advance();
        }

        if(current - start != 2) {
            advance();
            if(peek() !=']') {
            Code.error(line, "Invalid Escape Character.");
            return;
            }
        }

        // consuming the ending ]
        advance();

        char value = source.charAt(start+1);
        if(characters.contains(value)) {
            addToken(ESCAPECHAR, value);
            return;
        }
        Code.error(line, "Invalid Escape Character.");
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(':
                addToken(LEFT_PAREN);
                break;
            case ')':
                addToken(RIGHT_PAREN);
                break;
            case '[':
                escapeChar();
                break;
            case ',':
                addToken(COMMA);
                break;
            case ':':
                addToken(COLON);
                break;
            case '.':
                addToken(DOT);
                break;
            case '-':
                addToken(MINUS);
                break;
            case '+':
                addToken(PLUS);
                break;
            case '/':
                addToken(SLASH);
                break;
            case '*':
                addToken(STAR);
                break;
            case '$':
                addToken(NEW_LINE);
                break;
            case '%':
                addToken(MODULO);
                break;
            case '&':
                addToken(CONCAT);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EVAL : ASSIGN);
                break;
            case '<':
                if (match('=')) {
                    addToken(LESS_OR_EQUAL);
                } else if (match('>')) {
                    addToken(NOT_EQUAL);
                } else {
                    addToken(LESS_THAN);
                }
                break;
            case '>':
                addToken(match('=') ? GREATER_OR_EQUAL : GREATER_THAN);
                break;
            case '#':
                while (peek() != '\n' && !isAtEnd())
                    advance();
            case '\t':
            case '\r':
            case ' ':
                // Ignore whitespace.
                break;
            case '\n':
                line++;
                break;
            case '"': string(); break;
            case '\'': chars(); break;
            default:
                if(isDigit(c)) {
                    number();
                } else if(isAlpha(c)) {
                    identifier();
                } else {
                    Code.error(line, "Unexpected character.");
                }
                break;
        }
    }

    private void identifier() {
        while (isAlphanumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if(type==null) type = IDENTIFIER;

        addToken(type);
    }

    private void number() {
        while(isDigit(peek())) advance();

        // looking for fractional part
        if(peek() == '.' && isDigit(peekNext())) {
            advance();

            while(isDigit(peek())) advance();
            addToken(TYPEFLOAT, Double.parseDouble(source.substring(start, current)));
            return;
        } //else {
            addToken(TYPEINT, Integer.parseInt(source.substring(start, current)));
        //}
    }

    private char peekNext() {
        if(current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z' ||
                c >= 'A' && c <= 'Z' ||
                c == '_');
    }

    private boolean isAlphanumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void string() {
        while(peek() != '"' && !isAtEnd()) {
            if(peek() == '\n') line++;
            advance();
        }

        if(isAtEnd()) {
            Code.error(line, "Unterminated String.");
            return;
        }

        // consuming the ending "
        advance();

        String value = source.substring(start+1, current-1);
        if(value.equals("TRUE")) {
            addToken(TRUE, Boolean.parseBoolean(value));
        } else if(value.equals("FALSE")) {
            addToken(FALSE, Boolean.parseBoolean(value));
        } else {
            addToken(TYPESTRING, value);
        }
    }

    private void chars() {
        while(peek() != '\'' && !isAtEnd()) {
            if(peek() == '\n') line++;
            advance();
        }

        if(current - start != 2) {
            Code.error(line, "Unterminated Character.");
            return;
        }

        // consuming the ending '
        advance();

        char value = source.charAt(start+1);
        addToken(TYPECHAR, value);
    }

    private boolean match(char expected) {
        if (isAtEnd())
            return false;
        if (source.charAt(current) != expected)
            return false;

        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd())
            return '\0';
        return source.charAt(current);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}
