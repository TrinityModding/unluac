package me.hydos.unluac.decompile.expression;

import me.hydos.unluac.decompile.Declaration;
import me.hydos.unluac.decompile.Decompiler;
import me.hydos.unluac.decompile.Output;
import me.hydos.unluac.decompile.Walker;

import java.util.Objects;

public class LocalVariable extends Expression {

    public final Declaration decl;

    public LocalVariable(Declaration decl) {
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
}
