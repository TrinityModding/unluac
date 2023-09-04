package me.hydos.unluac.decompile.expression;

import me.hydos.unluac.decompile.core.Decompiler;
import me.hydos.unluac.decompile.core.Local;
import me.hydos.unluac.decompile.core.Output;
import me.hydos.unluac.decompile.core.Walker;

import java.util.Map;
import java.util.Objects;

public class UpvalueExpression extends Expression {

    public String name;

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

    @Override
    public void remapLocals(Map<Local, Local> localRemaps) {
        for (var entry : localRemaps.entrySet()) {
            if (entry.getKey().name.equals(name)) this.name = entry.getValue().name;
        }
    }

    @Override
    public void inlineLocal(Local local, Expression statement) {
        // Can't inline this deep.
    }
}

