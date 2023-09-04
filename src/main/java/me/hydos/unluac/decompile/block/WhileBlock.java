package me.hydos.unluac.decompile.block;

import me.hydos.unluac.decompile.condition.Condition;
import me.hydos.unluac.decompile.core.*;
import me.hydos.unluac.decompile.expression.Expression;
import me.hydos.unluac.bytecode.BFunction;

abstract public class WhileBlock extends ContainerBlock {

    protected Condition cond;

    private Expression condexpr;

    public WhileBlock(BFunction function, Condition cond, int begin, int end, CloseType closeType, int closeLine) {
        super(function, begin, end, closeType, closeLine, -1);
        this.cond = cond;
    }

    @Override
    public void resolve(Registers r) {
        condexpr = cond.asExpression(r);
    }

    @Override
    public boolean hasHeader() {
        return true;
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
        condexpr.walk(w);
        for (var statement : statements) {
            statement.walk(w);
        }
    }

    @Override
    public void print(Decompiler d, Output out) {
        out.print("while ");
        condexpr.print(d, out);
        out.print(" do");
        out.println();
        out.indent();
        printSequence(d, out, statements);
        out.dedent();
        out.print("end");
    }

}
