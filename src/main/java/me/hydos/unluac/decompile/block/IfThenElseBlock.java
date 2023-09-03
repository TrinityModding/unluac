package me.hydos.unluac.decompile.block;

import me.hydos.unluac.decompile.*;
import me.hydos.unluac.decompile.condition.Condition;
import me.hydos.unluac.decompile.expression.Expression;
import me.hydos.unluac.decompile.statement.Statement;
import me.hydos.unluac.bytecode.BFunction;

import java.util.Map;

public class IfThenElseBlock extends ContainerBlock {

    public final Condition cond;
    public final int elseTarget;
    public ElseEndBlock partner;

    public Expression condexpr;

    public IfThenElseBlock(BFunction function, Condition cond, int begin, int end, int elseTarget, CloseType closeType, int closeLine) {
        super(function, begin, end, closeType, closeLine, -1);
        this.cond = cond;
        this.elseTarget = elseTarget;
    }

    @Override
    public void resolve(Registers r) {
        condexpr = cond.asExpression(r);
    }

    @Override
    public int scopeEnd() {
        return usingClose && closeType == CloseType.CLOSE ? closeLine - 1 : end - 2;
    }

    @Override
    public boolean hasHeader() {
        return true;
    }

    @Override
    public boolean isUnprotected() {
        return true;
    }

    @Override
    public int getUnprotectedTarget() {
        return elseTarget;
    }

    @Override
    public int getUnprotectedLine() {
        return end - 1;
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
    public int compareTo(Block block) {
        if (block == partner) {
            return -1;
        } else {
            return super.compareTo(block);
        }
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

        // Handle the "empty else" case
        if (end == elseTarget) {
            out.println("else");
            out.println("end");
        }
    }

    @Override
    public boolean suppressNewline() {
        return true;
    }

    @Override
    public void remapLocals(Map<Local, Local> localRemaps) {
        condexpr.remapLocals(localRemaps);
    }
}
