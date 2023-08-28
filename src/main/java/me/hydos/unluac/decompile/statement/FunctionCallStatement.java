package me.hydos.unluac.decompile.statement;

import me.hydos.unluac.decompile.Decompiler;
import me.hydos.unluac.decompile.Output;
import me.hydos.unluac.decompile.Walker;
import me.hydos.unluac.decompile.expression.FunctionCall;

public class FunctionCallStatement extends Statement {

    private final FunctionCall call;

    public FunctionCallStatement(FunctionCall call) {
        this.call = call;
    }

    @Override
    public void print(Decompiler d, Output out) {
        call.print(d, out);
    }

    @Override
    public void walk(Walker w) {
        w.visitStatement(this);
        call.walk(w);
    }

    @Override
    public boolean beginsWithParen() {
        return call.beginsWithParen();
    }

}
