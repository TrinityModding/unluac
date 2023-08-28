package me.hydos.unluac.decompile.operation;

import me.hydos.unluac.decompile.Registers;
import me.hydos.unluac.decompile.block.Block;
import me.hydos.unluac.decompile.expression.Expression;
import me.hydos.unluac.decompile.expression.TableLiteral;
import me.hydos.unluac.decompile.statement.Assignment;
import me.hydos.unluac.decompile.statement.Statement;
import me.hydos.unluac.decompile.target.TableTarget;

import java.util.Collections;
import java.util.List;

public class TableSet extends Operation {

    private final Expression table;
    private final Expression index;
    private final Expression value;
    private final boolean isTable;
    private final int timestamp;

    public TableSet(int line, Expression table, Expression index, Expression value, boolean isTable, int timestamp) {
        super(line);
        this.table = table;
        this.index = index;
        this.value = value;
        this.isTable = isTable;
        this.timestamp = timestamp;
    }

    @Override
    public List<Statement> process(Registers r, Block block) {
        // .isTableLiteral() is sufficient when there is debugging info
        if (!r.isNoDebug && table.isTableLiteral() && (value.isMultiple() || table.isNewEntryAllowed())) {
            table.addEntry(new TableLiteral.Entry(index, value, !isTable, timestamp));
            return Collections.emptyList();
        } else {
            return List.of(new Assignment(new TableTarget(table, index), value, line));
        }
    }

}
