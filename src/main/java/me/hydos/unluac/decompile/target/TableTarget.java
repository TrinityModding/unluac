package me.hydos.unluac.decompile.target;

import me.hydos.unluac.decompile.core.Decompiler;
import me.hydos.unluac.decompile.core.Output;
import me.hydos.unluac.decompile.core.Walker;
import me.hydos.unluac.decompile.expression.Expression;
import me.hydos.unluac.decompile.expression.TableReference;

public class TableTarget extends Target {

    private final Expression table;
    private final Expression index;

    public TableTarget(Expression table, Expression index) {
        this.table = table;
        this.index = index;
    }

    @Override
    public void walk(Walker w) {
        table.walk(w);
        index.walk(w);
    }

    @Override
    public void print(Decompiler d, Output out, boolean declare) {
        new TableReference(table, index).print(d, out);
    }

    @Override
    public void printMethod(Decompiler d, Output out) {
        table.print(d, out);
        out.print(":");
        out.print(index.asName());
    }

    @Override
    public boolean isFunctionName() {
        if (!index.isIdentifier()) {
            return false;
        }
        return table.isDotChain();
    }

    @Override
    public boolean beginsWithParen() {
        return table.isUngrouped() || table.beginsWithParen();
    }

}
