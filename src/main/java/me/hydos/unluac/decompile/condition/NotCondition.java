package me.hydos.unluac.decompile.condition;

import me.hydos.unluac.decompile.core.Registers;
import me.hydos.unluac.decompile.expression.Expression;
import me.hydos.unluac.decompile.expression.UnaryExpression;

public class NotCondition implements Condition {

    private final Condition cond;

    public NotCondition(Condition cond) {
        this.cond = cond;
    }

    @Override
    public Condition inverse() {
        return cond;
    }

    @Override
    public boolean invertible() {
        return true;
    }

    @Override
    public int register() {
        return cond.register();
    }

    @Override
    public boolean isRegisterTest() {
        return cond.isRegisterTest();
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
        return new UnaryExpression("not ", cond.asExpression(r), Expression.PRECEDENCE_UNARY);
    }

    @Override
    public String toString() {
        return "not (" + cond + ")";
    }

}
