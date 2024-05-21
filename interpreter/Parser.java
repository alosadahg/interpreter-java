package interpreter;

import static interpreter.TokenType.*;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class Parser {
    private static class ParseError extends RuntimeException {
    }

    /*
     * Added symbol table, helps with error printing undefined variable even though
     * variable is null, check
     * varDeclaration for more information about the declaration
     * Error again with the symbol table cannot take in the literal value
     * can't think anymore will come back if error is still not resolved -Idul
     */
    private final List<Token> tokens;
    private Map<String, Object> symbolTable = new HashMap<>();
    private int current = 0;
    private Boolean variableDeclarationStarted = false;
    private Boolean executableStarted = false;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            // statements.add(statement());
            statements.addAll(declaration());
        }

        return statements;
    }

    private Expr expression() {
        // return equality();
        return assignment();
    }

    private Expr assignment() {
        Expr expr = equality();

        if (match(ASSIGN)) {
            Token assign = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            }

            error(assign, "Invalid assignment target.");
        }
        return expr;
    }

    // private Stmt declaration() {
    // try {
    // if (match(INT))
    // return varDeclaration("INT");
    // if (match(FLOAT))
    // return varDeclaration("FLOAT");
    // if (match(CHAR))
    // return varDeclaration("CHAR");
    // if (match(STRING))
    // return varDeclaration("STRING");
    // if (match(BOOL))
    // return varDeclaration("BOOL");

    // return statement();
    // } catch (ParseError error) {
    // synchronize();
    // return null;
    // }
    // }

    private List<Stmt> declaration() {
        List<Stmt> stmts = new ArrayList<>();

        try {
            if (match(INT)) {
                stmts.addAll(varDeclaration("INT"));
                return stmts;
            }
            if (match(FLOAT)) {
                stmts.addAll(varDeclaration("FLOAT"));
                return stmts;
            }
            if (match(CHAR)) {
                stmts.addAll(varDeclaration("CHAR"));
                return stmts;
            }
            if (match(STRING)) {
                stmts.addAll(varDeclaration("STRING"));
                return stmts;
            }
            if (match(BOOL)) {
                stmts.addAll(varDeclaration("BOOL"));
                return stmts;
            }

            stmts.add(statement());
        } catch (ParseError error) {
            synchronize();
        }

        return stmts;
    }

    private Stmt statement() {
        if (match(DISPLAY) && match(COLON))
            return displayStatement();
        if (match(BEGIN) && match(CODE)) {
            return new Stmt.Block(block());
        }
        if (match(SCAN) && match(COLON))
            return scanStatement();

        return expressionStatement();
    }

    private Stmt scanStatement() {
        Token variable = consume(IDENTIFIER, "Expect variable name after SCAN:");
        return new Stmt.Scan(variable, null);
    }

    // please help me resolve this error
    /*> INT a, b=100
        1:INT
        Initializer value: null
        Initializer value: interpreter.Expr$Literal@31befd9f
        [interpreter.Stmt$Int@1c20c684, interpreter.Stmt$Int@1fb3ebeb]
        Declared variable: a = null
        Declared variable: b = 100
        > DISPLAY: a 
        interpreter.Expr$Variable@6d311334
        Undefined variable 'a'.
        [line 0]
        > INT a
        1:INT
        Initializer value: null
        [interpreter.Stmt$Int@5f184fc6]
        Declared variable: a = null
        > DISPLAY: a
        interpreter.Expr$Variable@3feba861
        Undefined variable 'a'.
        [line 0]
     */
    private Stmt displayStatement() {
        Expr value = expression();
        return new Stmt.Display(value);
    }

    private List<Stmt> varDeclaration(String type) {
        System.out.println("1:" + type);
        List<Token> names = new ArrayList<>();
        List<Expr> initializers = new ArrayList<>();

        TokenType tokenType;
        switch (type) {
            case "FLOAT":
                tokenType = TokenType.FLOAT;
                break;
            case "INT":
                tokenType = TokenType.INT;
                break;
            case "STRING":
                tokenType = TokenType.STRING;
                break;
            case "CHAR":
                tokenType = TokenType.CHAR;
                break;
            case "BOOL":
                tokenType = TokenType.BOOL;
                break;
            default:
                System.out.println("Unidentifiable variable type");
                return null;
        }

        do {
            Token name = consume(IDENTIFIER, "Expect variable name.");

            Expr initializer = null;
            if (match(ASSIGN)) {
                initializer = expression();
            }

            names.add(name);
            initializers.add(initializer);

        } while (match(COMMA));

        // System.out.println("3:" + type);
        List<Stmt> stmts = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            Token name = names.get(i);
            Expr initializer = initializers.get(i);

            // // Add the variable and its initializer to the symbol table
            // System.out.println("Initializer value: " + initializer);
            symbolTable.put(name.lexeme, initializer);

            switch (tokenType) {
                case FLOAT:
                    variableDeclarationStarted = true;
                    stmts.add(new Stmt.Float(name, initializer));
                    break;
                case INT:
                    variableDeclarationStarted = true;
                    stmts.add(new Stmt.Int(name, initializer));
                    break;
                case STRING:
                    variableDeclarationStarted = true;
                    stmts.add(new Stmt.String(name, initializer));
                    break;
                case CHAR:
                    variableDeclarationStarted = true;
                    stmts.add(new Stmt.Char(name, initializer));
                    break;
                case BOOL:
                    variableDeclarationStarted = true;
                    stmts.add(new Stmt.Bool(name, initializer));
                    break;
                default:
                    System.out.println("Na default ka boss");
                    stmts.add(new Stmt.MultiVar(null, names, initializers));
                    break;
            }
        }
        System.out.println(stmts);
        return stmts;
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        return new Stmt.Expression(expr);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(END) && !checkNext(CODE) && !isAtEnd()) {
            statements.addAll(declaration());
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
            executableStarted = true;
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER_THAN, GREATER_OR_EQUAL, LESS_THAN, LESS_OR_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            executableStarted = true;
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(MINUS, PLUS, CONCAT, NEW_LINE)) {
            Token operator = previous();
            Expr right = unary();
            executableStarted = true;
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(STAR, SLASH, MODULO, NEW_LINE)) {
            Token operator = previous();
            Expr right = unary();
            executableStarted = true;
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

        if (match(TYPEFLOAT, TYPEINT, TYPESTRING, TYPECHAR, ESCAPECHAR)) {
            Token objectToken = previous();
            if (check(NEW_LINE) && !isAtEnd()) {
                advance();
                if (!isAtEnd()) {
                    Token nextToken = peek();
                    return new Expr.Binary(new Expr.Literal(objectToken.getLiteral()),
                            new Token(NEW_LINE, null, "\n", -1), primary());
                } else {
                    System.out.print(objectToken.getLiteral());
                    return new Expr.Literal(new Token(NEW_LINE, null, null, -1));
                }
            } else {
                return new Expr.Literal(objectToken.getLiteral());
            }
        }

        if (previous().type.equals(NEW_LINE)) {
            return new Expr.Literal("");
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
