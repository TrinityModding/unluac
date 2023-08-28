package me.hydos.unluac.decompile.block;

import me.hydos.unluac.decompile.*;
import me.hydos.unluac.decompile.condition.AndCondition;
import me.hydos.unluac.decompile.condition.Condition;
import me.hydos.unluac.decompile.condition.FinalSetCondition;
import me.hydos.unluac.decompile.condition.OrCondition;
import me.hydos.unluac.decompile.expression.Expression;
import me.hydos.unluac.decompile.operation.Operation;
import me.hydos.unluac.decompile.statement.Assignment;
import me.hydos.unluac.decompile.statement.Statement;
import me.hydos.unluac.parse.LFunction;

import java.util.Collections;
import java.util.List;

public class IfThenEndBlock extends ContainerBlock {

    private final Condition cond;
    private final boolean redirected;
    private final Registers r;

    private Expression condexpr;

    public IfThenEndBlock(LFunction function, Registers r, Condition cond, int begin, int end) {
        this(function, r, cond, begin, end, CloseType.NONE, -1, false);
    }

    public IfThenEndBlock(LFunction function, Registers r, Condition cond, int begin, int end, CloseType closeType, int closeLine, boolean redirected) {
        super(function, begin == end ? begin - 1 : begin, end, closeType, closeLine, -1);
        this.r = r;
        this.cond = cond;
        this.redirected = redirected;
    }

    @Override
    public void resolve(Registers r) {
        condexpr = cond.asExpression(r);
    }

    @Override
    public int scopeEnd() {
        return usingClose && closeType == CloseType.CLOSE ? closeLine - 1 : super.scopeEnd();
    }

    @Override
    public boolean hasHeader() {
        return false;
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
    public Operation process(Decompiler d) {
        final var test = cond.register();
        //System.err.println(cond);
        if (!scopeUsed && !redirected && test >= 0 && r.getUpdated(test, end - 1) >= begin && !d.getNoDebug()) {
            // Check for a single assignment
            Assignment assign = null;
            if (statements.size() == 1) {
                var stmt = statements.get(0);
                if (stmt instanceof Assignment) {
                    assign = (Assignment) stmt;
                    if (assign.getArity() > 1) {
                        var line = assign.getFirstLine();
                        if (line >= begin && line < end) {
                            assign = null;
                        }
                    }
                }
            }
            if (assign != null && (cond.isRegisterTest() || cond.isOrCondition() || assign.isDeclaration()) && assign.getLastTarget().isLocal() && assign.getLastTarget().getIndex() == test || statements.isEmpty()) {
                var finalset = new FinalSetCondition(end - 1, test);
                finalset.type = FinalSetCondition.Type.VALUE;
                Condition combined;

                if (cond.invertible()) {
                    combined = new OrCondition(cond.inverse(), finalset);
                } else {
                    combined = new AndCondition(cond, finalset);
                }
                final Assignment fassign;
                if (assign != null) {
                    fassign = assign;
                    fassign.replaceLastValue(combined.asExpression(r));
                } else {
                    fassign = null;
                }
                final var fcombined = combined;
                return new Operation(end - 1) {

                    @Override
                    public List<Statement> process(Registers r, Block block) {
                        if (fassign == null) {
                            r.setValue(test, end - 1, fcombined.asExpression(r));
                            return Collections.emptyList();
                        } else {
                            return List.of(fassign);
                        }
                    }

                };
            }
        }
        return super.process(d);
    }

    @Override
    public void walk(Walker w) {
        w.visitStatement(this);
        condexpr.walk(w);
        for (var statement : statements) {
            statement.walk(w);
        }
    }

    @Override
    public void print(Decompiler d, Output out) {
        out.print("if ");
        condexpr.print(d, out);
        out.print(" then");
        out.println();
        out.indent();
        Statement.printSequence(d, out, statements);
        out.dedent();
        out.print("end");
    }

}
