package me.hydos.unluac.decompile.target;

import me.hydos.unluac.decompile.Local;
import me.hydos.unluac.decompile.Decompiler;
import me.hydos.unluac.decompile.Output;
import me.hydos.unluac.decompile.Walker;

abstract public class Target {

    abstract public void walk(Walker w);

    abstract public void print(Decompiler d, Output out, boolean declare);

    abstract public void printMethod(Decompiler d, Output out);

    public boolean isDeclaration(Local decl) {
        return false;
    }

    public boolean isLocal() {
        return false;
    }

    public int getIndex() {
        throw new IllegalStateException();
    }

    public boolean isFunctionName() {
        return true;
    }

    public boolean beginsWithParen() {
        return false;
    }

}
