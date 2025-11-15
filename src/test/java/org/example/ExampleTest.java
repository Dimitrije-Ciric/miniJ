package org.example;

import org.example.ast.Program;
import org.example.lexer.Lexer;
import org.example.lexer.Token;
import org.example.parser.JsonPrinter;
import org.example.parser.Parser;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class ExampleTest {

    private void generateJsonFile(String code, String filename) {
        try {
            // 1. Tokenizacija
            Lexer lexer = new Lexer(code);
            List<Token> tokens = lexer.tokenize();

            // 2. Parsiranje u AST
            Parser parser = new Parser(tokens);
            Program ast = parser.parse().program;

            // 3. Generisanje JSON-a
            JsonPrinter printer = new JsonPrinter();
            String json = printer.print(ast);

            // 4. Upis u fajl
//            File dir = new File("C:\\Users\\Korisnik\\Documents\\miniJ\\src\\main\\resources");
//            if (!dir.exists()) dir.mkdirs();

            File file = new File(".", filename);
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(json);
            }

            System.out.println("AST JSON generated: " + file.getAbsolutePath());
            System.out.println(json);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testParserExamples() {
        // Primeri koda
        String code1 = "intJ x = 5!";
        String code2 = "print(x, y)!";
        String code3 = "intJ x! ifJ (x > 0) { print(x)! } elseJ { print(-x)! }";
        String code4 = "intJ x = 10! whileJ (x < 10) { x = x + 1! }";
        String code5 = "forJ (intJ i = 0! i < 5! i = i + 1!) { print(i)! }";

        // Generisanje JSON fajlova
        generateJsonFile(code1, "ast1.json");
        generateJsonFile(code2, "ast2.json");
        generateJsonFile(code3, "ast3.json");
        generateJsonFile(code4, "ast4.json");
        generateJsonFile(code5, "ast5.json");
    }

    @Test
    void testParserUnaryExpr() {
        String code1 = "+ 10 !";
        String code2 = "- \"aaa\"   !";
        String code3 = "- sum   !";

        generateJsonFile(code1, "ast_unary_1.json");
        generateJsonFile(code2, "ast_unary_2.json");
        generateJsonFile(code3, "ast_unary_3.json");
    }

    @Test
    void testParserMultiplicationExpr() {
        String code1 = "+ 10 * -13!";
        String code2 = "- \"aaa\" / 10   !";
        String code3 = "sum % 2   !";

        generateJsonFile(code1, "ast_multiplication_1.json");
        generateJsonFile(code2, "ast_multiplication_2.json");
        generateJsonFile(code3, "ast_multiplication_3.json");
    }

    @Test
    void testParserExpr() {
        String code1 = "+ 10 * -13 / 5 == 10 and false or 10 + 3 * 5 - 1 != \"aa\" + \"cc\" and 10 >= sum-1 !";
        String code2 = "(1 + 2) !";
        String code3 = "sum(a, b) !";
        String code4 = "sum(a, b, ((1 + 2) / -sum(1, 2) + 5) == 10 and false) !";

        generateJsonFile(code1, "ast_expr_1.json");
        generateJsonFile(code2, "ast_expr_2.json");
        generateJsonFile(code3, "ast_expr_3.json");
        generateJsonFile(code4, "ast_expr_4.json");
    }

}
