package me.hydos.unluac.decompile.expression;

import me.hydos.unluac.decompile.*;
import me.hydos.unluac.bytecode.LBoolean;
import me.hydos.unluac.bytecode.LNil;
import me.hydos.unluac.decompile.core.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ConstantExpression extends Expression {

    private final Constant constant;
    private final boolean identifier;
    private final int index;
    private final int line;

    public ConstantExpression(Constant constant, boolean identifier, int index) {
        this(constant, identifier, index, -1);
    }

    private ConstantExpression(Constant constant, boolean identifier, int index, int line) {
        super(getPrecedence(constant));
        this.constant = constant;
        this.identifier = identifier;
        this.index = index;
        this.line = line;
    }

    public static ConstantExpression createNil(int line) {
        return new ConstantExpression(new Constant(LNil.NIL), false, -1, line);
    }

    public static ConstantExpression createBoolean(boolean v) {
        return new ConstantExpression(new Constant(v ? LBoolean.LTRUE : LBoolean.LFALSE), false, -1);
    }

    public static ConstantExpression createInteger(int i) {
        return new ConstantExpression(new Constant(i), false, -1);
    }

    public static ConstantExpression createDouble(double x) {
        return new ConstantExpression(new Constant(x), false, -1);
    }

    private static int getPrecedence(Constant constant) {
        if (constant.isNumber() && constant.isNegative()) {
            return PRECEDENCE_UNARY;
        } else {
            return PRECEDENCE_ATOMIC;
        }
    }

    @Override
    public void walk(Walker w) {
        w.visitExpression(this);
    }

    @Override
    public void print(Decompiler d, Output out) {
        constant.print(d, out, false);
    }

    @Override
    public void printBraced(Decompiler d, Output out) {
        constant.print(d, out, true);
    }

    @Override
    public int getConstantIndex() {
        return index;
    }

    @Override
    public int getConstantLine() {
        return line;
    }

    @Override
    public boolean isNil() {
        return constant.isNil();
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public boolean isUngrouped() {
        return true;
    }

    @Override
    public boolean isBoolean() {
        return constant.isBoolean();
    }

    @Override
    public boolean isInteger() {
        return constant.isInteger();
    }

    @Override
    public int asInteger() {
        return constant.asInteger();
    }

    @Override
    public boolean isString() {
        return constant.isString();
    }

    @Override
    public boolean isIdentifier() {
        return identifier;
    }

    @Override
    public String asName() {
        return constant.asName();
    }

    @Override
    public boolean isBrief() {
        return !constant.isString() || constant.asName().length() <= 10;
    }

    @Override
    public void remapLocals(Map<Local, Local> localRemaps) {}

    @Override
    public void inlineLocal(Local local, Expression statement, Expression src) {}

    @Override
    public List<Local> getLocals() {
        return Collections.emptyList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (ConstantExpression) o;
        return index == that.index && Objects.equals(constant, that.constant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(constant, index);
    }

    @Override
    public String toString() {
        try {
            return "ConstantExpression{" + "constant=" + DecompilerDebugger.print(null, (decompiler, output) -> constant.print(decompiler, output, false)) + '}';
        } catch (Exception e) {
            return "ConstantExpression{constant=UNRESOLVABLE}";
        }
    }
}
