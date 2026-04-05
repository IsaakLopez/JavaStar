package javastar;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

public class StarIDE extends JFrame {

    // ── Paleta de colores: espacio profundo ────────────────────────────────
    private static final Color BG        = new Color(10,  10,  22);
    private static final Color BG_EDITOR = new Color(13,  13,  28);
    private static final Color BG_GUTTER = new Color(16,  16,  36);
    private static final Color BG_PANEL  = new Color(18,  18,  38);
    private static final Color BG_OUT    = new Color( 8,   8,  20);
    private static final Color C_BORDER  = new Color(40,  40,  80);
    private static final Color C_GOLD    = new Color(255, 200,  50);
    private static final Color C_CYAN    = new Color( 80, 220, 255);
    private static final Color C_GREEN   = new Color( 50, 220, 100);
    private static final Color C_RED     = new Color(255,  80,  80);
    private static final Color C_DIM     = new Color(110, 110, 170);
    private static final Color C_TEXT    = new Color(220, 220, 255);
    private static final Color C_TAB    = new Color(50, 50, 140);
    private static final Color C_KW      = new Color(255, 185,  50);   // keywords
    private static final Color C_STR     = new Color( 80, 210, 130);   // strings
    private static final Color C_NUM     = new Color(120, 175, 255);   // números
    private static final Color C_CMT     = new Color( 80,  80, 130);   // comentarios

    private static final String[] KEYWORDS = {
        "main","entero","decimal","texto","booleano","escanear",
        "si","sino","mientras","para","seleccionar","caso","defecto",
        "Y","O","NO","verdadero","falso","star","imprimir","nuevo"
    };

    // ── Componentes principales ────────────────────────────────────────────
    private JTextPane  editor;
    private JTextArea  outputArea, tokenArea, astArea, errorArea;
    private JTabbedPane tabs;
    private JLabel     statusLabel;
    private boolean    highlighting = false;
    private File       currentFile  = null;

    // ══════════════════════════════════════════════════════════════════════
    public StarIDE() {
        super("JavaStar IDE");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1260, 780);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        add(buildHeader(),    BorderLayout.NORTH);
        add(buildContent(),   BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        applyHighlighting();
        setVisible(true);
    }

    // ── HEADER: banner con campo de estrellas ──────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Fondo degradado oscuro
                g2.setPaint(new GradientPaint(0, 0, new Color(5, 5, 25),
                        getWidth(), 0, new Color(28, 8, 55)));
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Muchas estrellas pequeñas (semilla fija → siempre iguales)
                Random rnd = new Random(42);
                for (int i = 0; i < 160; i++) {
                    int alpha = 60 + rnd.nextInt(160);
                    g2.setColor(new Color(190, 210, 255, alpha));
                    g2.fillOval(rnd.nextInt(getWidth()), rnd.nextInt(getHeight()),
                            rnd.nextInt(2) + 1, rnd.nextInt(2) + 1);
                }

                // Estrellas con forma de 5 puntas
                Random rnd2 = new Random(99);
                g2.setColor(new Color(255, 220, 80, 210));
                for (int i = 0; i < 6; i++) {
                    paintStar(g2,
                            rnd2.nextInt(getWidth()),
                            rnd2.nextInt(getHeight()), 6, 2, 5);
                }

                // Línea sutil de brillo inferior
                g2.setPaint(new GradientPaint(
                        0, getHeight() - 2, new Color(C_GOLD.getRed(), C_GOLD.getGreen(), 0, 0),
                        getWidth() / 2, getHeight() - 2, new Color(255, 200, 50, 160)));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawLine(0, getHeight() - 2, getWidth(), getHeight() - 2);
            }
        };
        header.setPreferredSize(new Dimension(0, 74));
        header.setOpaque(false);

        // Logo
        JLabel logo = new JLabel("JavaStar");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 30));
        logo.setForeground(C_GOLD);

        JLabel tagline = new JLabel("     Compilador  ·  Analizador  ·  Intérprete");
        tagline.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        tagline.setForeground(C_DIM);

        JPanel logoBox = new JPanel(new GridLayout(2, 1));
        logoBox.setOpaque(false);
        logoBox.setBorder(BorderFactory.createEmptyBorder(8, 18, 6, 0));
        logoBox.add(logo);
        logoBox.add(tagline);

        // Barra de herramientas
        JButton btnNew   = makeButton("Probar",      C_DIM);
        JButton btnOpen  = makeButton("Abrir",      C_CYAN);
        JButton btnSave  = makeButton("Guardar",    C_GOLD);
        JButton btnRun   = makeButton("Ejecutar",   C_GREEN);
        JButton btnClear = makeButton("Limpiar",    C_RED);
        btnRun.setFont(new Font("Segoe UI", Font.BOLD, 13));

        btnNew.addActionListener  (e -> newFile());
        btnOpen.addActionListener (e -> openFile());
        btnSave.addActionListener (e -> saveFile());
        btnRun.addActionListener  (e -> runCode());
        btnClear.addActionListener(e -> clearOutput());

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 20));
        toolbar.setOpaque(false);
        toolbar.add(btnNew);
        toolbar.add(btnOpen);
        toolbar.add(btnSave);
        toolbar.add(btnRun);
        toolbar.add(btnClear);

        header.add(logoBox, BorderLayout.WEST);
        header.add(toolbar, BorderLayout.EAST);
        return header;
    }

    // ── CONTENIDO: editor ⟷ salida ─────────────────────────────────────────
    private JSplitPane buildContent() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildEditorPanel(), buildOutputPanel());
        split.setDividerLocation(560);
        split.setDividerSize(5);
        split.setBackground(C_BORDER);
        split.setBorder(null);
        split.setContinuousLayout(true);
        return split;
    }

    // ── PANEL EDITOR ───────────────────────────────────────────────────────
    private JPanel buildEditorPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_EDITOR);
        panel.add(panelTitle("  Editor  JavaStar", C_CYAN), BorderLayout.NORTH);

        // JTextPane con coloreado sintáctico
        editor = new JTextPane();
        editor.setBackground(BG_EDITOR);
        editor.setForeground(C_TEXT);
        editor.setCaretColor(C_GOLD);
        editor.setSelectionColor(new Color(60, 60, 120));
        editor.setFont(monoFont(14));
        editor.setMargin(new Insets(6, 8, 6, 8));
        editor.setText(sampleCode());
        SwingUtilities.invokeLater(() -> applyTabSize(editor, 4));

        // Debounce: resaltar 280 ms después de la última tecla
        javax.swing.Timer hlTimer = new javax.swing.Timer(280, e -> applyHighlighting());
        hlTimer.setRepeats(false);
        editor.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) {
                hlTimer.restart();
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_ENTER) runCode();
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_S)     saveFile();
            }
        });

        // Números de línea
        JTextArea gutter = new JTextArea("1");
        gutter.setBackground(BG_GUTTER);
        gutter.setForeground(new Color(70, 70, 120));
        gutter.setFont(monoFont(14));
        gutter.setEditable(false);
        gutter.setMargin(new Insets(6, 10, 6, 10));
        gutter.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, C_BORDER));

        editor.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate (javax.swing.event.DocumentEvent e) { refreshGutter(gutter); }
            public void removeUpdate (javax.swing.event.DocumentEvent e) { refreshGutter(gutter); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { }
        });

        JScrollPane scroll = new JScrollPane(editor);
        scroll.setRowHeaderView(gutter);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_EDITOR);
        darkScrollBars(scroll);

        panel.add(scroll, BorderLayout.CENTER);
        panel.add(labelBar("  Ctrl+Enter  para ejecutar  ·  Ctrl+S  para guardar", C_DIM, false), BorderLayout.SOUTH);
        return panel;
    }

    // ── PANEL DE SALIDA ────────────────────────────────────────────────────
    private JPanel buildOutputPanel() {
        outputArea = resultArea(C_GREEN);
        tokenArea  = resultArea(new Color(120, 175, 255));
        astArea    = resultArea(new Color(190, 150, 255));
        errorArea  = resultArea(C_RED);

        tabs = new JTabbedPane();
        tabs.setBackground(BG_PANEL);
        tabs.setForeground(C_TAB);
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        tabs.addTab("Salida",   scrollOf(outputArea));
        tabs.addTab("Tokens",   scrollOf(tokenArea));
        tabs.addTab("Árbol",    scrollOf(astArea));
        tabs.addTab("Errores",  scrollOf(errorArea));

        // Colores de tab seleccionado/no seleccionado
        tabs.addChangeListener(e -> repaintTabs());

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(panelTitle("Resultados", C_GOLD), BorderLayout.NORTH);
        panel.add(tabs, BorderLayout.CENTER);
        panel.setBackground(BG);
        return panel;
    }

    // ── BARRA DE ESTADO ────────────────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(5, 5, 14));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, C_BORDER));
        bar.setPreferredSize(new Dimension(0, 26));

        statusLabel = new JLabel("JavaStar listo. Escribe tu código y presiona Ejecutar");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(C_DIM);

        JLabel ver = new JLabel("JavaStar  v1.0  ★  ");
        ver.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        ver.setForeground(new Color(45, 45, 85));

        bar.add(statusLabel, BorderLayout.WEST);
        bar.add(ver, BorderLayout.EAST);
        return bar;
    }

    // ══ ACCIÓN: EJECUTAR ══════════════════════════════════════════════════
    private void runCode() {
        status("  ⚡ Compilando...", C_GOLD);
        outputArea.setText("");
        tokenArea.setText("");
        astArea.setText("");
        errorArea.setText("");
        tabs.setSelectedIndex(0);

        String source = editor.getText();

        // 1) Análisis léxico
        Lexer lexer = new Lexer();
        Lexer.LexResult lex = lexer.scan(source);

        StringBuilder tokBuf = new StringBuilder();
        lex.tokens().forEach(t -> tokBuf.append(t).append("\n"));
        tokenArea.setText(tokBuf.toString());

        if (!lex.errors().isEmpty()) {
            showErrors("ERRORES LÉXICOS", lex.errors());
            status("Error léxico — revisa la pestaña Errores", C_RED);
            return;
        }

        // 2) Análisis sintáctico
        Parser parser = new Parser(lex.tokens());
        Parser.ParseResult parse = parser.parse();
        astArea.setText(new AstPrinter().print(parse.program()));

        if (!parse.errors().isEmpty()) {
            showErrors("ERRORES SINTÁCTICOS", parse.errors());
            status("Error sintáctico — revisa la pestaña Errores", C_RED);
            return;
        }

        // 3) Interpretación en hilo separado para salida en tiempo real
        status("  ⚡ Ejecutando...", C_GOLD);

        PrintStream original = System.out;
        PrintStream realTime = new PrintStream(new OutputStream() {
            @Override public void write(byte[] b, int off, int len) {
                String text = new String(b, off, len);
                SwingUtilities.invokeLater(() -> outputArea.append(text));
            }
            @Override public void write(int b) { write(new byte[]{(byte) b}, 0, 1); }
        }, true);
        System.setOut(realTime);

        Interpreter interp = new Interpreter();
        interp.setInputProvider(varName -> {
            String[] result = {""};
            try {
                SwingUtilities.invokeAndWait(() -> {
                    String val = JOptionPane.showInputDialog(
                        this, "Ingresa el valor para: " + varName,
                        varName, JOptionPane.PLAIN_MESSAGE);
                    result[0] = (val != null) ? val : "";
                });
            } catch (Exception ignored) {}
            return result[0];
        });

        new Thread(() -> {
            try {
                interp.execute(parse.program());
            } finally {
                System.setOut(original);
                SwingUtilities.invokeLater(() -> {
                    if (!interp.getErrors().isEmpty()) {
                        showErrors("ERRORES EN EJECUCIÓN", interp.getErrors());
                        status(" Ejecutado con errores — revisa Errores", new Color(255, 180, 50));
                    } else {
                        status(" Ejecución completada exitosamente", C_GREEN);
                    }
                });
            }
        }, "javastar-exec").start();
    }

    private void newFile() {
        int r = JOptionPane.showConfirmDialog(this,
                "¿Descartar el código actual y empezar de cero?",
                "Nuevo archivo", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) { editor.setText(sampleCode()); applyHighlighting(); }
    }

    private void openFile() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Archivos JavaStar (*.jstar)", "jstar"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                currentFile = fc.getSelectedFile();
                String content = new String(java.nio.file.Files.readAllBytes(currentFile.toPath()));
                editor.setText(content);
                applyTabSize(editor, 4);
                applyHighlighting();
                status("  ✦ Archivo cargado: " + currentFile.getName(), C_CYAN);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "No se pudo leer el archivo.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveFile() {
        if (currentFile == null) {
            // Sin archivo actual → pedir dónde guardar
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Archivos JavaStar (*.jstar)", "jstar"));
            fc.setSelectedFile(new File("programa.jstar"));
            if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
            File selected = fc.getSelectedFile();
            // Asegurar extensión .jstar
            if (!selected.getName().endsWith(".jstar"))
                selected = new File(selected.getParentFile(), selected.getName() + ".jstar");
            currentFile = selected;
        }
        try {
            java.nio.file.Files.writeString(currentFile.toPath(), editor.getText());
            status("  ✔ Guardado: " + currentFile.getName(), C_GOLD);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo guardar el archivo:\n" + ex.getMessage(),
                    "Error al guardar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearOutput() {
        outputArea.setText(""); tokenArea.setText(""); astArea.setText(""); errorArea.setText("");
        status(" Salida limpiada.", C_DIM);
    }

    // ══ SYNTAX HIGHLIGHTING ═══════════════════════════════════════════════
    private void applyHighlighting() {
        if (highlighting) return;
        highlighting = true;
        int caret = editor.getCaretPosition();
        try {
            String text = editor.getText();
            StyledDocument doc = editor.getStyledDocument();

            // Estilo base
            SimpleAttributeSet base = new SimpleAttributeSet();
            StyleConstants.setForeground(base, C_TEXT);
            StyleConstants.setBold(base, false);
            doc.setCharacterAttributes(0, text.length(), base, true);

            colorize(doc, text, "\"[^\"\\n]*\"",           C_STR, false);  // strings
            colorize(doc, text, "\\b\\d+(\\.\\d+)?\\b",    C_NUM, false);  // números
            colorize(doc, text, "#.*",    new Color(75, 75, 125), false);   // comentarios
            for (String kw : KEYWORDS)
                colorize(doc, text, "\\b" + Pattern.quote(kw) + "\\b", C_KW, true);

        } catch (Exception ignored) {
        } finally {
            applyTabSize(editor, 4);
            try { editor.setCaretPosition(caret); } catch (Exception ignored2) {}
            highlighting = false;
        }
    }

    private void applyTabSize(JTextPane pane, int chars) {
        FontMetrics fm = pane.getFontMetrics(pane.getFont());
        int tabWidth = chars * fm.charWidth(' ');
        TabStop[] stops = new TabStop[50];
        for (int i = 0; i < 50; i++) stops[i] = new TabStop((i + 1) * tabWidth);
        SimpleAttributeSet a = new SimpleAttributeSet();
        StyleConstants.setTabSet(a, new TabSet(stops));
        pane.getStyledDocument().setParagraphAttributes(0, pane.getDocument().getLength(), a, false);
    }

    private void colorize(StyledDocument doc, String text, String regex, Color color, boolean bold) {
        SimpleAttributeSet s = new SimpleAttributeSet();
        StyleConstants.setForeground(s, color);
        StyleConstants.setBold(s, bold);
        Matcher m = Pattern.compile(regex).matcher(text);
        while (m.find())
            doc.setCharacterAttributes(m.start(), m.end() - m.start(), s, false);
    }

    // ══ HELPERS UI ════════════════════════════════════════════════════════
    private void showErrors(String title, java.util.List<String> errs) {
        String sep = "═".repeat(46);
        errorArea.setText(sep + "\n  ⚠  " + title + "\n" + sep + "\n\n"
                + String.join("\n\n", errs) + "\n");
        tabs.setSelectedIndex(3);
    }

    private void status(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
    }

    private void refreshGutter(JTextArea gutter) {
        SwingUtilities.invokeLater(() -> {
            int n = editor.getText().split("\n", -1).length;
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= n; i++) sb.append(i).append("\n");
            gutter.setText(sb.toString());
        });
    }

    private void repaintTabs() { tabs.repaint(); }

    private JLabel panelTitle(String text, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(color);
        lbl.setBackground(BG_PANEL);
        lbl.setOpaque(true);
        lbl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, C_BORDER),
                BorderFactory.createEmptyBorder(7, 8, 7, 8)));
        return lbl;
    }

    private JLabel labelBar(String text, Color color, boolean top) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(color);
        lbl.setBackground(BG_PANEL);
        lbl.setOpaque(true);
        lbl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(top ? 0 : 1, 0, top ? 1 : 0, 0, C_BORDER),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        return lbl;
    }

    private JTextArea resultArea(Color fg) {
        JTextArea a = new JTextArea();
        a.setBackground(BG_OUT);
        a.setForeground(fg);
        a.setFont(monoFont(13));
        a.setEditable(false);
        a.setMargin(new Insets(10, 12, 10, 12));
        a.setLineWrap(false);
        return a;
    }

    private JScrollPane scrollOf(JTextArea a) {
        JScrollPane sp = new JScrollPane(a);
        sp.setBorder(null);
        darkScrollBars(sp);
        return sp;
    }

    private void darkScrollBars(JScrollPane sp) {
        sp.getVerticalScrollBar()  .setBackground(BG_PANEL);
        sp.getHorizontalScrollBar().setBackground(BG_PANEL);
        sp.setBackground(BG);
    }

    // Botón redondeado con borde de color
    private JButton makeButton(String text, Color accent) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int r = accent.getRed(), gr = accent.getGreen(), b = accent.getBlue();
                Color bg = getModel().isPressed()
                        ? new Color(r, gr, b, 80)
                        : getModel().isRollover()
                        ? new Color(r, gr, b, 50)
                        : new Color(r, gr, b, 22);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 9, 9);
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(1.3f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 9, 9);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(accent);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(128, 34));
        return btn;
    }

    // Dibuja estrella de N puntas centrada en (cx, cy)
    private void paintStar(Graphics2D g2, int cx, int cy, int outer, int inner, int pts) {
        double angle = Math.PI / pts;
        int[] xs = new int[pts * 2], ys = new int[pts * 2];
        for (int i = 0; i < pts * 2; i++) {
            double r = (i % 2 == 0) ? outer : inner;
            xs[i] = (int)(cx + r * Math.sin(i * angle));
            ys[i] = (int)(cy - r * Math.cos(i * angle));
        }
        g2.fillPolygon(xs, ys, pts * 2);
    }

    private static Font monoFont(int size) {
        for (String name : new String[]{"Consolas", "Cascadia Code", "Courier New"}) {
            Font f = new Font(name, Font.PLAIN, size);
            if (f.getFamily().equalsIgnoreCase(name)) return f;
        }
        return new Font(Font.MONOSPACED, Font.PLAIN, size);
    }

    private static String sampleCode() {
        return
            "main\n" +
            "\tentero x = 10\n" +
            "\tentero y = 20\n" +
            "\n" +
            "\tsi x < y Y y > 5\n" +
            "\t\tstar.imprimir(\"X es menor que Y\")\n" +
            "\tsino\n" +
            "\t\tstar.imprimir(\"X es mayor que Y\")\n" +
            "\n" +
            "\tpara entero i = 0; i < 3; i++\n" +
            "\t\tstar.imprimir(i)\n" +
            "\n" +
            "\tmientras x < 15\n" +
            "\t\tx = x + 1\n" +
            "\n" +
            "\tseleccionar x\n" +
            "\t\tcaso 15\n" +
            "\t\t\tstar.imprimir(\"X vale 15\")\n" +
            "\t\tdefecto\n" +
            "\t\t\tstar.imprimir(\"Otro valor\")\n";
    }

    // ── Punto de entrada para lanzar el IDE ───────────────────────────────
    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new StarIDE();
        });
    }
}
