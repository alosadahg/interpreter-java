package interpreter;

import javax.management.RuntimeErrorException;

import interpreter.Expr.Binary;
import interpreter.Expr.Grouping;
import interpreter.Expr.Literal;
import interpreter.Expr.Unary;

public class Interpreter implements Expr.Visitor<Object> {

    @Override
    public Object visitBinaryExpr(Binary expr) {
        Object left = expr.left;
        Object right = expr.right;
        switch (expr.operator.type) {
            case MINUS:
            System.out.println("left: " + left + " | right: " + right.toString());
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
            System.out.println("Checking: " + operand.getClass().getName());
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        if (left instanceof Integer && right instanceof Integer) return;
        System.out.println("Checking: " + left.getClass().getName() + "|" + right.getClass().getName());
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
        if(object == null) return "null";

        if(object instanceof Double) {
            String text = object.toString();
            if(text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

    void interpret(Expr expression) {
        try {
            Object value = evaluate(expression);
            System.out.println(stringify(value));
        } catch (RuntimeError error) {
            Code.runtimeError(error);
        }
    }
}
