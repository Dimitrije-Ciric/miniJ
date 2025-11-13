package org.example.parser;

import org.example.lexer.Token;
import org.example.lexer.TokenType;

import java.util.ArrayList;
import java.util.List;

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

    private ParseError error(Token token, String message) {
        return new ParseError("Parse error at '" + token.lexeme() + "': " + message);
    }

    private boolean checkNext(TokenType type) {
        if (current + 1 >= tokens.size()) return false;
        return tokens.get(current + 1).type() == type;
    }

    // ----- ENTRY POINT -----
    public Expr parse() {
        List<Expr> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(statement());
        }
        if (statements.size() == 1) return statements.get(0);
        return new Expr.Block(statements);
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
    private Expr statement() {
        if (match(TokenType.IF)) return ifStatement();
        if (match(TokenType.WHILE)) return whileStatement();
        if (match(TokenType.FOR)) return forStatement();

        if (check(TokenType.INT, TokenType.DOUBLE, TokenType.BOOL,
                TokenType.CHAR, TokenType.STRING, TokenType.ARRAY)) {
            return varDeclaration();
        }

        return expressionStatement();
    }

    private Expr varDeclaration() {
        Token typeToken = advance(); // ovo je tip, npr. intJ
        Token name = consume(TokenType.IDENT, "Expect variable name.");

        Expr initializer = null;
        if (match(TokenType.ASSIGN)) {
            initializer = expression();
        }

        consume(TokenType.SEP_EX, "Expect '!' after variable declaration.");

        if (initializer != null) {
            return new Expr.Assign(name, initializer);
        }

        return new Expr.Assign(name, new Expr.Literal(null));
    }

    private Expr expressionStatement() {
        Expr expr = expression();
        consume(TokenType.SEP_EX, "Expect '!' after expression.");
        return expr;
    }

    private Expr ifStatement() {
        consume(TokenType.LPAREN, "Expect '(' after 'ifJ'.");
        Expr condition = expression();
        consume(TokenType.RPAREN, "Expect ')' after if condition.");
        Expr.Block thenBranch = block();
        Expr.Block elseBranch = null;
        if (match(TokenType.ELSE)) {
            elseBranch = block();
        }
        return new Expr.IfElse(condition, thenBranch, elseBranch);
    }

    private Expr.Block block() {
        consume(TokenType.BEGIN, "Expect '{' to start block.");
        List<Expr> stmts = new ArrayList<>();
        while (!check(TokenType.END) && !isAtEnd()) {
            stmts.add(statement());
        }
        consume(TokenType.END, "Expect '}' to close block.");
        return new Expr.Block(stmts);
    }

    private Expr whileStatement() {
        consume(TokenType.LPAREN, "Expect '(' after 'whileJ'.");
        Expr condition = expression();
        consume(TokenType.RPAREN, "Expect ')' after while condition.");
        Expr.Block body = block();
        return new Expr.While(condition, body);
    }

    private Expr forStatement() {
        consume(TokenType.LPAREN, "Expect '(' after 'forJ'.");

        // Inicijalizacija može biti varDeclaration ili expressionStatement
        Expr init;
        if (check(TokenType.INT, TokenType.DOUBLE, TokenType.BOOL,
                TokenType.CHAR, TokenType.STRING, TokenType.ARRAY)) {
            init = varDeclaration();
        } else {
            init = expressionStatement();
        }

        Expr condition = expressionStatement(); // i dalje expression + '!'
        Expr increment = expressionStatement(); // i dalje expression + '!'

        consume(TokenType.RPAREN, "Expect ')' after for clauses.");
        Expr.Block body = block();
        return new Expr.For(init, condition, increment, body);
    }

    // ----- EXPRESSIONS -----
    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = or();
        if (match(TokenType.ASSIGN)) {
            Token equals = previous();
            Expr value = assignment();
            if (expr instanceof Expr.Var var) {
                return new Expr.Assign(var.name, value);
            }
            throw error(equals, "Invalid assignment target.");
        }
        return expr;
    }

    private Expr or() {
        Expr expr = and();
        while (match(TokenType.OR)) {
            Token op = previous();
            Expr right = and();
            expr = new Expr.Binary(expr, op, right);
        }
        return expr;
    }

    private Expr and() {
        Expr expr = equality();
        while (match(TokenType.AND)) {
            Token op = previous();
            Expr right = equality();
            expr = new Expr.Binary(expr, op, right);
        }
        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();
        while (match(TokenType.EQ, TokenType.NEQ)) {
            Token op = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, op, right);
        }
        return expr;
    }

    private Expr comparison() {
        Expr expr = add();
        while (match(TokenType.LT, TokenType.LE, TokenType.GT, TokenType.GE)) {
            Token op = previous();
            Expr right = add();
            expr = new Expr.Binary(expr, op, right);
        }
        return expr;
    }

    private Expr add() {
        Expr expr = mul();
        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token op = previous();
            Expr right = mul();
            expr = new Expr.Binary(expr, op, right);
        }
        return expr;
    }

    private Expr mul() {
        Expr expr = unary();
        while (match(TokenType.MULTIPLY, TokenType.DIVIDE, TokenType.MOD)) {
            Token op = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, op, right);
        }
        return expr;
    }

    private Expr unary() {
        if (match(TokenType.NOT, TokenType.MINUS)) {
            Token op = previous();
            Expr right = unary();
            return new Expr.Unary(op, right);
        }
        return power();
    }

    private Expr power() {
        Expr expr = primary();
        while (match(TokenType.CARET)) {
            Token op = previous();
            Expr right = primary();
            expr = new Expr.Binary(expr, op, right);
        }
        return expr;
    }

    private Expr primary() {
        if (match(TokenType.INT_LIT, TokenType.DOUBLE_LIT,
                TokenType.CHAR_LIT, TokenType.STRING_LIT,
                TokenType.BOOL_TRUE_LIT, TokenType.BOOL_FALSE_LIT)) {
            return new Expr.Literal(previous().literal());
        }

        if (match(TokenType.IDENT)) {
            Token ident = previous();
            if (match(TokenType.LPAREN)) { // function call
                List<Expr> args = new ArrayList<>();
                if (!check(TokenType.RPAREN)) {
                    do {
                        args.add(expression());
                    } while (match(TokenType.SEP_COMMA));
                }
                consume(TokenType.RPAREN, "Expect ')' after function arguments.");
                return new Expr.FuncCall(ident, args);
            }
            return new Expr.Var(ident);
        }

        if (match(TokenType.LPAREN)) {
            Expr expr = expression();
            consume(TokenType.RPAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
    }
}