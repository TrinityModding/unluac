package me.hydos.unluac.decompile.block;

import me.hydos.unluac.decompile.core.CloseType;
import me.hydos.unluac.decompile.core.Decompiler;
import me.hydos.unluac.decompile.core.Output;
import me.hydos.unluac.decompile.statement.ReturnStatement;
import me.hydos.unluac.decompile.statement.Statement;
import me.hydos.unluac.bytecode.BFunction;

public class OuterBlock extends ContainerBlock {

    public OuterBlock(BFunction function, int length) {
        super(function, 0, length + 1, CloseType.NONE, -1, -2);
    }

    @Override
    public int scopeEnd() {
        return (end - 1) + function.header.version.outerblockscopeadjustment.get();
    }

    @Override
    public boolean hasHeader() {
        return false;
    }

    @Override
    public boolean isUnprotected() {
        return false;
    }

    @Override
    public int getLoopback() {
        throw new IllegalStateException();
    }

    @Override
    public boolean breakable() {
        return false;
    }

    @Override
    public void print(Decompiler d, Output out) {
        /* extra return statement */
        var last = statements.size() - 1;
        if (last < 0 || !(statements.get(last) instanceof ReturnStatement))
            throw new IllegalStateException(statements.get(last).toString());
        statements.remove(last);
        Statement.printSequence(d, out, statements);
    }

}
