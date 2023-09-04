package me.hydos.unluac.decompile.block;

import me.hydos.unluac.decompile.core.Decompiler;
import me.hydos.unluac.decompile.core.Local;
import me.hydos.unluac.decompile.core.Output;
import me.hydos.unluac.decompile.core.Walker;
import me.hydos.unluac.decompile.statement.Statement;
import me.hydos.unluac.bytecode.BFunction;

import java.util.List;
import java.util.Map;

public class Goto extends Block {

    public final int target;

    public Goto(BFunction function, int line, int target) {
        super(function, line, line, 2);
        this.target = target;
    }

    @Override
    public void addStatement(Statement statement) {
        throw new IllegalStateException();
    }

    @Override
    public List<Statement> getStatements() {
        throw new IllegalStateException();
    }

    @Override
    public boolean hasHeader() {
        return true;
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
        out.print("goto lbl_" + target);
    }

    @Override
    public void walk(Walker w) {
        w.visitStatement(this);
    }

    @Override
    public void remapLocals(Map<Local, Local> localRemaps, Map<Local, Local> lastLocalRemaps) {}

    @Override
    public void fillUsageMap(Map<Local, Boolean> localUsageMap, boolean includeAssignments) {}
}
