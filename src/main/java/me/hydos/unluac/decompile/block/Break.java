package me.hydos.unluac.decompile.block;

import me.hydos.unluac.decompile.Decompiler;
import me.hydos.unluac.decompile.Output;
import me.hydos.unluac.decompile.Walker;
import me.hydos.unluac.decompile.statement.Statement;
import me.hydos.unluac.parse.LFunction;

public class Break extends Block {

    public final int target;
    public String comment;

    public Break(LFunction function, int line, int target) {
        super(function, line, line, 2);
        this.target = target;
    }

    @Override
    public void addStatement(Statement statement) {
        throw new IllegalStateException();
    }

    @Override
    public boolean hasHeader() {
        return false;
    }

    @Override
    public boolean isUnprotected() {
        //Actually, it is unprotected, but not really a block
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
    public boolean isContainer() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public void print(Decompiler d, Output out) {
        out.print("do break end");
        if (comment != null) out.print(" -- " + comment);
    }

    @Override
    public void printTail(Decompiler d, Output out) {
        out.print("break");
        if (comment != null) out.print(" -- " + comment);
    }

    @Override
    public void walk(Walker w) {
        w.visitStatement(this);
    }

}
