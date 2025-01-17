package me.hydos.unluac.decompile.condition;

import me.hydos.unluac.decompile.core.Registers;
import me.hydos.unluac.decompile.expression.Expression;

public class TestCondition implements Condition {

    private final int line;
    private final int register;

    public TestCondition(int line, int register) {
        this.line = line;
        this.register = register;
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
        return true;
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
        return r.getExpression(register, line);
    }

    @Override
    public String toString() {
        return "(" + register + ")";
    }

}
