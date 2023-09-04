package me.hydos.unluac.decompile.operation;

import me.hydos.unluac.decompile.core.Registers;
import me.hydos.unluac.decompile.block.Block;
import me.hydos.unluac.decompile.statement.Statement;

import java.util.List;

abstract public class Operation {

    public final int line;

    public Operation(int line) {
        this.line = line;
    }

    abstract public List<Statement> process(Registers r, Block block);

}
