package me.hydos.unluac.decompile.block;

import me.hydos.unluac.decompile.core.CloseType;
import me.hydos.unluac.decompile.core.Decompiler;
import me.hydos.unluac.decompile.core.Output;
import me.hydos.unluac.decompile.statement.Statement;
import me.hydos.unluac.bytecode.BFunction;

public class OnceLoop extends ContainerBlock {

    public OnceLoop(BFunction function, int begin, int end) {
        super(function, begin, end, CloseType.NONE, -1, 0);
    }

    @Override
    public int scopeEnd() {
        return end - 1;
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
        return begin;
    }

    @Override
    public boolean breakable() {
        return true;
    }

    @Override
    public void print(Decompiler d, Output out) {
        out.println("repeat");
        out.indent();
        Statement.printSequence(d, out, statements);
        out.dedent();
        out.print("until true");
    }

}
