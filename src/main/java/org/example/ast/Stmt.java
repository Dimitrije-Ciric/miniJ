package org.example.ast;

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

}
