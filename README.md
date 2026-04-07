# JavaStar

Compilador completo del lenguaje **JavaStar** implementado en Java puro, con IDE gráfico integrado. Incluye todas las fases de un compilador frontend más un intérprete que ejecuta programas directamente con salida en tiempo real.

## Características

| Componente | Descripción |
|---|---|
| Analizador léxico | Tokenización completa con reporte de errores (línea y columna) |
| Analizador sintáctico | Parser recursivo descendente con recuperación de errores |
| Árbol sintáctico (AST) | Generación y visualización con conectores gráficos en español |
| Intérprete | Ejecución real del programa con salida en tiempo real |
| Entrada de usuario | `star.escanear(var)` — diálogo emergente durante la ejecución |
| Manejo de errores | Detección y reporte léxico, sintáctico y de ejecución |
| Gramática EBNF | Especificación formal en `GRAMATICA_EBNF.md` |
| Indentación significativa | Basada en **tabs** (como Python), genera tokens INDENT/DEDENT |
| IDE gráfico (StarIDE) | Interfaz visual con tema espacial, syntax highlighting y autocompletado |
| Manual PDF integrado | Botón `★?` flotante genera y abre el manual de usuario en PDF |

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

### Sentencias

```
main
    # Declaración de variables
    entero x = 10
    decimal precio = 9.99
    texto nombre = "JavaStar"
    booleano ok = verdadero

    # Entrada de usuario: declarar primero, luego escanear
    entero edad = 0
    star.escanear(edad)

    # Asignación
    x = x + 1

    # Impresión
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

### Entrada interactiva

La variable **debe declararse primero** con su tipo antes de llamar `star.escanear`. El intérprete lanza un error si la variable no existe.

```
main
    entero numero = 0          # 1. declarar
    star.escanear(numero)      # 2. leer
    star.imprimir(numero)
```

### Operadores

| Categoría | Operadores |
|---|---|
| Aritméticos | `+`  `-`  `*`  `/`  `%` |
| Relacionales | `<`  `<=`  `>`  `>=`  `==`  `!=` |
| Lógicos | `Y`  `O`  `NO` |
| Incremento | `++`  `--` (postfijo) |

### Comentarios

```
# Esto es un comentario (línea ignorada por el lexer)
```

### Indentación

El lenguaje usa **tabs** (`\t`) para indentar. Cada nivel de profundidad es un tab adicional. Mezclar tabs con espacios genera un error léxico.

---

## Keywords del lenguaje

| Keyword | Categoría | Descripción |
|---|---|---|
| `main` | Estructura | Punto de entrada del programa |
| `entero` | Tipo | Número entero |
| `decimal` | Tipo | Número decimal |
| `texto` | Tipo | Cadena de texto |
| `booleano` | Tipo | Valor lógico |
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
| `star` | Objeto | Prefijo para llamadas del sistema |
| `imprimir` | Método de star | `star.imprimir(expr)` — imprime en consola |
| `escanear` | Método de star | `star.escanear(var)` — lee entrada del usuario |

---

## Estructura del proyecto

```
javastar/
├── src/javastar/
│   ├── Main.java             # Punto de entrada (GUI o CLI)
│   ├── Lexer.java            # Analizador léxico
│   ├── Parser.java           # Analizador sintáctico
│   ├── Ast.java              # Definición de nodos del AST
│   ├── AstPrinter.java       # Visualización del árbol con conectores en español
│   ├── Interpreter.java      # Intérprete / motor de ejecución
│   ├── StarIDE.java          # IDE gráfico con tema espacial
│   ├── ManualGenerator.java  # Generador del manual PDF (pure-Java, sin dependencias)
│   ├── Token.java            # Representación de tokens
│   └── TokenType.java        # Enumeración de tipos de token
├── examples/
│   ├── programa_ok.jstar     # Programa de ejemplo sin errores
│   └── programa_error.jstar  # Programa con errores intencionales
├── out/                      # Clases compiladas (.class)
├── GRAMATICA_EBNF.md         # Gramática formal del lenguaje
└── README.md
```

---

## Compilar y ejecutar

```bash
# Compilar
javac -d out src/javastar/*.java

# Modo IDE gráfico (recomendado)
java -cp out javastar.Main

# Modo consola
java -cp out javastar.Main examples/programa_ok.jstar
```

---

## StarIDE

IDE gráfico con tema espacial integrado en el compilador.

```
┌──────────────────────────────────────────────────────────────────┐
│ JavaStar   [Probar] [Abrir] [Guardar] [Ejecutar] [Limpiar]       │
├─────────────────────────────┬────────────────────────────────────┤
│   Editor JavaStar           │    Resultados                      │
│                             │  ┌──────────────────────────────┐  │
│  main                       │  │  Salida                      │  │
│      entero x = 0           │  │  Tokens                      │  │
│      star.escanear(x)       │  │  Árbol                       │  │
│      star.imprimir(x)       │  │  Errores                     │  │
│                             │  └──────────────────────────────┘  │
├─────────────────────────────┴────────────────────────────────────┤
│   Ctrl+Enter para ejecutar  ·  Ctrl+S para guardar         [★?]  │
└──────────────────────────────────────────────────────────────────┘
```

### Pestañas de resultado

| Pestaña | Contenido |
|---|---|
| Salida | Salida real del programa en tiempo real |
| Tokens | Lista completa de tokens reconocidos por el lexer |
| Árbol | Árbol sintáctico con conectores visuales en español |
| Errores | Errores léxicos, sintácticos o de ejecución |

### Atajos de teclado

| Atajo | Acción |
|---|---|
| `Ctrl + Enter` | Compilar y ejecutar |
| `Ctrl + S` | Guardar archivo |
| `↑` / `↓` | Navegar en el popup de autocompletado |
| `Tab` o `Enter` | Aceptar sugerencia |
| `Escape` | Cerrar el popup de autocompletado |

### Syntax highlighting

El editor coloriza en tiempo real. Los keywords dentro de strings se muestran en verde como parte de la cadena, nunca en dorado. Los comentarios anulan el color de cualquier token dentro de ellos.

| Color | Elemento |
|---|---|
| Dorado | Keywords |
| Verde | Cadenas de texto (prioridad sobre keywords) |
| Azul claro | Números literales |
| Gris | Comentarios (prioridad sobre keywords) |

### Manual PDF

El botón `★?` flotante en la esquina inferior derecha genera el manual completo en PDF (3 páginas: portada, guía de uso, referencia rápida) sin dependencias externas y lo abre con el visor del sistema.

---

## Árbol sintáctico (AST)

```
Program
├── VarDecl [entero]  x
│   └── Literal: 0
├── EscanearStmt  →  x
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
| `ImprimirStmt` | `star.imprimir(expr)` |
| `EscanearStmt` | `star.escanear(variable)` |
| `SiStmt` | Condicional si/sino |
| `MientrasStmt` | Bucle mientras |
| `ParaStmt` | Bucle para |
| `SeleccionarStmt` | Sentencia seleccionar |
| `Binary` | Operación binaria (`+`, `<`, `Y`, etc.) |
| `Unary` | Operación unaria (`-`, `NO`) |
| `Literal` | Valor constante |
| `Variable` | Referencia a variable |
| `Postfix` | `i++` / `i--` |

---

## Manejo de errores

El compilador reporta errores con mensaje, línea y columna, y continúa analizando para encontrar todos los errores en una sola pasada.

```
# Léxicos
Error léxico: símbolo no reconocido '@' en línea 3, columna 5
Error léxico: cadena sin cerrar en línea 7, columna 10

# Sintácticos
Error sintáctico: se debe escribir 'star.imprimir(...)' no 'imprimir(...)'
Error sintáctico: declara primero la variable y luego usa 'star.escanear(variable)'

# Ejecución
Error de ejecución: variable 'z' no declarada
Error de ejecución: división por cero
Error de ejecución: la variable 'dato' no está declarada. Declárela antes de usar star.escanear(...)
```

---

## Requisitos

- **Java 21** o superior (usa sealed classes y pattern matching)
- No requiere dependencias externas
