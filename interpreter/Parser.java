package interpreter;

import static interpreter.TokenType.*;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            //statements.add(statement());
            statements.add(declaration());
        }

        return statements;
    }

    private Expr expression() {
        //return equality();
        return assignment();
    }

    private Expr assignment() {
        Expr expr = equality();

        if(match(ASSIGN)) {
            Token assign = previous();
            Expr value = assignment();

            if(expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            }

            error(assign, "Invalid assignment target.");
        }
        return expr;
    }

    private Stmt declaration() {
        try {
            if(match(INT)) return varDeclaration("INT");
            if(match(FLOAT)) return varDeclaration("FLOAT");
            if(match(CHAR)) return varDeclaration("CHAR");
            if(match(STRING)) return varDeclaration("STRING");
            if(match(BOOL)) return varDeclaration("BOOL");

            return statement();
        } catch(ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt statement() {
        if (match(DISPLAY) && match(COLON)) return displayStatement();
        if (match(BEGIN) && match(CODE)) return new Stmt.Block(block());
    
        return expressionStatement();
      }

      private Stmt displayStatement() {
        Expr value = expression();
        return new Stmt.Display(value);
      }

      private Stmt varDeclaration(String type) {
        Token name = consume(IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if(match(ASSIGN)) {
            initializer = expression();
        }

        if(type.equals("FLOAT")) {
            return new Stmt.Float(name, initializer);
        }
        if(type.equals("INT")) {
            return new Stmt.Int(name, initializer);
        }
        if(type.equals("STRING")) {
            return new Stmt.String(name, initializer);
        }
        if(type.equals("CHAR")) {
            return new Stmt.Char(name, initializer);
        }
        if(type.equals("BOOL")) {
            return new Stmt.Char(name, initializer);
        }
        
        return null;
      }

      private Stmt expressionStatement() {
        Expr expr = expression();
        return new Stmt.Expression(expr);
      }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while(!check(END) && !checkNext(CODE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(END, "Expect END after block.");
        consume(CODE, "Expect CODE after END.");
        return statements;
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(NOT_EQUAL, EQUAL_EVAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER_THAN, GREATER_OR_EQUAL, LESS_THAN, LESS_OR_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(MINUS, PLUS, CONCAT)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(STAR, SLASH, MODULO)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr unary() {
        if (match(NOT, MINUS, PLUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    private Expr primary() {
        if (match(FALSE))
            return new Expr.Literal(false);
        if (match(TRUE))
            return new Expr.Literal(true);
        if (match(NULL))
            new Expr.Literal(null);

        if (match(TYPEFLOAT, TYPEINT, TYPESTRING, TYPECHAR)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());    
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after the expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
    }

    private Token consume(TokenType type, String message) {
        if (check(type))
            return advance();

        throw error(peek(), message);
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd())
            return false;
        return peek().type == type;
    }

    private boolean checkNext(TokenType type) {
        if (isAtEnd())
            return false;
        return peekNext().type == type;
    }

    private Token advance() {
        if (!isAtEnd())
            current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token peekNext() {
        return tokens.get(current + 1);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParseError error(Token token, String message) {
        Code.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == CODE)
                return;

            switch (peek().type) {
                case INT:
                case TYPECHAR:
                case BOOL:
                case IF:
                case WHILE:
                case SCAN:
                case DISPLAY:
                case END:
                    return;
            }

            advance();
        }
    }
}
