package javastar;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Genera el manual de usuario de JavaStar IDE en formato PDF,
 * sin ninguna dependencia externa (pure-Java raw PDF writer).
 */
public class ManualGenerator {

    private static final int PW = 595, PH = 842; // A4 en puntos

    // ── Punto de entrada ──────────────────────────────────────────────
    public static File generate() throws IOException {
        File tmp = File.createTempFile("javastar_manual_", ".pdf");
        tmp.deleteOnExit();
        assemblePDF(tmp, buildCover(), buildGuide(), buildReference());
        return tmp;
    }

    // ══════════════════════════════════════════════════════════════════
    //  PÁGINAS
    // ══════════════════════════════════════════════════════════════════

    /** Portada con fondo oscuro y tema espacial */
    private static byte[] buildCover() {
        Pen p = new Pen();

        // Fondo oscuro total
        p.fill(10, 10, 22).rect(0, 0, PW, PH);

        // Puntos estrella dispersos (campo de estrellas)
        Random rnd = new Random(42);
        for (int i = 0; i < 90; i++) {
            int b = 80 + rnd.nextInt(140);
            int sz = rnd.nextInt(2) + 1;
            p.fill(b - 20, b, Math.min(255, b + 40));
            p.rect(rnd.nextInt(PW), rnd.nextInt(PH), sz, sz);
        }

        // Barra dorada superior
        p.fill(255, 200, 50).rect(0, PH - 88, PW, 88);

        // Título en la barra
        p.text("JavaStar IDE", 52, PH - 48, 34, true, 10, 10, 22);
        p.text("Manual de Usuario  v2.0", 54, PH - 72, 12, false, 40, 25, 60);

        // Estrellas de esquina en la barra
        p.fill(10, 10, 22).star(32, PH - 44, 10, 4, 5);
        p.fill(10, 10, 22).star(PW - 32, PH - 44, 10, 4, 5);

        // Línea de acento doble
        p.stroke(255, 200, 50, 2.2f).line(50, PH - 118, PW - 50, PH - 118);
        p.stroke(255, 200, 50, 0.6f).line(50, PH - 123, PW - 50, PH - 123);

        // Título de sección
        p.text("Gu\u00eda de Referencia R\u00e1pida", 52, PH - 152, 22, true, 255, 200, 50);
        p.text("Compilador  \u00b7  Analizador  \u00b7  Int\u00e9rprete", 54, PH - 178, 13, false, 80, 220, 255);

        // Estrella grande central (doble)
        p.fill(255, 200, 50).star(PW / 2, 410, 78, 31, 5);
        p.fill(255, 230, 120).star(PW / 2, 410, 44, 18, 5);
        p.fill(255, 255, 200).star(PW / 2, 410, 14, 6, 5);

        // Texto descriptivo
        int dy = 290;
        for (String line : wrap(
                "JavaStar es un IDE dise\u00f1ado para StarLang, un lenguaje did\u00e1ctico " +
                "en espa\u00f1ol que permite aprender el funcionamiento interno de un compilador " +
                "de forma pr\u00e1ctica: an\u00e1lisis l\u00e9xico, sint\u00e1ctico e interpretaci\u00f3n.", 74)) {
            p.text(line, 52, dy, 11, false, 170, 170, 220);
            dy -= 16;
        }

        // Pequeñas estrellas decorativas en esquinas inferiores
        p.fill(80, 80, 180).star(28, 28, 10, 4, 5);
        p.fill(80, 80, 180).star(PW - 28, 28, 10, 4, 5);

        // Línea y texto de pie de página
        p.stroke(40, 40, 80, 1.0f).line(0, 78, PW, 78);
        p.text("Universidad Nacional Autónoma de Honduras  \u00b7  Dise\u00f1o de Compiladores  \u00b7  2026",
                50, 54, 10, false, 70, 70, 120);
        p.text("Versi\u00f3n 2.0", PW - 95, 54, 10, false, 70, 70, 120);

        return p.build();
    }

    /** Página 2: Guía de uso */
    private static byte[] buildGuide() {
        Pen p = new Pen();

        // Fondo claro
        p.fill(243, 244, 250).rect(0, 0, PW, PH);

        // Cabecera oscura
        p.fill(10, 10, 22).rect(0, PH - 44, PW, 44);
        p.fill(255, 200, 50).rect(0, PH - 44, 4, 44);
        p.text("JavaStar IDE  -  Gu\u00eda de Uso", 18, PH - 27, 13, true, 255, 200, 50);
        p.text("P\u00e1g. 2", PW - 60, PH - 27, 10, false, 100, 100, 160);

        int y = PH - 66;

        // ── Sección 1: La Interfaz ─────────────────────────────────────
        y = section(p, y, "1.  La Interfaz del IDE");
        y = bullet(p, y, "Editor (panel izquierdo)",
                "JTextPane con resaltado de sintaxis, n\u00fameros de l\u00ednea y autocompletado de palabras clave.");
        y = bullet(p, y, "Resultados (panel derecho)",
                "Cuatro pesta\u00f1as: Salida (consola), Tokens (tabla l\u00e9xica), \u00c1rbol AST y Errores.");
        y = bullet(p, y, "Barra de herramientas",
                "Botones Probar, Abrir, Guardar, Ejecutar y Limpiar en la parte superior derecha.");
        y = bullet(p, y, "Barra de estado",
                "Muestra el estado actual del compilador (listo, compilando, error, etc.).");
        y -= 8;

        // ── Sección 2: Tipos de Datos ──────────────────────────────────
        y = section(p, y, "2.  Tipos de Datos");
        y = colHeader(p, y, "Keyword", "Descripci\u00f3n", "Ejemplo");
        y = tableRow(p, y, "entero",   "N\u00famero entero (32 bits)",       "entero x = 10");
        y = tableRow(p, y, "decimal",  "N\u00famero con punto flotante",     "decimal pi = 3.14");
        y = tableRow(p, y, "texto",    "Cadena de caracteres (String)",      "texto s = \"hola\"");
        y = tableRow(p, y, "booleano", "Valor l\u00f3gico verdadero/falso",  "booleano ok = verdadero");
        y -= 8;

        // ── Sección 3: Operadores ──────────────────────────────────────
        y = section(p, y, "3.  Operadores L\u00f3gicos");
        y = colHeader(p, y, "Operador", "Significado", "Ejemplo de uso");
        y = tableRow(p, y, "Y",   "AND l\u00f3gico (ambas condiciones)",  "a > 0 Y b > 0");
        y = tableRow(p, y, "O",   "OR l\u00f3gico (al menos una)",        "a > 0 O b > 0");
        y = tableRow(p, y, "NO",  "NOT l\u00f3gico (negaci\u00f3n)",      "NO (a > 0)");
        y -= 8;

        // ── Sección 4: Entrada / Salida ────────────────────────────────
        y = section(p, y, "4.  Entrada y Salida");
        y = bullet(p, y, "star.imprimir( expr )",
                "Imprime el valor de la expresi\u00f3n en la consola de Salida del IDE.");
        y = bullet(p, y, "star.escanear( variable )",
                "Declara la variable primero con su tipo, luego llama star.escanear(var). " +
                "Muestra un di\u00e1logo emergente y asigna el valor ingresado.");

        // Pie de página
        footer(p, 2);
        return p.build();
    }

    /** Página 3: Referencia rápida */
    private static byte[] buildReference() {
        Pen p = new Pen();

        // Fondo claro
        p.fill(243, 244, 250).rect(0, 0, PW, PH);

        // Cabecera
        p.fill(10, 10, 22).rect(0, PH - 44, PW, 44);
        p.fill(255, 200, 50).rect(0, PH - 44, 4, 44);
        p.text("JavaStar IDE  - Referencia R\u00e1pida", 18, PH - 27, 13, true, 255, 200, 50);
        p.text("P\u00e1g. 3", PW - 60, PH - 27, 10, false, 100, 100, 160);

        int y = PH - 66;

        // ── Sección 5: Estructuras de Control ─────────────────────────
        y = section(p, y, "5.  Estructuras de Control");

        p.text("si  /  sino  (condicional)", 30, y, 10, true, 70, 0, 150);
        y -= 13;
        y = codeBlock(p, y, "si condicion", "    # bloque verdadero", "sino", "    # bloque falso");
        y -= 7;

        p.text("mientras  (bucle condicional)", 30, y, 10, true, 70, 0, 150);
        y -= 13;
        y = codeBlock(p, y, "mientras condicion", "    # cuerpo del bucle");
        y -= 7;

        p.text("para  (bucle con contador)", 30, y, 10, true, 70, 0, 150);
        y -= 13;
        y = codeBlock(p, y, "para entero i = 0; i < 10; i++", "    # cuerpo del bucle");
        y -= 7;

        p.text("seleccionar  /  caso  /  defecto  (switch)", 30, y, 10, true, 70, 0, 150);
        y -= 13;
        y = codeBlock(p, y,
                "seleccionar variable",
                "    caso valor1",
                "        # acci\u00f3n para valor1",
                "    defecto",
                "        # acci\u00f3n por defecto");
        y -= 14;

        // ── Sección 6: Atajos ──────────────────────────────────────────
        y = section(p, y, "6.  Atajos de Teclado");
        y = shortcutRow(p, y, "Ctrl + Enter", "Ejecutar el c\u00f3digo actual");
        y = shortcutRow(p, y, "Ctrl + S",     "Guardar el archivo actual");
        y = shortcutRow(p, y, "Tab / Enter",  "Aceptar sugerencia de autocompletado");
        y = shortcutRow(p, y, "Esc",          "Cerrar el popup de autocompletado");
        y = shortcutRow(p, y, "Boton ? (FAB)", "Abrir este manual PDF desde el IDE");
        y -= 14;

        // ── Sección 7: Ejemplo completo ────────────────────────────────
        y = section(p, y, "7.  Ejemplo Completo");
        y = codeBlock(p, y,
                "main",
                "    entero edad = 0",
                "    star.escanear(edad)",
                "    si edad >= 18 Y edad < 120",
                "        star.imprimir(\"Mayor de edad\")",
                "    sino",
                "        star.imprimir(\"Menor de edad\")",
                "    para entero i = 0; i < edad; i++",
                "        star.imprimir(i)");

        footer(p, 3);
        return p.build();
    }

    // ══════════════════════════════════════════════════════════════════
    //  HELPERS DE LAYOUT
    // ══════════════════════════════════════════════════════════════════

    private static int section(Pen p, int y, String title) {
        y -= 16;
        p.fill(10, 10, 22).rect(28, y - 4, PW - 56, 20);
        p.fill(255, 200, 50).rect(28, y - 4, 4, 20);
        p.text(title, 38, y + 3, 11, true, 255, 200, 50);
        y -= 20;
        return y;
    }

    private static int bullet(Pen p, int y, String label, String desc) {
        y -= 5;
        p.text("\u00bb  " + label, 32, y, 10, true, 40, 40, 140);
        List<String> lines = wrap(desc, 76);
        y -= 13;
        for (String line : lines) {
            p.text(line, 46, y, 9, false, 55, 55, 85);
            y -= 12;
        }
        return y;
    }

    private static int colHeader(Pen p, int y, String c1, String c2, String c3) {
        y -= 4;
        p.fill(30, 30, 70).rect(28, y - 3, PW - 56, 16);
        p.text(c1, 33, y, 9, true, 200, 210, 255);
        p.text(c2, 138, y, 9, true, 200, 210, 255);
        p.text(c3, 360, y, 9, true, 200, 210, 255);
        y -= 16;
        return y;
    }

    private static int tableRow(Pen p, int y, String kw, String desc, String ex) {
        y -= 4;
        boolean alt = (y % 36) < 18; // alternating row color
        p.fill(alt ? 228 : 238, alt ? 228 : 238, alt ? 242 : 248).rect(28, y - 3, PW - 56, 16);
        p.text(kw, 33, y, 9, true, 100, 0, 180);
        p.text(desc, 138, y, 9, false, 40, 40, 80);
        p.text(ex, 360, y, 8, false, 0, 110, 60);
        y -= 16;
        return y;
    }

    private static int shortcutRow(Pen p, int y, String key, String desc) {
        y -= 4;
        p.fill(225, 225, 238).rect(28, y - 3, PW - 56, 16);
        // tecla
        p.fill(190, 195, 220).rect(33, y - 2, 108, 13);
        p.stroke(150, 155, 190, 0.7f).line(33, y - 2, 141, y - 2);
        p.text(key, 37, y, 8, true, 20, 20, 90);
        // descripción
        p.text(desc, 150, y, 9, false, 40, 40, 80);
        y -= 16;
        return y;
    }

    private static int codeBlock(Pen p, int y, String... lines) {
        int bh = lines.length * 13 + 8;
        p.fill(215, 218, 238).rect(28, y - bh + 5, PW - 56, bh);
        p.fill(70, 0, 160).rect(28, y - bh + 5, 4, bh);     // barra izq. morada
        p.fill(255, 200, 50).rect(28, y - bh + 5, 4, 4);    // pequeño punto dorado
        for (String line : lines) {
            p.text(line, 40, y, 8, false, 18, 18, 75);
            y -= 13;
        }
        return y - 3;
    }

    private static void footer(Pen p, int page) {
        p.stroke(190, 195, 215, 0.8f).line(28, 32, PW - 28, 32);
        p.text("JavaStar IDE  -  Manual de Usuario  -  v2.0",
                28, 18, 8, false, 140, 145, 170);
        p.text("P\u00e1gina " + page + " de 3", PW - 80, 18, 8, false, 140, 145, 170);
    }

    // ══════════════════════════════════════════════════════════════════
    //  WRITER DE CONTENIDO PDF (interno)
    // ══════════════════════════════════════════════════════════════════

    private static class Pen {
        private final StringBuilder sb = new StringBuilder();

        Pen fill(int r, int g, int b) {
            sb.append(f(r)).append(' ').append(f(g)).append(' ').append(f(b)).append(" rg\n");
            return this;
        }

        Pen stroke(int r, int g, int b, float w) {
            sb.append(f(r)).append(' ').append(f(g)).append(' ').append(f(b)).append(" RG\n");
            sb.append(w).append(" w\n");
            return this;
        }

        Pen rect(int x, int y, int w, int h) {
            sb.append(x).append(' ').append(y).append(' ').append(w).append(' ').append(h)
              .append(" re f\n");
            return this;
        }

        Pen line(int x1, int y1, int x2, int y2) {
            sb.append(x1).append(' ').append(y1).append(" m ")
              .append(x2).append(' ').append(y2).append(" l S\n");
            return this;
        }

        Pen text(String s, int x, int y, int size, boolean bold, int r, int g, int b) {
            sb.append("BT\n")
              .append(bold ? "/F2 " : "/F1 ").append(size).append(" Tf\n")
              .append(f(r)).append(' ').append(f(g)).append(' ').append(f(b)).append(" rg\n")
              .append("1 0 0 1 ").append(x).append(' ').append(y).append(" Tm\n")
              .append(enc(s)).append(" Tj\n")
              .append("ET\n");
            return this;
        }

        Pen star(int cx, int cy, int outer, int inner, int pts) {
            sb.append("q\n");
            for (int i = 0; i < pts * 2; i++) {
                double a = Math.PI / pts * i - Math.PI / 2;
                double r = (i % 2 == 0) ? outer : inner;
                int x = (int) Math.round(cx + r * Math.cos(a));
                int y = (int) Math.round(cy + r * Math.sin(a));
                sb.append(x).append(' ').append(y).append(i == 0 ? " m\n" : " l\n");
            }
            sb.append("h f\nQ\n");
            return this;
        }

        byte[] build() {
            return sb.toString().getBytes(StandardCharsets.ISO_8859_1);
        }

        /** Convierte float RGB (0-255) al formato 0.000 de PDF */
        private static String f(int c) { return String.format("%.3f", c / 255f); }

        /** Codifica texto para literal PDF (soporta Latin-1 / ISO-8859-1) */
        private static String enc(String s) {
            StringBuilder b = new StringBuilder("(");
            for (char c : s.toCharArray()) {
                if      (c == '(')  b.append("\\(");
                else if (c == ')')  b.append("\\)");
                else if (c == '\\') b.append("\\\\");
                else if (c > 127 && c <= 255) b.append(String.format("\\%03o", (int) c));
                else if (c < 32)   b.append(String.format("\\%03o", (int) c));
                else               b.append(c);
            }
            b.append(')');
            return b.toString();
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  ENSAMBLADO DEL PDF
    // ══════════════════════════════════════════════════════════════════

    private static void assemblePDF(File out, byte[]... streams) throws IOException {
        int n = streams.length;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        List<Long> offs = new ArrayList<>();

        // Cabecera PDF
        w(baos, "%PDF-1.4\n");

        // Obj 1 – Catálogo
        offs.add((long) baos.size());
        w(baos, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

        // Obj 2 – Pages
        offs.add((long) baos.size());
        StringBuilder kids = new StringBuilder("[");
        for (int i = 0; i < n; i++) kids.append(3 + i).append(" 0 R ");
        w(baos, "2 0 obj\n<< /Type /Pages /Kids " + kids + "] /Count " + n + " >>\nendobj\n");

        // Fuentes: en objetos justo después de las pages + streams
        int fontBase = 3 + 2 * n;

        // Obj 3..3+n-1 – Objetos de página
        for (int i = 0; i < n; i++) {
            offs.add((long) baos.size());
            w(baos, (3 + i) + " 0 obj\n" +
                    "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 " + PW + " " + PH + "]\n" +
                    "   /Contents " + (3 + n + i) + " 0 R\n" +
                    "   /Resources << /Font << /F1 " + fontBase + " 0 R" +
                    " /F2 " + (fontBase + 1) + " 0 R >> >> >>\nendobj\n");
        }

        // Obj 3+n..3+2n-1 – Streams de contenido
        for (int i = 0; i < n; i++) {
            offs.add((long) baos.size());
            w(baos, (3 + n + i) + " 0 obj\n<< /Length " + streams[i].length + " >>\nstream\n");
            baos.write(streams[i]);
            w(baos, "\nendstream\nendobj\n");
        }

        // Fonts
        offs.add((long) baos.size());
        w(baos, fontBase + " 0 obj\n" +
                "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica" +
                " /Encoding /WinAnsiEncoding >>\nendobj\n");
        offs.add((long) baos.size());
        w(baos, (fontBase + 1) + " 0 obj\n" +
                "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold" +
                " /Encoding /WinAnsiEncoding >>\nendobj\n");

        // XRef
        long xref = baos.size();
        int total = 1 + offs.size();
        w(baos, "xref\n0 " + total + "\n");
        w(baos, "0000000000 65535 f \n");
        for (long o : offs) w(baos, String.format("%010d 00000 n \n", o));

        w(baos, "trailer\n<< /Size " + total + " /Root 1 0 R >>\n");
        w(baos, "startxref\n" + xref + "\n%%EOF\n");

        try (FileOutputStream fos = new FileOutputStream(out)) {
            baos.writeTo(fos);
        }
    }

    private static void w(ByteArrayOutputStream b, String s) throws IOException {
        b.write(s.getBytes(StandardCharsets.ISO_8859_1));
    }

    private static List<String> wrap(String text, int maxChars) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder cur = new StringBuilder();
        for (String word : words) {
            if (cur.length() + word.length() + 1 > maxChars && cur.length() > 0) {
                lines.add(cur.toString().trim());
                cur = new StringBuilder();
            }
            cur.append(word).append(' ');
        }
        String last = cur.toString().trim();
        if (!last.isEmpty()) lines.add(last);
        if (lines.isEmpty()) lines.add("");
        return lines;
    }
}
