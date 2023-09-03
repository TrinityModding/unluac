package me.hydos.unluac.decompile.statement;

import me.hydos.unluac.decompile.Decompiler;
import me.hydos.unluac.decompile.Local;
import me.hydos.unluac.decompile.Output;
import me.hydos.unluac.decompile.Walker;
import me.hydos.unluac.decompile.expression.Expression;
import me.hydos.unluac.decompile.expression.LocalVariable;
import me.hydos.unluac.decompile.expression.TableLiteral;
import me.hydos.unluac.decompile.target.Target;
import me.hydos.unluac.decompile.target.VariableTarget;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AssignmentStatement extends Statement {

    public final ArrayList<Target> targets = new ArrayList<>(1);
    public final ArrayList<Expression> values = new ArrayList<>(1);
    public final ArrayList<Integer> lines = new ArrayList<>(1);

    private boolean allNil = true;
    public boolean declare = false;
    private int declareStart = 0;

    public AssignmentStatement() {
    }

    public AssignmentStatement(Target target, Expression value, int line) {
        targets.add(target);
        values.add(value);
        lines.add(line);
        allNil = value.isNil();
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

    public boolean assignsTarget(Local decl) {
        for (var target : targets) {
            if (target.isDeclaration(decl)) {
                return true;
            }
        }
        return false;
    }

    public void addFirst(Target target, Expression value, int line) {
        targets.add(0, target);
        values.add(0, value);
        lines.add(0, line);
        allNil = allNil && value.isNil();
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
        allNil = allNil && value.isNil();
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

    public boolean assignListEquals(List<Local> decls) {
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
        this.declare = true;
        this.declareStart = declareStart;
    }

    public boolean assigns(Local decl) {
        for (var target : targets) {
            if (target.isDeclaration(decl)) return true;
        }
        return false;
    }

    @Override
    public void print(Decompiler d, Output out) {
        if (!targets.isEmpty()) {
            // You are defining a new class instance. Organise your code
            if (values.get(0) instanceof TableLiteral literal && literal.entries.isEmpty()) out.println();


            this.declare = targets.stream()
                    .filter(target -> target instanceof VariableTarget)
                    .map(target -> (VariableTarget) target)
                    .anyMatch(target -> target.local.needsDeclaring && !d.currentState.definedLocals.contains(target.local));

            if (declare)
                out.print("local ");

            var functionSugar = isFunctionSugar();
            if (!functionSugar) {
                targets.get(0).print(d, out, declare);
                for (var i = 1; i < targets.size(); i++) {
                    out.print(", ");
                    targets.get(i).print(d, out, declare);
                }
                if (!declare || !allNil) {
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

            if (declare) targets.stream()
                    .filter(target -> target instanceof VariableTarget)
                    .map(target -> (VariableTarget) target)
                    .forEach(variableTarget -> d.currentState.definedLocals.add(variableTarget.local));
        }
    }

    private boolean isFunctionSugar() {
        var functionSugar = false;
        if (targets.size() == 1 && values.size() == 1 && values.get(0).isClosure() && targets.get(0).isFunctionName()) {
            var closure = values.get(0);
            // This check only works in Lua version 0x51
            if (!declare || declareStart >= closure.closureUpvalueLine()) functionSugar = true;
            if (targets.get(0).isLocal() && closure.isUpvalueOf(targets.get(0).getIndex())) functionSugar = true;

            //if(closure.isUpvalueOf(targets.get(0).))
        }
        return functionSugar;
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

    @Override
    public void fillUsageMap(Map<Local, Boolean> localUsageMap, boolean includeAssignments) {
        if (includeAssignments)
            targets.stream()
                    .filter(target -> target instanceof VariableTarget)
                    .map(target -> (VariableTarget) target)
                    .forEach(variableTarget -> localUsageMap.put(variableTarget.local, true));

        values.forEach(expression -> {
            if (!(expression instanceof LocalVariable))  // Most likely useless reassignment that will get inlined later
                expression.fillUsageMap(localUsageMap, includeAssignments);
        });
    }

    @Override
    public void remapLocals(Map<Local, Local> localRemaps) {
        values.forEach(expression -> expression.remapLocals(localRemaps));

        targets.stream()
                .filter(target -> target instanceof VariableTarget)
                .map(target -> (VariableTarget) target)
                .forEach(variableTarget -> {
                    var local = variableTarget.local;
                    if (localRemaps.containsKey(local)) variableTarget.local = localRemaps.get(local);
                });
    }
}
