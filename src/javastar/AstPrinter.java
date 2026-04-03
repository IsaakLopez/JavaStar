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
                node(sb, prefix, isLast, "PrintStmt");
                printExpr(p.value(), sb, cp, true);
            }
            case IfStmt i -> {
                node(sb, prefix, isLast, "IfStmt");
                boolean hasElse = !i.elseBranch().isEmpty();
                // Condition
                node(sb, cp, false, "Condition");
                printExpr(i.condition(), sb, childPrefix(cp, false), true);
                // Then
                node(sb, cp, !hasElse, "Then");
                printList(i.thenBranch(), sb, childPrefix(cp, !hasElse), this::printStatement);
                // Else (opcional)
                if (hasElse) {
                    node(sb, cp, true, "Else");
                    printList(i.elseBranch(), sb, childPrefix(cp, true), this::printStatement);
                }
            }
            case WhileStmt w -> {
                node(sb, prefix, isLast, "WhileStmt");
                boolean hasBody = !w.body().isEmpty();
                node(sb, cp, !hasBody, "Condition");
                printExpr(w.condition(), sb, childPrefix(cp, !hasBody), true);
                if (hasBody) {
                    node(sb, cp, true, "Body");
                    printList(w.body(), sb, childPrefix(cp, true), this::printStatement);
                }
            }
            case ForStmt f -> {
                node(sb, prefix, isLast, "ForStmt");
                node(sb, cp, false, "Init");
                printStatement(f.initializer(), sb, childPrefix(cp, false), true);
                node(sb, cp, false, "Condition");
                printExpr(f.condition(), sb, childPrefix(cp, false), true);
                node(sb, cp, false, "Increment");
                printStatement(f.increment(), sb, childPrefix(cp, false), true);
                node(sb, cp, true, "Body");
                printList(f.body(), sb, childPrefix(cp, true), this::printStatement);
            }
            case SwitchStmt s -> {
                node(sb, prefix, isLast, "SwitchStmt");
                boolean hasDefault = !s.defaultBranch().isEmpty();
                boolean noCases    = s.cases().isEmpty();
                // Target
                node(sb, cp, noCases && !hasDefault, "Target");
                printExpr(s.target(), sb, childPrefix(cp, noCases && !hasDefault), true);
                // Cases
                List<CaseBlock> cases = s.cases();
                for (int i = 0; i < cases.size(); i++) {
                    CaseBlock c = cases.get(i);
                    boolean caseLast = (i == cases.size() - 1) && !hasDefault;
                    node(sb, cp, caseLast, "Case");
                    String caseCp = childPrefix(cp, caseLast);
                    boolean bodyEmpty = c.body().isEmpty();
                    node(sb, caseCp, bodyEmpty, "Value");
                    printExpr(c.value(), sb, childPrefix(caseCp, bodyEmpty), true);
                    if (!bodyEmpty) {
                        node(sb, caseCp, true, "Body");
                        printList(c.body(), sb, childPrefix(caseCp, true), this::printStatement);
                    }
                }
                // Default (opcional)
                if (hasDefault) {
                    node(sb, cp, true, "Default");
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
