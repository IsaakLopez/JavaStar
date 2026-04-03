package javastar;

import java.util.*;

public class Lexer {
    private static final Map<String, TokenType> KEYWORDS = new HashMap<>();
    static {
        KEYWORDS.put("main", TokenType.MAIN);
        KEYWORDS.put("ente", TokenType.ENTE);
        KEYWORDS.put("deci", TokenType.DECI);
        KEYWORDS.put("text", TokenType.TEXT);
        KEYWORDS.put("bool", TokenType.BOOL);
        KEYWORDS.put("scan", TokenType.SCAN);
        KEYWORDS.put("if", TokenType.IF);
        KEYWORDS.put("else", TokenType.ELSE);
        KEYWORDS.put("while", TokenType.WHILE);
        KEYWORDS.put("for", TokenType.FOR);
        KEYWORDS.put("switch", TokenType.SWITCH);
        KEYWORDS.put("case", TokenType.CASE);
        KEYWORDS.put("default", TokenType.DEFAULT);
        KEYWORDS.put("AND", TokenType.AND);
        KEYWORDS.put("OR", TokenType.OR);
        KEYWORDS.put("NOT", TokenType.NOT);
        KEYWORDS.put("true", TokenType.TRUE);
        KEYWORDS.put("false", TokenType.FALSE);
        KEYWORDS.put("neww", TokenType.NEWW);
        KEYWORDS.put("star", TokenType.STAR);
        KEYWORDS.put("println", TokenType.PRINTLN);
    }

    private final List<Token> tokens = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();
    private final Deque<Integer> indents = new ArrayDeque<>();

    public LexResult scan(String source) {
        tokens.clear();
        errors.clear();
        indents.clear();
        indents.push(0);

        String normalized = source.replace("\r\n", "\n").replace('\r', '\n');
        String[] lines = normalized.split("\n", -1);

        for (int i = 0; i < lines.length; i++) {
            scanLine(lines[i], i + 1);
        }

        while (indents.size() > 1) {
            indents.pop();
            tokens.add(new Token(TokenType.DEDENT, "<DEDENT>", null, lines.length + 1, 1));
        }
        tokens.add(new Token(TokenType.EOF, "", null, lines.length + 1, 1));
        return new LexResult(tokens, errors);
    }

    private void scanLine(String line, int lineNumber) {
        int rawIndent = countIndent(line);
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            tokens.add(new Token(TokenType.NEWLINE, "\\n", null, lineNumber, 1));
            return;
        }
        int indentLevel = rawIndent;
        if (indentLevel > indents.peek()) {
            indents.push(indentLevel);
            tokens.add(new Token(TokenType.INDENT, "<INDENT>", null, lineNumber, 1));
        } else {
            while (indentLevel < indents.peek()) {
                indents.pop();
                tokens.add(new Token(TokenType.DEDENT, "<DEDENT>", null, lineNumber, 1));
            }
            if (indentLevel != indents.peek()) {
                errors.add("Error de indentación inconsistente en línea " + lineNumber);
            }
        }

        int current = rawIndent;
        while (current < line.length()) {
            char c = line.charAt(current);
            int col = current + 1;
            if (Character.isWhitespace(c)) {
                current++;
                continue;
            }
            switch (c) {
                case '(' -> { add(TokenType.LPAREN, "(", null, lineNumber, col); current++; }
                case ')' -> { add(TokenType.RPAREN, ")", null, lineNumber, col); current++; }
                case '.' -> { add(TokenType.DOT, ".", null, lineNumber, col); current++; }
                case ',' -> { add(TokenType.COMMA, ",", null, lineNumber, col); current++; }
                case ':' -> { add(TokenType.COLON, ":", null, lineNumber, col); current++; }
                case ';' -> { add(TokenType.SEMICOLON, ";", null, lineNumber, col); current++; }
                case '+' -> {
                    if (match(line, current, "++")) { add(TokenType.PLUS_PLUS, "++", null, lineNumber, col); current += 2; }
                    else { add(TokenType.PLUS, "+", null, lineNumber, col); current++; }
                }
                case '-' -> {
                    if (match(line, current, "--")) { add(TokenType.MINUS_MINUS, "--", null, lineNumber, col); current += 2; }
                    else { add(TokenType.MINUS, "-", null, lineNumber, col); current++; }
                }
                case '*' -> { add(TokenType.STAR_OP, "*", null, lineNumber, col); current++; }
                case '/' -> { add(TokenType.SLASH, "/", null, lineNumber, col); current++; }
                case '%' -> { add(TokenType.PERCENT, "%", null, lineNumber, col); current++; }
                case '=' -> {
                    if (match(line, current, "==")) { add(TokenType.EQUAL_EQUAL, "==", null, lineNumber, col); current += 2; }
                    else { add(TokenType.ASSIGN, "=", null, lineNumber, col); current++; }
                }
                case '!' -> {
                    if (match(line, current, "!=")) { add(TokenType.BANG_EQUAL, "!=", null, lineNumber, col); current += 2; }
                    else { errors.add("Error léxico: '!' solo es válido como '!=' en línea " + lineNumber + ", columna " + col); current++; }
                }
                case '<' -> {
                    if (match(line, current, "<=")) { add(TokenType.LESS_EQUAL, "<=", null, lineNumber, col); current += 2; }
                    else { add(TokenType.LESS, "<", null, lineNumber, col); current++; }
                }
                case '>' -> {
                    if (match(line, current, ">=")) { add(TokenType.GREATER_EQUAL, ">=", null, lineNumber, col); current += 2; }
                    else { add(TokenType.GREATER, ">", null, lineNumber, col); current++; }
                }
                case '"' -> current = scanString(line, current, lineNumber);
                default -> {
                    if (Character.isDigit(c)) current = scanNumber(line, current, lineNumber);
                    else if (Character.isLetter(c) || c == '_') current = scanIdentifier(line, current, lineNumber);
                    else {
                        errors.add("Error léxico: símbolo no reconocido '" + c + "' en línea " + lineNumber + ", columna " + col);
                        current++;
                    }
                }
            }
        }
        tokens.add(new Token(TokenType.NEWLINE, "\\n", null, lineNumber, line.length() + 1));
    }

    private int scanString(String line, int start, int lineNumber) {
        int current = start + 1;
        while (current < line.length() && line.charAt(current) != '"') current++;
        if (current >= line.length()) {
            errors.add("Error léxico: cadena sin cerrar en línea " + lineNumber + ", columna " + (start + 1));
            return line.length();
        }
        String value = line.substring(start + 1, current);
        add(TokenType.STRING, line.substring(start, current + 1), value, lineNumber, start + 1);
        return current + 1;
    }

    private int scanNumber(String line, int start, int lineNumber) {
        int current = start;
        while (current < line.length() && Character.isDigit(line.charAt(current))) current++;
        boolean isDecimal = false;
        if (current < line.length() && line.charAt(current) == '.') {
            if (current + 1 < line.length() && Character.isDigit(line.charAt(current + 1))) {
                isDecimal = true;
                current++;
                while (current < line.length() && Character.isDigit(line.charAt(current))) current++;
            }
        }
        String lexeme = line.substring(start, current);
        if (isDecimal) add(TokenType.DECIMAL, lexeme, Double.parseDouble(lexeme), lineNumber, start + 1);
        else add(TokenType.INTEGER, lexeme, Integer.parseInt(lexeme), lineNumber, start + 1);
        return current;
    }

    private int scanIdentifier(String line, int start, int lineNumber) {
        int current = start;
        while (current < line.length() && (Character.isLetterOrDigit(line.charAt(current)) || line.charAt(current) == '_')) current++;
        String lexeme = line.substring(start, current);
        TokenType type = KEYWORDS.getOrDefault(lexeme, TokenType.IDENTIFIER);
        add(type, lexeme, null, lineNumber, start + 1);
        return current;
    }

    private boolean match(String line, int index, String expected) {
        return line.startsWith(expected, index);
    }

    private int countIndent(String line) {
        int count = 0;
        while (count < line.length() && line.charAt(count) == '\t') count++;
        return count;
    }

    private void add(TokenType type, String lexeme, Object literal, int line, int col) {
        tokens.add(new Token(type, lexeme, literal, line, col));
    }

    public record LexResult(List<Token> tokens, List<String> errors) {}
}
