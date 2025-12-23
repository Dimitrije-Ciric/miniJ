package org.example.semantic;

import org.example.ast.Expr;
import org.example.ast.Program;
import org.example.ast.Stmt;
import org.example.ast.Visitor;
import org.example.lexer.Token;

import java.util.ArrayList;
import java.util.List;


public class SematicAnalyzer implements Visitor<Type> {

    private Scope currentScope = new Scope(null);
    private Type currentFunctionReturnType = null;
    private final List<String> analysisLog = new ArrayList<>();
    private void error(String msg, Token t) {
        throw new RuntimeException("Sematic error: " + msg + "as line " + t.line() + ", col " + t.colStart());
    }
    private void log(String msg) {
        analysisLog.add(msg);
    }
    public void printAnalysis() {
        for (String entry : analysisLog) {
            System.out.println(entry);
        }
    }
    private Type mapType(Token typeToken) {
        return switch (typeToken.type()) {
            case INT -> Type.INT;
            case DOUBLE -> Type.DOUBLE;
            case BOOL -> Type.BOOL;
            case CHAR -> Type.CHAR;
            case STRING -> Type.STRING;
            default -> Type.ERROR;
        };
    }

    private void enterScope() {
        currentScope = new Scope(currentScope);
        log("Entering new scope");
    }

    private void exitScope() {
        currentScope = currentScope.parent();
        log("Exiting scope");
    }
    @Override
    public Type visitProgram(Program program) {
        for(Stmt stmt: program.stmts) {
            stmt.accept(this);
        }

        return Type.VOID;
    }

    @Override
    public Type visitExprStmt(Stmt.ExprStmt exprStmt) {
        exprStmt.expr.accept(this);
        return Type.VOID;
    }

    @Override
    public Type visitLogicalOrExpr(Expr.LogicalOrExpr logicalOrExpr) {
        for (Expr ex : logicalOrExpr.andExprs) {
            if (ex.accept(this) != Type.BOOL) {
                error("Logical OR requires boolean operands", null);
            }
        }
        log("LogicalOrExpr evaluated as BOOL");
        return Type.BOOL;
    }

    @Override
    public Type visitLogicalAndExpr(Expr.LogicalAndExpr logicalAndExpr) {
        for(Expr ex: logicalAndExpr.equalityExprs) {
            if(ex.accept(this) != Type.BOOL) {
                error("Logical expression requires boolean", null);
            }
        }
        log("LogicalOrExpr evaluated as BOOL");
        return Type.BOOL;
    }

    @Override
    public Type visitEqualityExpr(Expr.EqualityExpr equalityExpr) {
        Type l = equalityExpr.left.accept(this);
        Type r = equalityExpr.right.accept(this);

        if (l != r) {
            error("Equality operands must have same type", equalityExpr.op);
        }
        log("EqualityExpr evaluated as BOOL with operands: " + l + " and " + r);
        return Type.BOOL;
    }

    @Override
    public Type visitRelationalExpr(Expr.RelationalExpr relationalExpr) {
        Type l = relationalExpr.left.accept(this);
        Type r = relationalExpr.right.accept(this);

        if((l != Type.INT && l != Type.DOUBLE) || l != r) {
            error("Invalid relational operands", relationalExpr.op);
        }
        log("RelationalExpr evaluated as BOOL with operands: " + l + " and " + r);
        return Type.BOOL;
    }

    @Override
    public Type visitAdditiveExpr(Expr.AdditiveExpr additiveExpr) {
        Type t = additiveExpr.multiplicativeExprs.get(0). accept(this);
        for(int i = 1; i < additiveExpr.multiplicativeExprs.size(); i++) {
            Type next = additiveExpr.multiplicativeExprs.get(i).accept(this);
            if(t != next || (t != Type.INT && t != Type.DOUBLE)) {
                error("Invalid arithmetic expression", additiveExpr.ops.get(i - 1));
            }

        }
        log("AdditiveExpr evaluated with type: " + t);
        return t;
    }

    @Override
    public Type visitMultiplicativeExpr(Expr.MultiplicativeExpr multiplicativeExpr) {
        Type t = multiplicativeExpr.unaryExprs.get(0).accept(this);

        for (int i = 1; i < multiplicativeExpr.unaryExprs.size(); i++) {
            Type next = multiplicativeExpr.unaryExprs.get(i).accept(this);
            if (t != next || (t != Type.INT && t != Type.DOUBLE)) {
                error("Invalid multiplicative expression", multiplicativeExpr.ops.get(i - 1));
            }
        }
        log("MultiplicativeExpr evaluated with type: " + t);
        return t;
    }

    @Override
    public Type visitUnaryExpr(Expr.UnaryExpr unaryExpr) {
        Type t = unaryExpr.expr.accept(this);

        switch (unaryExpr.unaryOp.type()) {
            case NOT:
                if (t != Type.BOOL)
                    error("Logical NOT requires boolean", unaryExpr.unaryOp);
                log("UnaryExpr NOT with type: BOOL");
                return Type.BOOL;

            case MINUS:
                if (t != Type.INT && t != Type.DOUBLE)
                    error("Unary minus requires number", unaryExpr.unaryOp);
                log("UnaryExpr MINUS with type: " + t);
                return t;

            default: {
                log("UnaryExpr unknown operation");
                return Type.ERROR;
            }
        }
    }

    @Override
    public Type visitTermExpr(Expr.TermExpr termExpr) {
        Type t = switch (termExpr.term.type()) {
            case INT_LIT -> Type.INT;
            case DOUBLE_LIT -> Type.DOUBLE;
            case BOOL_TRUE_LIT, BOOL_FALSE_LIT -> Type.BOOL;
            case STRING_LIT -> Type.STRING;
            case CHAR_LIT -> Type.CHAR;
            case IDENT -> {
                Symbol s = currentScope.resolve(termExpr.term.lexeme());
                if (s == null) error("Undeclared variable", termExpr.term);
                yield s.type;
            }
            default -> Type.ERROR;
        };
        log("TermExpr " + termExpr.term.lexeme() + " evaluated as type: " + t);
        return t;
    }

    @Override
    public Type visitGroupExpr(Expr.GroupExpr group) {
        return group.expr.accept(this);
    }

    @Override
    public Type visitFunctionalCall(Expr.FunctionalCall functionalCall) {
        String name = functionalCall.ident.lexeme();

        if (name.equals("read")) {
            if (!functionalCall.args.isEmpty())
                error("read() ne prima argumente", functionalCall.ident);
            log("Called read()");
            return Type.INT;
        }

        if (name.equals("print")) {
            if (functionalCall.args.size() != 1)
                error("print() prima tačno jedan argument", functionalCall.ident);

            functionalCall.args.get(0).accept(this);
            log("Called print()");
            return Type.VOID;
        }

        Symbol s = currentScope.resolve(name);
        if (s == null || s.type != Type.FUNCTION)
            error("Calling non-function", functionalCall.ident);

        if (s.paramTypes.size() != functionalCall.args.size())
            error("Wrong number of arguments", functionalCall.ident);

        for (int i = 0; i < s.paramTypes.size(); i++) {
            Type argType = functionalCall.args.get(i).accept(this);
            if (argType != s.paramTypes.get(i))
                error("Argument type mismatch", functionalCall.ident);
        }
        log("Called function: " + name);
        return s.returnType;
    }

    @Override
    public Type visitVarDecl(Stmt.VarDecl varDecl) {
        Type varType = mapType(varDecl.type);
        if(!currentScope.define(new Symbol(varDecl.name.lexeme(), varType))) {
            error("Variable already declared in this scope", varDecl.name);
        }
        log("Declared variable: " + varDecl.name.lexeme() + " : " + varType);

        if(varDecl.init != null) {
            Type initType = varDecl.init.accept(this);
            if(initType != varType) {
                error("Type mismatch in variable initialization", varDecl.name);
            }
            log("Initialized variable: " + varDecl.name.lexeme() + " with type " + initType);
        }
        return Type.VOID;
    }

    @Override
    public Type visitIfStmt(Stmt.IfStmt ifStmt) {
        log("Entering IF statement");
        Type condType = ifStmt.condition.accept(this);
        log("IF condition type: " + condType);
        if (condType != Type.BOOL)
            error("If condition must be boolean", null);

        enterScope();
        ifStmt.ifBranch.forEach(st -> st.accept(this));
        exitScope();
        if (ifStmt.elseIfBranch != null) {
            for (Stmt.ElseIfStmt elseifStmt : ifStmt.elseIfBranch) {
                log("Entering ELSE IF statement");
                Type elseifType = elseifStmt.condition.accept(this);
                log("ELSE IF condition type: " + elseifType);
                if (elseifType != Type.BOOL)
                    error("ElseIf condition must be boolean", null);

                enterScope();
                elseifStmt.body.forEach(st -> st.accept(this));
                exitScope();
            }
        }

        if (ifStmt.elseBranch != null) {
            log("Entering ELSE statement");
            enterScope();
            ifStmt.elseBranch.forEach(st -> st.accept(this));
            exitScope();
        }
        log("Exiting IF statement");
        return Type.VOID;
    }

    @Override
    public Type visitWhileStmt(Stmt.WhileStmt whileStmt) {
        log("Entering WHILE loop");
        Type condType = whileStmt.condition.accept(this);
        log("WHILE condition type: " + condType);
        if (condType != Type.BOOL)
            error("While condition must be boolean", null);

        enterScope();
        whileStmt.body.forEach(st -> st.accept(this));
        exitScope();

        log("Exiting WHILE loop");
        return Type.VOID;
    }

    @Override
    public Type visitForStmt(Stmt.ForStmt forStmt) {
        log("Entering FOR loop");

        enterScope();

        if (forStmt.init != null) {
            log("FOR initialization:");
            forStmt.init.accept(this); // log unutar visitVarDecl/visitVarAssign će se automatski pozvati
        }

        if (forStmt.condition != null) {
            Type condType = forStmt.condition.accept(this);
            log("FOR condition type: " + condType);
            if (condType != Type.BOOL)
                error("For condition must be boolean", null);
        }

        forStmt.body.forEach(st -> st.accept(this));

        exitScope();
        log("Exiting FOR loop");

        return Type.VOID;
    }

    @Override
    public Type visitReturnStmt(Stmt.ReturnStmt returnStmt) {
        Type t = returnStmt.expr.accept(this);
        log("RETURN statement with type: " + t);
        if (t != currentFunctionReturnType)
            error("Return type mismatch", null);
        return Type.VOID;
    }

    @Override
    public Type visitFuncDeclStmt(Stmt.FuncDecl funcDecl) {

        Symbol fun = new Symbol(funcDecl.name.lexeme(), Type.FUNCTION);
        fun.returnType = mapType(funcDecl.type);
        fun.paramTypes = funcDecl.params.stream()
                .map(p -> mapType(p.type))
                .toList();
        if (!currentScope.define(fun))
            error("Function already defined", funcDecl.name);
        log("Declared function: " + funcDecl.name.lexeme() + " returns " + fun.returnType);

        currentFunctionReturnType = fun.returnType;
        enterScope();
        for (Stmt.Param p : funcDecl.params) {
            currentScope.define(new Symbol(p.name.lexeme(), mapType(p.type)));
            log("Function param: " + p.name.lexeme() + " : " + mapType(p.type));
        }

        for (Stmt stmt : funcDecl.body)
            stmt.accept(this);

        exitScope();
        currentFunctionReturnType = null;

        return Type.VOID;
    }

    @Override
    public Type visitVarAssign(Stmt.VarAssign varAssign) {
        Symbol s = currentScope.resolve(varAssign.name.lexeme());
        if (s == null)
            error("Assignment to undeclared variable", varAssign.name);

        Type exprType = varAssign.value.accept(this);
        if (exprType != s.type)
            error("Type mismatch in assignment", varAssign.name);
        log("Assigned variable: " + varAssign.name.lexeme() + " = " + exprType);
        return Type.VOID;
    }
}
