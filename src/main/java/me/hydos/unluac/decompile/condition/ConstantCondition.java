package me.hydos.unluac.decompile.condition;

import me.hydos.unluac.decompile.core.Registers;
import me.hydos.unluac.decompile.expression.ConstantExpression;
import me.hydos.unluac.decompile.expression.Expression;

public class ConstantCondition implements Condition {

    private final int register;
    private final boolean value;

    public ConstantCondition(int register, boolean value) {
        this.register = register;
        this.value = value;
    }

    @Override
    public Condition inverse() {
        return new ConstantCondition(register, !value);
    }

    @Override
    public boolean invertible() {
        return true;
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
        return ConstantExpression.createBoolean(value);
    }

}
