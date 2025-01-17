package me.hydos.unluac.decompile.condition;

import me.hydos.unluac.decompile.core.Registers;
import me.hydos.unluac.decompile.expression.ConstantExpression;
import me.hydos.unluac.decompile.expression.Expression;

public class FixedCondition implements Condition {

    public static final FixedCondition TRUE = new FixedCondition(ConstantExpression.createBoolean(true));

    private final Expression expression;

    private FixedCondition(Expression expr) {
        expression = expr;
    }

    @Override
    public Condition inverse() {
        throw new IllegalStateException();
    }

    @Override
    public boolean invertible() {
        return false;
    }

    @Override
    public int register() {
        return -1;
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
        return expression;
    }

}
