package me.hydos.unluac.decompile.block;

import me.hydos.unluac.decompile.CloseType;
import me.hydos.unluac.decompile.Decompiler;
import me.hydos.unluac.decompile.Output;
import me.hydos.unluac.decompile.statement.Statement;
import me.hydos.unluac.bytecode.BFunction;

public class DoEndBlock extends ContainerBlock {

    public DoEndBlock(BFunction function, int begin, int end) {
        super(function, begin, end, CloseType.NONE, -1, 1);
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

    public boolean allowsPreDeclare() {
        return true;
    }

    @Override
    public void print(Decompiler d, Output out) {
        out.println("do");
        out.indent();
        Statement.printSequence(d, out, statements);
        out.dedent();
        out.print("end");
    }

}
