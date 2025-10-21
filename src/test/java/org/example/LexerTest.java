package org.example;

import org.example.lexer.Lexer;
import org.example.lexer.Token;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.example.lexer.TokenType.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class LexerTest {

    private  Lexer lexer;

    @Test
    public void test1() {
        lexer = new Lexer("bool x = false!");

        List<Token> tokens = lexer.tokenize();

        assertThat(
                tokens,
                contains(
                        new Token(BOOL, "bool", 1, 1, 4),
                        new Token(IDENT, "x", 1, 6, 6),
                        new Token(ASSIGN, "=", 1, 8, 8),
                        new Token(BOOL_FALSE_LIT, "false", 1, 10, 14),
                        new Token(SEP_EX, "!", 1, 15, 15)
                )
        );
    }

}
