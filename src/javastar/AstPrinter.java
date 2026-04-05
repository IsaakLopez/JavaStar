package javastar;

import javastar.Ast.*;
import java.util.List;

public class AstPrinter {

    // ── Interfaz funcional para imprimir cualquier nodo ──────────────────
    @FunctionalInterface
    private interface NodePrinter<T> {
        void print(T item, StringBuilder sb, String prefix, boolean isLast);
    }

    // ── Punto de entrada ─────────────────────────────────────────────────
    public String print(Program program) {
        StringBuilder sb = new StringBuilder();
        sb.append("Program\n");
        printList(program.statements(), sb, "", this::printStatement);
        return sb.toString();
    }

    // ── Helpers de estructura ─────────────────────────────────────────────

    /** Imprime una línea de nodo con el conector correcto. */
    private void node(StringBuilder sb, String prefix, boolean isLast, String label) {
        sb.append(prefix)
          .append(isLast ? "└── " : "├── ")
          .append(label)
          .append("\n");
    }

    /** Calcula el prefijo de los hijos según si el padre era el último. */
    private String childPrefix(String prefix, boolean isLast) {
        return prefix + (isLast ? "    " : "│   ");
    }

    /** Imprime una lista de nodos del mismo tipo con conectores correctos. */
    private <T> void printList(List<T> items, StringBuilder sb, String prefix, NodePrinter<T> printer) {
        for (int i = 0; i < items.size(); i++) {
            printer.print(items.get(i), sb, prefix, i == items.size() - 1);
        }
    }

    // ── Sentencias ────────────────────────────────────────────────────────
    private void printStatement(Statement stmt, StringBuilder sb, String prefix, boolean isLast) {
        String cp = childPrefix(prefix, isLast);
        switch (stmt) {
            case VarDecl v -> {
                node(sb, prefix, isLast, "VarDecl [" + v.typeName() + "]  " + v.name());
                printExpr(v.initializer(), sb, cp, true);
            }
            case Assignment a -> {
                node(sb, prefix, isLast, "Assignment  " + a.name());
                printExpr(a.value(), sb, cp, true);
            }
            case PrintStmt p -> {
                node(sb, prefix, isLast, "ImprimirStmt");
                printExpr(p.value(), sb, cp, true);
            }
            case IfStmt i -> {
                node(sb, prefix, isLast, "SiStmt");
                boolean hasElse = !i.elseBranch().isEmpty();
                // Condición
                node(sb, cp, false, "Condicion");
                printExpr(i.condition(), sb, childPrefix(cp, false), true);
                // Entonces
                node(sb, cp, !hasElse, "Entonces");
                printList(i.thenBranch(), sb, childPrefix(cp, !hasElse), this::printStatement);
                // Sino (opcional)
                if (hasElse) {
                    node(sb, cp, true, "Sino");
                    printList(i.elseBranch(), sb, childPrefix(cp, true), this::printStatement);
                }
            }
            case WhileStmt w -> {
                node(sb, prefix, isLast, "MientrasStmt");
                boolean hasBody = !w.body().isEmpty();
                node(sb, cp, !hasBody, "Condicion");
                printExpr(w.condition(), sb, childPrefix(cp, !hasBody), true);
                if (hasBody) {
                    node(sb, cp, true, "Cuerpo");
                    printList(w.body(), sb, childPrefix(cp, true), this::printStatement);
                }
            }
            case ForStmt f -> {
                node(sb, prefix, isLast, "ParaStmt");
                node(sb, cp, false, "Inicio");
                printStatement(f.initializer(), sb, childPrefix(cp, false), true);
                node(sb, cp, false, "Condicion");
                printExpr(f.condition(), sb, childPrefix(cp, false), true);
                node(sb, cp, false, "Incremento");
                printStatement(f.increment(), sb, childPrefix(cp, false), true);
                node(sb, cp, true, "Cuerpo");
                printList(f.body(), sb, childPrefix(cp, true), this::printStatement);
            }
            case SwitchStmt s -> {
                node(sb, prefix, isLast, "SeleccionarStmt");
                boolean hasDefault = !s.defaultBranch().isEmpty();
                boolean noCases    = s.cases().isEmpty();
                // Objetivo
                node(sb, cp, noCases && !hasDefault, "Objetivo");
                printExpr(s.target(), sb, childPrefix(cp, noCases && !hasDefault), true);
                // Casos
                List<CaseBlock> cases = s.cases();
                for (int i = 0; i < cases.size(); i++) {
                    CaseBlock c = cases.get(i);
                    boolean caseLast = (i == cases.size() - 1) && !hasDefault;
                    node(sb, cp, caseLast, "Caso");
                    String caseCp = childPrefix(cp, caseLast);
                    boolean bodyEmpty = c.body().isEmpty();
                    node(sb, caseCp, bodyEmpty, "Valor");
                    printExpr(c.value(), sb, childPrefix(caseCp, bodyEmpty), true);
                    if (!bodyEmpty) {
                        node(sb, caseCp, true, "Cuerpo");
                        printList(c.body(), sb, childPrefix(caseCp, true), this::printStatement);
                    }
                }
                // Defecto (opcional)
                if (hasDefault) {
                    node(sb, cp, true, "Defecto");
                    printList(s.defaultBranch(), sb, childPrefix(cp, true), this::printStatement);
                }
            }
            case ExprStmt e -> {
                node(sb, prefix, isLast, "ExprStmt");
                printExpr(e.value(), sb, cp, true);
            }
        }
    }

    // ── Expresiones ───────────────────────────────────────────────────────
    private void printExpr(Expression expr, StringBuilder sb, String prefix, boolean isLast) {
        String cp = childPrefix(prefix, isLast);
        switch (expr) {
            case Literal  l -> node(sb, prefix, isLast, "Literal: "  + l.value());
            case Variable v -> node(sb, prefix, isLast, "Variable: " + v.name());
            case Postfix  p -> node(sb, prefix, isLast, "Postfix: "  + p.name() + p.operator());
            case Grouping g -> {
                node(sb, prefix, isLast, "Grouping");
                printExpr(g.expr(), sb, cp, true);
            }
            case Unary u -> {
                node(sb, prefix, isLast, "Unary: " + u.operator());
                printExpr(u.right(), sb, cp, true);
            }
            case Binary b -> {
                node(sb, prefix, isLast, "Binary: " + b.operator());
                printExpr(b.left(),  sb, cp, false);
                printExpr(b.right(), sb, cp, true);
            }
            case MemberCall m -> {
                node(sb, prefix, isLast, "MemberCall: " + m.target() + "." + m.member());
                printList(m.args(), sb, cp, this::printExpr);
            }
        }
    }
}
