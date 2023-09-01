package me.hydos.unluac.decompile.operation;

import me.hydos.unluac.decompile.Registers;
import me.hydos.unluac.decompile.block.Block;
import me.hydos.unluac.decompile.expression.Expression;
import me.hydos.unluac.decompile.statement.AssignmentStatement;
import me.hydos.unluac.decompile.statement.Statement;
import me.hydos.unluac.decompile.target.TableTarget;

import java.util.List;

public class TableSet extends Operation {

    private final Expression table;
    private final Expression index;
    private final Expression value;

    public TableSet(int line, Expression table, Expression index, Expression value, int timestamp) {
        super(line);
        this.table = table;
        this.index = index;
        this.value = value;
    }

    @Override
    public List<Statement> process(Registers r, Block block) {
        // .isTableLiteral() is sufficient when there is debugging info
        return List.of(new AssignmentStatement(new TableTarget(table, index), value, line));
    }
}
