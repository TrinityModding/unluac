package me.hydos.unluac.decompile.block;

import me.hydos.unluac.Version;
import me.hydos.unluac.decompile.CloseType;
import me.hydos.unluac.decompile.Decompiler;
import me.hydos.unluac.decompile.Function;
import me.hydos.unluac.decompile.Output;
import me.hydos.unluac.decompile.expression.ConstantExpression;
import me.hydos.unluac.decompile.statement.Statement;
import me.hydos.unluac.parse.LFunction;

public class AlwaysLoop extends ContainerBlock {

    private final boolean repeat;
    private final Version.WhileFormat whileFormat;
    private ConstantExpression condition;

    public AlwaysLoop(LFunction function, int begin, int end, CloseType closeType, int closeLine, boolean repeat) {
        super(function, begin, end, closeType, closeLine, 0);
        this.repeat = repeat;
        this.whileFormat = function.header.version.whileformat.get();
        condition = null;
    }

    @Override
    public int scopeEnd() {
        return usingClose && closeType == CloseType.CLOSE ? closeLine - 1 : end - 2;
    }

    @Override
    public boolean hasHeader() {
        if (whileFormat == Version.WhileFormat.BOTTOM_CONDITION) {
            return !repeat;
        } else {
            return false;
        }
    }

    @Override
    public boolean isUnprotected() {
        return true;
    }

    @Override
    public int getUnprotectedTarget() {
        return begin;
    }

    @Override
    public int getUnprotectedLine() {
        return end - 1;
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
        if (repeat) {
            out.println("repeat");
        } else {
            out.print("while ");
            if (condition == null) {
                out.print("true");
            } else {
                condition.print(d, out);
            }
            out.println(" do");
        }
        out.indent();
        Statement.printSequence(d, out, statements);
        out.dedent();
        if (repeat) {
            out.print("until false");
        } else {
            out.print("end");
        }
    }

    @Override
    public boolean useConstant(Function f, int index) {
        if (!repeat && condition == null) {
            condition = f.getConstantExpression(index);
            return true;
        } else {
            return false;
        }
    }
}