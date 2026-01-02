package org.example.lexer;

import lombok.Builder;

@Builder
public record Token(TokenType type, String lexeme, int line, int colStart, int colEnd) {
    public Object literal() {
        return switch (type) {
            case INT_LIT -> Integer.valueOf(lexeme);
            case DOUBLE_LIT -> Double.valueOf(lexeme);
            case BOOL_TRUE_LIT -> true;
            case BOOL_FALSE_LIT -> false;
            case STRING_LIT -> lexeme.substring(1, lexeme.length() - 1); // uklanja " "
            case CHAR_LIT -> lexeme.charAt(1); // 'A' -> A,
            case ARRAY -> Integer.valueOf(lexeme);
            case IDENT ->  lexeme;
            default -> null;
        };
    }

    public String position() {
        return colStart + "-" + colEnd;
    }
}
