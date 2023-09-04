package me.hydos.unluac.decompile.operation;

import me.hydos.unluac.decompile.core.Registers;
import me.hydos.unluac.decompile.block.Block;
import me.hydos.unluac.decompile.expression.Expression;
import me.hydos.unluac.decompile.statement.AssignmentStatement;
import me.hydos.unluac.decompile.statement.Statement;

import java.util.Collections;
import java.util.List;

public class MultipleRegisterSet extends Operation {

    public final int registerFirst;
    public final int registerLast;
    public final Expression value;

    public MultipleRegisterSet(int line, int registerFirst, int registerLast, Expression value) {
        super(line);
        this.registerFirst = registerFirst;
        this.registerLast = registerLast;
        this.value = value;
    }

    @Override
    public List<Statement> process(Registers r, Block block) {
        var count = 0;
        var assignment = new AssignmentStatement();
        for (var register = registerFirst; register <= registerLast; register++) {
            r.setValue(register, line, value);
            if (r.isAssignable(register, line)) {
                assignment.addLast(r.getTarget(register, line), value, line);
                count++;
            }
        }
        if (count > 0) {
            return List.of(assignment);
        } else {
            return Collections.emptyList();
        }
    }
}
