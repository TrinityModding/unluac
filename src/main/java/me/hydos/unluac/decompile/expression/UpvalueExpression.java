package me.hydos.unluac.decompile.expression;

import me.hydos.unluac.decompile.Decompiler;
import me.hydos.unluac.decompile.Output;
import me.hydos.unluac.decompile.Walker;

import java.util.Objects;

public class UpvalueExpression extends Expression {

    private final String name;

    public UpvalueExpression(String name) {
        super(PRECEDENCE_ATOMIC);
        this.name = name;
    }

    @Override
    public void walk(Walker w) {
        w.visitExpression(this);
    }

    @Override
    public void print(Decompiler d, Output out) {
        out.print(name);
    }

    @Override
    public int getConstantIndex() {
        return -1;
    }

    @Override
    public boolean isDotChain() {
        return true;
    }

    @Override
    public boolean isBrief() {
        return true;
    }

    @Override
    public boolean isEnvironmentTable(Decompiler d) {
        return d.bytecodeVersion.isEnvironmentTable(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (UpvalueExpression) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
