package org.example;

import org.example.lexer.Lexer;
import org.example.lexer.Token;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.example.lexer.TokenType.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LexerTest {

    private Lexer lexer;

    @Test
    public void testBoolDeclaration() {
        lexer = new Lexer("boolJ x = false!");
        List<Token> tokens = lexer.tokenize();
        assertThat(tokens, contains(
                new Token(BOOL, "boolJ", 1, 1, 5),
                new Token(IDENT, "x", 1, 7, 7),
                new Token(ASSIGN, "=", 1, 9, 9),
                new Token(BOOL_FALSE_LIT, "false", 1, 11, 15),
                new Token(SEP_EX, "!", 1, 16, 16),
                new Token(EOF, "", 1, 17, 17)
        ));
    }

    @Test
    public void testIntAndDoubleDeclarations() {
        lexer = new Lexer("""
                intJ a = 10!
                doubleJ b = 4.52!
                """);
        List<Token> tokens = lexer.tokenize();
        assertThat(tokens, contains(
                new Token(INT, "intJ", 1, 1, 4),
                new Token(IDENT, "a", 1, 6, 6),
                new Token(ASSIGN, "=", 1, 8, 8),
                new Token(INT_LIT, "10", 1, 10, 11),
                new Token(SEP_EX, "!", 1, 12, 12),
                new Token(DOUBLE, "doubleJ", 2, 1, 7),
                new Token(IDENT, "b", 2, 9, 9),
                new Token(ASSIGN, "=", 2, 11, 11),
                new Token(DOUBLE_LIT, "4.52", 2, 13, 16),
                new Token(SEP_EX, "!", 2, 17, 17),
                new Token(EOF, "", 3, 1, 1)
        ));
    }

    @Test
    public void testStringAndChar() {
        lexer = new Lexer("""
                stringJ s = "hello"!
                charJ c = 'A'!
                """);
        List<Token> tokens = lexer.tokenize();
        assertThat(tokens, contains(
                new Token(STRING, "stringJ", 1, 1, 7),
                new Token(IDENT, "s", 1, 9, 9),
                new Token(ASSIGN, "=", 1, 11, 11),
                new Token(STRING_LIT, "\"hello\"", 1, 13, 19),
                new Token(SEP_EX, "!", 1, 20, 20),
                new Token(CHAR, "charJ", 2, 1, 5),
                new Token(IDENT, "c", 2, 7, 7),
                new Token(ASSIGN, "=", 2, 9, 9),
                new Token(CHAR_LIT, "'A'", 2, 11, 13),
                new Token(SEP_EX, "!", 2, 14, 14),
                new Token(EOF, "", 3, 1, 1)
        ));
    }

    @Test
    public void testIfElseStructure() {
        lexer = new Lexer("""
                ifJ(a > b) {
                    print("a je vece")!
                } elseJ {
                    print("b je vece")!
                }
                """);
        List<Token> tokens = lexer.tokenize();
        assertThat(tokens, contains(
                new Token(IF, "ifJ", 1, 1, 3),
                new Token(LPAREN, "(", 1, 4, 4),
                new Token(IDENT, "a", 1, 5, 5),
                new Token(GT, ">", 1, 7, 7),
                new Token(IDENT, "b", 1, 9, 9),
                new Token(RPAREN, ")", 1, 10, 10),
                new Token(BEGIN, "{", 1, 12, 12),
                new Token(IDENT, "print", 2, 5, 9),
                new Token(LPAREN, "(", 2, 10, 10),
                new Token(STRING_LIT, "\"a je vece\"", 2, 11, 21),
                new Token(RPAREN, ")", 2, 22, 22),
                new Token(SEP_EX, "!", 2, 23, 23),
                new Token(END, "}", 3, 1, 1),
                new Token(ELSE, "elseJ", 3, 3, 7),
                new Token(BEGIN, "{", 3, 9, 9),
                new Token(IDENT, "print", 4, 5, 9),
                new Token(LPAREN, "(", 4, 10, 10),
                new Token(STRING_LIT, "\"b je vece\"", 4, 11, 21),
                new Token(RPAREN, ")", 4, 22, 22),
                new Token(SEP_EX, "!", 4, 23, 23),
                new Token(END, "}", 5, 1, 1),
                new Token(EOF, "", 6, 1, 1)
        ));
    }

    @Test
    public void testForLoop() {
        lexer = new Lexer("""
                forJ(intJ i = 0! i < 10! i = i + 1){
                    print(i)!
                }
                """);
        List<Token> tokens = lexer.tokenize();
        assertThat(tokens, contains(
                new Token(FOR, "forJ", 1, 1, 4),
                new Token(LPAREN, "(", 1, 5, 5),
                new Token(INT, "intJ", 1, 6, 9),
                new Token(IDENT, "i", 1, 11, 11),
                new Token(ASSIGN, "=", 1, 13, 13),
                new Token(INT_LIT, "0", 1, 15, 15),
                new Token(SEP_EX, "!", 1, 16, 16),
                new Token(IDENT, "i", 1, 18, 18),
                new Token(LT, "<", 1, 20, 20),
                new Token(INT_LIT, "10", 1, 22, 23),
                new Token(SEP_EX, "!", 1, 24, 24),
                new Token(IDENT, "i", 1, 26, 26),
                new Token(ASSIGN, "=", 1, 28, 28),
                new Token(IDENT, "i", 1, 30, 30),
                new Token(PLUS, "+", 1, 32, 32),
                new Token(INT_LIT, "1", 1, 34, 34),
                new Token(RPAREN, ")", 1, 35, 35),
                new Token(BEGIN, "{", 1, 36, 36),
                new Token(IDENT, "print", 2, 5, 9),
                new Token(LPAREN, "(", 2, 10, 10),
                new Token(IDENT, "i", 2, 11, 11),
                new Token(RPAREN, ")", 2, 12, 12),
                new Token(SEP_EX, "!", 2, 13, 13),
                new Token(END, "}", 3, 1, 1),
                new Token(EOF, "", 4, 1, 1)
        ));
    }

    @Test
    public void testFunctionDeclaration() {
        lexer = new Lexer("""
                intJ saberi(intJ a, intJ b){
                    return a + b!
                }
                """);
        List<Token> tokens = lexer.tokenize();
        assertThat(tokens, contains(
                new Token(INT, "intJ", 1, 1, 4),
                new Token(IDENT, "saberi", 1, 6, 11),
                new Token(LPAREN, "(", 1, 12, 12),
                new Token(INT, "intJ", 1, 13, 16),
                new Token(IDENT, "a", 1, 18, 18),
                new Token(SEP_COMMA, ",", 1, 19, 19),
                new Token(INT, "intJ", 1, 21, 24),
                new Token(IDENT, "b", 1, 26, 26),
                new Token(RPAREN, ")", 1, 27, 27),
                new Token(BEGIN, "{", 1, 28, 28),
                new Token(RET, "return", 2, 5, 10),
                new Token(IDENT, "a", 2, 12, 12),
                new Token(PLUS, "+", 2, 14, 14),
                new Token(IDENT, "b", 2, 16, 16),
                new Token(SEP_EX, "!", 2, 17, 17),
                new Token(END, "}", 3, 1, 1),
                new Token(EOF, "", 4, 1, 1)
        ));
    }

    @Test
    public void testArrayDeclarationAndAccess() {
        lexer = new Lexer("""
                arrayJ[5] niz!
                niz[0] = "jedan"!
                print(niz[0])!
                """);
        List<Token> tokens = lexer.tokenize();
        assertThat(tokens, contains(
                new Token(ARRAY, "arrayJ", 1, 1, 6),
                new Token(LBRACKET, "[", 1, 7, 7),
                new Token(INT_LIT, "5", 1, 8, 8),
                new Token(RBRACKET, "]", 1, 9, 9),
                new Token(IDENT, "niz", 1, 11, 13),
                new Token(SEP_EX, "!", 1, 14, 14),
                new Token(IDENT, "niz", 2, 1, 3),
                new Token(LBRACKET, "[", 2, 4, 4),
                new Token(INT_LIT, "0", 2, 5, 5),
                new Token(RBRACKET, "]", 2, 6, 6),
                new Token(ASSIGN, "=", 2, 8, 8),
                new Token(STRING_LIT, "\"jedan\"", 2, 10, 16),
                new Token(SEP_EX, "!", 2, 17, 17),
                new Token(IDENT, "print", 3, 1, 5),
                new Token(LPAREN, "(", 3, 6, 6),
                new Token(IDENT, "niz", 3, 7, 9),
                new Token(LBRACKET, "[", 3, 10, 10),
                new Token(INT_LIT, "0", 3, 11, 11),
                new Token(RBRACKET, "]", 3, 12, 12),
                new Token(RPAREN, ")", 3, 13, 13),
                new Token(SEP_EX, "!", 3, 14, 14),
                new Token(EOF, "", 4, 1, 1)
        ));
    }

    @Test
    public void testCustomFunctionCall() {
        lexer = new Lexer("""
                intJ rezultat = saberi(3, 5)!
                print(rezultat)!
                """);
        List<Token> tokens = lexer.tokenize();
        assertThat(tokens, contains(
                new Token(INT, "intJ", 1, 1, 4),
                new Token(IDENT, "rezultat", 1, 6, 13),
                new Token(ASSIGN, "=", 1, 15, 15),
                new Token(IDENT, "saberi", 1, 17, 22),
                new Token(LPAREN, "(", 1, 23, 23),
                new Token(INT_LIT, "3", 1, 24, 24),
                new Token(SEP_COMMA, ",", 1, 25, 25),
                new Token(INT_LIT, "5", 1, 27, 27),
                new Token(RPAREN, ")", 1, 28, 28),
                new Token(SEP_EX, "!", 1, 29, 29),
                new Token(IDENT, "print", 2, 1, 5),
                new Token(LPAREN, "(", 2, 6, 6),
                new Token(IDENT, "rezultat", 2, 7, 14),
                new Token(RPAREN, ")", 2, 15, 15),
                new Token(SEP_EX, "!", 2, 16, 16),
                new Token(EOF, "", 3, 1, 1)
        ));
    }

    @Test
    public void testInvalidCharacter() {
        lexer = new Lexer("intJ a = 5$!");
        assertThrows(RuntimeException.class, lexer::tokenize);
    }

    @Test
    public void testUnterminatedString() {
        lexer = new Lexer("stringJ s = \"hello!");
        assertThrows(RuntimeException.class, lexer::tokenize);
    }

    @Test
    public void testUnterminatedChar() {
        lexer = new Lexer("charJ c = 'h");
        assertThrows(RuntimeException.class, lexer::tokenize);
    }

    @Test
    public void testCharOf5() {
        lexer = new Lexer("charJ c = 'abcde'");
        assertThrows(RuntimeException.class, lexer::tokenize);
    }

    @Test
    public void testInvalidNumberFormat() {
        lexer = new Lexer("intJ x = 12.3.4!");
        assertThrows(RuntimeException.class, lexer::tokenize);
    }

    @Test
    public void testInvalidCharLiteral() {
        lexer = new Lexer("charJ c = 'AB'!");
        assertThrows(RuntimeException.class, lexer::tokenize);
    }
}