package me.hydos.unluac.decompile.statement;

import me.hydos.unluac.decompile.core.Decompiler;
import me.hydos.unluac.decompile.core.Local;
import me.hydos.unluac.decompile.core.Output;
import me.hydos.unluac.decompile.core.Walker;

import java.util.Map;

public class Label extends Statement {

    private final String name;

    public Label(int line) {
        this.name = "label_" + line;
    }

    @Override
    public void print(Decompiler d, Output out) {
        out.print("::" + name + "::");
    }

    public void walk(Walker w) {
        w.visitStatement(this);
    }

    @Override
    public void remapLocals(Map<Local, Local> localRemaps, Map<Local, Local> lastLocalRemaps) {}

    @Override
    public void fillUsageMap(Map<Local, Boolean> localUsageMap, boolean includeAssignments) {}
}
