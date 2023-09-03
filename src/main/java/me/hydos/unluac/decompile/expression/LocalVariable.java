package me.hydos.unluac.decompile.expression;

import me.hydos.unluac.decompile.Decompiler;
import me.hydos.unluac.decompile.Local;
import me.hydos.unluac.decompile.Output;
import me.hydos.unluac.decompile.Walker;

import java.util.Map;
import java.util.Objects;

public class LocalVariable extends Expression {

    public Local decl;

    public LocalVariable(Local decl) {
        super(PRECEDENCE_ATOMIC);
        this.decl = decl;
    }

    @Override
    public void walk(Walker w) {
        w.visitExpression(this);
    }

    @Override
    public void print(Decompiler d, Output out) {
        out.print(decl.name);
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
        localUsageMap.put(decl, true);
    }

    @Override
    public void remapLocals(Map<Local, Local> localRemaps) {
        if (localRemaps.containsKey(decl)) this.decl = localRemaps.get(decl);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (LocalVariable) o;
        return Objects.equals(decl, that.decl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(decl);
    }

    @Override
    public String toString() {
        return "LocalVariable{" + "local=" + decl.name + '}';
    }
}
