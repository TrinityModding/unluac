package me.hydos.unluac.decompile.block;

import me.hydos.unluac.decompile.core.CloseType;
import me.hydos.unluac.decompile.core.Local;
import me.hydos.unluac.decompile.core.Walker;
import me.hydos.unluac.decompile.statement.Statement;
import me.hydos.unluac.bytecode.BFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

abstract public class ContainerBlock extends Block {

    public List<Statement> statements;
    protected final CloseType closeType;
    protected final int closeLine;
    protected boolean usingClose;

    public ContainerBlock(BFunction function, int begin, int end, CloseType closeType, int closeLine, int priority) {
        super(function, begin, end, priority);
        this.closeType = closeType;
        this.closeLine = closeLine;
        this.usingClose = false;
        this.statements = new ArrayList<>(Math.max(4, end - begin + 1));
    }

    @Override
    public void walk(Walker w) {
        w.visitStatement(this);
        for (var statement : statements) {
            statement.walk(w);
        }
    }

    @Override
    public void addStatement(Statement statement) {
        statements.add(statement);
    }

    @Override
    public List<Statement> getStatements() {
        return statements;
    }

    @Override
    public boolean hasCloseLine() {
        return closeType != CloseType.NONE;
    }

    @Override
    public int getCloseLine() {
        if (closeType == CloseType.NONE) {
            throw new IllegalStateException();
        }
        return closeLine;
    }

    @Override
    public void useClose() {
        usingClose = true;
    }

    @Override
    public boolean isContainer() {
        return begin < end;
    }

    @Override
    public boolean isEmpty() {
        return statements.isEmpty();
    }

    @Override
    public void remapLocals(Map<Local, Local> localRemaps, Map<Local, Local> lastLocalRemaps) {
        statements.forEach(statement -> statement.remapLocals(localRemaps, lastLocalRemaps));
    }

    @Override
    public void fillUsageMap(Map<Local, Boolean> localUsageMap, boolean includeAssignments) {
        statements.forEach(statement -> statement.fillUsageMap(localUsageMap, includeAssignments));
    }
}
