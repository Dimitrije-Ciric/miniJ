package org.example.lexer;

public enum TokenType {

    // literals
    BOOL_TRUE_LIT, BOOL_FALSE_LIT, INT_LIT, DOUBLE_LIT, CHAR_LIT, STRING_LIT,

    // data type keywords
    BOOL, INT, DOUBLE, CHAR, STRING, ARRAY,

    // variable assignment
    ASSIGN,

    // arithmetic operators
    PLUS, MINUS, MULTIPLY, DIVIDE, MOD, CARET,

    // relational operators
    EQ, NEQ, LT, LE, GT, GE,

    // logic operators
    OR, AND, NOT,

    // control flow
    IF, ELSE, ELSEIF, FOR, WHILE,

    // functions
    FUNC, RET, READ, PRINT,

    // other
    BEGIN, END,
    LPAREN, RPAREN, LBRACKET, RBRACKET, SEP_COMMA, SEP_EX,
    SPACE, NEW_LINE, EOF, CAST_WRAP,

    // identifier
    IDENT
}
