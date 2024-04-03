package interpreter;

import java.util.List;

abstract class Stmt {
    interface Visitor<R> {
      R visitBlockStmt(Block stmt);
      R visitExpressionStmt(Expression stmt);
      R visitDisplayStmt(Display stmt);
      R visitScanStmt(Scan stmt);
      R visitIntStmt(Int stmt);
      R visitFloatStmt(Float stmt);
      R visitCharStmt(Char stmt);
      R visitStringStmt(String stmt);
      R visitBoolStmt(Bool stmt);
    }
    static class Block extends Stmt {
        Block(List<Stmt> statements) {
            this.statements = statements;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
        return visitor.visitBlockStmt(this);
        }

        final List<Stmt> statements;
    }
    static class Expression extends Stmt {
        Expression(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
        return visitor.visitExpressionStmt(this);
        }

        final Expr expression;
    }
    static class Display extends Stmt {
        Display(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
        return visitor.visitDisplayStmt(this);
        }

        final Expr expression;
    }
    static class Scan extends Stmt {
        Scan(Token name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
        return visitor.visitScanStmt(this);
        }

        final Token name;
        final Expr initializer;
    }
    static class Int extends Stmt {
        Int(Token name, Expr intializer) {
            this.name = name;
            this.intializer = intializer;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
        return visitor.visitIntStmt(this);
        }

        final Token name;
        final Expr intializer;
    }
    static class Float extends Stmt {
        Float(Token name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
        return visitor.visitFloatStmt(this);
        }

        final Token name;
        final Expr initializer;
    }
    static class Char extends Stmt {
        Char(Token name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
        return visitor.visitCharStmt(this);
        }

        final Token name;
        final Expr initializer;
    }
    static class String extends Stmt {
        String(Token name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
        return visitor.visitStringStmt(this);
        }

        final Token name;
        final Expr initializer;
    }
    static class Bool extends Stmt {
        Bool(Token name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
        return visitor.visitBoolStmt(this);
        }

        final Token name;
        final Expr initializer;
    }

    abstract <R> R accept(Visitor<R> visitor);
}
