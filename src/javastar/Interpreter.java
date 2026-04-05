package javastar;

import javastar.Ast.*;
import java.util.*;
import java.util.function.Function;

public class Interpreter {
    private final Map<String, Object> environment = new LinkedHashMap<>();
    private final List<String> errors = new ArrayList<>();
    private Function<String, String> inputProvider = prompt -> {
        System.out.print(prompt + ": ");
        try { return new java.util.Scanner(System.in).nextLine(); }
        catch (Exception e) { return ""; }
    };

    public void setInputProvider(Function<String, String> provider) {
        this.inputProvider = provider;
    }

    public List<String> getErrors() { return errors; }

    public void execute(Program program) {
        for (Statement stmt : program.statements()) {
            executeStatement(stmt);
        }
    }

    private void executeStatement(Statement stmt) {
        switch (stmt) {
            case VarDecl d -> {
                if (d.typeName().equals("escanear")) {
                    String raw = inputProvider.apply(d.name());
                    environment.put(d.name(), parseInput(raw));
                } else {
                    environment.put(d.name(), evaluate(d.initializer()));
                }
            }
            case Assignment a -> {
                if (!environment.containsKey(a.name())) {
                    errors.add("Error de ejecución: variable '" + a.name() + "' no declarada");
                    return;
                }
                environment.put(a.name(), evaluate(a.value()));
            }
            case PrintStmt p -> System.out.println(stringify(evaluate(p.value())));
            case IfStmt i -> {
                if (isTruthy(evaluate(i.condition()))) {
                    for (Statement s : i.thenBranch()) executeStatement(s);
                } else {
                    for (Statement s : i.elseBranch()) executeStatement(s);
                }
            }
            case WhileStmt w -> {
                while (isTruthy(evaluate(w.condition()))) {
                    for (Statement s : w.body()) executeStatement(s);
                }
            }
            case ForStmt f -> {
                executeStatement(f.initializer());
                while (isTruthy(evaluate(f.condition()))) {
                    for (Statement s : f.body()) executeStatement(s);
                    executeStatement(f.increment());
                }
            }
            case SwitchStmt sw -> {
                Object target = evaluate(sw.target());
                boolean matched = false;
                for (CaseBlock c : sw.cases()) {
                    if (isEqual(target, evaluate(c.value()))) {
                        for (Statement s : c.body()) executeStatement(s);
                        matched = true;
                        break;
                    }
                }
                if (!matched) {
                    for (Statement s : sw.defaultBranch()) executeStatement(s);
                }
            }
            case ExprStmt e -> evaluate(e.value());
        }
    }

    private Object evaluate(Expression expr) {
        return switch (expr) {
            case Literal l -> l.value();
            case Variable v -> {
                if (!environment.containsKey(v.name())) {
                    errors.add("Error de ejecución: variable '" + v.name() + "' no declarada");
                    yield 0;
                }
                yield environment.get(v.name());
            }
            case Grouping g -> evaluate(g.expr());
            case Unary u -> {
                Object right = evaluate(u.right());
                yield switch (u.operator()) {
                    case "-"  -> negate(right);
                    case "NO" -> !isTruthy(right);
                    default    -> right;
                };
            }
            case Binary b -> {
                Object left = evaluate(b.left());
                if (b.operator().equals("Y")) yield isTruthy(left) && isTruthy(evaluate(b.right()));
                if (b.operator().equals("O")) yield isTruthy(left) || isTruthy(evaluate(b.right()));
                Object right = evaluate(b.right());
                yield switch (b.operator()) {
                    case "+"  -> add(left, right);
                    case "-"  -> subtract(left, right);
                    case "*"  -> multiply(left, right);
                    case "/"  -> divide(left, right);
                    case "%"  -> modulo(left, right);
                    case "<"  -> compare(left, right) < 0;
                    case "<=" -> compare(left, right) <= 0;
                    case ">"  -> compare(left, right) > 0;
                    case ">=" -> compare(left, right) >= 0;
                    case "==" -> isEqual(left, right);
                    case "!=" -> !isEqual(left, right);
                    default   -> null;
                };
            }
            case Postfix p -> {
                Object cur = environment.getOrDefault(p.name(), 0);
                Object next = p.operator().equals("++") ? add(cur, 1) : subtract(cur, 1);
                environment.put(p.name(), next);
                yield cur;
            }
            case MemberCall mc -> null;
        };
    }

    // --- Aritmética ---

    private Object add(Object a, Object b) {
        if (a instanceof String || b instanceof String) return stringify(a) + stringify(b);
        if (a instanceof Double || b instanceof Double) return toDouble(a) + toDouble(b);
        return toInt(a) + toInt(b);
    }

    private Object subtract(Object a, Object b) {
        if (a instanceof Double || b instanceof Double) return toDouble(a) - toDouble(b);
        return toInt(a) - toInt(b);
    }

    private Object multiply(Object a, Object b) {
        if (a instanceof Double || b instanceof Double) return toDouble(a) * toDouble(b);
        return toInt(a) * toInt(b);
    }

    private Object divide(Object a, Object b) {
        if (toDouble(b) == 0) {
            errors.add("Error de ejecución: división por cero");
            return 0;
        }
        if (a instanceof Double || b instanceof Double) return toDouble(a) / toDouble(b);
        return toInt(a) / toInt(b);
    }

    private Object modulo(Object a, Object b) {
        if (a instanceof Double || b instanceof Double) return toDouble(a) % toDouble(b);
        return toInt(a) % toInt(b);
    }

    private Object negate(Object a) {
        if (a instanceof Double d) return -d;
        return -toInt(a);
    }

    // --- Comparación ---

    private int compare(Object a, Object b) {
        if (a instanceof Number && b instanceof Number) return Double.compare(toDouble(a), toDouble(b));
        return stringify(a).compareTo(stringify(b));
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        if (a instanceof Integer ia && b instanceof Double db) return ia.doubleValue() == db;
        if (a instanceof Double da && b instanceof Integer ib) return da == ib.doubleValue();
        return a.equals(b);
    }

    private boolean isTruthy(Object val) {
        if (val == null) return false;
        if (val instanceof Boolean bl) return bl;
        return true;
    }

    // --- Conversión de tipos ---

    private int toInt(Object v) {
        if (v instanceof Integer i) return i;
        if (v instanceof Double d) return d.intValue();
        try { return Integer.parseInt(stringify(v)); } catch (NumberFormatException e) { return 0; }
    }

    private double toDouble(Object v) {
        if (v instanceof Double d) return d;
        if (v instanceof Integer i) return i.doubleValue();
        try { return Double.parseDouble(stringify(v)); } catch (NumberFormatException e) { return 0; }
    }

    private String stringify(Object v) {
        if (v == null) return "null";
        if (v instanceof Double d && d == Math.floor(d) && !Double.isInfinite(d)) return String.valueOf(d.intValue());
        return v.toString();
    }

    private Object parseInput(String raw) {
        if (raw == null) return "";
        String trimmed = raw.trim();
        if (trimmed.equals("true")) return true;
        if (trimmed.equals("false")) return false;
        try { return Integer.parseInt(trimmed); } catch (NumberFormatException ignored) {}
        try { return Double.parseDouble(trimmed); } catch (NumberFormatException ignored) {}
        return trimmed;
    }
}
