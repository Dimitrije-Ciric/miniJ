package org.example.evaulator.Evaluator;

import org.example.parser.Expr;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Evaluator implements Expr.Visitor<Object>{
    private final Map<String, Object> env = new HashMap<>();
    private final Scanner sc = new Scanner(System.in);

    public Object eval(Expr expr) { return expr.accept(this); }

    @Override
    public Object visitLiteral(Expr.Literal e) { return e.value; }

    @Override
    public Object visitVar(Expr.Var e) {
        if (!env.containsKey(e.name.lexeme())) throw new RuntimeException("Undefined variable: " + e.name.lexeme());
        return env.get(e.name.lexeme());
    }

    @Override
    public Object visitAssign(Expr.Assign e) {
        Object val = e.value.accept(this);
        env.put(e.name.lexeme(), val);
        return val;
    }

    @Override
    public Object visitUnary(Expr.Unary e) {
        Object r = e.right.accept(this);
        switch (e.op.type()) {
            case MINUS -> { return -(Double)r; }
            case NOT -> { return !(Boolean)r; }
            default -> throw new RuntimeException("Unknown unary op: " + e.op.lexeme());
        }
    }

    @Override
    public Object visitBinary(Expr.Binary e) {
        Object l = e.left.accept(this);
        Object r = e.right.accept(this);

        switch (e.op.type()) {
            case PLUS: return (Double)l + (Double)r;
            case MINUS: return (Double)l - (Double)r;
            case MULTIPLY: return (Double)l * (Double)r;
            case DIVIDE: if ((Double)r == 0) throw new ArithmeticException("Division by zero"); return (Double)l / (Double)r;
            case CARET: return Math.pow((Double)l, (Double)r);
            case EQ: return l.equals(r);
            case NEQ: return !l.equals(r);
            case LT: return ((Double)l) < ((Double)r);
            case LE: return ((Double)l) <= ((Double)r);
            case GT: return ((Double)l) > ((Double)r);
            case GE: return ((Double)l) >= ((Double)r);
            case AND: return (Boolean)l && (Boolean)r;
            case OR: return (Boolean)l || (Boolean)r;
            default: throw new RuntimeException("Unknown binary op: " + e.op.lexeme());
        }
    }

    @Override
    public Object visitGrouping(Expr.Grouping e) { return e.expr.accept(this); }

    @Override
    public Object visitFuncCall(Expr.FuncCall e) {
        switch (e.name.lexeme()) {
            case "print" -> {
                for (Expr arg : e.args) System.out.print(arg.accept(this) + " ");
                System.out.println();
                return null;
            }
            case "read" -> {
                return sc.nextLine();
            }
            default -> throw new RuntimeException("Unknown function: " + e.name.lexeme());
        }
    }

    @Override
    public Object visitBlock(Expr.Block e) {
        Object result = null;
        for (Expr stmt : e.statements) result = stmt.accept(this);
        return result;
    }

    @Override
    public Object visitIfElse(Expr.IfElse e) {
        Boolean cond = (Boolean) e.condition.accept(this);
        if (cond) return e.thenBranch.accept(this);
        else if (e.elseBranch != null) return e.elseBranch.accept(this);
        return null;
    }

    @Override
    public Object visitWhile(Expr.While e) {
        Object result = null;
        while ((Boolean)e.condition.accept(this)) {
            result = e.body.accept(this);
        }
        return result;
    }

    @Override
    public Object visitFor(Expr.For e) {
        Object result = null;
        for (e.init.accept(this); (Boolean)e.condition.accept(this); e.increment.accept(this)) {
            result = e.body.accept(this);
        }
        return result;
    }
}
