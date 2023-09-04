package me.hydos.unluac.decompile.statement;

import me.hydos.unluac.decompile.core.*;
import me.hydos.unluac.decompile.expression.Expression;

import java.util.List;
import java.util.Map;

abstract public class Statement {

    public String comment;

    /**
     * Prints out a sequences of statements on separate lines. Correctly
     * informs the last statement that it is last in a block.
     */
    public static void printSequence(Decompiler d, Output out, List<Statement> stmts) {
        for (var i = 0; i < stmts.size(); i++) {
            var last = (i + 1 == stmts.size());
            var stmt = stmts.get(i);
            if (stmt.beginsWithParen() && (i > 0 || d.bytecodeVersion.allowpreceedingsemicolon.get())) out.print(";");
            if (last) stmt.printTail(d, out);
            else stmt.print(d, out);
            if (!stmt.suppressNewline()) out.println();
        }
    }

    abstract public void print(Decompiler d, Output out);

    public void printTail(Decompiler d, Output out) {
        print(d, out);
    }

    public void addComment(String comment) {
        this.comment = comment;
    }

    public abstract void walk(Walker w);

    public boolean beginsWithParen() {
        return false;
    }

    public boolean suppressNewline() {
        return false;
    }

    public boolean useConstant(FunctionQuery f, int index) {
        return false;
    }

    public abstract void fillUsageMap(Map<Local, Boolean> localUsageMap, boolean includeAssignments);

    public abstract void remapLocals(Map<Local, Local> localRemaps, Map<Local, Local> lastLocalRemaps);

    public boolean isActionStatement() {
        return false;
    }

    public List<Local> getActionVars() {
        throw new IllegalStateException(getClass().getSimpleName());
    }

    public void inlineLocal(Local local, Expression statement) {
        throw new IllegalStateException(getClass().getSimpleName());
    }
}
