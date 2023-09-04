package me.hydos.unluac.decompile.expression;

import me.hydos.unluac.decompile.core.Decompiler;
import me.hydos.unluac.decompile.core.Local;
import me.hydos.unluac.decompile.core.Output;
import me.hydos.unluac.decompile.core.Walker;

import java.util.Map;
import java.util.Objects;

public class TableReference extends Expression {

    public Expression table;
    public Expression index;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (TableReference) o;
        return Objects.equals(table, that.table) && Objects.equals(index, that.index);
    }

    @Override
    public int hashCode() {
        return Objects.hash(table, index);
    }

    @Override
    public void remapLocals(Map<Local, Local> localRemaps) {
        table.remapLocals(localRemaps);
        index.remapLocals(localRemaps);
    }

    @Override
    public void fillUsageMap(Map<Local, Boolean> localUsageMap, boolean includeAssignments) {
        table.fillUsageMap(localUsageMap, includeAssignments);
        index.fillUsageMap(localUsageMap, includeAssignments);
    }
}
