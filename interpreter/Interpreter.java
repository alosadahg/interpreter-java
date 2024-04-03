package interpreter;

import java.util.List;

import javax.management.RuntimeErrorException;

import interpreter.Expr.Binary;
import interpreter.Expr.Grouping;
import interpreter.Expr.Literal;
import interpreter.Expr.Unary;
import interpreter.Stmt.Display;

class Interpreter implements Expr.Visitor<Object>,
        Stmt.Visitor<Void> {

    @Override
    public Object visitBinaryExpr(Binary expr) {
        Object left = expr.left;
        Object right = expr.right;
        switch (expr.operator.type) {
            case MINUS:
                // System.out.println("left: " + left + " | right: " + right.toString());
                checkNumberOperands(expr.operator, left, right);
                return (double) left - (double) right;
            case PLUS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left + (double) right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double) left * (double) right;
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double) left / (double) right;
            case MODULO:
                checkNumberOperands(expr.operator, left, right);
                return (double) left % (double) right;
            case GREATER_THAN:
                checkNumberOperands(expr.operator, left, right);
                return (double) left > (double) right;
            case GREATER_OR_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left >= (double) right;
            case LESS_THAN:
                checkNumberOperands(expr.operator, left, right);
                return (double) left < (double) right;
            case LESS_OR_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left <= (double) right;
            case NOT_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EVAL:
                return isEqual(left, right);
        }
        return null;
    }

    @Override
    public Object visitGroupingExpr(Grouping expr) {
        return evaluate(expr.expression);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitDisplayStmt(Display stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Object visitLiteralExpr(Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Unary expr) {
        Object right = expr.right;

        switch (expr.operator.type) {
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double) right;
            case PLUS:
                checkNumberOperand(expr.operator, right);
                return +(double) right;
            case NOT:
                return !isTruthy(right);
        }

        return null;
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double || operand instanceof Integer)
            return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double)
            return;
        if (left instanceof Integer && right instanceof Integer)
            return;
        System.out.println("Checking: " + left.getClass().getName() + "|" +
        right.getClass().getName());
        throw new RuntimeError(operator, "Operand must be numbers.");
    }

    private boolean isTruthy(Object object) {
        if (object == null)
            return false;
        if (object instanceof Boolean)
            return (boolean) object;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null)
            return true;
        if (a == null)
            return false;

        return a.equals(b);
    }

    private String stringify(Object object) {
        if (object == null)
            return "null";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Code.runtimeError(error);
        }
    }
}
