package me.hydos.unluac.decompile.expression;

import me.hydos.unluac.decompile.core.Decompiler;
import me.hydos.unluac.decompile.core.Local;
import me.hydos.unluac.decompile.core.Output;
import me.hydos.unluac.decompile.core.Walker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BinaryExpression extends Expression {

    public String op;
    public Expression left;
    public Expression right;
    private final int associativity;

    public BinaryExpression(String op, Expression left, Expression right, int precedence, int associativity) {
        super(precedence);
        this.op = op;
        this.left = left;
        this.right = right;
        this.associativity = associativity;
    }

    public static BinaryExpression replaceRight(BinaryExpression template, Expression replacement) {
        return new BinaryExpression(template.op, template.left, replacement, template.precedence, template.associativity);
    }

    @Override
    public void walk(Walker w) {
        w.visitExpression(this);
        left.walk(w);
        right.walk(w);
    }

    @Override
    public void print(Decompiler d, Output out) {
        final var leftGroup = leftGroup();
        final var rightGroup = rightGroup();
        if (leftGroup) out.print("(");
        left.print(d, out);
        if (leftGroup) out.print(")");
        out.print(" ");
        out.print(op);
        out.print(" ");
        if (rightGroup) out.print("(");
        right.print(d, out);
        if (rightGroup) out.print(")");
    }

    @Override
    public int getConstantIndex() {
        return Math.max(left.getConstantIndex(), right.getConstantIndex());
    }

    @Override
    public boolean beginsWithParen() {
        return leftGroup() || left.beginsWithParen();
    }

    @Override
    public boolean isUngrouped() {
        return !beginsWithParen();
    }

    public String getOp() {
        return op;
    }

    private boolean leftGroup() {
        return precedence > left.precedence || (precedence == left.precedence && associativity == ASSOCIATIVITY_RIGHT);
    }

    private boolean rightGroup() {
        return precedence > right.precedence || (precedence == right.precedence && associativity == ASSOCIATIVITY_LEFT);
    }

    @Override
    public void remapLocals(Map<Local, Local> localRemaps) {
        left.remapLocals(localRemaps);
        right.remapLocals(localRemaps);
    }

    @Override
    public List<Local> getLocals() {
        var locals = new ArrayList<Local>();
        locals.addAll(left.getLocals());
        locals.addAll(right.getLocals());
        return locals;
    }

    @Override
    public void inlineLocal(Local local, Expression statement, Expression src) {
        if (statement == this) return;
        if (left instanceof LocalVariable lvar && lvar.local.equals(local)) left = statement;
        if (right instanceof LocalVariable lvar && lvar.local.equals(local)) right = statement;
        left.inlineLocal(local, statement, this);
        right.inlineLocal(local, statement, this);
    }
}
