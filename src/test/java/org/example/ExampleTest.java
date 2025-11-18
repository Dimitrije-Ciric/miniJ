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
                Lexer lexer = new Lexer(code);
                List<Token> tokens = lexer.tokenize();

                Parser parser = new Parser(tokens);
                Program ast = parser.parse().program;

                JsonPrinter printer = new JsonPrinter();
                String json = printer.print(ast);

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

        @Test
        void testVarDeclAndAssign() {
            String code1 = "intJ x = 5!";
            String code2 = "doubleJ y!";
            String code3 = "x = 10!";
            generateJsonFile(code1, "test_varDecl_1.json");
            generateJsonFile(code2, "test_varDecl_2.json");
            generateJsonFile(code3, "test_varAssign_1.json");
        }

        @Test
        void testIfElseIfElse() {
            String code1 = "ifJ (x > 0) { x = 1! } elseifJ (x == 0) { x = 2! } elseJ { x = 3! }";
            String code2 = "ifJ (true) { } elseJ { }";
            String code3 = "ifJ (false) { }";
            generateJsonFile(code1, "test_ifElseIfElse_1.json");
            generateJsonFile(code2, "test_ifElseIfElse_2.json");
            generateJsonFile(code3, "test_ifOnly_1.json");
        }

        @Test
        void testForAndWhileLoops() {
            String code1 = "forJ (intJ i = 0! i < 5! i = i + 1) { print(i)! }";
            String code2 = "whileJ (x < 10) { x = x + 1! }";
            generateJsonFile(code1, "test_forLoop.json");
            generateJsonFile(code2, "test_whileLoop.json");
        }

        @Test
        void testFunctionDeclarationAndCall() {
            String code1 = "sum(1, 2)!";
            String code2 = "sum(a, b, c)!";
            generateJsonFile(code1, "test_funcDecl.json");
            generateJsonFile(code2, "test_funcCall_2.json");
        }

        @Test
        void testEdgeCasesEmptyBranches() {
            // If sa praznim elseIf i else
            String code1 = "ifJ (true) { }";
            String code2 = "ifJ (true) { } elseifJ (false) { }";
            String code3 = "ifJ (true) { } elseJ { }";
            generateJsonFile(code1, "test_emptyIf_1.json");
            generateJsonFile(code2, "test_emptyIf_2.json");
            generateJsonFile(code3, "test_emptyIf_3.json");
        }

        @Test
        void testReturnStatements() {
            String code1 = "return 5!";
            generateJsonFile(code1, "test_return_1.json");
        }

        @Test
        void testFunctionParamsWithTypes() {
            String code1 = "intJ add(intJ a, intJ b) { return a + b! }";
            String code2 = "stringJ hello() { return \"Hi\"! }";

            generateJsonFile(code1, "test_funcParams_1.json");
            generateJsonFile(code2, "test_funcParams_2.json");
        }
    }
