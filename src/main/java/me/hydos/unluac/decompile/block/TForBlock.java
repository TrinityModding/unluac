package me.hydos.unluac.decompile.block;

import me.hydos.unluac.decompile.core.*;
import me.hydos.unluac.decompile.expression.Expression;
import me.hydos.unluac.decompile.statement.Statement;
import me.hydos.unluac.decompile.target.Target;
import me.hydos.unluac.bytecode.BFunction;

import java.util.ArrayList;
import java.util.List;

public class TForBlock extends ContainerBlock {

    protected final int internalRegisterFirst;
    protected final int internalRegisterLast;

    protected final int explicitRegisterFirst;
    protected final int explicitRegisterLast;

    protected final int internalScopeBegin;
    protected final int internalScopeEnd;

    protected final int explicitScopeBegin;
    protected final int explicitScopeEnd;

    protected final int innerScopeEnd;

    private Target[] targets;
    private Expression[] values;

    public TForBlock(BFunction function, int begin, int end,
                     int internalRegisterFirst, int internalRegisterLast,
                     int explicitRegisterFirst, int explicitRegisterLast,
                     int internalScopeBegin, int internalScopeEnd,
                     int explicitScopeBegin, int explicitScopeEnd,
                     int innerScopeEnd
    ) {
        super(function, begin, end, CloseType.NONE, -1, -1);
        this.internalRegisterFirst = internalRegisterFirst;
        this.internalRegisterLast = internalRegisterLast;
        this.explicitRegisterFirst = explicitRegisterFirst;
        this.explicitRegisterLast = explicitRegisterLast;
        this.internalScopeBegin = internalScopeBegin;
        this.internalScopeEnd = internalScopeEnd;
        this.explicitScopeBegin = explicitScopeBegin;
        this.explicitScopeEnd = explicitScopeEnd;
        this.innerScopeEnd = innerScopeEnd;
    }

    public static TForBlock make50(BFunction function, int begin, int end, int register, int length, boolean innerClose) {
        var innerScopeEnd = end - 3;
        if (innerClose) {
            innerScopeEnd--;
        }
        return new TForBlock(
                function, begin, end,
                register, register + 1, register + 2, register + 1 + length,
                begin - 1, end - 1,
                begin - 1, end - 1,
                innerScopeEnd
        );
    }

    public static TForBlock make51(BFunction function, int begin, int end, int register, int length, boolean forvarClose, boolean innerClose) {
        var explicitScopeEnd = end - 3;
        var innerScopeEnd = end - 3;
        if (forvarClose) {
            explicitScopeEnd--;
            innerScopeEnd--;
        }
        if (innerClose) {
            innerScopeEnd--;
        }
        return new TForBlock(
                function, begin, end,
                register, register + 2, register + 3, register + 2 + length,
                begin - 2, end - 1,
                begin - 1, explicitScopeEnd,
                innerScopeEnd
        );
    }

    public static TForBlock make54(BFunction function, int begin, int end, int register, int length, boolean forvarClose) {
        var innerScopeEnd = end - 3;
        if (forvarClose) {
            innerScopeEnd--;
        }
        return new TForBlock(
                function, begin, end,
                register, register + 3, register + 4, register + 3 + length,
                begin - 2, end,
                begin - 1, end - 3,
                innerScopeEnd
        );
    }

    public List<Target> getTargets(Registers r) {
        var targets = new ArrayList<Target>(explicitRegisterLast - explicitRegisterFirst + 1);
        for (var register = explicitRegisterFirst; register <= explicitRegisterLast; register++) {
            targets.add(r.getTarget(register, begin - 1));
        }
        return targets;
    }

    public void handleVariableDeclarations(Registers r) {
        for (var register = internalRegisterFirst; register <= internalRegisterLast; register++) {
            r.setInternalLoopVariable(register, internalScopeBegin, internalScopeEnd);
        }
        for (var register = explicitRegisterFirst; register <= explicitRegisterLast; register++) {
            r.setExplicitLoopVariable(register, explicitScopeBegin, explicitScopeEnd);
        }
    }

    @Override
    public void resolve(Registers r) {
        var targets = getTargets(r);
        var values = new ArrayList<Expression>(3);
        for (var register = internalRegisterFirst; register <= internalRegisterLast; register++) {
            var value = r.getValue(register, begin - 1);
            values.add(value);
            if (value.isMultiple()) break;
        }

        this.targets = targets.toArray(new Target[0]);
        this.values = values.toArray(new Expression[0]);
    }

    @Override
    public int scopeEnd() {
        return innerScopeEnd;
    }

    @Override
    public boolean hasHeader() {
        return true;
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
        for (var expression : values) {
            expression.walk(w);
        }
        for (var statement : statements) {
            statement.walk(w);
        }
    }

    @Override
    public void print(Decompiler d, Output out) {
        out.print("for ");
        targets[0].print(d, out, false);
        for (var i = 1; i < targets.length; i++) {
            out.print(", ");
            targets[i].print(d, out, false);
        }
        out.print(" in ");
        values[0].print(d, out);
        for (var i = 1; i < values.length; i++) {
            out.print(", ");
            values[i].print(d, out);
        }
        out.print(" do");
        out.println();
        out.indent();
        Statement.printSequence(d, out, statements);
        out.dedent();
        out.print("end");
    }

}
