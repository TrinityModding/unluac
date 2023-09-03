package me.hydos.unluac.decompile.expression;

import me.hydos.unluac.decompile.Decompiler;
import me.hydos.unluac.decompile.Local;
import me.hydos.unluac.decompile.Output;
import me.hydos.unluac.decompile.Walker;

import java.util.Map;

public class UnaryExpression extends Expression {

    private final String op;
    public Expression expression;

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
    public void print(Decompiler d, Output out) {
        out.print(op);
        if (precedence > expression.precedence) out.print("(");
        expression.print(d, out);
        if (precedence > expression.precedence) out.print(")");
    }

    @Override
    public int getConstantIndex() {
        return expression.getConstantIndex();
    }

    @Override
    public boolean isUngrouped() {
        return true;
    }

    @Override
    public void remapLocals(Map<Local, Local> localRemaps) {
        expression.remapLocals(localRemaps);
    }
}
