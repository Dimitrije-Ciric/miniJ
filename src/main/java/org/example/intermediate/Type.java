package org.example.intermediate;

import org.example.lexer.TokenType;

public enum Type {
    INT("I"),
    DOUBLE("D"),
    BOOL(""),
    CHAR(""),
    STRING("Ljava/lang/String;"),
    VOID("V"),
    ARRAY(""),
    FUNCTION(""),
    ERROR("");

    private final String jasminSerialized;

    Type(String jasminSerialized) {
        this.jasminSerialized = jasminSerialized;
    }

    public static Type of(TokenType tt) {
        if (tt == null)
            return Type.VOID;
        if (tt == TokenType.INT)
            return INT;
        if (tt == TokenType.DOUBLE)
            return DOUBLE;
        if (tt == TokenType.BOOL)
            return BOOL;
        if (tt == TokenType.CHAR)
            return CHAR;
        if (tt == TokenType.STRING)
            return STRING;
        return null;
    }

    public String jasminSerialize() {
        return jasminSerialized;
    }
}
