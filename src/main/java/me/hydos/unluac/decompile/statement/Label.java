package me.hydos.unluac.decompile.statement;

import me.hydos.unluac.decompile.Decompiler;
import me.hydos.unluac.decompile.Local;
import me.hydos.unluac.decompile.Output;
import me.hydos.unluac.decompile.Walker;

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
    public void remapLocals(Map<Local, Local> localRemaps) {}

    @Override
    public void fillUsageMap(Map<Local, Boolean> localUsageMap, boolean includeAssignments) {}
}
