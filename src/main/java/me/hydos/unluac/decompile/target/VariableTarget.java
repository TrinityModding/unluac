package me.hydos.unluac.decompile.target;

import me.hydos.unluac.decompile.Local;
import me.hydos.unluac.decompile.Decompiler;
import me.hydos.unluac.decompile.Output;
import me.hydos.unluac.decompile.Walker;

import java.util.Objects;

public class VariableTarget extends Target {

    public Local local;

    public VariableTarget(Local local) {
        this.local = local;
    }

    @Override
    public void walk(Walker w) {
    }

    @Override
    public void print(Decompiler d, Output out, boolean declare) {
        out.print(local.name);
        if (declare && local.tbc) {
            out.print(" <close>");
        }
    }

    @Override
    public void printMethod(Decompiler d, Output out) {
        throw new IllegalStateException();
    }

    @Override
    public boolean isDeclaration(Local decl) {
        return this.local == decl;
    }

    @Override
    public boolean isLocal() {
        return true;
    }

    @Override
    public int getIndex() {
        return local.register;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (VariableTarget) o;
        return Objects.equals(local, that.local);
    }

    @Override
    public int hashCode() {
        return Objects.hash(local);
    }

    @Override
    public String toString() {
        return "VariableTarget{" + "local=" + local.name + '}';
    }
}
