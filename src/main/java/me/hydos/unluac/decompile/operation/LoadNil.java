package me.hydos.unluac.decompile.operation;

import me.hydos.unluac.decompile.Registers;
import me.hydos.unluac.decompile.block.Block;
import me.hydos.unluac.decompile.expression.ConstantExpression;
import me.hydos.unluac.decompile.expression.Expression;
import me.hydos.unluac.decompile.statement.Assignment;
import me.hydos.unluac.decompile.statement.Statement;

import java.util.ArrayList;
import java.util.List;

public class LoadNil extends Operation {

    public final int registerFirst;
    public final int registerLast;

    public LoadNil(int line, int registerFirst, int registerLast) {
        super(line);
        this.registerFirst = registerFirst;
        this.registerLast = registerLast;
    }

    @Override
    public List<Statement> process(Registers r, Block block) {
        List<Statement> assignments = new ArrayList<>(registerLast - registerFirst + 1);
        Expression nil = ConstantExpression.createNil(line);
        Assignment declare = null;
        var scopeEnd = -1;
        for (var register = registerFirst; register <= registerLast; register++) {
            if (r.isAssignable(register, line)) {
                scopeEnd = r.getDeclaration(register, line).end;
            }
        }
        for (var register = registerFirst; register <= registerLast; register++) {
            r.setValue(register, line, nil);
            if (r.isAssignable(register, line) && r.getDeclaration(register, line).end == scopeEnd && register >= block.closeRegister) {
                if ((r.getDeclaration(register, line).begin == line)) {
                    if (declare == null) {
                        declare = new Assignment();
                        assignments.add(declare);
                    }
                    declare.addLast(r.getTarget(register, line), nil, line);
                } else {
                    assignments.add(new Assignment(r.getTarget(register, line), nil, line));
                }
            }
        }
        return assignments;
    }

}
