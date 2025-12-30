package org.example;

import org.example.lexer.Lexer;
import org.example.lexer.Token;
import org.example.lexer.TokenFormatter;
import org.example.parser.JsonPrinter;
import org.example.parser.Parser;
import org.example.semantic.SematicAnalyzer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java main.Application <source-file>");
            System.exit(64);
        }

        try {
            String code = Files.readString(Path.of(args[0]));
            Lexer lexer = new Lexer(code);
            List<Token> tokens = lexer.tokenize();

            System.out.println(TokenFormatter.formatList(tokens));

            Parser parser = new Parser(tokens);

            Parser.ParserOutput p = parser.parse();
            System.out.println(p);

            if (p.program == null) {
                System.out.println("Error: " + p.errorMessage);
                System.out.println("Leksema: " + p.errorToken.lexeme());
                System.out.println("Linija: " + p.errorToken.line());
                System.out.println("Kolona: " + p.errorToken.colEnd());
            }
            else {
                JsonPrinter printer = new JsonPrinter();
                System.out.println(printer.print(p.program));
            }

            SematicAnalyzer sa = new SematicAnalyzer();
            p.program.accept(sa);
            sa.printAnalysis();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

}