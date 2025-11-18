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

    private String errorMessage = null;
    private Token errorToken = null;

    private void setMessage(String message) {
        if (errorMessage == null) {
            errorMessage = message;
            errorToken = peek();
        }
    }

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
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
        setMessage(message);
        return null;
    }


    public ParserOutput parse() {
        List<Stmt> statements = new LinkedList<>();

        Stmt stmt = null;
        while ((stmt = statement()) != null)
            statements.add(stmt);

        if (!isAtEnd() || peek().type() != TokenType.EOF) {
            setMessage("Ocekuje se EOF");
            return new ParserOutput(null, errorMessage, errorToken);
        }

        return new ParserOutput(new Program(statements), errorMessage, errorToken);
    }

    public static class ParserOutput {
        public Program program;
        public String errorMessage;
        public Token errorToken;

        public ParserOutput(Program program, String errorMessage, Token errorToken) {
            this.program = program;
            this.errorMessage = errorMessage;
            this.errorToken = errorToken;
        }
    }

    private boolean isType(Token token) {
        String lexeme = token.lexeme();
        return lexeme.equals("intJ") || lexeme.equals("doubleJ") || lexeme.equals("charJ")
                || lexeme.equals("boolJ") || lexeme.equals("stringJ") || lexeme.equals("arrayJ");
    }

    private Stmt statement() {
        if(checkTypeAheadFuncDecl()) return funcDecl();

        Stmt stmt = varDecl();
        if(stmt != null) return stmt;

        stmt = varAssign();
        if(stmt != null) return stmt;

        if(match(TokenType.IF)) return ifStatment();
        if(match(TokenType.WHILE)) return whileStatment();
        if(match(TokenType.FOR)) return forStatment();
        if(check(TokenType.RET)) return retStatment();

        return exprStmt();
    }

    private boolean checkTypeAheadFuncDecl() {
        if (!isType(peek())) return false;
        if (current + 1 >= tokens.size()) return false;
        Token next = tokens.get(current + 1);
        if (next.type() != TokenType.IDENT) return false;
        if (current + 2 >= tokens.size()) return false;
        return tokens.get(current + 2).type() == TokenType.LPAREN;
    }

    private List<Stmt> parseBlock() {
        int cursor = current;
        List<Stmt> statements = new LinkedList<>();
        while (!check(TokenType.END) && !isAtEnd()) {
            Stmt stmt = statement();
            if (stmt != null) statements.add(stmt);
            else break;
        }
        if (consume(TokenType.END, "Očekuje se '}'") == null) {
            current = cursor;
            return null;
        }
        return statements;
    }

    private Stmt funcDecl() {
        int cursor = current;
        Token type = advance();
        Token name = consume(TokenType.IDENT, "Očekuje se ime funkcije");
        if (name == null) {
            current = cursor;
            return null;
        }
        if (consume(TokenType.LPAREN, "Očekuje se '('") == null) {
            current = cursor;
            return null;
        }
        List<Stmt.Param> params = parseParamList();

        if (params == null) {
            current = cursor;
            return null;
        }

        if (consume(TokenType.RPAREN, "Očekuje se ')'") == null) {
            current = cursor;
            return null;
        }
        if (consume(TokenType.BEGIN, "Očekuje se '{'") == null) {
            current = cursor;
            return null;
        }
        List<Stmt> body = parseBlock();

        if (body == null) {
            current = cursor;
            return null;
        }

        return new Stmt.FuncDecl(type, name, params, body);
    }

    private List<Stmt.Param> parseParamList() {
        int cursor = current;
        List<Stmt.Param> params = new LinkedList<>();
        while (isType(peek())) {
            Token type = advance();
            Token name = consume(TokenType.IDENT, "Očekuje se identifikator parametra");
            if (name == null) {
                current = cursor;
                return null;
            }
            params.add(new Stmt.Param(type, name));
            if (!match(TokenType.SEP_COMMA)) break;
        }
        return params;
    }

    private Stmt retStatment() {
        int cursor = current;
        if (consume(TokenType.RET, "Očekuje se 'return'") == null)
            return null;
        Expr value = expr();

        if (value == null) {
            current = cursor;
            return null;
        }

        if (consume(TokenType.SEP_EX, "Iskaz mora da se završi '!'") == null) {
            current = cursor;
            return null;
        }
        return new Stmt.ReturnStmt(value);
    }

    private Stmt forStatment() {
        int cursor = current;
        if (consume(TokenType.LPAREN, "Očekuje se '('") == null)
            return null;

        Stmt init = null;
        if (isType(peek())) {
            Token type = advance();
            Token name = consume(TokenType.IDENT, "Očekuje se identifikator");

            if (name == null) {
                current = cursor;
                return null;
            }

            Expr value = null;
            if (match(TokenType.ASSIGN)) {
                value = expr();
                if (value == null) {
                    current = cursor;
                    return null;
                }
            }

            if (consume(TokenType.SEP_EX, "Očekuje se '!' nakon inicijalizacije") == null) {
                current = cursor;
                return null;
            }
            init = new Stmt.VarDecl(type, null, name, value);
        } else if (peek().type() == TokenType.IDENT) {
            Token name = advance();
            if (consume(TokenType.ASSIGN, "Očekuje se '=' nakon imena varijable") == null) {
                current = cursor;
                return null;
            }
            Expr value = expr();

            if (value == null) {
                current = cursor;
                return null;
            }

            if (consume(TokenType.SEP_EX, "Očekuje se '!' nakon inicijalizacije") == null) {
                current = cursor;
                return null;
            }
            init = new Stmt.VarAssign(name, null, value);
        }

        Expr condition = expr();
        if (condition == null) {
            current = cursor;
            return null;
        }

        if (consume(TokenType.SEP_EX, "Očekuje se '!' nakon uslova") == null) {
            current = cursor;
            return null;
        }

        Stmt increment = forIncrement();

        if (increment == null) {
            current = cursor;
            return null;
        }

        if (consume(TokenType.RPAREN, "Očekuje se ')' nakon inkrementa") == null) {
            current = cursor;
            return null;
        }

        if (consume(TokenType.BEGIN, "Očekuje se '{'") == null) {
            current = cursor;
            return null;
        }

        List<Stmt> body = parseBlock();

        if (body == null) {
            current = cursor;
            return null;
        }

        return new Stmt.ForStmt(init, condition, increment, body);
    }

    private Stmt forIncrement() {
        if (peek().type() != TokenType.IDENT)
            return new Stmt.VarAssign(null, null,null);

        int cursor = current;
        Token name = advance();
        if (!match(TokenType.ASSIGN)) {
            Expr incrExpr = expr();
            if (incrExpr != null) return new Stmt.ExprStmt(incrExpr);

            current = cursor;
            return null;
        }
        Expr value = expr();

        if (value == null) {
            current = cursor;
            return null;
        }

        return new Stmt.VarAssign(name, null, value);
    }

    private Stmt whileStatment() {
        int cursor = current;
        if (consume(TokenType.LPAREN, "Očekuje se '('") == null) {
            return null;
        }
        Expr condition = expr();

        if (condition == null) {
            current = cursor;
            return null;
        }

        if (consume(TokenType.RPAREN, "Očekuje se ')'") == null) {
            current = cursor;
            return null;
        }

        if (consume(TokenType.BEGIN, "Očekuje se '{'") == null) {
            current = cursor;
            return null;
        }
        List<Stmt> body = parseBlock();

        if (body == null) {
            current = cursor;
            return null;
        }

        return new Stmt.WhileStmt(condition, body);
    }

    private Stmt ifStatment() {
        int cursor = current;

        if (consume(TokenType.LPAREN, "Očekuje se '('") == null) {
            current = cursor;
            return null;
        }
        Expr condition = expr();

        if (condition == null) {
            current = cursor;
            return null;
        }

        if (consume(TokenType.RPAREN, "Očekuje se ')'") == null) {
            current = cursor;
            return null;
        }
        if (consume(TokenType.BEGIN, "Očekuje se '{'") == null) {
            current = cursor;
            return null;
        }

        List<Stmt> ifBranch = parseBlock();
        List<Stmt.ElseIfStmt> elseIfs = new LinkedList<>();
        while (match(TokenType.ELSEIF)) {
            if (consume(TokenType.LPAREN, "Očekuje se '('") == null) {
                current = cursor;
                return null;
            }
            Expr elseifCond = expr();

            if (elseifCond == null) {
                current = cursor;
                return null;
            }

            if (consume(TokenType.RPAREN, "Očekuje se ')'") == null) {
                current = cursor;
                return null;
            }

            if (consume(TokenType.BEGIN, "Očekuje se '{'") == null) {
                current = cursor;
                return null;
            }
            List<Stmt> elseifBody = parseBlock();

            if (elseifBody == null) {
                current = cursor;
                return null;
            }

            elseIfs.add(new Stmt.ElseIfStmt(elseifCond, elseifBody));
        }
        List<Stmt> elseBranch = new LinkedList<>();
        if (match(TokenType.ELSE)) {
            if (consume(TokenType.BEGIN, "Očekuje se '{'") == null) {
                current = cursor;
                return null;
            }
            elseBranch = parseBlock();
            if (elseBranch == null) {
                current = cursor;
                return null;
            }
        }
        return new Stmt.IfStmt(condition, ifBranch, elseIfs, elseBranch);
    }

    private Stmt varAssign() {
        if (peek().type() != TokenType.IDENT) return null;
        int cursor = current;
        Token name = advance();

        Token arrayIndex = null;

        if (peek().type() == TokenType.LBRACKET) {
            advance();
            if (peek().type() != TokenType.INT_LIT && peek().type() != TokenType.IDENT) {
                setMessage("Ocekuje se broj");
                current = cursor;
                return null;
            }
            arrayIndex = advance();
            if (consume(TokenType.RBRACKET, "Ocekuje se ]") == null) {
                current = cursor;
                return null;
            }
        }

        if (!match(TokenType.ASSIGN)) {
            current = cursor;
            return null;
        }
        Expr value = expr();

        if (value == null) {
            current = cursor;
            return null;
        }

        if (consume(TokenType.SEP_EX, "Iskaz mora da se završi '!'") == null) {
            current = cursor;
            return null;
        }

        return new Stmt.VarAssign(name, arrayIndex, value);
    }

    private Stmt varDecl() {
        int cursor = current;
        if (!isType(peek())) return null;
        Token type = advance();

        Token arrayLength = null;

        if (type.type() == TokenType.ARRAY) {
            if (consume(TokenType.LBRACKET, "Ocekuje se [") == null) {
                current = cursor;
                return null;
            }
            if (peek().type() != TokenType.INT_LIT && peek().type() != TokenType.IDENT) {
                setMessage("Ocekuje se broj");
                current = cursor;
                return null;
            }
            arrayLength = advance();
            if (consume(TokenType.RBRACKET, "Ocekuje se ]") == null) {
                current = cursor;
                return null;
            }
        }

        Token name = consume(TokenType.IDENT, "Očekuje se identifikator");
        if (name == null) {
            current = cursor;
            return null;
        }
        Expr init = null;
        if (match(TokenType.ASSIGN)) {
            init = expr();
            if (init == null) {
                current = cursor;
                return null;
            }
        }
        if (consume(TokenType.SEP_EX, "Iskaz mora da se završi '!'") == null) {
            current = cursor;
            return null;
        }
        return new Stmt.VarDecl(type, arrayLength, name, init);
    }

    private Stmt exprStmt() {
        int cursor = current;

        Expr ex = expr();

        if (ex == null)
            return null;

        if (advance().type() != TokenType.SEP_EX) {
            setMessage("Iskaz mora da se zavrsi \"!\"");
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
                setMessage("Ocekuje se izraz");
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
                setMessage("Ocekuje se izraz");
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
                setMessage("Ocekuje se izraz");
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
                setMessage("Ocekuje se izraz");
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
                setMessage("Ocekuje se izraz");
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
                setMessage("Ocekuje se izraz");
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
            if (op != null)
                setMessage("Ocekuje se izraz");

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

        Token arrayIndex = null;

        if (term.type() == TokenType.IDENT && peek().type() == TokenType.LBRACKET) {
            advance();
            if (peek().type() != TokenType.INT_LIT && peek().type() != TokenType.IDENT) {
                setMessage("Ocekuje se broj");
                current = cursor;
                return null;
            }
            arrayIndex = advance();
            if (consume(TokenType.RBRACKET, "Ocekuje se ]") == null) {
                current = cursor;
                return null;
            }
        }

        return new Expr.TermExpr(term, arrayIndex);
    }

    private Expr groupExpr() {
        int cursor = current;

        if (peek().type() != TokenType.LPAREN)
            return null;

        advance();

        Expr group = expr();

        if (peek().type() != TokenType.RPAREN || group == null) {
            setMessage("Ocekuje se )");
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
                setMessage("Ocekivan je izraz nakon zareza");
                this.current = cursor;
                return null;
            }

            args.add(arg);
        }

        if (peek().type() != TokenType.RPAREN) {
            setMessage("Ocekuje se )");
            this.current = cursor;
            return null;
        }

        advance();

        return new Expr.FunctionalCall(ident, args);
    }

}