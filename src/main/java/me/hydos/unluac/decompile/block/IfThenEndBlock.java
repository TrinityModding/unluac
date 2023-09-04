package me.hydos.unluac.decompile.block;

import me.hydos.unluac.bytecode.BFunction;
import me.hydos.unluac.decompile.condition.Condition;
import me.hydos.unluac.decompile.core.*;
import me.hydos.unluac.decompile.expression.Expression;
import me.hydos.unluac.decompile.operation.Operation;
import me.hydos.unluac.decompile.statement.Statement;

public class IfThenEndBlock extends ContainerBlock {

    private final Condition cond;
    private final boolean redirected;
    private final Registers r;

    private Expression condexpr;

    public IfThenEndBlock(BFunction function, Registers r, Condition cond, int begin, int end) {
        this(function, r, cond, begin, end, CloseType.NONE, -1, false);
    }

    public IfThenEndBlock(BFunction function, Registers r, Condition cond, int begin, int end, CloseType closeType, int closeLine, boolean redirected) {
        super(function, begin == end ? begin - 1 : begin, end, closeType, closeLine, -1);
        this.r = r;
        this.cond = cond;
        this.redirected = redirected;
    }

    @Override
    public void resolve(Registers r) {
        condexpr = cond.asExpression(r);
    }

    @Override
    public int scopeEnd() {
        return usingClose && closeType == CloseType.CLOSE ? closeLine - 1 : super.scopeEnd();
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
    public Operation process(Decompiler d) {
        final var test = cond.register();
        if (!scopeUsed && !redirected && test >= 0) r.getUpdated(test, end - 1);
        return super.process(d);
    }

    @Override
    public void walk(Walker w) {
        w.visitStatement(this);
        condexpr.walk(w);
        for (var statement : statements) {
            statement.walk(w);
        }
    }

    @Override
    public void print(Decompiler d, Output out) {
        out.print("if ");
        condexpr.print(d, out);
        out.print(" then");
        out.println();
        out.indent();
        Statement.printSequence(d, out, statements);
        out.dedent();
        out.print("end");
    }

}
