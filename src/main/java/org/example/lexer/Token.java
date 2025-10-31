package org.example.lexer;

import lombok.Builder;

@Builder
public record Token(TokenType type, String lexeme, int line, int colStart, int colEnd) {}
