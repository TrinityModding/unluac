package me.hydos.unluac.decompile.condition;

import me.hydos.unluac.decompile.core.Registers;
import me.hydos.unluac.decompile.expression.BinaryExpression;
import me.hydos.unluac.decompile.expression.Expression;

public class OrCondition implements Condition {

    private final Condition left;
    private final Condition right;

    public OrCondition(Condition left, Condition right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public Condition inverse() {
        if (invertible()) {
            return new AndCondition(left.inverse(), right.inverse());
        } else {
            return new NotCondition(this);
        }
    }

    @Override
    public boolean invertible() {
        return right.invertible();
    }

    @Override
    public int register() {
        return right.register();
    }

    @Override
    public boolean isRegisterTest() {
        return false;
    }

    @Override
    public boolean isOrCondition() {
        return true;
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
        return new BinaryExpression("or", left.asExpression(r), right.asExpression(r), Expression.PRECEDENCE_OR, Expression.ASSOCIATIVITY_NONE);
    }

    @Override
    public String toString() {
        return "(" + left + " or " + right + ")";
    }

}
