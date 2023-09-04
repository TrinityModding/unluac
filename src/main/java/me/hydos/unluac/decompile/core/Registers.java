package me.hydos.unluac.decompile.core;

import me.hydos.unluac.Version;
import me.hydos.unluac.decompile.expression.ConstantExpression;
import me.hydos.unluac.decompile.expression.Expression;
import me.hydos.unluac.decompile.expression.LocalVariable;
import me.hydos.unluac.decompile.target.Target;
import me.hydos.unluac.decompile.target.VariableTarget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Registers {

    public final int registers;
    public final int length;
    private final Local[][] decls;
    private final FunctionQuery f;
    private final Expression[][] values;
    private final int[][] updated;

    @Deprecated
    public Registers(int registers, int length, Local[] declList, FunctionQuery f, boolean isNoDebug) {
        this(registers, length, Arrays.stream(declList).toList(), f);
    }

    public Registers(int registers, int length, List<Local> declList, FunctionQuery f) {
        this.registers = registers;
        this.length = length;
        decls = new Local[registers][length + 1];

        for (var decl : declList) {
            var register = 0;
            while (decls[register][decl.begin] != null) register++;
            decl.register = register;
            for (var line = decl.begin; line <= decl.end; line++) decls[register][line] = decl;
        }

        values = new Expression[registers][length + 1];
        Expression nil = ConstantExpression.createNil(0);
        for (var register = 0; register < registers; register++) {
            values[register][0] = nil;
        }
        updated = new int[registers][length + 1];
        this.f = f;
    }

    public FunctionQuery getFunction() {
        return f;
    }

    public boolean isAssignable(int register, int line) {
        return isLocal(register, line);
    }

    public boolean isLocal(int register, int line) {
        if (register < 0) return false;
        return decls[register][line] != null;
    }

    public boolean isNewLocal(int register, int line) {
        var decl = decls[register][line];
        return decl != null && decl.begin == line && !decl.forLoop && !decl.forLoopExplicit;
    }

    public List<Local> getNewLocals(int line) {
        return getNewLocals(line, 0);
    }

    public List<Local> getNewLocals(int line, int first) {
        first = Math.max(0, first);
        var locals = new ArrayList<Local>(Math.max(registers - first, 0));

        for (var register = first; register < registers; register++) {
            if (isNewLocal(register, line)) {
                locals.add(getDeclaration(register, line));
            }
        }
        return locals;
    }

    public Local getDeclaration(int register, int line) {
        return decls[register][line];
    }

    public void startLine(int line) {
        for (var register = 0; register < registers; register++) {
            values[register][line] = values[register][line - 1];
            updated[register][line] = updated[register][line - 1];
        }
    }

    public boolean isKConstant(int register) {
        return f.isConstant(register);
    }

    public Expression getExpression(int register, int line) {
        if (isLocal(register, line - 1)) return new LocalVariable(getDeclaration(register, line - 1));
        else return values[register][line - 1];
    }

    public Expression getKExpression(int register, int line) {
        if (f.isConstant(register)) return f.getConstantExpression(f.constantIndex(register));
        else return getExpression(register, line);
    }

    public Expression getKExpression54(int register, boolean k, int line) {
        if (k) return f.getConstantExpression(register);
        else return getExpression(register, line);
    }

    public Expression getValue(int register, int line) {
        return getExpression(register, line);
    }

    public int getUpdated(int register, int line) {
        return updated[register][line];
    }

    public void setValue(int register, int line, Expression expression) {
        values[register][line] = expression;
        updated[register][line] = line;
    }

    public Target getTarget(int register, int line) {
        if (!isLocal(register, line)) {
            throw new IllegalStateException("No declaration exists in register " + register + " at line " + line);
        }
        return new VariableTarget(decls[register][line]);
    }

    public void setInternalLoopVariable(int register, int begin, int end) {
        var decl = getDeclaration(register, begin);
        if (decl == null) {
            decl = new Local("_FOR_", begin, end);
            decl.register = register;
            newDeclaration(decl, register, begin, end);
        }

        decl.forLoop = true;
    }

    public void setExplicitLoopVariable(int register, int begin, int end) {
        var decl = getDeclaration(register, begin);
        if (decl == null) {
            decl = new Local("_FORV_" + register + "_", begin, end);
            decl.register = register;
            newDeclaration(decl, register, begin, end);
        }

        decl.forLoopExplicit = true;
    }

    private void newDeclaration(Local decl, int register, int begin, int end) {
        for (var line = begin; line <= end; line++) {
            decls[register][line] = decl;
        }
    }

    public Version getVersion() {
        return f.getVersion();
    }

}
