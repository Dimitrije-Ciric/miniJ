package org.example.parser;

public class AstPrinter implements Expr.Visitor<String> {
    public String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitLiteral(Expr.Literal e) {
        if (e.value == null) return "nil";
        if (e.value instanceof String s) return "\"" + s + "\"";
        if (e.value instanceof Character c) return "'" + c + "'";
        return e.value.toString();
    }

    @Override
    public String visitVar(Expr.Var e) {
        return e.name.lexeme();
    }

    @Override
    public String visitAssign(Expr.Assign e) {
        return "(" + e.name.lexeme() + " = " + e.value.accept(this) + ")";
    }

    @Override
    public String visitFuncCall(Expr.FuncCall e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.name.lexeme()).append("(");
        for (int i = 0; i < e.args.size(); i++) {
            sb.append(e.args.get(i).accept(this));
            if (i < e.args.size() - 1) sb.append(", ");
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String visitBlock(Expr.Block e) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        for (Expr stmt : e.statements) {
            sb.append("  ").append(stmt.accept(this)).append("\n");
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String visitIfElse(Expr.IfElse e) {
        String result = "(if " + e.condition.accept(this) + " " + e.thenBranch.accept(this);
        if (e.elseBranch != null) {
            result += " else " + e.elseBranch.accept(this);
        }
        result += ")";
        return result;
    }

    @Override
    public String visitWhile(Expr.While e) {
        return "(while " + e.condition.accept(this) + " " + e.body.accept(this) + ")";
    }

    @Override
    public String visitFor(Expr.For e) {
        return "(for " + e.init.accept(this) + "; " + e.condition.accept(this) +
                "; " + e.increment.accept(this) + " " + e.body.accept(this) + ")";
    }

    @Override
    public String visitGrouping(Expr.Grouping e) {
        return "(group " + e.expr.accept(this) + ")";
    }

    @Override
    public String visitUnary(Expr.Unary e) {
        return "(" + e.op.lexeme() + " " + e.right.accept(this) + ")";
    }

    @Override
    public String visitBinary(Expr.Binary e) {
        return "(" + e.op.lexeme() + " " + e.left.accept(this) + " " + e.right.accept(this) + ")";
    }
}
