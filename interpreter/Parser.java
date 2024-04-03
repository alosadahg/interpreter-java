package interpreter;

import static interpreter.TokenType.*;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.CheckedInputStream;

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
            statements.add(statement());
            //statements.add(declaration());
        }

        return statements;
    }

    private Expr expression() {
        return equality();
    }

    // private Stmt declaration() {
    //     try {
    //         if(match(INT)) return varDeclaration();

    //         return statement();
    //     } catch(ParseError error) {
    //         synchronize();
    //         return null;
    //     }
    // }

    private Stmt statement() {
        if (match(DISPLAY) && match(COLON)) return displayStatement();
    
        return expressionStatement();
      }

      private Stmt displayStatement() {
        Expr value = expression();
        return new Stmt.Display(value);
      }

      private Stmt expressionStatement() {
        Expr expr = expression();
        return new Stmt.Expression(expr);
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

        while (match(MINUS, PLUS, CONCAT, NEW_LINE)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(STAR, SLASH, MODULO, NEW_LINE)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr unary() {
        if (match(NOT, MINUS, PLUS, NEW_LINE)) {
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

//        if (match(TYPEFLOAT, TYPEINT, TYPESTRING, TYPECHAR)) {
//            return new Expr.Literal(previous().getLiteral());
//        }

        if (match(TYPEFLOAT, TYPEINT, TYPESTRING, TYPECHAR)) {
            Token objectToken = previous();
            if (check(NEW_LINE) && !isAtEnd()) {
                advance();
                if (!isAtEnd()) {
                    Token nextToken = peek();
                    return new Expr.Binary(new Expr.Literal(objectToken.getLiteral()), new Token(NEW_LINE, null, "\n", -1), primary());
                } else {
                    System.out.print(objectToken.getLiteral());
                    return new Expr.Literal(new Token(NEW_LINE, null, null, -1));
                }
            } else {
                return new Expr.Literal(objectToken.getLiteral());
            }
        }

        if(previous().type.equals(NEW_LINE)){
            return new Expr.Literal("");
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
