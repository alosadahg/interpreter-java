package interpreter;

abstract class Stmt {
    interface Visitor<h> {
      h visitExpressionStmt(Expression stmt);
      h visitDisplayStmt(Display stmt);
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

    abstract <R> R accept(Visitor<R> visitor);
}
