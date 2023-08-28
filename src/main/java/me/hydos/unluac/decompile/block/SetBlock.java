package me.hydos.unluac.decompile.block;

import me.hydos.unluac.decompile.*;
import me.hydos.unluac.decompile.condition.Condition;
import me.hydos.unluac.decompile.expression.Expression;
import me.hydos.unluac.decompile.operation.Operation;
import me.hydos.unluac.decompile.statement.Assignment;
import me.hydos.unluac.decompile.statement.Statement;
import me.hydos.unluac.parse.LFunction;

import java.util.Collections;
import java.util.List;

public class SetBlock extends Block {

    public final int target;
    public final Condition cond;
    private final Registers r;
    private final boolean finalize = false;
    private Assignment assign;

    public SetBlock(LFunction function, Condition cond, int target, int begin, int end, Registers r) {
        super(function, begin, end, 2);
        if (begin == end) throw new IllegalStateException();
        this.target = target;
        this.cond = cond;
        this.r = r;
        if (target == -1) {
            throw new IllegalStateException();
        }
        // System.out.println("-- set block " + begin + " .. " + end);
    }

    @Override
    public void addStatement(Statement statement) {
        if (!finalize && statement instanceof Assignment) {
            this.assign = (Assignment) statement;
        }/* else if(statement instanceof BooleanIndicator) {
      finalize = true;
    }*/
    }

    @Override
    public boolean hasHeader() {
        return true;
    }

    @Override
    public boolean isUnprotected() {
        return false;
    }

    @Override
    public int getLoopback() {
        throw new IllegalStateException();
    }

    @Override
    public boolean breakable() {
        return false;
    }

    @Override
    public boolean isContainer() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public Operation process(final Decompiler d) {
        if (ControlFlowHandler.verbose) {
            System.out.print("set expression: ");
            cond.asExpression(r).print(d, new Output());
            System.out.println();
        }
        if (assign != null) {
            assign.replaceValue(target, getValue());
            return new Operation(end - 1) {

                @Override
                public List<Statement> process(Registers r, Block block) {
                    return Collections.singletonList(assign);
                }

            };
        } else {
            return new Operation(end - 1) {

                @Override
                public List<Statement> process(Registers r, Block block) {
                    if (r.isLocal(target, end - 1)) {
                        return List.of(new Assignment(r.getTarget(target, end - 1), cond
                                .asExpression(r), end - 1));
                    }
                    r.setValue(target, end - 1, cond.asExpression(r));
                    return Collections.emptyList();
                }

            };
            // return super.process();
        }
    }

    @Override
    public void print(Decompiler d, Output out) {
        if (assign != null && assign.getFirstTarget() != null) {
            var assignOut = new Assignment(assign.getFirstTarget(), getValue(), assign.getFirstLine());
            assignOut.print(d, out);
        } else {
            throw new IllegalStateException();
        }
    }

    public void walk(Walker w) {
        throw new IllegalStateException();
    }

    public void useAssignment(Assignment assign) {
        this.assign = assign;
        // branch.useExpression(assign.getFirstValue());
    }

    public Expression getValue() {
        return cond.asExpression(r);
    }

}
