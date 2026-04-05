package javastar;

public enum TokenType {
    // Palabras clave — estructura
    MAIN,                               // main

    // Palabras clave — tipos de dato
    ENTE,                               // entero
    DECI,                               // decimal
    TEXT,                               // texto
    BOOL,                               // booleano
    SCAN,                               // escanear

    // Palabras clave — control de flujo
    IF,                                 // si
    ELSE,                               // sino
    WHILE,                              // mientras
    FOR,                                // para
    SWITCH,                             // seleccionar
    CASE,                               // caso
    DEFAULT,                            // defecto

    // Palabras clave — operadores lógicos
    AND,                                // Y
    OR,                                 // O
    NOT,                                // NO

    // Palabras clave — literales booleanos
    TRUE,                               // verdadero
    FALSE,                              // falso

    // Palabras clave — otros
    NEWW,                               // nuevo
    STAR,                               // star
    PRINTLN,                            // imprimir

    // Literales e identificadores
    IDENTIFIER, INTEGER, DECIMAL, STRING,

    // Operadores aritméticos y de asignación
    PLUS, MINUS, STAR_OP, SLASH, PERCENT,
    ASSIGN, EQUAL_EQUAL, BANG_EQUAL,
    LESS, LESS_EQUAL, GREATER, GREATER_EQUAL,
    PLUS_PLUS, MINUS_MINUS,

    // Delimitadores y control de indentación
    LPAREN, RPAREN, DOT, COMMA, COLON, SEMICOLON,
    INDENT, DEDENT, NEWLINE, EOF
}
