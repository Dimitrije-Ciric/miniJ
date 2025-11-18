package org.example.ast;

import org.example.lexer.Token;

import java.util.LinkedList;
import java.util.List;

public abstract class Stmt implements VisitorAccept {

    public static class ExprStmt extends Stmt {

        public Expr expr;

        public ExprStmt(Expr expr) {
            super();
            this.expr = expr;
        }

        @Override
        public <R> R accept(Visitor<R> v) {
            return v.visitExprStmt(this);
        }
    }

    public static class VarDecl extends Stmt {
        public Token type, arrayLength, name;
        public Expr init;

        public VarDecl(Token type, Token arrayLength, Token name, Expr init) {
            super();
            this.type = type;
            this.arrayLength = arrayLength;
            this.name = name;
            this.init = init;
        }

        @Override
        public <R> R accept(Visitor<R> v) {
            return v.visitVarDecl(this);
        }
    }

    public static class VarAssign extends Stmt {
        public Token name;
        public Token arrayIndex;
        public Expr value;

        public VarAssign(Token name, Token arrayIndex, Expr value) {
            super();

            this.name = name;
            this.arrayIndex = arrayIndex;
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> v) {
            return v.visitVarAssign(this);
        }
    }

    public static class IfStmt extends Stmt {
        public Expr condition;
        public List<Stmt> ifBranch;
        public List<ElseIfStmt> elseIfBranch;
        public List<Stmt> elseBranch;

        public IfStmt(Expr condition, List<Stmt> ifBranch, List<ElseIfStmt> elseIfBranch, List<Stmt> elseBranch) {
            super();
            this.condition = condition;
            this.ifBranch = ifBranch != null ? ifBranch : new LinkedList<>();
            this.elseIfBranch = elseIfBranch != null ? elseIfBranch : new LinkedList<>();
            this.elseBranch = elseBranch != null ? elseBranch : new LinkedList<>();
        }

        @Override
        public <R> R accept(Visitor<R> v) {
            return v.visitIfStmt(this);
        }
    }
    public static class ElseIfStmt {
        public Expr condition;
        public List<Stmt> body;
        public ElseIfStmt(Expr condition, List<Stmt> body) { this.condition = condition; this.body = body; }
    }

    public static class WhileStmt extends Stmt {
        public Expr condition;
        public List<Stmt> body;

        public WhileStmt(Expr condition, List<Stmt> body) {
            this.condition = condition;
            this.body = body;
        }

        @Override
        public <R> R accept(Visitor<R> v) {
            return v.visitWhileStmt(this);
        }
    }

    public static class ForStmt extends Stmt {
        public Stmt init;
        public Expr condition;
        public Stmt increment;
        public List<Stmt> body;

        public ForStmt(Stmt init, Expr condition, Stmt increment, List<Stmt> body) {
            this.init = init;
            this.condition = condition;
            this.increment = increment;
            this.body = body;
        }

        @Override
        public <R> R accept(Visitor<R> v) {
            return v.visitForStmt(this);
        }
    }

    public static class ReturnStmt extends Stmt {
        public Expr expr;

        public Expr extr() {
            return expr;
        }
        public ReturnStmt(Expr expr) {
            this.expr = expr;
        }

        @Override
        public <R> R accept(Visitor<R> v) {
            return v.visitReturnStmt(this);
        }
    }

    public static class Param {
        public Token type;
        public Token name;

        public Param(Token type, Token name) {
            this.type = type;
            this.name = name;
        }
    }

    public static class FuncDecl extends Stmt {
        public Token type, name;
        public List<Param> params;
        public List<Stmt> body;

        public FuncDecl(Token type, Token name, List<Param> params, List<Stmt> body) {
            this.type = type;
            this.name = name;
            this.params = params;
            this.body = body;
        }

        @Override
        public <R> R accept(Visitor<R> v) {
            return v.visitFuncDeclStmt(this);
        }
    }
}