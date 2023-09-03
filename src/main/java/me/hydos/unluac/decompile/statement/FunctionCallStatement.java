package me.hydos.unluac.decompile.statement;

import me.hydos.unluac.decompile.Decompiler;
import me.hydos.unluac.decompile.Local;
import me.hydos.unluac.decompile.Output;
import me.hydos.unluac.decompile.Walker;
import me.hydos.unluac.decompile.expression.FunctionCall;

import java.util.Map;

public class FunctionCallStatement extends Statement {

    public FunctionCall call;

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

    @Override
    public void remapLocals(Map<Local, Local> localRemaps) {
        call.remapLocals(localRemaps);
    }

    @Override
    public void fillUsageMap(Map<Local, Boolean> localUsageMap, boolean includeAssignments) {
        call.fillUsageMap(localUsageMap, includeAssignments);
    }
}
