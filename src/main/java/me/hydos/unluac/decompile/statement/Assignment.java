package me.hydos.unluac.decompile.statement;

import me.hydos.unluac.decompile.Declaration;
import me.hydos.unluac.decompile.Decompiler;
import me.hydos.unluac.decompile.Output;
import me.hydos.unluac.decompile.Walker;
import me.hydos.unluac.decompile.expression.Expression;
import me.hydos.unluac.decompile.target.Target;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Assignment extends Statement {

    private final ArrayList<Target> targets = new ArrayList<>(5);
    private final ArrayList<Expression> values = new ArrayList<>(5);
    private final ArrayList<Integer> lines = new ArrayList<>(5);

    private boolean allnil = true;
    private boolean declare = false;
    private int declareStart = 0;

    public Assignment() {

    }

    public Assignment(Target target, Expression value, int line) {
        targets.add(target);
        values.add(value);
        lines.add(line);
        allnil = allnil && value.isNil();
    }

    public Target getFirstTarget() {
        return targets.get(0);
    }

    public Target getLastTarget() {
        return targets.get(targets.size() - 1);
    }

    public Expression getFirstValue() {
        return values.get(0);
    }

    public void replaceLastValue(Expression value) {
        values.set(values.size() - 1, value);
    }

    public int getFirstLine() {
        return lines.get(0);
    }

    public boolean assignsTarget(Declaration decl) {
        for (var target : targets) {
            if (target.isDeclaration(decl)) {
                return true;
            }
        }
        return false;
    }

    public int getArity() {
        return targets.size();
    }

    public void addFirst(Target target, Expression value, int line) {
        targets.add(0, target);
        values.add(0, value);
        lines.add(0, line);
        allnil = allnil && value.isNil();
    }

    public void addLast(Target target, Expression value, int line) {
        if (targets.contains(target)) {
            var index = targets.indexOf(target);
            targets.remove(index);
            value = values.remove(index);
            lines.remove(index);
        }
        targets.add(target);
        values.add(value);
        lines.add(line);
        allnil = allnil && value.isNil();
    }

    public Expression getValue(int target) {
        var index = 0;
        for (var t : targets) {
            if (t.isLocal() && t.getIndex() == target) {
                return values.get(index);
            }
            index++;
        }
        throw new IllegalStateException();
    }

    public void replaceValue(int target, Expression value) {
        var index = 0;
        for (var t : targets) {
            if (t.isLocal() && t.getIndex() == target) {
                values.set(index, value);
                //lines.set(index, line);
                return;
            }
            index++;
        }
        throw new IllegalStateException();
    }

    public boolean assignListEquals(List<Declaration> decls) {
        if (decls.size() != targets.size()) return false;
        for (var target : targets) {
            var found = false;
            for (var decl : decls) {
                if (target.isDeclaration(decl)) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    public void declare(int declareStart) {
        declare = true;
        this.declareStart = declareStart;
    }

    public boolean isDeclaration() {
        return declare;
    }

    public boolean assigns(Declaration decl) {
        for (var target : targets) {
            if (target.isDeclaration(decl)) return true;
        }
        return false;
    }

    public boolean canDeclare(List<Declaration> locals) {
        for (var target : targets) {
            var isNewLocal = false;
            for (var decl : locals) {
                if (target.isDeclaration(decl)) {
                    isNewLocal = true;
                    break;
                }
            }
            if (!isNewLocal) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void print(Decompiler d, Output out) {
        if (!targets.isEmpty()) {
            if (declare) {
                out.print("local ");
            }
            var functionSugar = false;
            if (targets.size() == 1 && values.size() == 1 && values.get(0).isClosure() && targets.get(0).isFunctionName()) {
                var closure = values.get(0);
                //comment = "" + declareStart + " >= " + closure.closureUpvalueLine();
                //System.out.println("" + declareStart + " >= " + closure.closureUpvalueLine());
                // This check only works in Lua version 0x51
                if (!declare || declareStart >= closure.closureUpvalueLine()) {
                    functionSugar = true;
                }
                if (targets.get(0).isLocal() && closure.isUpvalueOf(targets.get(0).getIndex())) {
                    functionSugar = true;
                }
                //if(closure.isUpvalueOf(targets.get(0).))
            }
            if (!functionSugar) {
                targets.get(0).print(d, out, declare);
                for (var i = 1; i < targets.size(); i++) {
                    out.print(", ");
                    targets.get(i).print(d, out, declare);
                }
                if (!declare || !allnil) {
                    out.print(" = ");

                    var expressions = new LinkedList<Expression>();

                    var size = values.size();
                    if (size >= 2 && values.get(size - 1).isNil() && (lines.get(size - 1) == values.get(size - 1).getConstantLine() || values.get(size - 1).getConstantLine() == -1)) {

                        expressions.addAll(values);

                    } else {

                        var include = false;
                        for (var i = size - 1; i >= 0; i--) {
                            var value = values.get(i);
                            if (include || !value.isNil() || value.getConstantIndex() != -1) {
                                include = true;
                            }
                            if (include) {
                                expressions.addFirst(value);
                            }
                        }

                        if (expressions.isEmpty() && !declare) {
                            expressions.addAll(values);
                        }
                    }

                    Expression.printSequence(d, out, expressions, false, targets.size() > expressions.size());
                }
            } else {
                values.get(0).printClosure(d, out, targets.get(0));
            }
            if (comment != null) {
                out.print(" -- ");
                out.print(comment);
            }
        }
    }

    @Override
    public void walk(Walker w) {
        w.visitStatement(this);
        for (var target : targets) {
            target.walk(w);
        }
        for (var expression : values) {
            expression.walk(w);
        }
    }

    @Override
    public boolean beginsWithParen() {
        return !declare && targets.get(0).beginsWithParen();
    }

}
