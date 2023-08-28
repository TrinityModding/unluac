package me.hydos.unluac.decompile.condition;

import me.hydos.unluac.decompile.Registers;
import me.hydos.unluac.decompile.expression.BinaryExpression;
import me.hydos.unluac.decompile.expression.Expression;

public class BinaryCondition implements Condition {

    private final Operator op;
    private final int line;
    private final Operand left;
    private final Operand right;
    private final boolean inverted;
    public BinaryCondition(Operator op, int line, Operand left, Operand right) {
        this(op, line, left, right, false);
    }
    private BinaryCondition(Operator op, int line, Operand left, Operand right, boolean inverted) {
        this.op = op;
        this.line = line;
        this.left = left;
        this.right = right;
        this.inverted = inverted;
    }

    private static String operator_to_string(Operator op, boolean inverted, boolean transposed) {
        return switch (op) {
            case EQ -> inverted ? "~=" : "==";
            case LT -> transposed ? ">" : "<";
            case LE -> transposed ? ">=" : "<=";
            case GT -> transposed ? "<" : ">";
            case GE -> transposed ? "<=" : ">=";
        };
    }

    @Override
    public Condition inverse() {
        if (op == Operator.EQ) {
            return new BinaryCondition(op, line, left, right, !inverted);
        } else {
            return new NotCondition(this);
        }
    }

    @Override
    public boolean invertible() {
        return op == Operator.EQ;
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
        var transpose = false;
        var leftExpression = left.asExpression(r, line);
        var rightExpression = right.asExpression(r, line);
        if (op != Operator.EQ || left.type() == OperandType.K) {
            if (left.isRegister(r) && right.isRegister(r)) {
                transpose = left.getUpdated(r, line) > right.getUpdated(r, line);
            } else {
                var rightIndex = rightExpression.getConstantIndex();
                var leftIndex = leftExpression.getConstantIndex();
                if (rightIndex != -1 && leftIndex != -1) {
                    if (left.type() == OperandType.K && rightIndex == leftIndex) {
                        transpose = true;
                    } else {
                        transpose = rightIndex < leftIndex;
                    }
                }
            }
        }
        var opstring = operator_to_string(op, inverted, transpose);
        Expression rtn = new BinaryExpression(opstring, !transpose ? leftExpression : rightExpression, !transpose ? rightExpression : leftExpression, Expression.PRECEDENCE_COMPARE, Expression.ASSOCIATIVITY_LEFT);
    /*
    if(inverted) {
      rtn = new UnaryExpression("not ", rtn, Expression.PRECEDENCE_UNARY);
    }
    */
        return rtn;
    }

    @Override
    public String toString() {
        return left + " " + operator_to_string(op, inverted, false) + " " + right;
    }

    public enum Operator {
        EQ,
        LT,
        LE,
        GT,
        GE
    }

}
