package me.hydos.unluac.decompile.expression;

import me.hydos.unluac.decompile.Decompiler;
import me.hydos.unluac.decompile.Output;
import me.hydos.unluac.decompile.Walker;

public class TableReference extends Expression {

    private final Expression table;
    private final Expression index;

    public TableReference(Expression table, Expression index) {
        super(PRECEDENCE_ATOMIC);
        this.table = table;
        this.index = index;
    }

    @Override
    public void walk(Walker w) {
        w.visitExpression(this);
        table.walk(w);
        index.walk(w);
    }

    @Override
    public void print(Decompiler d, Output out) {
        var isGlobal = table.isEnvironmentTable(d) && index.isIdentifier();
        if (!isGlobal) {
            if (table.isUngrouped()) {
                out.print("(");
                table.print(d, out);
                out.print(")");
            } else {
                table.print(d, out);
            }
        }
        if (index.isIdentifier()) {
            if (!isGlobal) {
                out.print(".");
            }
            out.print(index.asName());
        } else {
            out.print("[");
            index.printBraced(d, out);
            out.print("]");
        }
    }

    @Override
    public int getConstantIndex() {
        return Math.max(table.getConstantIndex(), index.getConstantIndex());
    }

    @Override
    public boolean beginsWithParen() {
        return table.isUngrouped() || table.beginsWithParen();
    }

    @Override
    public boolean isDotChain() {
        return index.isIdentifier() && table.isDotChain();
    }

    @Override
    public boolean isMemberAccess() {
        return index.isIdentifier();
    }

    @Override
    public Expression getTable() {
        return table;
    }

    @Override
    public String getField() {
        return index.asName();
    }


}
