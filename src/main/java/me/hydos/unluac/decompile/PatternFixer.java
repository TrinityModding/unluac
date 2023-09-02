package me.hydos.unluac.decompile;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.hydos.unluac.decompile.expression.*;
import me.hydos.unluac.decompile.statement.AssignmentStatement;
import me.hydos.unluac.decompile.statement.FunctionCallStatement;
import me.hydos.unluac.decompile.statement.Statement;
import me.hydos.unluac.decompile.target.VariableTarget;

import java.util.*;

/**
 * Apply pattern based fixes to the code
 */
public class PatternFixer {

    private static int handleAssigningFunction(FunctionCall functionCall, int i, ObjectArrayList<Statement> newCode, Decompiler decompiler) {
        if (functionCall.function instanceof LocalVariable localVariable) {
            AssignmentStatement function = null;
            var params = new AssignmentStatement[functionCall.arguments.length];
            // We can always assume the params are local. I have not seen a time when this is wrong
            var localParams = Arrays.stream(functionCall.arguments).map(expression -> ((LocalVariable) expression).decl).toList();
            var searchDistance = 0;

            for (var j = i - 1; j >= 0; j--) {
                searchDistance = j;
                if (Arrays.stream(params).noneMatch(Objects::isNull) && function != null) break; // We got all parameters we are good
                var searchLine = newCode.get(j);
                if (searchLine instanceof AssignmentStatement assignment && assignment.targets.get(0) instanceof VariableTarget varTarget) {
                    if (varTarget.decl.equals(localVariable.decl)) {
                        if (function != null) {
                            // We already found it. How did we get this far? Stop before we cause issues
                            return i;
                        } else function = assignment;
                    }

                    if (localParams.contains(varTarget.decl)) params[localParams.indexOf(varTarget.decl)] = assignment;
                }
            }

/*            for (var offsetI = i - functionCall.arguments.length - 1; offsetI >= 0 && offsetI < i; offsetI++) {
                var statement = newCode.get(offsetI);
                if (statement instanceof AssignmentStatement assignmentStatement && assignmentStatement.targets.size() > 1)
                    assignments.add(assignmentStatement);
                else {
                    isMethodAssignment = false;
                    break;
                }
            }*/

            // Where the value 'i' should return to after this has run to make sure it doesn't miss anything
            var loopReturnPoint = i - (functionCall.arguments.length + searchDistance);

            // Remove old data
            newCode.removeAll(Arrays.stream(params).toList());

            // Replace data with new data
            functionCall.function = function.values.get(0);
            for (var j = 0; j < params.length; j++)
                functionCall.arguments[j] = params[j].values.get(0);

            // Mark declarations as invalid to stop generation
            decompiler.deadDeclarations.add(((VariableTarget) function.targets.get(0)).decl);
            for (var assignment : params)
                decompiler.deadDeclarations.add(((VariableTarget) assignment.targets.get(0)).decl);

            return Math.max(loopReturnPoint, 0);
        } else if (functionCall.function instanceof TableReference tableReference && i > 0) {
            var previousLine = newCode.get(i - 1);

            if (tableReference.table instanceof LocalVariable localVariable) {
                // If the previous line assigned the variable the table is pointing to. Basically checking for:
                // local L_0 = blahblah()
                // local L_1 = --> L_0.something()
                if (previousLine instanceof AssignmentStatement assignmentStatement && assignmentStatement.targets.get(0) instanceof VariableTarget varTarget && varTarget.decl.equals(localVariable.decl)) {
                    tableReference.table = assignmentStatement.getValue(0);
                    newCode.remove(previousLine);
                    return Math.max(i - 20, 0);
                }
            }
        }

        return i;
    }

    public static List<Statement> rewriteStatements(Decompiler decompiler, List<Statement> originalCode) {
        var newCode = new ObjectArrayList<>(originalCode);

        System.out.println("Step 1: Forwards/Backwards pattern fixing");
//        for (var i = 0; i < newCode.size(); i++) {
//            var statement = newCode.get(i);
//
//            // Search for function calls and inline the function local and args
//            if (statement instanceof AssignmentStatement assignmentStatement) {
//                if (assignmentStatement.values.get(0) instanceof FunctionCall functionCall)
//                    i = handleAssigningFunction(functionCall, i, newCode, decompiler);
//            } else if (statement instanceof FunctionCallStatement functionCallStatement) {
//                i = handleAssigningFunction(functionCallStatement.call, i, newCode, decompiler);
//            }
//
//            if (statement instanceof AssignmentStatement current) {
//                var target = current.targets.get(0);
//                // Search backwards for every reassignment of this local. We will inline them to improve the look of
//                // decompiled code. We don't need to worry about locals that get reused because there will be either a
//                // new assignment or a function call separating other usage.
//                var reassignments = new ArrayList<AssignmentStatement>();
//                for (var j = i - 1; j >= 0; j--) {
//                    if (newCode.get(j) instanceof AssignmentStatement assignmentStatement && assignmentStatement.targets.get(0).equals(target))
//                        reassignments.add(assignmentStatement);
//                    else break;
//                }
//
//                Collections.reverse(reassignments); // We want the closer to the final value to be higher
//                reassignments.add(current);
//                for (var k = 1; k < reassignments.size(); k++) {
//                    var lastAssignment = reassignments.get(k - 1); // <-- IntelliJ bug?
//                    var currentAssignment = reassignments.get(k);
//                    if (!lastAssignment.values.get(0).equals(currentAssignment.values.get(0)))
//                        inlineAssignment(currentAssignment, lastAssignment);
//                    newCode.remove(lastAssignment);
//                }
//
//                if (reassignments.size() > 1)
//                    i = Math.max(0, i - (reassignments.size() + 1)); // Go back to before reassignments to catch any more issues
//            }
//
//        }

//        // Such a sensitive step that doing this line by line is crucial, so it cannot be handled by the previous loop
//        System.out.println("Step 2: Refactor locals");
//        for (var i = 0; i < newCode.size(); i++) {
//            var localReplacementMap = new HashMap<Declaration, Declaration>();
//
//        }

        return newCode;
    }

    private static void inlineAssignment(AssignmentStatement currentAssignment, AssignmentStatement lastAssignment) {
        replaceDeclWithExpr(currentAssignment, lastAssignment, null);
    }

    private static boolean replaceDeclWithExpr(AssignmentStatement currentAssignment, AssignmentStatement lastAssignment, Expression innerCurrentVariable) {
        var currentValue = innerCurrentVariable == null ? currentAssignment.values.get(0) : innerCurrentVariable;
        var targetLocal = currentAssignment.targets.get(0);
        var lastValue = lastAssignment.values.get(0);

        if (currentValue instanceof LocalVariable localVar && targetLocal.isDeclaration(localVar.decl)) {
            currentAssignment.values.set(0, lastValue);
            return true;
        } else if (currentValue instanceof FunctionCall functionCall) {
            return replaceDeclWithExpr(currentAssignment, lastAssignment, functionCall.function);
        } else if (currentValue instanceof TableLiteral literal) {
            return !literal.isField(); // We can't fix these
        } else if (currentValue instanceof TableReference reference) {
            return replaceDeclWithExpr(currentAssignment, lastAssignment, reference.table) ||
                   replaceDeclWithExpr(currentAssignment, lastAssignment, reference.index);
        } else if (currentValue instanceof BinaryExpression binary) {
            return replaceDeclWithExpr(currentAssignment, lastAssignment, binary.left) ||
                   replaceDeclWithExpr(currentAssignment, lastAssignment, binary.right);
        } else if (currentValue instanceof UnaryExpression unary) {
            return replaceDeclWithExpr(currentAssignment, lastAssignment, unary.expression);
        } else if (currentValue instanceof ClosureExpression && lastValue instanceof ClosureExpression) {
            return true; // local function got replaced as it's never used after X point. Another fix should rename the locals for these, so it's easy to read
        } else return lastValue.isNil();
    }
}
