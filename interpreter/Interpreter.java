package interpreter;

import java.math.BigDecimal;
import java.util.List;

import javax.management.RuntimeErrorException;

import interpreter.Expr.Binary;
import interpreter.Expr.Grouping;
import interpreter.Expr.Literal;
import interpreter.Expr.Unary;
import interpreter.Expr.Variable;
import interpreter.Stmt.Char;
import interpreter.Stmt.Display;
import interpreter.Stmt.Float;
import interpreter.Stmt.Int;

class Interpreter implements Expr.Visitor<Object>,
        Stmt.Visitor<Void> {

    private Environment environment = new Environment();

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Code.runtimeError(error);
        }
    }

    @Override
    public Object visitVariableExpr(Variable expr) {
        return environment.get(expr.name);
    }

    @Override
    public Void visitCharStmt(Char stmt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitFloatStmt(Float stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme, value);
        // System.out.println("var = " + stmt.initializer.accept(this));
        return null;
    }

    @Override
    public Void visitIntStmt(Int stmt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitStringStmt(interpreter.Stmt.String stmt) {
        // TODO Auto-generated method stub
        return null;
    }

    private Number getArithmetic(Object obj) {
        if (obj instanceof Integer) {
            return (int) obj;
        }
        if (obj instanceof Double) {
            return (double) obj;
        }
        return null;
    }

    @Override
    public Object visitBinaryExpr(Binary expr) {
        Object left = expr.left.accept(this);
        Object right = expr.right.accept(this);
        Number leftValue = getArithmetic(left);
        Number rightValue = getArithmetic(right);
        switch (expr.operator.type) {
            case CONCAT:
                return (stringify(left) + stringify(right));
            case MINUS:
                if (leftValue instanceof Integer && rightValue instanceof Integer) {
                    return leftValue.intValue() - rightValue.intValue();
                }
                return (BigDecimal.valueOf(leftValue.doubleValue())
                        .subtract(BigDecimal.valueOf(rightValue.doubleValue())));
            case PLUS:
                if (leftValue instanceof Integer && rightValue instanceof Integer) {
                    return leftValue.intValue() + rightValue.intValue();
                }
                return (BigDecimal.valueOf(leftValue.doubleValue()).add(BigDecimal.valueOf(rightValue.doubleValue())));
            case STAR:
                if (leftValue instanceof Integer && rightValue instanceof Integer) {
                    return leftValue.intValue() * rightValue.intValue();
                }
                return (BigDecimal.valueOf(leftValue.doubleValue())
                        .multiply(BigDecimal.valueOf(rightValue.doubleValue())));
            case SLASH:
                if (left instanceof Integer) {
                    left = Double.parseDouble(left.toString());
                }
                if (right instanceof Integer) {
                    right = Double.parseDouble(right.toString());
                }
                return (double) left / (double) right;
            case MODULO:
                if (leftValue instanceof Integer && rightValue instanceof Integer) {
                    return leftValue.intValue() % rightValue.intValue();
                }
                if (left instanceof Integer) {
                    left = Double.parseDouble(left.toString());
                }
                if (right instanceof Integer) {
                    right = Double.parseDouble(right.toString());
                }
                return (double) left % (double) right;
            case GREATER_THAN:
                if (left instanceof Integer) {
                    left = Double.parseDouble(left.toString());
                }
                if (right instanceof Integer) {
                    right = Double.parseDouble(right.toString());
                }
                return (double) left > (double) right;
            case GREATER_OR_EQUAL:
                if (left instanceof Integer) {
                    left = Double.parseDouble(left.toString());
                }
                if (right instanceof Integer) {
                    right = Double.parseDouble(right.toString());
                }
                return (double) left >= (double) right;
            case LESS_THAN:
                if (left instanceof Integer) {
                    left = Double.parseDouble(left.toString());
                }
                if (right instanceof Integer) {
                    right = Double.parseDouble(right.toString());
                }
                return (double) left < (double) right;
            case LESS_OR_EQUAL:
                if (left instanceof Integer) {
                    left = Double.parseDouble(left.toString());
                }
                if (right instanceof Integer) {
                    right = Double.parseDouble(right.toString());
                }
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
        // System.out.println(expr.value.getClass().getName());
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Unary expr) {
        Object right = expr.right.accept(this);

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
        System.out.println("a: " + a + " | b: " + b);
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
}
