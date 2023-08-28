package me.hydos.unluac.decompile.target;

import me.hydos.unluac.decompile.Decompiler;
import me.hydos.unluac.decompile.Output;
import me.hydos.unluac.decompile.Walker;
import me.hydos.unluac.decompile.expression.ConstantExpression;
import me.hydos.unluac.decompile.expression.Expression;

public class GlobalTarget extends Target {

    private final Expression name;

    public GlobalTarget(ConstantExpression name) {
        this.name = name;
    }

    @Override
    public void walk(Walker w) {
        name.walk(w);
    }

    @Override
    public void print(Decompiler d, Output out, boolean declare) {
        out.print(name.asName());
    }

    @Override
    public void printMethod(Decompiler d, Output out) {
        throw new IllegalStateException();
    }

}
