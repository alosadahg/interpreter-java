package interpreter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import interpreter.Expr.Assign;
import interpreter.Expr.Binary;
import interpreter.Expr.Grouping;
import interpreter.Expr.Literal;
import interpreter.Expr.Logical;
import interpreter.Expr.Unary;
import interpreter.Expr.Variable;
import interpreter.Stmt.Block;
import interpreter.Stmt.Bool;
import interpreter.Stmt.Char;
import interpreter.Stmt.Display;
import interpreter.Stmt.Float;
import interpreter.Stmt.If;
import interpreter.Stmt.Int;
import interpreter.Stmt.Scan;

class Interpreter implements Expr.Visitor<Object>,
        Stmt.Visitor<Void>{

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
        // return environment.get(expr.name);
        Object value = environment.get(expr.name);
        // if (value == null) {
        //     throw new RuntimeError(expr.name, "Undefined variable '" + expr.name.lexeme + "'.");
        // }
        return value;
    }

    @Override
    public Void visitBoolStmt(Bool stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        String Tokentype = "Boolean";

        environment.define(stmt.name.lexeme, value, Tokentype);
        // System.out.println("var = " + stmt.initializer.accept(this));
        return null;
    }

    @Override
    public Void visitCharStmt(Char stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
            if (!(value instanceof Character)) {
                throw new RuntimeError(stmt.name, "Input must be a character");
            }
        }
        String Tokentype = "Character";

        environment.define(stmt.name.lexeme, value, Tokentype);
        // System.out.println("var = " + stmt.initializer.accept(this));
        return null;
    }

    @Override
    public Void visitFloatStmt(Float stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
            if (!(value instanceof Double)) {
                throw new RuntimeError(stmt.name, "Input must be a float");
            }
        }

        String Tokentype = "Float";

        environment.define(stmt.name.lexeme, value, Tokentype);
        // System.out.println("var = " + stmt.initializer.accept(this));
        return null;
    }

    @Override
    public Void visitIntStmt(Int stmt) {
        Object value = null;
        if (stmt.intializer != null) {
            value = evaluate(stmt.intializer);
            if (!(value instanceof Integer)) {
                throw new RuntimeError(stmt.name, "Input must be an Integer");
            }
        }

        String Tokentype = "Integer";

        environment.define(stmt.name.lexeme, value, Tokentype);
        //System.out.println("Declared variable: " + stmt.name.lexeme + " = " + value);
        // System.out.println("var = " + stmt.initializer.accept(this));
        return null;
    }

    @Override
    public Void visitStringStmt(interpreter.Stmt.String stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
            if (!(value instanceof String)) {
                throw new RuntimeError(stmt.name, "Input must be a String");
            }
        }

        String Tokentype = "String";

        environment.define(stmt.name.lexeme, value, Tokentype);
        // System.out.println("var = " + stmt.initializer.accept(this));
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
    public Void visitBlockStmt(Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Object visitAssignExpr(Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Void visitMultiVarStmt(Stmt.MultiVar stmt) {
        for (int i = 0; i < stmt.names.size(); i++) {
            Token name = stmt.names.get(i);
            Expr initializer = stmt.initializers.get(i);

            Object value = null;
            if (initializer != null) {
                value = evaluate(initializer);
            } else {
                value = null;
            }

            environment.assign(name, value);
        }
        return null;
    }

    @Override
    public Object visitBinaryExpr(Binary expr) {
        Object left = expr.left.accept(this);
        Object right = expr.right.accept(this);
        Number leftValue = getArithmetic(left);
        Number rightValue = getArithmetic(right);
        //System.out.println("Left Value: " + leftValue + ", Right Value: " + rightValue);
        switch (expr.operator.type) {
            case NEW_LINE:
                return (stringify(left) + "\n" + stringify(right));
            case CONCAT:
                return stringify(left) + stringify(right);
            case MINUS:
                if (leftValue instanceof Integer && rightValue instanceof Integer) {
                    return leftValue.intValue() - rightValue.intValue();
                }
                return leftValue.doubleValue() - rightValue.doubleValue();
            case PLUS:
                if (leftValue instanceof Integer && rightValue instanceof Integer) {
                    return leftValue.intValue() + rightValue.intValue();
                }
                return leftValue.doubleValue() + rightValue.doubleValue();
            case STAR:
                // System.out.println(rightValue instanceof Integer);
                if (leftValue instanceof Integer && rightValue instanceof Integer) {
                    return leftValue.intValue() * rightValue.intValue();
                }
                return leftValue.doubleValue() * rightValue.doubleValue();
            case SLASH:
                if (rightValue.doubleValue() == 0) {
                    throw new RuntimeError(expr.operator, "Division by zero.");
                }
                return leftValue.doubleValue() / rightValue.doubleValue();
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
            default:
                break;
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

    private Object scanInput() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        String line = reader.readLine().trim();

        try {
            Integer intValue = Integer.parseInt(line);
            System.out.println("scanInput : " + intValue);
            return intValue;
        } catch (NumberFormatException ignored) {
        }

        try {
            Double doubleValue = Double.parseDouble(line);
            System.out.println("scanInput : " + doubleValue);
            return doubleValue;
        } catch (NumberFormatException ignored) {
        }

        if (line.length() == 1) {
            char charValue = line.charAt(0);
            return charValue;
        }

        return line;
    }

    @Override
    public Void visitScanStmt(Scan stmt) {
        try {
            Object scannedValue = scanInput();
            String tokenType = environment.getTokenFromName(stmt.name.lexeme);

            // Check if tokenType == scannedValue Type
            // NOTE: Debug this please the token type recognized for boolean is character
            // Check the hashMap under Environment to debug
            // Problem: the saved token type in the hashmap for type BOOL is character
            if (tokenType.equals("Boolean") & (scannedValue.equals("TRUE") || scannedValue.equals("FALSE"))) {
                environment.assign(stmt.name, scannedValue);
                return null;
            }

            if (tokenType.equals(scannedValue.getClass().getSimpleName())) {
                environment.assign(stmt.name, scannedValue);
                return null;
            }

            throw new RuntimeError(stmt.name, "Input must be " + tokenType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Object visitLiteralExpr(Literal expr) {
        // System.out.println(expr.value.getClass().getName());
        if (expr.value.equals("\n")) {
            System.out.println();
        }
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(Logical expr) {
      Object left = evaluate(expr.left);
  
      if (expr.operator.type == TokenType.OR) {
        if (isTruthy(left)) return left;
      } else {
        if (!isTruthy(left)) return left;
      }
  
      return evaluate(expr.right);
    }
  

    @Override
    public Object visitUnaryExpr(Unary expr) {
        Object right = expr.right.accept(this);

        switch (expr.operator.type) {
            case MINUS:
                checkNumberOperand(expr.operator, right);
                if(right instanceof Integer) {
                    return -1 * (int) right;
                }
                return -1 * (double) right;
            case PLUS:
                if(right instanceof Integer) {
                    return (int) right;
                }
                checkNumberOperand(expr.operator, right);
                return (double) right;
            case NOT:
                return !isTruthy(right);
            case NEW_LINE:
                return "\n" + stringify(right);
            default:
                break;
        }

        return null;
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double || operand instanceof Integer)
            return;
        throw new RuntimeError(operator, "Operand must be a number.");
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

        if (object.toString().equals("new_line")) {
            System.out.println();
        }

        if (object instanceof Boolean) {
            return object.toString().toUpperCase();
        }

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

    @Override
    public Void visitIfStmt(If stmt) {
        if(isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if(stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }
}
