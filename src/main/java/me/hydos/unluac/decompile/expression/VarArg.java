package me.hydos.unluac.decompile.expression;

import me.hydos.unluac.decompile.core.Decompiler;
import me.hydos.unluac.decompile.core.Local;
import me.hydos.unluac.decompile.core.Output;
import me.hydos.unluac.decompile.core.Walker;

import java.util.Map;

public class VarArg extends Expression {

    private final boolean multiple;

    public VarArg(boolean multiple) {
        super(PRECEDENCE_ATOMIC);
        this.multiple = multiple;
    }

    @Override
    public void walk(Walker w) {
        w.visitExpression(this);
    }

    @Override
    public void print(Decompiler d, Output out) {
        //out.print("...");
        out.print(multiple ? "..." : "(...)");
    }

    @Override
    public void printMultiple(Decompiler d, Output out) {
        out.print(multiple ? "..." : "(...)");
    }

    @Override
    public int getConstantIndex() {
        return -1;
    }

    @Override
    public boolean isMultiple() {
        return multiple;
    }

    @Override
    public void remapLocals(Map<Local, Local> localRemaps) {}

    @Override
    public void inlineLocal(Local local, Expression statement, Expression src) {}
}
