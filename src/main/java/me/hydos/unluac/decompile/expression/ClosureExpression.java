package me.hydos.unluac.decompile.expression;

import me.hydos.unluac.bytecode.BFunction;
import me.hydos.unluac.decompile.core.Decompiler;
import me.hydos.unluac.decompile.core.Local;
import me.hydos.unluac.decompile.core.Output;
import me.hydos.unluac.decompile.core.Walker;
import me.hydos.unluac.decompile.target.TableTarget;
import me.hydos.unluac.decompile.target.Target;
import me.hydos.unluac.decompile.target.VariableTarget;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ClosureExpression extends Expression {

    private final BFunction function;
    private final int upvalueLine;

    public ClosureExpression(BFunction function, int upvalueLine) {
        super(PRECEDENCE_ATOMIC);
        this.function = function;
        this.upvalueLine = upvalueLine;
    }

    @Override
    public void walk(Walker w) {
        w.visitExpression(this);
    }

    @Override
    public void print(Decompiler outer, Output out) {
        var d = new Decompiler(function, outer.locals, outer.deadLocals, upvalueLine, outer.currentState);
        out.print("function");
        printMain(out, d, true);
    }

    public int getConstantIndex() {
        return -1;
    }

    @Override
    public boolean isClosure() {
        return true;
    }

    @Override
    public boolean isUngrouped() {
        return true;
    }

    @Override
    public boolean isUpvalueOf(int register) {
    /*
    if(function.header.version == 0x51) {
      return false; //TODO:
    }
    */
        for (var i = 0; i < function.upvalues.length; i++) {
            var upvalue = function.upvalues[i];
            if (upvalue.instack && upvalue.idx == register) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int closureUpvalueLine() {
        return upvalueLine;
    }

    @Override
    public void printClosure(Decompiler decompiler, Output out, Target name) {
        var d = new Decompiler(function, decompiler.locals, decompiler.deadLocals, upvalueLine, decompiler.currentState);
        out.print("function ");
        if (function.paramCount >= 1 && d.locals.get(0).name.equals("self") && name instanceof TableTarget) {
            name.printMethod(decompiler, out);
            printMain(out, d, false);
        } else {
            name.print(decompiler, out, false);
            printMain(out, d, true);
        }
    }

    private void printMain(Output out, Decompiler d, boolean includeFirst) {
        out.print("(");
        var start = includeFirst ? 0 : 1;
        if (function.paramCount > start) {
            new VariableTarget(d.locals.get(start)).print(d, out, false);
            for (var i = start + 1; i < function.paramCount; i++) {
                out.print(", ");
                new VariableTarget(d.locals.get(i)).print(d, out, false);
            }
        }
        if (function.vararg != 0) {
            if (function.paramCount > start) {
                out.print(", ...");
            } else {
                out.print("...");
            }
        }
        out.print(")");
        out.println();
        out.indent();
        var result = d.getResult();
        d.writeResult(result, out);
        out.dedent();
        out.print("end");
    }

    @Override
    public void remapLocals(Map<Local, Local> localRemaps) {
    }

    @Override
    public void inlineLocal(Local local, Expression statement, Expression src) {
    }

    @Override
    public List<Local> getLocals() {
        return Collections.emptyList();
    }
}
