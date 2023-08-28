package me.hydos.unluac.decompile.expression;

import me.hydos.unluac.decompile.Decompiler;
import me.hydos.unluac.decompile.Output;
import me.hydos.unluac.decompile.Walker;

public class UnaryExpression extends Expression {

  private final String op;
  private final Expression expression;
  
  public UnaryExpression(String op, Expression expression, int precedence) {
    super(precedence);
    this.op = op;
    this.expression = expression;
  }

  @Override
  public void walk(Walker w) {
    w.visitExpression(this);
    expression.walk(w);
  }
  
  @Override
  public boolean isUngrouped() {
    return true;
  }
  
  @Override
  public int getConstantIndex() {
    return expression.getConstantIndex();
  }
  
  @Override
  public void print(Decompiler d, Output out) {
    out.print(op);
    if(precedence > expression.precedence) out.print("(");
    expression.print(d, out);
    if(precedence > expression.precedence) out.print(")");
  }
  
}
