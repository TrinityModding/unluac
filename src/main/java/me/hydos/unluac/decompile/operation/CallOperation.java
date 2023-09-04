package me.hydos.unluac.decompile.operation;

import me.hydos.unluac.decompile.core.Registers;
import me.hydos.unluac.decompile.block.Block;
import me.hydos.unluac.decompile.expression.FunctionCall;
import me.hydos.unluac.decompile.statement.FunctionCallStatement;
import me.hydos.unluac.decompile.statement.Statement;

import java.util.List;

public class CallOperation extends Operation {

    private final FunctionCall call;

    public CallOperation(int line, FunctionCall call) {
        super(line);
        this.call = call;
    }

    @Override
    public List<Statement> process(Registers r, Block block) {
        return List.of(new FunctionCallStatement(call));
    }
}
