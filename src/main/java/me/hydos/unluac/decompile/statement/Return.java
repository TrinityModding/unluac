package me.hydos.unluac.decompile.statement;

import me.hydos.unluac.decompile.Decompiler;
import me.hydos.unluac.decompile.Output;
import me.hydos.unluac.decompile.Walker;
import me.hydos.unluac.decompile.expression.Expression;

import java.util.ArrayList;
import java.util.Collections;

public class Return extends Statement {

    private final Expression[] values;

    public Return() {
        values = new Expression[0];
    }

    public Return(Expression value) {
        values = new Expression[1];
        values[0] = value;
    }

    public Return(Expression[] values) {
        this.values = values;
    }

    @Override
    public void print(Decompiler d, Output out) {
        out.print("do ");
        printTail(d, out);
        out.print(" end");
    }

    @Override
    public void printTail(Decompiler d, Output out) {
        out.print("return");
        if (values.length > 0) {
            out.print(" ");
            var rtns = new ArrayList<Expression>(values.length);
            Collections.addAll(rtns, values);
            Expression.printSequence(d, out, rtns, false, true);
        }
    }

    public void walk(Walker w) {
        w.visitStatement(this);
        for (var expression : values) {
            expression.walk(w);
        }
    }

}
