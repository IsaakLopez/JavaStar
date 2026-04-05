# Gramática EBNF de JavaStar

```ebnf
programa        = "main", saltoLinea, bloquePrincipal ;

bloquePrincipal = INDENT, { sentencia }, DEDENT ;

sentencia       = declaracion
                | asignacion
                | impresion
                | siStmt
                | mientrasStmt
                | paraStmt
                | seleccionarStmt
                | expresion, saltoLinea ;

declaracion     = tipoDato, identificador, "=", expresion, saltoLinea ;

asignacion      = identificador, "=", expresion, saltoLinea ;

impresion       = "star", ".", "imprimir", "(", expresion, ")", saltoLinea ;

siStmt          = "si", expresion, saltoLinea,
                  bloque,
                  [ "sino", saltoLinea, bloque ] ;

mientrasStmt    = "mientras", expresion, saltoLinea, bloque ;

paraStmt        = "para", tipoDato, identificador, "=", expresion, ";",
                  expresion, ";",
                  ( identificador, "++"
                  | identificador, "--"
                  | identificador, "=", expresion ),
                  saltoLinea, bloque ;

seleccionarStmt = "seleccionar", expresion, saltoLinea,
                  INDENT,
                  { casoStmt },
                  [ defectoStmt ],
                  DEDENT ;

casoStmt        = "caso", expresion, saltoLinea, bloque ;
defectoStmt     = "defecto", saltoLinea, bloque ;

bloque          = INDENT, { sentencia }, DEDENT ;

tipoDato        = "entero" | "decimal" | "texto" | "booleano" | "escanear" ;

expresion       = orExpr ;
orExpr          = andExpr, { "O", andExpr } ;
andExpr         = equalityExpr, { "Y", equalityExpr } ;
equalityExpr    = comparisonExpr, { ("==" | "!="), comparisonExpr } ;
comparisonExpr  = term, { ("<" | "<=" | ">" | ">="), term } ;
term            = factor, { ("+" | "-"), factor } ;
factor          = unary, { ("*" | "/" | "%"), unary } ;
unary           = [ "NO" | "-" ], primary ;
primary         = entero | decimal | cadena | "verdadero" | "falso"
                | identificador
                | identificador, ("++" | "--")
                | "(", expresion, ")" ;

identificador   = letra, { letra | digito | "_" } ;
saltoLinea      = NEWLINE ;
```
