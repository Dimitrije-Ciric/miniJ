package org.example.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonPrinter implements Expr.Visitor<JsonNode> {
    private static final ObjectMapper M = new ObjectMapper();

    public String print(Expr e) {
        try {
            JsonNode node = e.accept(this);
            return M.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public JsonNode visitLiteral(Expr.Literal e) {
        ObjectNode o = M.createObjectNode();
        o.put("type", "literal");
        if (e.value == null) o.putNull("value");
        else if (e.value instanceof Boolean b) o.put("value", b);
        else if (e.value instanceof Number n) o.put("value", n.doubleValue());
        else if (e.value instanceof String s) o.put("value", s);
        else if (e.value instanceof Character c) o.put("value", String.valueOf(c));
        else o.put("value", e.value.toString());
        return o;
    }

    @Override
    public JsonNode visitVar(Expr.Var e) {
        ObjectNode o = M.createObjectNode();
        o.put("type", "var");
        o.put("name", e.name.lexeme());
        return o;
    }

    @Override
    public JsonNode visitAssign(Expr.Assign e) {
        ObjectNode o = M.createObjectNode();
        o.put("type", "assign");
        o.put("name", e.name.lexeme());
        o.set("value", e.value.accept(this));
        return o;
    }

    @Override
    public JsonNode visitFuncCall(Expr.FuncCall e) {
        ObjectNode o = M.createObjectNode();
        o.put("type", "func_call");
        o.put("name", e.name.lexeme());
        ArrayNode args = M.createArrayNode();
        for (Expr arg : e.args) {
            args.add(arg.accept(this));
        }
        o.set("args", args);
        return o;
    }

    @Override
    public JsonNode visitBlock(Expr.Block e) {
        ObjectNode o = M.createObjectNode();
        o.put("type", "block");
        ArrayNode stmts = M.createArrayNode();
        for (Expr stmt : e.statements) {
            stmts.add(stmt.accept(this));
        }
        o.set("statements", stmts);
        return o;
    }

    @Override
    public JsonNode visitIfElse(Expr.IfElse e) {
        ObjectNode o = M.createObjectNode();
        o.put("type", "if_else");
        o.set("condition", e.condition.accept(this));
        o.set("thenBranch", e.thenBranch.accept(this));
        if (e.elseBranch != null) o.set("elseBranch", e.elseBranch.accept(this));
        return o;
    }

    @Override
    public JsonNode visitWhile(Expr.While e) {
        ObjectNode o = M.createObjectNode();
        o.put("type", "while");
        o.set("condition", e.condition.accept(this));
        o.set("body", e.body.accept(this));
        return o;
    }

    @Override
    public JsonNode visitFor(Expr.For e) {
        ObjectNode o = M.createObjectNode();
        o.put("type", "for");
        o.set("init", e.init.accept(this));
        o.set("condition", e.condition.accept(this));
        o.set("increment", e.increment.accept(this));
        o.set("body", e.body.accept(this));
        return o;
    }

    @Override
    public JsonNode visitGrouping(Expr.Grouping e) {
        ObjectNode o = M.createObjectNode();
        o.put("type", "grouping");
        o.set("expression", e.expr.accept(this));
        return o;
    }

    @Override
    public JsonNode visitUnary(Expr.Unary e) {
        ObjectNode o = M.createObjectNode();
        o.put("type", "unary");
        o.put("op", e.op.lexeme());
        o.set("operand", e.right.accept(this));
        return o;
    }

    @Override
    public JsonNode visitBinary(Expr.Binary e) {
        ObjectNode o = M.createObjectNode();
        o.put("type", "binary");
        o.put("op", e.op.lexeme());
        o.set("left", e.left.accept(this));
        o.set("right", e.right.accept(this));
        return o;
    }
}
