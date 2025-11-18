package org.example.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.ast.*;
import org.example.lexer.Token;

import java.util.HashMap;
import java.util.Map;

public class JsonPrinter implements Visitor<JsonNode> {
    private static final ObjectMapper M = new ObjectMapper();

    public String print(VisitorAccept e) {
        try {
            JsonNode node = e.accept(this);
            return M.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public JsonNode visitProgram(Program program) {
        ObjectNode o = M.createObjectNode();
        o.put("type", "program");
        ArrayNode stmts = M.createArrayNode();
        for (Stmt stmt : program.stmts) {
            stmts.add(stmt.accept(this));
        }
        o.set("statements", stmts);
        return o;
    }

    @Override
    public JsonNode visitExprStmt(Stmt.ExprStmt exprStmt) {
        ObjectNode o = M.createObjectNode();
        o.put("type", "expr_stmt");
        o.set("expr", exprStmt.expr.accept(this));
        return o;
    }

    @Override
    public JsonNode visitLogicalOrExpr(Expr.LogicalOrExpr logicalOrExpr) {
        ObjectNode o = M.createObjectNode();
        o.put("type", "logical_or_expr");

        ArrayNode expr = M.createArrayNode();

        for (int i = 0; i < logicalOrExpr.andExprs.size(); i++)
            expr.add(logicalOrExpr.andExprs.get(i).accept(this));

        o.set("expr", expr);

        return o;
    }

    @Override
    public JsonNode visitLogicalAndExpr(Expr.LogicalAndExpr logicalAndExpr) {
        ObjectNode o = M.createObjectNode();
        o.put("type", "logical_and_expr");

        ArrayNode expr = M.createArrayNode();

        for (int i = 0; i < logicalAndExpr.equalityExprs.size(); i++)
            expr.add(logicalAndExpr.equalityExprs.get(i).accept(this));

        o.set("expr", expr);

        return o;
    }

    @Override
    public JsonNode visitEqualityExpr(Expr.EqualityExpr equalityExpr) {
        ObjectNode o = M.createObjectNode();
        o.put("type", "equality_expr");

        o.set("left_expr", equalityExpr.left.accept(this));
        o.put("op", equalityExpr.op.lexeme());
        o.set("right_expr", equalityExpr.right.accept(this));

        return o;
    }

    @Override
    public JsonNode visitRelationalExpr(Expr.RelationalExpr relationalExpr) {
        ObjectNode o = M.createObjectNode();
        o.put("type", "relational_expr");

        o.set("left_expr", relationalExpr.left.accept(this));
        o.put("op", relationalExpr.op.lexeme());
        o.set("right_expr", relationalExpr.right.accept(this));

        return o;
    }

    @Override
    public JsonNode visitAdditiveExpr(Expr.AdditiveExpr additiveExpr) {
        ObjectNode o = M.createObjectNode();
        o.put("type", "additive_expr");

        ArrayNode expr = M.createArrayNode();

        expr.add(additiveExpr.multiplicativeExprs.get(0).accept(this));

        for (int i = 0; i < additiveExpr.ops.size(); i++) {
            ObjectNode op = M.createObjectNode();
            op.put("type", "additive_expr_op");
            op.put("op", additiveExpr.ops.get(i).lexeme());

            expr.add(op);
            expr.add(additiveExpr.multiplicativeExprs.get(i+1).accept(this));
        }

        o.set("expr", expr);

        return o;
    }

    @Override
    public JsonNode visitMultiplicativeExpr(Expr.MultiplicativeExpr multiplicativeExpr) {
        ObjectNode o = M.createObjectNode();
        o.put("type", "multiplicative_expr");

        ArrayNode expr = M.createArrayNode();

        expr.add(multiplicativeExpr.unaryExprs.get(0).accept(this));

        for (int i = 0; i < multiplicativeExpr.ops.size(); i++) {
            ObjectNode op = M.createObjectNode();
            op.put("type", "multiplicative_expr_op");
            op.put("op", multiplicativeExpr.ops.get(i).lexeme());

            expr.add(op);
            expr.add(multiplicativeExpr.unaryExprs.get(i+1).accept(this));
        }

        o.set("expr", expr);

        return o;
    }

    @Override
    public JsonNode visitUnaryExpr(Expr.UnaryExpr unaryExpr) {
        ObjectNode o = M.createObjectNode();
        o.put("type", "unary_expr");

        if (unaryExpr.unaryOp != null)
                o.put("unary_op", unaryExpr.unaryOp.lexeme());
        o.set("expr", unaryExpr.expr.accept(this));

        return o;
    }

    @Override
    public JsonNode visitTermExpr(Expr.TermExpr termExpr) {
        ObjectNode o = M.createObjectNode();
        o.put("type", "term_expr");

        o.put("term", termExpr.term.lexeme());

        if (termExpr.arrayIndex != null)
            o.put("array_index", termExpr.arrayIndex.lexeme());

        return o;
    }

    @Override
    public JsonNode visitGroupExpr(Expr.GroupExpr group) {
        ObjectNode o = M.createObjectNode();
        o.put("type", "group_expr");

        o.set("expr", group.expr.accept(this));

        return o;
    }

    @Override
    public JsonNode visitFunctionalCall(Expr.FunctionalCall functionalCall) {
        ObjectNode o = M.createObjectNode();

        o.put("type", "functional_call");
        o.put("function_name", functionalCall.ident.lexeme());

        ArrayNode args = M.createArrayNode();

        for  (Expr arg : functionalCall.args)
            args.add(arg.accept(this));

        o.set("args", args);

        return o;
    }

    @Override
    public JsonNode visitVarDecl(Stmt.VarDecl varDecl) {
        ObjectNode o = M.createObjectNode();
        o.put("type", "var_decl");
        if (varDecl.arrayLength != null)
            o.put("array_length", varDecl.arrayLength.lexeme());
        o.put("var_type", varDecl.type.lexeme());
        o.put("name", varDecl.name.lexeme());
        if (varDecl.init != null) {
            o.set("init", varDecl.init.accept(this));
        }
        return o;
    }


    @Override
    public JsonNode visitIfStmt(Stmt.IfStmt ifStmt) {
        ObjectNode o = M.createObjectNode();
        o.put("type", "if_stmt");
        o.set("condition", ifStmt.condition.accept(this));

        ArrayNode thenBranch = M.createArrayNode();
        for (Stmt stmt : ifStmt.ifBranch) thenBranch.add(stmt.accept(this));
        o.set("then_branch", thenBranch);

        ArrayNode elseIfBranches = M.createArrayNode();
        for (Stmt.ElseIfStmt elseif : ifStmt.elseIfBranch) {
            ObjectNode elseifNode = M.createObjectNode();
            elseifNode.set("condition", elseif.condition.accept(this));
            ArrayNode body = M.createArrayNode();
            for (Stmt stmt : elseif.body) body.add(stmt.accept(this));
            elseifNode.set("body", body);
            elseIfBranches.add(elseifNode);
        }
        o.set("elseif_branches", elseIfBranches);

        ArrayNode elseBranch = M.createArrayNode();
        for (Stmt stmt : ifStmt.elseBranch) elseBranch.add(stmt.accept(this));
        o.set("else_branch", elseBranch);

        return o;
    }

    @Override
    public JsonNode visitWhileStmt(Stmt.WhileStmt whileStmt) {
        ObjectNode o = M.createObjectNode();
        o.put("type", "while_stmt");
        o.set("condition", whileStmt.condition.accept(this));
        ArrayNode body = M.createArrayNode();
        for (Stmt stmt : whileStmt.body) body.add(stmt.accept(this));
        o.set("body", body);
        return o;
    }

    @Override
    public JsonNode visitForStmt(Stmt.ForStmt forStmt) {
        ObjectNode o = M.createObjectNode();
        o.put("type", "for_stmt");
        o.set("init", forStmt.init.accept(this));
        o.set("condition", forStmt.condition.accept(this));
        o.set("increment", forStmt.increment.accept(this));
        ArrayNode body = M.createArrayNode();
        for (Stmt stmt : forStmt.body) body.add(stmt.accept(this));
        o.set("body", body);
        return o;
    }

    @Override
    public JsonNode visitReturnStmt(Stmt.ReturnStmt returnStmt) {
        ObjectNode o = M.createObjectNode();
        o.put("type", "return_stmt");
        o.set("expr", returnStmt.expr.accept(this));
        return o;
    }

    @Override
    public JsonNode visitFuncDeclStmt(Stmt.FuncDecl funcDecl) {
        ObjectNode o = M.createObjectNode();
        o.put("type", "func_decl");
        o.put("return_type", funcDecl.type.lexeme());
        o.put("name", funcDecl.name.lexeme());

        // params
        ArrayNode params = M.createArrayNode();
        for (Stmt.Param t : funcDecl.params) {
            ObjectNode paramObj = M.createObjectNode();
            paramObj.put("type", t.type.lexeme());
            paramObj.put("name", t.name.lexeme());
            params.add(paramObj);
        }
        o.set("params", params);

        // body
        ArrayNode body = M.createArrayNode();
        for (Stmt stmt : funcDecl.body) {
            body.add(stmt.accept(this));
        }
        o.set("body", body);

        return o;
    }

    @Override
    public JsonNode visitVarAssign(Stmt.VarAssign varAssign) {
        ObjectNode o = M.createObjectNode();
        o.put("type", "var_assign");
        o.put("name", varAssign.name.lexeme());
        o.set("value", varAssign.value.accept(this));
        if (varAssign.arrayIndex != null)
            o.put("array_index", varAssign.arrayIndex.lexeme());
        return o;
    }
}
