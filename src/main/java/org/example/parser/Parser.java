package org.example.parser;

import org.example.ast.Program;
import org.example.ast.Stmt;
import org.example.lexer.Token;
import org.example.lexer.TokenType;
import org.example.ast.Expr;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public final class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public static class ParseError extends RuntimeException {
        public ParseError(String message) {
            super(message);
        }
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type() == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type() == type;
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    private Token consume(TokenType type) {
        if (check(type)) return advance();
        return null;
    }

    private ParseError error(Token token, String message) {
        return new ParseError("Parse error at '" + token.lexeme() + "': " + message);
    }

    private boolean checkNext(TokenType type) {
        if (current + 1 >= tokens.size()) return false;
        return tokens.get(current + 1).type() == type;
    }

    // ----- ENTRY POINT -----
    public Program parse() {
        List<Stmt> statements = new LinkedList<>();

        Stmt stmt = null;
        while ((stmt = statement()) != null)
            statements.add(stmt);

        if (!isAtEnd() || peek().type() != TokenType.EOF)
            return null; // program must end with EOF!!!!

        return new Program(statements);
    }

    private boolean isType(Token token) {
        String lexeme = token.lexeme();
        return lexeme.equals("intJ") || lexeme.equals("doubleJ") || lexeme.equals("charJ")
                || lexeme.equals("boolJ") || lexeme.equals("stringJ") || lexeme.equals("arrayJ");
    }

    // ----- STATEMENTS -----
    private boolean check(TokenType... types) {
        if (isAtEnd()) return false;
        for (TokenType type : types) {
            if (peek().type() == type) return true;
        }
        return false;
    }

    private Stmt statement() {
        Stmt stmt = exprStmt();

//        if (stmt == null && match(TokenType.IF)) return ifStatement();
//        if (stmt == null && match(TokenType.WHILE)) return whileStatement();
//        if (stmt == null && match(TokenType.FOR)) return forStatement();

        return stmt;
    }

    private Stmt exprStmt() {
        int cursor = current;

        Expr ex = expr();

        if (ex == null)
            return null;

        if (advance().type() != TokenType.SEP_EX) {
            this.current = cursor;
            return null;
        }

        return new Stmt.ExprStmt(ex);
    }

    private Expr expr() {
        return logicalOrExpr();
    }

    private Expr logicalOrExpr() {
        int cursor = current;

        Expr logicalAndExpr = logicalAndExpr();

        if (logicalAndExpr == null)
            return null;

        List<Expr> exprs = new LinkedList<>(List.of(logicalAndExpr));

        while (true) {
            Token token = peek();

            if (!Objects.equals(TokenType.OR, token.type()))
                break;

            advance();

            logicalAndExpr = logicalAndExpr();

            if (logicalAndExpr == null) {
                current = cursor;
                return null;
            }

            exprs.add(logicalAndExpr);
        }

        if (exprs.size() == 1)
            return exprs.getFirst();

        return new Expr.LogicalOrExpr(exprs);
    }

    private Expr logicalAndExpr() {
        int cursor = current;

        Expr equalityExpr = equalityExpr();

        if (equalityExpr == null)
            return null;

        List<Expr> exprs = new LinkedList<>(List.of(equalityExpr));

        while (true) {
            Token token = peek();

            if (!Objects.equals(TokenType.AND, token.type()))
                break;

            advance();

            equalityExpr = equalityExpr();

            if (equalityExpr == null) {
                current = cursor;
                return null;
            }

            exprs.add(equalityExpr);
        }

        if (exprs.size() == 1)
            return exprs.getFirst();

        return new Expr.LogicalAndExpr(exprs);
    }

    private Expr equalityExpr() {
        int cursor = current;

        Expr left = relationalExpr();

        if (left == null)
            return null;

        Token op = null;
        Expr right = null;

        if (List.of(TokenType.EQ, TokenType.NEQ).contains(peek().type())) {
            op = peek();

            advance();

            right = relationalExpr();

            if (right == null) {
                this.current = cursor;
                return null;
            }
        }

        if (op == null)
            return left;

        return new Expr.EqualityExpr(left, op, right);
    }

    private Expr relationalExpr() {
        int cursor = current;

        Expr left = additiveExpr();

        if (left == null)
            return null;

        Token op = null;
        Expr right = null;

        if (List.of(
                TokenType.GT, TokenType.GE,
                TokenType.LT, TokenType.LE
        ).contains(peek().type())) {
            op = peek();

            advance();

            right = additiveExpr();

            if (right == null) {
                this.current = cursor;
                return null;
            }
        }

        if (op == null)
            return left;

        return new Expr.RelationalExpr(left, op, right);
    }


    private Expr additiveExpr() {
        int cursor = current;

        Expr multiplicativeExpr = multiplicativeExpr();

        if (multiplicativeExpr == null)
            return null;

        List<Expr> exprs = new LinkedList<>(List.of(multiplicativeExpr));
        List<Token> ops = new LinkedList<>();

        while (true) {
            Token token = peek();

            if (!List.of(TokenType.PLUS, TokenType.MINUS).contains(token.type()))
                break;

            advance();

            multiplicativeExpr = multiplicativeExpr();

            if (multiplicativeExpr == null) {
                current = cursor;
                return null;
            }

            exprs.add(multiplicativeExpr);
            ops.add(token);
        }

        if (exprs.size() == 1)
            return exprs.getFirst();

        return new Expr.AdditiveExpr(exprs, ops);
    }

    private Expr multiplicativeExpr() {
        int cursor = current;

        Expr unary = unaryExpr();

        if (unary == null)
            return null;

        List<Expr> exprs = new LinkedList<>(List.of(unary));
        List<Token> ops = new LinkedList<>();

        while (true) {
            Token token = peek();

            if (!List.of(
                    TokenType.MULTIPLY, TokenType.DIVIDE, TokenType.MOD
            ).contains(token.type()))
                break;

            advance();

            unary = unaryExpr();

            if (unary == null) {
                current = cursor;
                return null;
            }

            exprs.add(unary);
            ops.add(token);
        }

        if (exprs.size() == 1)
            return exprs.getFirst();

        return new Expr.MultiplicativeExpr(exprs, ops);
    }

    private Expr unaryExpr() {
        int cursor = current;

        Token op = null;

        if (peek().type() == TokenType.PLUS || peek().type() == TokenType.MINUS || peek().type() == TokenType.NOT)
            op = advance();

        Expr e = functionCall();

        if (e == null)
            e = groupExpr();

        if (e == null)
            e = termExpr();

        if (e == null) {
            this.current = cursor;
            return null;
        }

        if (op == null)
             return e;

        return new Expr.UnaryExpr(op, e);
    }

    private Expr termExpr() {
        int cursor = current;

        if (!List.of(
                TokenType.IDENT,
                TokenType.INT_LIT, TokenType.DOUBLE_LIT,
                TokenType.STRING_LIT, TokenType.CHAR_LIT,
                TokenType.BOOL_TRUE_LIT, TokenType.BOOL_FALSE_LIT
        ).contains(peek().type())) {
            this.current = cursor;
            return null;
        }

        Token term = advance();

        return new Expr.TermExpr(term);
    }

    private Expr groupExpr() {
        int cursor = current;

        if (peek().type() != TokenType.LPAREN)
            return null;

        advance();

        Expr group = expr();

        if (peek().type() != TokenType.RPAREN || group == null) {
            this.current = cursor;
            return null;
        }

        advance();

        return new Expr.GroupExpr(group);
    }

    private Expr functionCall() {
        int cursor = current;

        if (peek().type() != TokenType.IDENT)
            return null;

        Token ident = advance();

        if  (peek().type() != TokenType.LPAREN) {
            this.current = cursor;
            return null;
        }

        advance();

        List<Expr> args = new LinkedList<>();

        Expr arg = expr();

        if (arg != null)
            args.add(arg);

        while (arg != null && peek().type() == TokenType.SEP_COMMA) {
            advance();

            arg = expr();
            if (arg == null) {
                this.current = cursor;
                return null;
            }

            args.add(arg);
        }

        if (peek().type() != TokenType.RPAREN) {
            this.current = cursor;
            return null;
        }

        advance();

        return new Expr.FunctionalCall(ident, args);
    }

}