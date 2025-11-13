package org.example.lexer;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
    private final ScannerCore sc;
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private static final Pattern[] TOKEN_PATTERNS = {
            Pattern.compile("^\\s+"),           // whitespace
            Pattern.compile("^true\\b"),        // boolean literal
            Pattern.compile("^false\\b"),
            Pattern.compile("^forJ\\b"),        // keywords
            Pattern.compile("^ifJ\\b"),
            Pattern.compile("^elseJ\\b"),
            Pattern.compile("^elseifJ\\b"),
            Pattern.compile("^whileJ\\b"),
            Pattern.compile("^intJ\\b"),
            Pattern.compile("^boolJ\\b"),
            Pattern.compile("^doubleJ\\b"),
            Pattern.compile("^charJ\\b"),
            Pattern.compile("^stringJ\\b"),
            Pattern.compile("^arrayJ\\b"),
            Pattern.compile("^and\\b"),
            Pattern.compile("^or\\b"),
            Pattern.compile("^not\\b"),
            Pattern.compile("^<="),
            Pattern.compile("^>="),
            Pattern.compile("^=="),
            Pattern.compile("^!="),
            Pattern.compile("^\\d+\\.\\d+"), // double literal
            Pattern.compile("^\\d+"),         // int literal
            Pattern.compile("^\"[^\"]*\""),   // string literal
            Pattern.compile("^'[^']'"),       // char literal
            Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*"), // identifikator
            Pattern.compile("^[+\\-*/%()\\[\\]{}<>=!,]") // single char token
    };

    private static final Map<String, TokenType> KEYWORDS = Map.ofEntries(
            Map.entry("boolJ", TokenType.BOOL),
            Map.entry("intJ", TokenType.INT),
            Map.entry("doubleJ", TokenType.DOUBLE),
            Map.entry("charJ", TokenType.CHAR),
            Map.entry("stringJ", TokenType.STRING),
            Map.entry("arrayJ", TokenType.ARRAY),
            Map.entry("and", TokenType.AND),
            Map.entry("or", TokenType.OR),
            Map.entry("not", TokenType.NOT),
            Map.entry("return", TokenType.RET),
            Map.entry("ifJ", TokenType.IF),
            Map.entry("elseJ", TokenType.ELSE),
            Map.entry("elseifJ", TokenType.ELSEIF),
            Map.entry("forJ", TokenType.FOR),
            Map.entry("whileJ", TokenType.WHILE)
    );

    public Lexer(String source) {
        this.source = source;
        this.sc = new ScannerCore(source);
    }

    public List<Token> tokenize() {
        while (!sc.isAtEnd()) {
            sc.beginToken();

            if (!tryMatchToken()) {
                String problematic = source.substring(sc.getCur(), Math.min(sc.getCur() + 10, source.length()));
                throw error("Not valid token matching at: " + problematic);
            }
        }

        tokens.add(new Token(TokenType.EOF, "", sc.getLine(), sc.getCol(), sc.getCol()));
        return tokens;
    }

    private boolean tryMatchToken() {
        String remainingText = source.substring(sc.getCur());

        Token bestMatch = null;
        int bestLength = 0;

        for (int i = 0; i < TOKEN_PATTERNS.length; i++) {
            Matcher matcher = TOKEN_PATTERNS[i].matcher(remainingText);
            if (matcher.find() && matcher.start() == 0) {
                String matchedText = matcher.group();
                int matchLength = matchedText.length();

                if ((i == 10 || i == 11) && isNumberWithKeyword(remainingText, matchedText)) {
                    continue;
                }

                if (matchLength > bestLength) {
                    bestLength = matchLength;
                    bestMatch = createTokenForPattern(i, matchedText);
                }
            }
        }

        if (bestMatch != null) {
            if (bestMatch.type() == TokenType.IDENT) {
                validateIdentifier(bestMatch.lexeme());
            }

            validateNumberNotStartingWithKeyword(bestMatch, remainingText);

            if (bestMatch.type() != TokenType.SPACE) {
                tokens.add(bestMatch);
            }

            for (int i = 0; i < bestLength; i++) {
                sc.advance();
            }
            return true;
        }
        return false;
    }

    private boolean isNumberWithKeyword(String remainingText, String matchedNumber) {
        int numberLength = matchedNumber.length();
        if (numberLength >= remainingText.length()) return false;

        String afterNumber = remainingText.substring(numberLength).trim();

        for (String keyword : KEYWORDS.keySet()) {
            if (afterNumber.toLowerCase().startsWith(keyword.toLowerCase())) {
                return true;
            }
        }

        return afterNumber.toLowerCase().startsWith("true") ||
                afterNumber.toLowerCase().startsWith("false") ||
                afterNumber.toLowerCase().startsWith("and") ||
                afterNumber.toLowerCase().startsWith("or") ||
                afterNumber.toLowerCase().startsWith("not");
    }


    private void validateIdentifier(String lexeme) {
        if (lexeme.isEmpty()) return;

        char firstChar = lexeme.charAt(0);

        if (!Character.isLetter(firstChar)) {
            throw error("Identifier cannot start with '" + firstChar + "': " + lexeme);
        }

        for (String keyword : KEYWORDS.keySet()) {
            if (lexeme.equalsIgnoreCase(keyword)) {
                return;
            }
            if (lexeme.toLowerCase().contains(keyword.toLowerCase()) &&
                    !lexeme.equals(keyword)) {
                throw error("Invalid identifier containing keyword '" + keyword + "': " + lexeme);
            }
        }
    }

    private void validateNumberNotStartingWithKeyword(Token token, String remainingText) {
        if (token.type() == TokenType.INT_LIT || token.type() == TokenType.DOUBLE_LIT) {
            String number = token.lexeme();

            String afterNumber = remainingText.substring(number.length());
            if (!afterNumber.isEmpty() && !Character.isWhitespace(afterNumber.charAt(0))) {
                for (String keyword : KEYWORDS.keySet()) {
                    if (afterNumber.toLowerCase().startsWith(keyword.toLowerCase())) {
                        throw error("Number '" + number + "' cannot be immediately followed by keyword '" + keyword + "'");
                    }
                }

                if (afterNumber.toLowerCase().startsWith("true") ||
                        afterNumber.toLowerCase().startsWith("false")) {
                    throw error("Number '" + number + "' cannot be immediately followed by boolean literal");
                }
            }
        }
    }

    private Token createTokenForPattern(int patternIndex, String matchedText) {
        TokenType type;

        // Ako je ključna reč (tip u KEYWORDS), koristi ga direktno
        if (KEYWORDS.containsKey(matchedText)) {
            type = KEYWORDS.get(matchedText);
        } else {
            // Inače, određujemo tip prema regex pattern indexu
            type = switch (patternIndex) {
                case 0 -> TokenType.SPACE;
                case 1 -> TokenType.BOOL_TRUE_LIT;
                case 2 -> TokenType.BOOL_FALSE_LIT;
                case 3 -> TokenType.FOR;
                case 4 -> TokenType.IF;
                case 5 -> TokenType.ELSE;
                case 6 -> TokenType.ELSEIF;
                case 7 -> TokenType.WHILE;
                case 8 -> TokenType.INT;
                case 9 -> TokenType.BOOL;
                case 10 -> TokenType.DOUBLE;
                case 11 -> TokenType.CHAR;
                case 12 -> TokenType.STRING;
                case 13 -> TokenType.ARRAY;
                case 14 -> TokenType.AND;
                case 15 -> TokenType.OR;
                case 16 -> TokenType.NOT;
                case 17 -> TokenType.LE;
                case 18 -> TokenType.GE;
                case 19 -> TokenType.EQ;
                case 20 -> TokenType.NEQ;
                case 21 -> TokenType.DOUBLE_LIT;
                case 22 -> TokenType.INT_LIT;
                case 23 -> TokenType.STRING_LIT;
                case 24 -> TokenType.CHAR_LIT;
                case 25 -> TokenType.IDENT;
                case 26 -> getSingleCharTokenType(matchedText.charAt(0));
                default -> throw new IllegalArgumentException("Unknown pattern index: " + patternIndex);
            };
        }

        return new Token(
                type,
                matchedText,
                sc.getStartLine(),
                sc.getStartCol(),
                sc.getStartCol() + matchedText.length() - 1
        );
    }

    private TokenType getSingleCharTokenType(char c) {
        return switch (c) {
            case '(' -> TokenType.LPAREN;
            case ')' -> TokenType.RPAREN;
            case '[' -> TokenType.LBRACKET;
            case ']' -> TokenType.RBRACKET;
            case '{' -> TokenType.BEGIN;
            case '}' -> TokenType.END;
            case '+' -> TokenType.PLUS;
            case '-' -> TokenType.MINUS;
            case '*' -> TokenType.MULTIPLY;
            case '/' -> TokenType.DIVIDE;
            case '%' -> TokenType.MOD;
            case '<' -> TokenType.LT;
            case '>' -> TokenType.GT;
            case '=' -> TokenType.ASSIGN;
            case '!' -> TokenType.SEP_EX;
            case ',' -> TokenType.SEP_COMMA;
            default -> {
                throw error("Unexpected character: '" + c + "'");
            }
        };
    }

    private RuntimeException error(String msg) {
        String near = source.substring(sc.getStartIdx(), Math.min(sc.getCur() + 10, source.length()));
        return new RuntimeException("LEXER ERROR > " + msg + " at " + sc.getStartLine() + ":" + sc.getStartCol() + " near '" + near + "'");
    }
}