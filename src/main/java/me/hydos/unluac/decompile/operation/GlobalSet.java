package me.hydos.unluac.decompile.operation;

import me.hydos.unluac.decompile.core.Registers;
import me.hydos.unluac.decompile.block.Block;
import me.hydos.unluac.decompile.expression.ConstantExpression;
import me.hydos.unluac.decompile.expression.Expression;
import me.hydos.unluac.decompile.statement.AssignmentStatement;
import me.hydos.unluac.decompile.statement.Statement;
import me.hydos.unluac.decompile.target.GlobalTarget;

import java.util.List;

public class GlobalSet extends Operation {

    private final ConstantExpression global;
    private final Expression value;

    public GlobalSet(int line, ConstantExpression global, Expression value) {
        super(line);
        this.global = global;
        this.value = value;
    }

    @Override
    public List<Statement> process(Registers r, Block block) {
        return List.of(new AssignmentStatement(new GlobalTarget(global), value, line));
    }

}
