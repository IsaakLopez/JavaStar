package javastar;

import java.util.List;

public class Ast {
    public sealed interface Node permits Program, Statement, Expression {}

    public record Program(List<Statement> statements) implements Node {}

    public sealed interface Statement extends Node permits VarDecl, Assignment, PrintStmt, IfStmt, WhileStmt, ForStmt, SwitchStmt, ExprStmt {}

    public record VarDecl(String typeName, String name, Expression initializer) implements Statement {}
    public record Assignment(String name, Expression value) implements Statement {}
    public record PrintStmt(Expression value) implements Statement {}
    public record IfStmt(Expression condition, List<Statement> thenBranch, List<Statement> elseBranch) implements Statement {}
    public record WhileStmt(Expression condition, List<Statement> body) implements Statement {}
    public record ForStmt(Statement initializer, Expression condition, Statement increment, List<Statement> body) implements Statement {}
    public record SwitchStmt(Expression target, List<CaseBlock> cases, List<Statement> defaultBranch) implements Statement {}
    public record CaseBlock(Expression value, List<Statement> body) {}
    public record ExprStmt(Expression value) implements Statement {}

    public sealed interface Expression extends Node permits Binary, Unary, Literal, Variable, Grouping, MemberCall, Postfix {}
    public record Binary(Expression left, String operator, Expression right) implements Expression {}
    public record Unary(String operator, Expression right) implements Expression {}
    public record Literal(Object value) implements Expression {}
    public record Variable(String name) implements Expression {}
    public record Grouping(Expression expr) implements Expression {}
    public record MemberCall(String target, String member, List<Expression> args) implements Expression {}
    public record Postfix(String name, String operator) implements Expression {}
}
