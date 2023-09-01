package me.hydos.unluac.decompile.operation;

import me.hydos.unluac.decompile.Registers;
import me.hydos.unluac.decompile.block.Block;
import me.hydos.unluac.decompile.expression.Expression;
import me.hydos.unluac.decompile.statement.AssignmentStatement;
import me.hydos.unluac.decompile.statement.Statement;
import me.hydos.unluac.decompile.target.UpvalueTarget;

import java.util.List;

public class UpvalueSet extends Operation {

    private final UpvalueTarget target;
    private final Expression value;

    public UpvalueSet(int line, String upvalue, Expression value) {
        super(line);
        target = new UpvalueTarget(upvalue);
        this.value = value;
    }

    @Override
    public List<Statement> process(Registers r, Block block) {
        return List.of(new AssignmentStatement(target, value, line));
    }

}
