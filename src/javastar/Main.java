package javastar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        // Sin argumentos → abrir el IDE gráfico
        if (args.length == 0) {
            StarIDE.launch();
            return;
        }

        // Con argumento → modo consola (CLI)
        String source = Files.readString(Path.of(args[0]));

        Lexer lexer = new Lexer();
        Lexer.LexResult lexResult = lexer.scan(source);

        System.out.println("=== TOKENS ===");
        lexResult.tokens().forEach(System.out::println);

        if (!lexResult.errors().isEmpty()) {
            System.out.println("\n=== ERRORES LÉXICOS ===");
            lexResult.errors().forEach(System.out::println);
            return;
        }

        Parser parser = new Parser(lexResult.tokens());
        Parser.ParseResult parseResult = parser.parse();

        if (!parseResult.errors().isEmpty()) {
            System.out.println("\n=== ERRORES SINTÁCTICOS ===");
            parseResult.errors().forEach(System.out::println);
            return;
        }

        AstPrinter printer = new AstPrinter();
        System.out.println("\n=== ÁRBOL SINTÁCTICO ===");
        System.out.println(printer.print(parseResult.program()));

        System.out.println("=== EJECUCIÓN ===");
        Interpreter interpreter = new Interpreter();
        interpreter.execute(parseResult.program());

        if (!interpreter.getErrors().isEmpty()) {
            System.out.println("\n=== ERRORES EN TIEMPO DE EJECUCIÓN ===");
            interpreter.getErrors().forEach(System.out::println);
        }
    }
}
