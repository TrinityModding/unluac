package me.hydos.unluac.decompile.target;

import me.hydos.unluac.decompile.core.Decompiler;
import me.hydos.unluac.decompile.core.Output;
import me.hydos.unluac.decompile.core.Walker;

public class UpvalueTarget extends Target {

    private final String name;

    public UpvalueTarget(String name) {
        this.name = name;
    }

    @Override
    public void walk(Walker w) {
    }

    @Override
    public void print(Decompiler d, Output out, boolean declare) {
        out.print(name);
    }

    @Override
    public void printMethod(Decompiler d, Output out) {
        throw new IllegalStateException();
    }

}
