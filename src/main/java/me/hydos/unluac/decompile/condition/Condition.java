package me.hydos.unluac.decompile.condition;

import me.hydos.unluac.decompile.core.Registers;
import me.hydos.unluac.decompile.expression.ConstantExpression;
import me.hydos.unluac.decompile.expression.Expression;

public interface Condition {

    Condition inverse();

    boolean invertible();

    int register();

    boolean isRegisterTest();

    boolean isOrCondition();

    boolean isSplitable();

    Condition[] split();

    Expression asExpression(Registers r);

    @Override
    String toString();

    enum OperandType {
        R,
        RK,
        K,
        I,
        F,
    }

  record Operand(OperandType type, int value) {

    public Expression asExpression(Registers r, int line) {
      return switch (type) {
        case R -> r.getExpression(this.value, line);
        case RK -> r.getKExpression(this.value, line);
        case K -> r.getFunction().getConstantExpression(this.value);
        case I -> ConstantExpression.createInteger(this.value);
        case F -> ConstantExpression.createDouble(this.value);
        default -> throw new IllegalStateException();
      };
    }

    public boolean isRegister(Registers r) {
      return switch (type) {
        case R -> true;
        case RK -> !r.isKConstant(this.value);
        case K -> false;
        case I -> false;
        case F -> false;
        default -> throw new IllegalStateException();
      };
    }

    public int getUpdated(Registers r, int line) {
      return switch (type) {
        case R -> r.getUpdated(this.value, line);
        case RK -> {
          if (r.isKConstant(this.value)) throw new IllegalStateException();
          yield r.getUpdated(this.value, line);
        }
        default -> throw new IllegalStateException();
      };
    }
  }
}
