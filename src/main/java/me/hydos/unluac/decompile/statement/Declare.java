package me.hydos.unluac.decompile.statement;

import me.hydos.unluac.decompile.Declaration;
import me.hydos.unluac.decompile.Decompiler;
import me.hydos.unluac.decompile.Output;
import me.hydos.unluac.decompile.Walker;

import java.util.List;

public class Declare extends Statement {

    private final List<Declaration> decls;

    public Declare(List<Declaration> decls) {
        this.decls = decls;
    }

    @Override
    public void print(Decompiler d, Output out) {
        out.print("local ");
        out.print(decls.get(0).name);
        for (var i = 1; i < decls.size(); i++) {
            out.print(", ");
            out.print(decls.get(i).name);
        }
    }

    @Override
    public void walk(Walker w) {
        w.visitStatement(this);
    }

}
