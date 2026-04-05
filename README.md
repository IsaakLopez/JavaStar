# JavaStar

Compilador completo del lenguaje **JavaStar** implementado en Java, con IDE gráfico integrado. Incluye todas las fases de un compilador frontend más un intérprete que ejecuta programas directamente con salida en tiempo real.

## Características

| Componente | Descripción |
|---|---|
| Analizador léxico | Tokenización completa con reporte de errores (línea y columna) |
| Analizador sintáctico | Parser recursivo descendente con recuperación de errores |
| Árbol sintáctico (AST) | Generación y visualización con conectores gráficos en español |
| Intérprete | Ejecución real del programa con salida en tiempo real |
| Entrada de usuario | `escanear` muestra un diálogo para leer valores durante la ejecución |
| Manejo de errores léxicos | Detección y reporte con línea y columna |
| Manejo de errores sintácticos | Recuperación y reporte sin colgar el parser |
| Gramática EBNF | Especificación formal en `GRAMATICA_EBNF.md` |
| Indentación significativa | Basada en **tabs** (como Python), genera tokens INDENT/DEDENT |
| IDE gráfico (StarIDE) | Interfaz visual con tema espacial y autocompletado de keywords |

---

## Sintaxis del lenguaje

### Estructura base

```
main
    # código aquí (indentado con tabs)
```

### Tipos de datos

| Keyword | Tipo | Ejemplo |
|---|---|---|
| `entero` | Número entero | `entero x = 10` |
| `decimal` | Número decimal | `decimal pi = 3.14` |
| `texto` | Cadena de texto | `texto msg = "hola"` |
| `booleano` | Verdadero / falso | `booleano activo = verdadero` |
| `escanear` | Entrada del usuario | `escanear dato = 0` |

### Sentencias soportadas

```
main
    # Declaración de variables
    entero x = 10
    decimal precio = 9.99
    texto nombre = "JavaStar"
    booleano ok = verdadero

    # Entrada de usuario (abre diálogo)
    escanear valor = 0

    # Asignación
    x = x + 1

    # Impresión (obligatorio el prefijo star.)
    star.imprimir("Hola mundo")
    star.imprimir(x)

    # Condicional
    si x < 20 Y ok
        star.imprimir("dentro del si")
    sino
        star.imprimir("dentro del sino")

    # Bucle mientras
    mientras x < 15
        x = x + 1

    # Bucle para
    para entero i = 0; i < 5; i++
        star.imprimir(i)

    # Seleccionar (switch)
    seleccionar x
        caso 10
            star.imprimir("es diez")
        defecto
            star.imprimir("otro valor")
```

### Operadores

| Categoría | Operadores |
|---|---|
| Aritméticos | `+`  `-`  `*`  `/`  `%` |
| Relacionales | `<`  `<=`  `>`  `>=`  `==`  `!=` |
| Lógicos | `Y`  `O`  `NO` |
| Incremento | `++`  `--` (postfijo) |

### Literales booleanos

| Keyword | Valor |
|---|---|
| `verdadero` | true |
| `falso` | false |

### Comentarios

```
# Esto es un comentario (línea ignorada por el lexer)
```

### Indentación

El lenguaje usa **tabs** (`\t`) para indentar, igual que Python. Cada nivel de profundidad es un tab adicional. Mezclar tabs con espacios genera un error léxico.

---

## Keywords del lenguaje

| Keyword | Categoría | Descripción |
|---|---|---|
| `main` | Estructura | Punto de entrada del programa |
| `entero` | Tipo | Número entero |
| `decimal` | Tipo | Número decimal |
| `texto` | Tipo | Cadena de texto |
| `booleano` | Tipo | Valor lógico |
| `escanear` | Tipo | Entrada del usuario |
| `si` | Control | Condicional if |
| `sino` | Control | Rama else |
| `mientras` | Control | Bucle while |
| `para` | Control | Bucle for |
| `seleccionar` | Control | Switch |
| `caso` | Control | Case dentro de seleccionar |
| `defecto` | Control | Default dentro de seleccionar |
| `Y` | Lógico | AND |
| `O` | Lógico | OR |
| `NO` | Lógico | NOT |
| `verdadero` | Literal | true |
| `falso` | Literal | false |
| `nuevo` | Otro | new |
| `star` | Objeto | Objeto de salida |
| `imprimir` | Método | println |

---

## Estructura del proyecto

```
javastar/
├── src/javastar/
│   ├── Main.java          # Punto de entrada (GUI o CLI)
│   ├── Lexer.java         # Analizador léxico
│   ├── Parser.java        # Analizador sintáctico
│   ├── Ast.java           # Definición de nodos del AST
│   ├── AstPrinter.java    # Visualización del árbol con conectores en español
│   ├── Interpreter.java   # Intérprete / motor de ejecución
│   ├── StarIDE.java       # IDE gráfico con tema espacial
│   ├── Token.java         # Representación de tokens
│   └── TokenType.java     # Enumeración de tipos de token (con comentarios)
├── examples/
│   ├── programa_ok.jstar    # Programa de ejemplo sin errores
│   └── programa_error.jstar # Programa con errores intencionales
├── out/                     # Clases compiladas (.class)
├── GRAMATICA_EBNF.md        # Gramática formal del lenguaje
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

Abre el **StarIDE**: editor con syntax highlighting, numeración de líneas, autocompletado y paneles de resultados.

### Modo consola (CLI)

```bash
java -cp out javastar.Main examples/programa_ok.jstar
```

Procesa el archivo y muestra los tokens, el árbol sintáctico y la salida en terminal.

---

## StarIDE

IDE gráfico con tema espacial integrado en el compilador.

### Interfaz

```
┌──────────────────────────────────────────────────────────────────┐
│ JavaStar   [Probar] [Abrir] [Guardar] [Ejecutar] [Limpiar]       │
├─────────────────────────────┬────────────────────────────────────┤
│   Editor JavaStar           │    Resultados                      │
│                             │  ┌──────────────────────────────┐  │
│  main                       │  │  Salida                      │  │
│      entero x = 10          │  │  Tokens                      │  │
│      star.imprimir(x)       │  │  Árbol                       │  │
│                             │  │  Errores                     │  │
│                             │  └──────────────────────────────┘  │
├─────────────────────────────┴────────────────────────────────────┤
│   Ctrl+Enter para ejecutar  ·  Ctrl+S para guardar               │
└──────────────────────────────────────────────────────────────────┘
```

### Pestañas de resultado

| Pestaña | Contenido |
|---|---|
| Salida | Salida real del programa (`star.imprimir`) en tiempo real |
| Tokens | Lista completa de tokens reconocidos por el lexer |
| Árbol | Árbol sintáctico con conectores visuales en español |
| Errores | Errores léxicos, sintácticos o de ejecución |

### Atajos de teclado

| Atajo | Acción |
|---|---|
| `Ctrl + Enter` | Compilar y ejecutar |
| `Ctrl + S` | Guardar archivo (pide ruta si es nuevo) |

### Autocompletado

Al escribir cualquier prefijo de keyword el editor muestra un popup flotante con las sugerencias disponibles:

- `↑` / `↓` — navegar entre sugerencias
- `Tab` o `Enter` — aceptar la sugerencia seleccionada
- `Escape` — cerrar el popup
- Doble clic — aceptar sugerencia con el mouse

### Entrada interactiva (`escanear`)

Cuando el programa contiene sentencias `escanear`, durante la ejecución aparece un diálogo emergente solicitando el valor para cada variable. La ejecución pausa hasta que el usuario ingresa el dato. Esto permite programas interactivos como juegos de adivinanza:

```
main
    entero secreto = 37
    entero ganaste = 0

    star.imprimir("=== Adivina el numero ===")

    mientras ganaste == 0
        escanear numero = 0
        si numero < secreto
            star.imprimir("Muy bajo!")
        sino
            si numero > secreto
                star.imprimir("Muy alto!")
            sino
                ganaste = 1
                star.imprimir("Felicidades!")
```

---

## Árbol sintáctico (AST)

El AST se visualiza con conectores tipo árbol de directorios y etiquetas en español:

```
Program
├── VarDecl [entero]  x
│   └── Literal: 10
├── SiStmt
│   ├── Condicion
│   │   └── Binary: <
│   │       ├── Variable: x
│   │       └── Literal: 20
│   ├── Entonces
│   │   └── ImprimirStmt
│   │       └── Literal: menor
│   └── Sino
│       └── ImprimirStmt
│           └── Literal: mayor
└── ParaStmt
    ├── Inicio
    │   └── VarDecl [entero]  i
    │       └── Literal: 0
    ├── Condicion
    │   └── Binary: <
    │       ├── Variable: i
    │       └── Literal: 3
    ├── Incremento
    │   └── ExprStmt
    │       └── Postfix: i++
    └── Cuerpo
        └── ImprimirStmt
            └── Variable: i
```

### Nodos del AST

| Nodo | Descripción |
|---|---|
| `VarDecl [tipo]` | Declaración de variable con su tipo |
| `Assignment` | Asignación a variable existente |
| `ImprimirStmt` | Sentencia `star.imprimir(...)` |
| `SiStmt` | Condicional si/sino |
| `MientrasStmt` | Bucle mientras |
| `ParaStmt` | Bucle para |
| `SeleccionarStmt` | Sentencia seleccionar |
| `Binary` | Operación binaria (`+`, `<`, `Y`, etc.) |
| `Unary` | Operación unaria (`-`, `NO`) |
| `Literal` | Valor constante |
| `Variable` | Referencia a variable |
| `Postfix` | Operador postfijo `i++` / `i--` |

---

## Manejo de errores

El compilador reporta errores con mensaje descriptivo, línea y columna, y continúa analizando el resto del código para encontrar todos los errores en una sola pasada.

### Errores léxicos

```
Error léxico: símbolo no reconocido '@' en línea 3, columna 5
Error léxico: cadena sin cerrar en línea 7, columna 10
Error de indentación inconsistente en línea 12
```

### Errores sintácticos

```
Error sintáctico en línea 5: Uso incorrecto: se debe escribir
'star.imprimir(...)' en lugar de solo 'imprimir(...)'

Error sintáctico en línea 8: ';' inesperado: el punto y coma
solo se usa dentro de 'para'
```

### Errores en tiempo de ejecución

```
Error de ejecución: variable 'z' no declarada
Error de ejecución: división por cero
```

---

## Intérprete

El intérprete ejecuta el AST directamente (tree-walking interpreter). Características:

- Variables con tipos `entero`, `decimal`, `texto`, `booleano`
- Entrada interactiva con `escanear` (diálogo emergente en el IDE)
- Aritmética mixta (entero + decimal → decimal)
- Concatenación de texto con `+`
- Cortocircuito en `Y` y `O`
- Operadores postfijos `i++` / `i--`
- Bloques `si/sino`, `mientras`, `para`, `seleccionar/caso/defecto`
- Salida en tiempo real durante la ejecución (no al finalizar)
- Ejecución en hilo separado para no bloquear la interfaz gráfica

---

## Requisitos

- **Java 21** o superior (usa sealed classes y pattern matching)
- No requiere dependencias externas
