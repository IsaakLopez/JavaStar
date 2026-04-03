# JavaStar

Compilador completo del lenguaje **JavaStar** implementado en Java, con IDE gráfico integrado. Incluye todas las fases de un compilador frontend más un intérprete para ejecutar programas directamente.

## Características

| Componente | Descripción |
|---|---|
| Analizador léxico | Tokenización completa con reporte de errores |
| Analizador sintáctico | Parser recursivo descendente |
| Árbol sintáctico (AST) | Generación y visualización con conectores gráficos |
| Intérprete | Ejecución real del programa con salida en consola |
| Manejo de errores léxicos | Detección y reporte con línea y columna |
| Manejo de errores sintácticos | Recuperación y reporte sin colgar el parser |
| Gramática EBNF | Especificación formal en `GRAMATICA_EBNF.md` |
| Indentación significativa | Basada en **tabs** (como Python), genera tokens INDENT/DEDENT |
| IDE gráfico (StarIDE) | Interfaz visual con tema espacial |

---

## Sintaxis del lenguaje

### Estructura base

```
main
    # código aquí (indentado con tabs)
```

### Tipos de datos

| Tipo | Descripción | Ejemplo |
|---|---|---|
| `ente` | Entero | `ente x = 10` |
| `deci` | Decimal | `deci pi = 3.14` |
| `text` | Cadena de texto | `text msg = "hola"` |
| `bool` | Booleano | `bool activo = true` |
| `scan` | Entrada de usuario | `scan dato = 0` |

### Sentencias soportadas

```
main
    # Declaración
    ente x = 10
    deci precio = 9.99
    text nombre = "JavaStar"
    bool ok = true

    # Asignación
    x = x + 1

    # Impresión  (obligatorio el prefijo star.)
    star.println("Hola mundo")
    star.println(x)

    # Condicional
    if x < 20 AND ok
        star.println("dentro del if")
    else
        star.println("dentro del else")

    # Bucle while
    while x < 15
        x = x + 1

    # Bucle for
    for ente i = 0; i < 5; i++
        star.println(i)

    # Switch
    switch x
        case 10
            star.println("es diez")
        default
            star.println("otro valor")
```

### Operadores

| Categoría | Operadores |
|---|---|
| Aritméticos | `+`  `-`  `*`  `/`  `%` |
| Relacionales | `<`  `<=`  `>`  `>=`  `==`  `!=` |
| Lógicos | `AND`  `OR`  `NOT` |
| Incremento | `++`  `--` (postfijo) |

### Indentación

El lenguaje usa **tabs** (`\t`) para indentar, igual que Python. Cada nivel de profundidad es un tab adicional. Mezclar tabs con espacios genera un error léxico.

---

## Estructura del proyecto

```
javastar/
├── src/javastar/
│   ├── Main.java          # Punto de entrada (GUI o CLI)
│   ├── Lexer.java         # Analizador léxico
│   ├── Parser.java        # Analizador sintáctico
│   ├── Ast.java           # Definición de nodos del AST
│   ├── AstPrinter.java    # Visualización del árbol con conectores
│   ├── Interpreter.java   # Intérprete / motor de ejecución
│   ├── StarIDE.java       # IDE gráfico con tema espacial
│   ├── Token.java         # Representación de tokens
│   └── TokenType.java     # Enumeración de tipos de token
├── examples/
│   ├── programa_ok.jstar  # Programa de ejemplo sin errores
│   └── programa_error.jstar # Programa con errores intencionales
├── out/                   # Clases compiladas (.class)
├── GRAMATICA_EBNF.md      # Gramática formal del lenguaje
└── README.md
```

---

## Compilar

```bash
javac -d out src/javastar/*.java
```

---

## Modos de ejecución

### Modo IDE gráfico (recomendado)

```bash
java -cp out javastar.Main
```

Abre el **StarIDE**: editor con syntax highlighting, numeración de líneas y paneles de resultados.

### Modo consola (CLI)

```bash
java -cp out javastar.Main examples/programa_ok.jstar
```

Procesa el archivo y muestra los resultados en terminal.

---

## StarIDE

IDE gráfico con tema espacial integrado en el compilador.

### Interfaz

```
┌─────────────────────────────────────────────────────┐
│ JavaStar        [Nuevo] [Abrir] [Ejecutar] [Limpiar]│
├────────────────────────┬────────────────────────────┤
│   Editor JavaStar      │    Resultados              │
│                        │  ┌──────────────────────┐  │
│  main                  │  │  Salida              │  │
│      ente x = 10       │  │  Tokens              │  │
│      star.println(x)   │  │  Árbol               │  │
│                        │  │  Errores             │  │
│                        │  └──────────────────────┘  │
├────────────────────────┴────────────────────────────┤
│   JavaStar listo.  Ctrl+Enter para ejecutar         │
└─────────────────────────────────────────────────────┘
```

### Pestañas de resultado

| Pestaña | Contenido |
|---|---|
|  Salida | Salida real del programa (`star.println`) |
|  Tokens | Lista completa de tokens reconocidos |
|   Árbol | Árbol sintáctico con conectores visuales |
| Errores | Errores léxicos, sintácticos o de ejecución |

### Atajos de teclado

| Atajo | Acción |
|---|---|
| `Ctrl + Enter` | Compilar y ejecutar |

---

## Árbol sintáctico

El AST se visualiza con conectores tipo árbol de directorios:

```
Program
├── VarDecl [ente]  x
│   └── Literal: 10
├── IfStmt
│   ├── Condition
│   │   └── Binary: <
│   │       ├── Variable: x
│   │       └── Literal: 20
│   ├── Then
│   │   └── PrintStmt
│   │       └── Literal: menor
│   └── Else
│       └── PrintStmt
│           └── Literal: mayor
└── ForStmt
    ├── Init
    │   └── VarDecl [ente]  i
    │       └── Literal: 0
    ├── Condition
    │   └── Binary: <
    │       ├── Variable: i
    │       └── Literal: 3
    ├── Increment
    │   └── ExprStmt
    │       └── Postfix: i++
    └── Body
        └── PrintStmt
            └── Variable: i
```

---

## Manejo de errores

El compilador reporta errores con mensaje descriptivo, línea y columna, y continúa analizando el resto del código para encontrar todos los errores en una sola pasada.

### Errores léxicos

```
Error léxico: símbolo no reconocido '@' en línea 3, columna 5
Error léxico: cadena sin cerrar en línea 7, columna 10
```

### Errores sintácticos

```
Error sintáctico en línea 5: Uso incorrecto: se debe escribir
'star.println(...)' en lugar de solo 'println(...)'

Error sintáctico en línea 8: ';' inesperado: el punto y coma
solo se usa dentro de 'for'
```

### Errores en tiempo de ejecución

```
Error de ejecución: variable 'z' no declarada
Error de ejecución: división por cero
```

---

## Intérprete

El intérprete ejecuta el AST directamente. Soporta:

- Variables con tipos `ente`, `deci`, `text`, `bool`
- Aritmética mixta (entero + decimal → decimal)
- Concatenación de texto con `+`
- Cortocircuito en `AND` y `OR`
- Operadores postfijos `i++` / `i--`
- Bloques `if/else`, `while`, `for`, `switch/case/default`

---

## Requisitos

- **Java 21** o superior (usa sealed classes y pattern matching)
- No requiere dependencias externas
