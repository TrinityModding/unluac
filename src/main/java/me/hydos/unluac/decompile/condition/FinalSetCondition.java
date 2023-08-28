package me.hydos.unluac.decompile.condition;

import me.hydos.unluac.decompile.Registers;
import me.hydos.unluac.decompile.expression.ConstantExpression;
import me.hydos.unluac.decompile.expression.Expression;

public class FinalSetCondition implements Condition {

    private final int register;
    public int line;
    public Type type;
    public FinalSetCondition(int line, int register) {
        this.line = line;
        this.register = register;
        this.type = Type.NONE;
        if (register < 0) {
            throw new IllegalStateException();
        }
    }

    @Override
    public Condition inverse() {
        return new NotCondition(this);
    }

    @Override
    public boolean invertible() {
        return false;
    }

    @Override
    public int register() {
        return register;
    }

    @Override
    public boolean isRegisterTest() {
        return false;
    }

    @Override
    public boolean isOrCondition() {
        return false;
    }

    @Override
    public boolean isSplitable() {
        return false;
    }

    @Override
    public Condition[] split() {
        throw new IllegalStateException();
    }

    @Override
    public Expression asExpression(Registers r) {
        var expr = switch (type) {
            case REGISTER -> r.getExpression(register, line + 1);
            case VALUE -> r.getValue(register, line + 1);
            default -> ConstantExpression.createDouble(register + ((double) line) / 100.0);
        };
        if (expr == null) {
            throw new IllegalStateException();
        }
        return expr;
    }

    @Override
    public String toString() {
        return "(" + register + ")";
    }

    public enum Type {
        NONE,
        REGISTER,
        VALUE,
    }

}