package me.hydos.unluac.decompile.statement;

import me.hydos.unluac.decompile.Decompiler;
import me.hydos.unluac.decompile.Function;
import me.hydos.unluac.decompile.Output;
import me.hydos.unluac.decompile.Walker;

import java.util.List;

abstract public class Statement {

    public String comment;

    /**
     * Prints out a sequences of statements on separate lines. Correctly
     * informs the last statement that it is last in a block.
     */
    public static void printSequence(Decompiler d, Output out, List<Statement> stmts) {
        var n = stmts.size();
        for (var i = 0; i < n; i++) {
            var last = (i + 1 == n);
            var stmt = stmts.get(i);
            if (stmt.beginsWithParen() && (i > 0 || d.getVersion().allowpreceedingsemicolon.get())) {
                out.print(";");
            }
            if (last) {
                stmt.printTail(d, out);
            } else {
                stmt.print(d, out);
            }
            if (!stmt.suppressNewline()) {
                out.println();
            }
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

    public boolean useConstant(Function f, int index) {
        return false;
    }

}
