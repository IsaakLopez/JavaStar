# Gramática EBNF de JavaStar

```ebnf
programa        = "main", saltoLinea, bloquePrincipal ;

bloquePrincipal = INDENT, { sentencia }, DEDENT ;

sentencia       = declaracion
                | asignacion
                | impresion
                | ifStmt
                | whileStmt
                | forStmt
                | switchStmt
                | expresion, saltoLinea ;

declaracion     = tipoDato, identificador, "=", expresion, saltoLinea ;

asignacion      = identificador, "=", expresion, saltoLinea ;

impresion       = "star", ".", "println", "(", expresion, ")", saltoLinea ;

ifStmt          = "if", expresion, saltoLinea,
                  bloque,
                  [ "else", saltoLinea, bloque ] ;

whileStmt       = "while", expresion, saltoLinea, bloque ;

forStmt         = "for", tipoDato, identificador, "=", expresion, ";",
                  expresion, ";",
                  ( identificador, "++"
                  | identificador, "--"
                  | identificador, "=", expresion ),
                  saltoLinea, bloque ;

switchStmt      = "switch", expresion, saltoLinea,
                  INDENT,
                  { caseStmt },
                  [ defaultStmt ],
                  DEDENT ;

caseStmt        = "case", expresion, saltoLinea, bloque ;
defaultStmt     = "default", saltoLinea, bloque ;

bloque          = INDENT, { sentencia }, DEDENT ;

tipoDato        = "ente" | "deci" | "text" | "bool" | "scan" ;

expresion       = orExpr ;
orExpr          = andExpr, { "OR", andExpr } ;
andExpr         = equalityExpr, { "AND", equalityExpr } ;
equalityExpr    = comparisonExpr, { ("==" | "!="), comparisonExpr } ;
comparisonExpr  = term, { ("<" | "<=" | ">" | ">="), term } ;
term            = factor, { ("+" | "-"), factor } ;
factor          = unary, { ("*" | "/" | "%"), unary } ;
unary           = [ "NOT" | "-" ], primary ;
primary         = entero | decimal | cadena | "true" | "false"
                | identificador
                | identificador, ("++" | "--")
                | "(", expresion, ")" ;

identificador   = letra, { letra | digito | "_" } ;
saltoLinea      = NEWLINE ;
```
