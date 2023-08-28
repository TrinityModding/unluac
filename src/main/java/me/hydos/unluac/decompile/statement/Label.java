package me.hydos.unluac.decompile.statement;

import me.hydos.unluac.decompile.Decompiler;
import me.hydos.unluac.decompile.Output;
import me.hydos.unluac.decompile.Walker;

public class Label extends Statement {

    private final String name;

    public Label(int line) {
        name = "lbl_" + line;
    }

    @Override
    public void print(Decompiler d, Output out) {
        out.print("::" + name + "::");
    }

    public void walk(Walker w) {
        w.visitStatement(this);
    }

}
