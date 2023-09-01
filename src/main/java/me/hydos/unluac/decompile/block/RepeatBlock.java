package me.hydos.unluac.decompile.block;

import me.hydos.unluac.decompile.*;
import me.hydos.unluac.decompile.condition.Condition;
import me.hydos.unluac.decompile.expression.Expression;
import me.hydos.unluac.decompile.statement.Statement;
import me.hydos.unluac.bytecode.BFunction;

public class RepeatBlock extends ContainerBlock {

    private final Condition cond;
    private final boolean extendedRepeatScope;
    private final int scopeEnd;

    private Expression condexpr;

    public RepeatBlock(BFunction function, Condition cond, int begin, int end, CloseType closeType, int closeLine, boolean extendedRepeatScope, int scopeEnd) {
        super(function, begin, end, closeType, closeLine, 0);
        this.cond = cond;
        this.extendedRepeatScope = extendedRepeatScope;
        this.scopeEnd = scopeEnd;
    }

    @Override
    public void resolve(Registers r) {
        condexpr = cond.asExpression(r);
    }

    @Override
    public int scopeEnd() {
        if (extendedRepeatScope) {
            return usingClose && closeType != CloseType.NONE ? closeLine - 1 : scopeEnd;
        } else {
            return usingClose && closeType != CloseType.NONE ? closeLine : super.scopeEnd();
        }
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
        return true;
    }

    @Override
    public void walk(Walker w) {
        w.visitStatement(this);
        for (var statement : statements) {
            statement.walk(w);
        }
        condexpr.walk(w);
    }

    @Override
    public void print(Decompiler d, Output out) {
        out.print("repeat");
        out.println();
        out.indent();
        Statement.printSequence(d, out, statements);
        out.dedent();
        out.print("until ");
        condexpr.print(d, out);
    }

}
