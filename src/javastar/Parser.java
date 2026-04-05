package javastar;

import javastar.Ast.*;
import java.util.*;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;
    private final List<String> errors = new ArrayList<>();

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public ParseResult parse() {
        List<Statement> statements = new ArrayList<>();
        skipNewlines();
        if (!match(TokenType.MAIN)) {
            error(peek(), "El programa debe iniciar con 'main'");
        }
        consumeLineEnd("Se esperaba fin de línea después de 'main'");
        skipNewlines();
        if (match(TokenType.INDENT)) {
            while (!check(TokenType.DEDENT) && !isAtEnd()) {
                skipNewlines();
                if (check(TokenType.DEDENT) || isAtEnd()) break;
                statements.add(statement());
                skipNewlines();
            }
            consume(TokenType.DEDENT, "Se esperaba cierre del bloque principal");
        } else {
            error(peek(), "Se esperaba bloque indentado debajo de 'main'");
        }
        return new ParseResult(new Program(statements), errors);
    }

    private Statement statement() {
        if (match(TokenType.ENTE, TokenType.DECI, TokenType.TEXT, TokenType.BOOL, TokenType.SCAN)) {
            return variableDeclaration(previous());
        }
        if (match(TokenType.IF)) return ifStatement();
        if (match(TokenType.WHILE)) return whileStatement();
        if (match(TokenType.FOR)) return forStatement();
        if (match(TokenType.SWITCH)) return switchStatement();
        if (check(TokenType.STAR) && checkNext(TokenType.DOT)) return printStatement();
        if (check(TokenType.IDENTIFIER) && checkNext(TokenType.ASSIGN)) return assignmentStatement();
        if (check(TokenType.PRINTLN)) {
            error(peek(), "Uso incorrecto: se debe escribir 'star.imprimir(...)' en lugar de solo 'imprimir(...)'");
            synchronize();
            return new ExprStmt(new Literal("<error>"));
        }
        if (check(TokenType.SEMICOLON)) {
            error(peek(), "';' inesperado: el punto y coma solo se usa dentro de 'for'");
            advance();
            return new ExprStmt(new Literal("<error>"));
        }
        Expression expr = expression();
        consumeLineEnd("Se esperaba fin de línea después de la expresión");
        return new ExprStmt(expr);
    }

    private Statement variableDeclaration(Token typeToken) {
        Token name = consume(TokenType.IDENTIFIER, "Se esperaba identificador de variable");
        consume(TokenType.ASSIGN, "Se esperaba '=' en declaración");
        Expression initializer = expression();
        consumeLineEnd("Se esperaba fin de línea después de la declaración");
        return new VarDecl(typeToken.lexeme, name.lexeme, initializer);
    }

    private Statement assignmentStatement() {
        Token name = consume(TokenType.IDENTIFIER, "Se esperaba identificador");
        consume(TokenType.ASSIGN, "Se esperaba '='");
        Expression value = expression();
        consumeLineEnd("Se esperaba fin de línea después de asignación");
        return new Assignment(name.lexeme, value);
    }

    private Statement printStatement() {
        consume(TokenType.STAR, "Se esperaba 'star'");
        consume(TokenType.DOT, "Se esperaba '.'");
        consume(TokenType.PRINTLN, "Se esperaba 'imprimir'");
        consume(TokenType.LPAREN, "Se esperaba '('");
        Expression value = expression();
        consume(TokenType.RPAREN, "Se esperaba ')'");
        consumeLineEnd("Se esperaba fin de línea después de imprimir");
        return new PrintStmt(value);
    }

    private Statement ifStatement() {
        Expression condition = expression();
        consumeLineEnd("Se esperaba salto de línea después de condición si");
        List<Statement> thenBranch = block("si");
        List<Statement> elseBranch = List.of();
        skipNewlines();
        if (match(TokenType.ELSE)) {
            consumeLineEnd("Se esperaba salto de línea después de sino");
            elseBranch = block("sino");
        }
        return new IfStmt(condition, thenBranch, elseBranch);
    }

    private Statement whileStatement() {
        Expression condition = expression();
        consumeLineEnd("Se esperaba salto de línea después de condición mientras");
        return new WhileStmt(condition, block("mientras"));
    }

    private Statement forStatement() {
        Token type = consumeAny(new TokenType[]{TokenType.ENTE, TokenType.DECI, TokenType.TEXT, TokenType.BOOL},
                "Se esperaba tipo de variable en inicialización del para");
        Token name = consume(TokenType.IDENTIFIER, "Se esperaba identificador en for");
        consume(TokenType.ASSIGN, "Se esperaba '=' en for");
        Expression initExpr = expression();
        consume(TokenType.SEMICOLON, "Se esperaba ';' en for");
        Expression condition = expression();
        consume(TokenType.SEMICOLON, "Se esperaba ';' en for");
        Statement increment;
        if (check(TokenType.IDENTIFIER) && (checkNext(TokenType.PLUS_PLUS) || checkNext(TokenType.MINUS_MINUS))) {
            Token incName = advance();
            Token op = advance();
            increment = new ExprStmt(new Postfix(incName.lexeme, op.lexeme));
        } else if (check(TokenType.IDENTIFIER) && checkNext(TokenType.ASSIGN)) {
            increment = assignmentNoLineEnd();
        } else {
            error(peek(), "Incremento no válido en for");
            increment = new ExprStmt(new Literal("<error>"));
        }
        consumeLineEnd("Se esperaba salto de línea después de encabezado para");
        return new ForStmt(new VarDecl(type.lexeme, name.lexeme, initExpr), condition, increment, block("para"));
    }

    private Statement assignmentNoLineEnd() {
        Token name = consume(TokenType.IDENTIFIER, "Se esperaba identificador");
        consume(TokenType.ASSIGN, "Se esperaba '='");
        Expression value = expression();
        return new Assignment(name.lexeme, value);
    }

    private Statement switchStatement() {
        Expression target = expression();
        consumeLineEnd("Se esperaba salto de línea después de seleccionar");
        consume(TokenType.INDENT, "Se esperaba bloque indentado en seleccionar");
        List<CaseBlock> cases = new ArrayList<>();
        List<Statement> defaultBranch = List.of();
        skipNewlines();
        while (!check(TokenType.DEDENT) && !isAtEnd()) {
            if (match(TokenType.CASE)) {
                Expression caseValue = expression();
                consumeLineEnd("Se esperaba salto de línea después de caso");
                List<Statement> body = block("caso");
                cases.add(new CaseBlock(caseValue, body));
            } else if (match(TokenType.DEFAULT)) {
                consumeLineEnd("Se esperaba salto de línea después de defecto");
                defaultBranch = block("defecto");
            } else {
                error(peek(), "Se esperaba 'caso' o 'defecto' dentro de seleccionar");
                synchronize();
            }
            skipNewlines();
        }
        consume(TokenType.DEDENT, "Se esperaba cierre del bloque seleccionar");
        return new SwitchStmt(target, cases, defaultBranch);
    }

    private List<Statement> block(String owner) {
        consume(TokenType.INDENT, "Se esperaba bloque indentado después de " + owner);
        List<Statement> statements = new ArrayList<>();
        skipNewlines();
        while (!check(TokenType.DEDENT) && !isAtEnd()) {
            statements.add(statement());
            skipNewlines();
        }
        consume(TokenType.DEDENT, "Se esperaba cierre de bloque para " + owner);
        return statements;
    }

    private Expression expression() { return or(); }
    private Expression or() {
        Expression expr = and();
        while (match(TokenType.OR)) expr = new Binary(expr, previous().lexeme, and());
        return expr;
    }
    private Expression and() {
        Expression expr = equality();
        while (match(TokenType.AND)) expr = new Binary(expr, previous().lexeme, equality());
        return expr;
    }
    private Expression equality() {
        Expression expr = comparison();
        while (match(TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL)) expr = new Binary(expr, previous().lexeme, comparison());
        return expr;
    }
    private Expression comparison() {
        Expression expr = term();
        while (match(TokenType.LESS, TokenType.LESS_EQUAL, TokenType.GREATER, TokenType.GREATER_EQUAL)) expr = new Binary(expr, previous().lexeme, term());
        return expr;
    }
    private Expression term() {
        Expression expr = factor();
        while (match(TokenType.PLUS, TokenType.MINUS)) expr = new Binary(expr, previous().lexeme, factor());
        return expr;
    }
    private Expression factor() {
        Expression expr = unary();
        while (match(TokenType.STAR_OP, TokenType.SLASH, TokenType.PERCENT)) expr = new Binary(expr, previous().lexeme, unary());
        return expr;
    }
    private Expression unary() {
        if (match(TokenType.NOT, TokenType.MINUS)) return new Unary(previous().lexeme, unary());
        return primary();
    }
    private Expression primary() {
        if (match(TokenType.INTEGER, TokenType.DECIMAL, TokenType.STRING)) return new Literal(previous().literal);
        if (match(TokenType.TRUE)) return new Literal(true);
        if (match(TokenType.FALSE)) return new Literal(false);
        if (match(TokenType.IDENTIFIER)) {
            if (match(TokenType.PLUS_PLUS)) return new Postfix(previous(1).lexeme, "++");
            if (match(TokenType.MINUS_MINUS)) return new Postfix(previous(1).lexeme, "--");
            return new Variable(previous().lexeme);
        }
        if (match(TokenType.LPAREN)) {
            Expression expr = expression();
            consume(TokenType.RPAREN, "Se esperaba ')' ");
            return new Grouping(expr);
        }
        error(peek(), "Expresión no válida: token inesperado '" + peek().lexeme + "'");
        advance(); // avanzar para no quedar en bucle infinito
        return new Literal("<error>");
    }

    private void skipNewlines() { while (match(TokenType.NEWLINE)) {} }
    private void consumeLineEnd(String message) {
        if (match(TokenType.NEWLINE)) return;
        error(peek(), message);
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        error(peek(), message);
        return new Token(type, "", null, peek().line, peek().column);
    }

    private Token consumeAny(TokenType[] types, String message) {
        for (TokenType type : types) if (check(type)) return advance();
        error(peek(), message);
        return new Token(types[0], "", null, peek().line, peek().column);
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) { advance(); return true; }
        }
        return false;
    }

    private boolean check(TokenType type) { return !isAtEnd() && peek().type == type; }
    private boolean checkNext(TokenType type) {
        if (current + 1 >= tokens.size()) return false;
        return tokens.get(current + 1).type == type;
    }
    private Token advance() { if (!isAtEnd()) current++; return previous(); }
    private boolean isAtEnd() { return peek().type == TokenType.EOF; }
    private Token peek() { return tokens.get(current); }
    private Token previous() { return tokens.get(current - 1); }
    private Token previous(int back) { return tokens.get(current - back); }

    private void error(Token token, String message) {
        errors.add("Error sintáctico en línea " + token.line + ", columna " + token.column + ": " + message + ". Token encontrado: '" + token.lexeme + "'");
    }

    private void synchronize() {
        while (!isAtEnd() && !check(TokenType.NEWLINE) && !check(TokenType.DEDENT)) advance();
        skipNewlines();
    }

    public record ParseResult(Program program, List<String> errors) {}
}
