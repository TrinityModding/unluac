package me.hydos.unluac.decompile.expression;

import me.hydos.unluac.decompile.Decompiler;
import me.hydos.unluac.decompile.Output;
import me.hydos.unluac.decompile.Walker;
import me.hydos.unluac.decompile.target.TableTarget;
import me.hydos.unluac.decompile.target.Target;
import me.hydos.unluac.decompile.target.VariableTarget;
import me.hydos.unluac.parse.LFunction;
import me.hydos.unluac.parse.LUpvalue;

public class ClosureExpression extends Expression {

    private final LFunction function;
    private final int upvalueLine;

    public ClosureExpression(LFunction function, int upvalueLine) {
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
        var d = new Decompiler(function, outer.declList, upvalueLine);
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
    public void printClosure(Decompiler outer, Output out, Target name) {
        var d = new Decompiler(function, outer.declList, upvalueLine);
        out.print("function ");
        if (function.numParams >= 1 && d.declList[0].name.equals("self") && name instanceof TableTarget) {
            name.printMethod(outer, out);
            printMain(out, d, false);
        } else {
            name.print(outer, out, false);
            printMain(out, d, true);
        }
    }

    private void printMain(Output out, Decompiler d, boolean includeFirst) {
        out.print("(");
        var start = includeFirst ? 0 : 1;
        if (function.numParams > start) {
            new VariableTarget(d.declList[start]).print(d, out, false);
            for (var i = start + 1; i < function.numParams; i++) {
                out.print(", ");
                new VariableTarget(d.declList[i]).print(d, out, false);
            }
        }
        if (function.vararg != 0) {
            if (function.numParams > start) {
                out.print(", ...");
            } else {
                out.print("...");
            }
        }
        out.print(")");
        out.println();
        out.indent();
        var result = d.decompile();
        d.print(result, out);
        out.dedent();
        out.print("end");
        //out.println(); //This is an extra space for formatting
    }

}