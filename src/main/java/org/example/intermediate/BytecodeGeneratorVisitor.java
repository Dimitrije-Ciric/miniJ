package org.example.intermediate;

import lombok.AllArgsConstructor;
import org.example.ast.Expr;
import org.example.ast.Program;
import org.example.ast.Stmt;
import org.example.ast.Visitor;
import org.example.lexer.TokenType;

@AllArgsConstructor
public class BytecodeGeneratorVisitor implements Visitor<Void> {

    private BytecodeClassGenerator codeGen;

    @Override
    public Void visitProgram(Program program) {
        codeGen.startMain();

        for (var stmt : program.stmts)
            stmt.accept(this);

        codeGen.endMain();

        return null;
    }

    @Override
    public Void visitExprStmt(Stmt.ExprStmt exprStmt) {
        exprStmt.expr.accept(this);
        codeGen.stackPop();
        return null;
    }

    @Override
    public Void visitLogicalOrExpr(Expr.LogicalOrExpr logicalOrExpr) {
        var firstExpr = logicalOrExpr.andExprs.getFirst();
        firstExpr.accept(this);

        for (int i = 1; i < logicalOrExpr.andExprs.size(); i++) {
            logicalOrExpr.andExprs.get(i).accept(this);

            codeGen.or();
        }

        return null;
    }

    @Override
    public Void visitLogicalAndExpr(Expr.LogicalAndExpr logicalAndExpr) {
        var firstExpr = logicalAndExpr.equalityExprs.getFirst();
        firstExpr.accept(this);

        for (int i = 1; i < logicalAndExpr.equalityExprs.size(); i++) {
            logicalAndExpr.equalityExprs.get(i).accept(this);

            codeGen.and();
        }

        return null;
    }

    @Override
    public Void visitEqualityExpr(Expr.EqualityExpr equalityExpr) {

        equalityExpr.left.accept(this);
        equalityExpr.right.accept(this);

        if (equalityExpr.op.type() == TokenType.EQ) {
            codeGen.compareEQ();
        } else if (equalityExpr.op.type() == TokenType.NEQ) {
            codeGen.compareNEQ();
        }

        return null;
    }

    @Override
    public Void visitRelationalExpr(Expr.RelationalExpr relationalExpr) {

        relationalExpr.left.accept(this);
        relationalExpr.right.accept(this);

        if (relationalExpr.op.type() == TokenType.GT) {
            codeGen.compareGT();
        } else if (relationalExpr.op.type() == TokenType.LT) {
            codeGen.compareLT();
        } else if (relationalExpr.op.type() == TokenType.GE) {
            codeGen.compareGE();
        } else if (relationalExpr.op.type() == TokenType.LE) {
            codeGen.compareLE();
        }

        return null;
    }

    @Override
    public Void visitAdditiveExpr(Expr.AdditiveExpr additiveExpr) {
        var firstExpr = additiveExpr.multiplicativeExprs.getFirst();
        firstExpr.accept(this);

        for (int i = 1; i < additiveExpr.multiplicativeExprs.size(); i++) {
            additiveExpr.multiplicativeExprs.get(i).accept(this);

            if (additiveExpr.ops.get(i-1).type() == TokenType.PLUS)
                codeGen.addition();
            else codeGen.subtraction();
        }

        return null;
    }

    @Override
    public Void visitMultiplicativeExpr(Expr.MultiplicativeExpr multiplicativeExpr) {

        var firstExpr = multiplicativeExpr.unaryExprs.getFirst();
        firstExpr.accept(this);

        for (int i = 1; i < multiplicativeExpr.unaryExprs.size(); i++) {
            multiplicativeExpr.unaryExprs.get(i).accept(this);

            if (multiplicativeExpr.ops.get(i-1).type() == TokenType.MULTIPLY)
                codeGen.multiply();
            else if (multiplicativeExpr.ops.get(i-1).type() == TokenType.DIVIDE)
                codeGen.divide();
            else codeGen.mod();
        }

        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.UnaryExpr unaryExpr) {
        unaryExpr.expr.accept(this);

        if (unaryExpr.unaryOp.type() == TokenType.MINUS)
            codeGen.stackPeekMultiplyByMinusOne();
        if (unaryExpr.unaryOp.type() == TokenType.NOT)
            codeGen.stackPeekNegate();

        return null;
    }

    @Override
    public Void visitTermExpr(Expr.TermExpr termExpr) {
        if (termExpr.arrayIndex != null)
            codeGen.stackPush(termExpr.arrayIndex);
        codeGen.stackPush(termExpr.term);
        return null;
    }

    @Override
    public Void visitGroupExpr(Expr.GroupExpr group) {
        group.expr.accept(this);
        return null;
    }

    @Override
    public Void visitFunctionalCall(Expr.FunctionalCall functionalCall) {
        codeGen.prepareFunctionCall(functionalCall.ident.lexeme());

        for (var arg : functionalCall.args)
            arg.accept(this);

        codeGen.callFunction(functionalCall.ident.lexeme());

        return null;
    }

    @Override
    public Void visitVarDecl(Stmt.VarDecl varDecl) {
        codeGen.declareVariable(varDecl.name.lexeme(), varDecl.type.type(), varDecl.type.type() == TokenType.ARRAY ? (Integer) varDecl.arrayLength.literal() : null);

        if (varDecl.init != null) {
            varDecl.init.accept(this);
            codeGen.initVariable(varDecl.name.lexeme());
        }

        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.IfStmt ifStmt) {
        ifStmt.condition.accept(this);

        codeGen.ifBranch();
        ifStmt.ifBranch.forEach(branch -> branch.accept(this));
        codeGen.elseBranch();

        for (var elseIf : ifStmt.elseIfBranch) {
            elseIf.condition.accept(this);

            codeGen.ifBranch();
            elseIf.body.forEach(branch -> branch.accept(this));
            codeGen.elseBranch();
        }

        ifStmt.elseBranch.forEach(branch -> branch.accept(this));
        codeGen.endIf(1 + ifStmt.elseIfBranch.size());

        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.WhileStmt whileStmt) {
        codeGen.whileCondition();

        whileStmt.condition.accept(this);

        codeGen.whileLoop();

        whileStmt.body.forEach(branch -> branch.accept(this));

        codeGen.endWhile();

        return null;
    }

    @Override
    public Void visitForStmt(Stmt.ForStmt forStmt) {
        codeGen.forInit();
        forStmt.init.accept(this);

        codeGen.forCondition();
        forStmt.condition.accept(this);

        codeGen.forLoop();
        forStmt.body.forEach(branch -> branch.accept(this));

        forStmt.increment.accept(this);
        codeGen.endFor();

        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.ReturnStmt returnStmt) {
        returnStmt.expr.accept(this);
        codeGen.returnStmt();
        return null;
    }

    @Override
    public Void visitFuncDeclStmt(Stmt.FuncDecl funcDecl) {
        codeGen.declareFunc(funcDecl.name.lexeme(), funcDecl.params, funcDecl.type.type());

        funcDecl.body.forEach(branch -> branch.accept(this));

        codeGen.endFunc();

        return null;
    }

    @Override
    public Void visitVarAssign(Stmt.VarAssign varAssign) {
        varAssign.value.accept(this);

        codeGen.varAssign(varAssign.name.lexeme(), varAssign.arrayIndex);

        return null;
    }
}
