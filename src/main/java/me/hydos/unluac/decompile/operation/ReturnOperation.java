package me.hydos.unluac.decompile.operation;

import me.hydos.unluac.decompile.Registers;
import me.hydos.unluac.decompile.block.Block;
import me.hydos.unluac.decompile.expression.Expression;
import me.hydos.unluac.decompile.statement.Return;
import me.hydos.unluac.decompile.statement.Statement;

import java.util.List;

public class ReturnOperation extends Operation {

    private final Expression[] values;

    public ReturnOperation(int line, Expression value) {
        super(line);
        values = new Expression[1];
        values[0] = value;
    }

    public ReturnOperation(int line, Expression[] values) {
        super(line);
        this.values = values;
    }

    @Override
    public List<Statement> process(Registers r, Block block) {
        return List.of(new Return(values));
    }

}