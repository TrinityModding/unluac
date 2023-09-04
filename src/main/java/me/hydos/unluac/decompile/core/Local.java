package me.hydos.unluac.decompile.core;

import me.hydos.unluac.bytecode.LLocal;

import java.util.Objects;

public class Local {

    public final String name;
    public final int begin;
    public final int end;
    public int register;
    public boolean tbc;
    public boolean needsDeclaring = true;

    /**
     * Whether this is an invisible for-loop book-keeping variable.
     */
    public boolean forLoop = false;

    /**
     * Whether this is an explicit for-loop declared variable.
     */
    public boolean forLoopExplicit = false;

    public Local(LLocal local, BytecodeReader bytecodeReader) {
        var adjust = 0;
        if (local.start >= 1) {
            var op = bytecodeReader.op(local.start);
            if (op == Op.MMBIN || op == Op.MMBINI || op == Op.MMBINK || op == Op.EXTRAARG) {
                adjust--;
            }
        }
        this.name = local.toString();
        this.begin = local.start + adjust;
        this.end = local.end;
        this.tbc = false;
    }

    public Local(String name, int begin, int end) {
        this.name = name;
        this.begin = begin;
        this.end = end;
    }

    public boolean isSplitBy(int line, int begin, int end) {
        var scopeEnd = end - 1;
        return this.begin >= line && this.begin < begin || this.end >= line && this.end < begin || this.begin < begin && this.end >= begin && this.end < scopeEnd || this.begin >= begin && this.begin <= scopeEnd && this.end > scopeEnd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var local = (Local) o;
        return Objects.equals(name, local.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Local{" + "name='" + name + '\'' + '}';
    }
}
