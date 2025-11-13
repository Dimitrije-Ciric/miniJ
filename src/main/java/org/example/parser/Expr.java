package org.example.parser;
import org.example.lexer.Token;

import java.util.List;

public abstract class Expr {
    public interface Visitor<R> {
        R visitBinary(Binary e);
        R visitUnary(Unary e);
        R visitGrouping(Grouping e);
        R visitLiteral(Literal e);
        R visitVar(Var e);
        R visitAssign(Assign e);
        R visitFuncCall(FuncCall e);
        R visitBlock(Block e);
        R visitIfElse(IfElse e);
        R visitWhile(While e);
        R visitFor(For e);
    }

    public static final class Binary extends Expr {
        public final Expr left; public final Token op; public final Expr right;
        public Binary(Expr left, Token op, Expr right) { this.left = left; this.op = op; this.right = right; }
        @Override public <R> R accept(Visitor<R> v) { return v.visitBinary(this); }
    }

    public static final class Unary extends Expr {
        public final Token op; public final Expr right;
        public Unary(Token op, Expr right) { this.op = op; this.right = right; }
        @Override public <R> R accept(Visitor<R> v) { return v.visitUnary(this); }
    }

    public static final class Grouping extends Expr {
        public final Expr expr;
        public Grouping(Expr expr) { this.expr = expr; }
        @Override public <R> R accept(Visitor<R> v) { return v.visitGrouping(this); }
    }

    public static final class Literal extends Expr {
        public final Object value;
        public Literal(Object value) { this.value = value; }
        @Override public <R> R accept(Visitor<R> v) { return v.visitLiteral(this); }
    }

    public static final class Var extends Expr {
        public final Token name;
        public Var(Token name) { this.name = name; }
        @Override public <R> R accept(Visitor<R> v) { return v.visitVar(this); }
    }

    public static final class Assign extends Expr {
        public final Token name;
        public final Expr value;
        public Assign(Token name, Expr value) { this.name = name; this.value = value; }
        @Override public <R> R accept(Visitor<R> v) { return v.visitAssign(this); }
    }

    public static final class FuncCall extends Expr {
        public final Token name;
        public final List<Expr> args;
        public FuncCall(Token name, List<Expr> args) { this.name = name; this.args = args; }
        @Override public <R> R accept(Visitor<R> v) { return v.visitFuncCall(this); }
    }

    public static final class Block extends Expr {
        public final List<Expr> statements;
        public Block(List<Expr> statements) { this.statements = statements; }
        @Override public <R> R accept(Visitor<R> v) { return v.visitBlock(this); }
    }

    public static final class IfElse extends Expr {
        public final Expr condition; public final Block thenBranch; public final Block elseBranch;
        public IfElse(Expr condition, Block thenBranch, Block elseBranch) {
            this.condition = condition; this.thenBranch = thenBranch; this.elseBranch = elseBranch;
        }
        @Override public <R> R accept(Visitor<R> v) { return v.visitIfElse(this); }
    }

    public static final class While extends Expr {
        public final Expr condition; public final Block body;
        public While(Expr condition, Block body) { this.condition = condition; this.body = body; }
        @Override public <R> R accept(Visitor<R> v) { return v.visitWhile(this); }
    }

    public static final class For extends Expr {
        public final Expr init; public final Expr condition; public final Expr increment; public final Block body;
        public For(Expr init, Expr condition, Expr increment, Block body) {
            this.init = init; this.condition = condition; this.increment = increment; this.body = body;
        }
        @Override public <R> R accept(Visitor<R> v) { return v.visitFor(this); }
    }

    public abstract <R> R accept(Visitor<R> v);
}
