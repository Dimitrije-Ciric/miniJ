package org.example.ast;

public interface Visitor<R> {
    R visitProgram(Program program);

    R visitExprStmt(Stmt.ExprStmt exprStmt);

    R visitLogicalOrExpr(Expr.LogicalOrExpr logicalOrExpr);
    R visitLogicalAndExpr(Expr.LogicalAndExpr logicalAndExpr);
    R visitEqualityExpr(Expr.EqualityExpr equalityExpr);
    R visitRelationalExpr(Expr.RelationalExpr relationalExpr);
    R visitAdditiveExpr(Expr.AdditiveExpr additiveExpr);
    R visitMultiplicativeExpr(Expr.MultiplicativeExpr multiplicativeExpr);
    R visitUnaryExpr(Expr.UnaryExpr unaryExpr);
    R visitTermExpr(Expr.TermExpr termExpr);
    R visitGroupExpr(Expr.GroupExpr group);
    R visitFunctionalCall(Expr.FunctionalCall functionalCall);

    R visitVarDecl(Stmt.VarDecl varDecl);

    R visitIfStmt(Stmt.IfStmt ifStmt);

    R visitWhileStmt(Stmt.WhileStmt whileStmt);

    R visitForStmt(Stmt.ForStmt forStmt);

    R visitReturnStmt(Stmt.ReturnStmt returnStmt);

    R visitFuncDeclStmt(Stmt.FuncDecl funcDecl);

    R visitVarAssign(Stmt.VarAssign varAssign);
}