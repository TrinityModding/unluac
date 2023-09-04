package me.hydos.unluac.decompile.statement;

import me.hydos.unluac.decompile.core.Decompiler;
import me.hydos.unluac.decompile.core.Local;
import me.hydos.unluac.decompile.core.Output;
import me.hydos.unluac.decompile.core.Walker;
import me.hydos.unluac.decompile.expression.Expression;
import me.hydos.unluac.decompile.expression.FunctionCall;
import me.hydos.unluac.decompile.expression.LocalVariable;

import java.util.*;

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
    public void remapLocals(Map<Local, Local> localRemaps, Map<Local, Local> lastLocalRemaps) {
        call.remapLocals(localRemaps);
    }

    @Override
    public void fillUsageMap(Map<Local, Boolean> localUsageMap, boolean includeAssignments) {
        call.fillUsageMap(localUsageMap, includeAssignments);
    }

    @Override
    public boolean isActionStatement() {
        return true;
    }

    @Override
    public List<Local> getActionVars() {
        var actions = new ArrayList<>(call.function.getLocals());
        actions.addAll(
                Arrays.stream(call.arguments)
                .map(Expression::getLocals)
                .flatMap(Collection::stream).toList()
        );
        return actions;
    }

    @Override
    public void inlineLocal(Local local, Expression statement) {
        call.inlineLocal(local, statement);
    }
}
