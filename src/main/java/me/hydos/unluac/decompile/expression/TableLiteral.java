package me.hydos.unluac.decompile.expression;

import me.hydos.unluac.decompile.core.Decompiler;
import me.hydos.unluac.decompile.core.Local;
import me.hydos.unluac.decompile.core.Output;
import me.hydos.unluac.decompile.core.Walker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class TableLiteral extends Expression {

    public final ArrayList<Entry> entries;
    private final int hashSize;
    private boolean isObject = true;
    private boolean isList = true;
    private int listLength = 1;
    private int hashCount;
    public TableLiteral(int arraySize, int hashSize) {
        super(PRECEDENCE_ATOMIC);
        entries = new ArrayList<>(arraySize + hashSize);
        this.hashSize = hashSize;
        hashCount = 0;
    }

    @Override
    public void walk(Walker w) {
        Collections.sort(entries);
        w.visitExpression(this);
        var lastEntry = false;
        for (var entry : entries) {
            entry.key.walk(w);
            if (!lastEntry) {
                entry.value.walk(w);
                if (entry.value.isMultiple()) {
                    lastEntry = true;
                }
            }
        }
    }

    @Override
    public void print(Decompiler d, Output out) {
        listLength = 1;
        if (entries.isEmpty()) {
            out.print("{}");
        } else {
            var lineBreak = isList && entries.size() > 5 || isObject && entries.size() > 2 || !isObject;
            //System.out.println(" -- " + (isList && entries.size() > 5));
            //System.out.println(" -- " + (isObject && entries.size() > 2));
            //System.out.println(" -- " + (!isObject));
            if (!lineBreak) {
                for (var entry : entries) {
                    var value = entry.value;
                    if (!(value.isBrief())) {
                        lineBreak = true;
                        break;
                    }
                }
            }
            out.print("{");
            if (lineBreak) {
                out.println();
                out.indent();
            }
            printEntry(d, 0, out);
            if (!entries.get(0).value.isMultiple()) {
                for (var index = 1; index < entries.size(); index++) {
                    out.print(",");
                    if (lineBreak) {
                        out.println();
                    } else {
                        out.print(" ");
                    }
                    printEntry(d, index, out);
                    if (entries.get(index).value.isMultiple()) {
                        break;
                    }
                }
            }
            if (lineBreak) {
                out.println();
                out.dedent();
            }
            out.print("}");
        }
    }

    @Override
    public int getConstantIndex() {
        var index = -1;
        for (var entry : entries) {
            index = Math.max(entry.key.getConstantIndex(), index);
            index = Math.max(entry.value.getConstantIndex(), index);
        }
        return index;
    }

    @Override
    public boolean isUngrouped() {
        return true;
    }

    @Override
    public boolean isTableLiteral() {
        return true;
    }

    @Override
    public boolean isNewEntryAllowed() {
        return true;
    }

    @Override
    public void addEntry(Entry entry) {
        if (hashCount < hashSize && entry.key.isIdentifier()) {
            entry.hash = true;
            hashCount++;
        }
        entries.add(entry);
        isObject = isObject && (entry.isList || entry.key.isIdentifier());
        isList = isList && entry.isList;
    }

    @Override
    public boolean isBrief() {
        return false;
    }

    private void printEntry(Decompiler d, int index, Output out) {
        var entry = entries.get(index);
        var key = entry.key;
        var value = entry.value;
        var isList = entry.isList;
        var multiple = index + 1 >= entries.size() || value.isMultiple();
        if (isList && key.isInteger() && listLength == key.asInteger()) {
            if (multiple) {
                value.printMultiple(d, out);
            } else {
                value.print(d, out);
            }
            listLength++;
        } else if (entry.hash/*isObject && key.isIdentifier()*/) {
            out.print(key.asName());
            out.print(" = ");
            value.print(d, out);
        } else {
            out.print("[");
            key.printBraced(d, out);
            out.print("] = ");
            value.print(d, out);
        }
    }

    public static class Entry implements Comparable<Entry> {

        public final Expression key;
        public final Expression value;
        public final boolean isList;
        public final int timestamp;
        private boolean hash;

        public Entry(Expression key, Expression value, boolean isList, int timestamp) {
            this.key = key;
            this.value = value;
            this.isList = isList;
            this.timestamp = timestamp;
        }

        @Override
        public int compareTo(Entry e) {
            return Integer.compare(timestamp, e.timestamp);
        }
    }

    @Override
    public void remapLocals(Map<Local, Local> localRemaps) {
        entries.forEach(entry -> {
            entry.key.remapLocals(localRemaps);
            entry.value.remapLocals(localRemaps);
        });
    }
}
