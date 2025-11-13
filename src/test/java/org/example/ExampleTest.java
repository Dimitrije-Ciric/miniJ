package org.example;

import org.example.lexer.Lexer;
import org.example.lexer.Token;
import org.example.parser.Expr;
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
            Expr ast = parser.parse();

            // 3. Generisanje JSON-a
            JsonPrinter printer = new JsonPrinter();
            String json = printer.print(ast);

            // 4. Upis u fajl
            File dir = new File("C:\\Users\\Korisnik\\Documents\\miniJ\\src\\main\\resources");
            if (!dir.exists()) dir.mkdirs();

            File file = new File(dir, filename);
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

}
