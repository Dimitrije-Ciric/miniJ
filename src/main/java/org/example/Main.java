package org.example;

import org.example.lexer.Lexer;
import org.example.lexer.Token;
import org.example.lexer.TokenFormatter;
import org.example.parser.JsonPrinter;
import org.example.parser.Parser;

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

            if (p.program == null) {
                System.out.println("Error: " + p.errorMessage);
                System.out.println("Leksema: " + p.errorToken.lexeme());
                System.out.println("Linija: " + p.errorToken.line());
                System.out.println("Kolona: " + p.errorToken.colEnd());
            }
            else {
                JsonPrinter printer = new JsonPrinter();
                printer.print(p.program);
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

}