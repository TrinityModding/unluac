package me.hydos.unluac.decompile.expression;

import me.hydos.unluac.decompile.core.Decompiler;
import me.hydos.unluac.decompile.core.Local;
import me.hydos.unluac.decompile.core.Output;
import me.hydos.unluac.decompile.core.Walker;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LocalVariable extends Expression {

    public Local local;

    public LocalVariable(Local local) {
        super(PRECEDENCE_ATOMIC);
        this.local = local;
    }

    @Override
    public void walk(Walker w) {
        w.visitExpression(this);
    }

    @Override
    public void print(Decompiler d, Output out) {
        out.print(local.name);
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
    public void fillUsageMap(Map<Local, Boolean> localUsageMap, boolean includeAssignments) {
        localUsageMap.put(local, true);
    }

    @Override
    public void remapLocals(Map<Local, Local> localRemaps) {
        if (localRemaps.containsKey(local)) this.local = localRemaps.get(local);
    }

    @Override
    public List<Local> getLocals() {
        return Collections.singletonList(local);
    }

    @Override
    public void inlineLocal(Local local, Expression statement) {
        // Too deep to inline. Getting here is just a result of a deep search don't worry
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (LocalVariable) o;
        return Objects.equals(local, that.local);
    }

    @Override
    public int hashCode() {
        return Objects.hash(local);
    }

    @Override
    public String toString() {
        return "LocalVariable{" + "local=" + local.name + '}';
    }
}
