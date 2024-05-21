package interpreter;
//package interpreter;
//
//import interpreter.Expr.Binary;
//import interpreter.Expr.Grouping;
//import interpreter.Expr.Literal;
//import interpreter.Expr.Unary;
//
//public class AstPrinter implements Expr.Visitor<String>{
//    String print(Expr expr) {
//        return expr.accept(this);
//    }
//
//    @Override
//    public String visitBinaryExpr(Binary expr) {
//        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
//    }
//
//    @Override
//    public String visitGroupingExpr(Grouping expr) {
//        return parenthesize("group", expr.expression);
//    }
//
//    @Override
//    public String visitLiteralExpr(Literal expr) {
//        if(expr.value == null) return "null";
//        return expr.value.toString();
//    }
//
//    @Override
//    public String visitUnaryExpr(Unary expr) {
//        return parenthesize(expr.operator.lexeme, expr.right);
//    }
//
//    private String parenthesize(String name, Expr...exprs) {
//        StringBuilder builder = new StringBuilder();
//
//        builder.append("(").append(name);
//        for(Expr expr: exprs) {
//            builder.append(" ");
//            builder.append(expr.accept(this));
//        }
//        builder.append(")");
//
//        return builder.toString();
//    }
//}
