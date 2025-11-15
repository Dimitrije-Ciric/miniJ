package org.example.ast;

import lombok.Builder;
import org.example.lexer.Token;

import java.util.LinkedList;
import java.util.List;

public abstract class Expr implements VisitorAccept {

    public static final class LogicalOrExpr extends Expr {
        public List<Expr> andExprs = new LinkedList<>();

        public LogicalOrExpr(List<Expr> exprs) {
            super();
            this.andExprs = exprs;
        }

        @Override
        public <R> R accept(Visitor<R> v) {
            return v.visitLogicalOrExpr(this);
        }
    }

    public static final class LogicalAndExpr extends Expr {
        public List<Expr> equalityExprs = new LinkedList<>();

        public LogicalAndExpr(List<Expr> exprs) {
            super();
            this.equalityExprs = exprs;
        }

        @Override
        public <R> R accept(Visitor<R> v) {
            return v.visitLogicalAndExpr(this);
        }
    }

    public static final class EqualityExpr extends Expr {
        public Expr left, right;
        public Token op;

        public EqualityExpr(Expr left, Token op, Expr right) {
            super();
            this.left = left;
            this.right = right;
            this.op = op;
        }

        @Override
        public <R> R accept(Visitor<R> v) {
            return v.visitEqualityExpr(this);
        }
    }

    public static final class RelationalExpr extends Expr {
        public Expr left, right;
        public Token op;

        public RelationalExpr(Expr left, Token op, Expr right) {
            super();
            this.left = left;
            this.right = right;
            this.op = op;
        }

        @Override
        public <R> R accept(Visitor<R> v) {
            return v.visitRelationalExpr(this);
        }
    }

    public static final class AdditiveExpr extends Expr {
        public List<Expr> multiplicativeExprs = new LinkedList<>();
        public List<Token> ops = new LinkedList<>();

        public AdditiveExpr(List<Expr> exprs, List<Token> ops) {
            super();
            this.multiplicativeExprs = exprs;
            this.ops = ops;
        }

        @Override
        public <R> R accept(Visitor<R> v) {
            return v.visitAdditiveExpr(this);
        }
    }

    public static final class MultiplicativeExpr extends Expr {
        public List<Expr> unaryExprs = new LinkedList<>();
        public List<Token> ops = new LinkedList<>();

        public MultiplicativeExpr(List<Expr> exprs, List<Token> ops) {
            super();
            this.unaryExprs = exprs;
            this.ops = ops;
        }

        @Override
        public <R> R accept(Visitor<R> v) {
            return v.visitMultiplicativeExpr(this);
        }
    }

    @Builder
    public static final class UnaryExpr extends Expr {
        public Token unaryOp;
        public Expr expr;

        public UnaryExpr(Token unaryOp, Expr expr) {
            this.unaryOp = unaryOp;
            this.expr = expr;
        }

        @Override
        public <R> R accept(Visitor<R> v) {
            return v.visitUnaryExpr(this);
        }
    }

    public static final class TermExpr extends Expr {

        public Token term;

        public TermExpr(Token term) {
            super();
            this.term = term;
        }

        @Override
        public <R> R accept(Visitor<R> v) {
            return v.visitTermExpr(this);
        }
    }

    public static final class GroupExpr extends Expr {

        public Expr expr;

        public GroupExpr(Expr expr) {
            super();
            this.expr = expr;
        }

        @Override
        public <R> R accept(Visitor<R> v) {
            return v.visitGroupExpr(this);
        }
    }

    public static final class FunctionalCall extends Expr {
        public Token ident;
        public List<Expr> args = new LinkedList<>();

        public FunctionalCall(Token ident, List<Expr> args) {
            this.ident = ident;
            this.args = args;
        }

        @Override
        public <R> R accept(Visitor<R> v) {
            return v.visitFunctionalCall(this);
        }
    }

}
