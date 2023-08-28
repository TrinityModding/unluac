package me.hydos.unluac.decompile.expression;

import me.hydos.unluac.decompile.Decompiler;
import me.hydos.unluac.decompile.Output;
import me.hydos.unluac.decompile.Walker;

public class Vararg extends Expression {

    private final boolean multiple;

    public Vararg(boolean multiple) {
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

}
