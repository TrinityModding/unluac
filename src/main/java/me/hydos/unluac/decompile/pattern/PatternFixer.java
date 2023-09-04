package me.hydos.unluac.decompile.pattern;

import me.hydos.unluac.decompile.core.Decompiler;
import me.hydos.unluac.decompile.core.Local;
import me.hydos.unluac.decompile.expression.FunctionCall;
import me.hydos.unluac.decompile.expression.LocalVariable;
import me.hydos.unluac.decompile.statement.AssignmentStatement;
import me.hydos.unluac.decompile.statement.Statement;
import me.hydos.unluac.decompile.target.Target;
import me.hydos.unluac.decompile.target.UpvalueTarget;
import me.hydos.unluac.decompile.target.VariableTarget;

import java.util.ArrayList;
import java.util.List;

/**
 * Apply pattern based fixes to the code
 */
public class PatternFixer {

    private static void handleFunctionCall(FunctionCall functionCall, int i, List<Statement> code, Decompiler d) {
        // Should ALWAYS be true but just in case
        if (functionCall.function instanceof LocalVariable localVar) {
            // Search backwards from the call until you hit the statement assigning the function name
            var assignmentPool = new ArrayList<AssignmentStatement>();
            AssignmentStatement functionAssignment = null;

            for (var restorePoint = i - 1; restorePoint >= 0; restorePoint--) {
                var pastLine = code.get(restorePoint);
                if (pastLine instanceof AssignmentStatement assignment && assignment.targets.get(0) instanceof VariableTarget varTarget) {
                    if (localVar.local.equals(varTarget.local)) {
                        functionAssignment = assignment;
                        break;
                    } else assignmentPool.add(assignment);
                }
            }

            if (functionAssignment == null) throw new IllegalStateException();

            // Inline params from back to front (current order of assignmentPool) as the further back we go, the more likely the local is reassigned which will be bad code
            var assignedDecls = new ArrayList<Local>();
            var assignmentsToRemove = new ArrayList<AssignmentStatement>();
            for (var assignment : assignmentPool) {
                if (assignment.targets.get(0) instanceof VariableTarget varTarget) {
                    if (assignedDecls.contains(varTarget.local)) continue;
                    assignedDecls.add(varTarget.local);

                    // Find argument index that we want to replace
                    for (var j = 0; j < functionCall.arguments.length; j++) {
                        var arg = functionCall.arguments[j];
                        if (arg instanceof LocalVariable localVarArg) {
                            if (localVarArg.local.equals(varTarget.local)) {
                                // Inline what local was set to the function arg
                                functionCall.arguments[j] = assignment.values.get(0);
                                assignmentsToRemove.add(assignment);
                                break;
                            }
                        }
                    }
                }
            }

            // Inline function call
            functionCall.function = functionAssignment.values.get(0);

            // Remove inlined stuff
            code.remove(functionAssignment);
            code.removeAll(assignmentsToRemove);
        }
    }

    public static void rewriteStatements(Decompiler decompiler, List<Statement> code) {
        for (var i = 0; i < code.size(); i++) {
            var statement = code.get(i);

            // Used at the moment to remove pesky global locals sticking around.
            statement.lastUpdate(decompiler);

            // Inline "action" statements
            // Example:
            //  local svar2_0 = A0_2
            //  local svar2_1 = " and " -- arg 1
            //  local var2_5 = A1_2 -- arg 2
            //  svar2_0 = svar2_0 .. svar2_1 .. var2_5 -- action statement
            // should become
            // svar2_0 = A0_2 .. " and " .. A1_2
            if (statement.isActionStatement())
                i = handleActionStatement(statement, i, code, decompiler);
        }

        // This loop cannot have unsafe backwards/forwards jumping, or it will break
        var newLocalCounter = 0;
//        FIXME: something is awfully wrong with this code
//        for (var i = 0; i < code.size(); i++) {
//            var statement = code.get(i);
//            statement.fillUsageMap(state.localUsed, false);
//            statement.remapLocals(state.localRemaps, state.lastLocalRemaps);
//
//            //noinspection UnusedAssignment Reason: Give the JVM some help here just in case by unassigning the last list
//            state.lastLocalRemaps = null;
//            state.lastLocalRemaps = Map.copyOf(state.localRemaps);
//
//            // Keep track of usage after it has been (re)assigned.
//            if (statement instanceof AssignmentStatement assignment && assignment.targets.size() == 1 && assignment.targets.get(0) instanceof VariableTarget varTarget) {
//                var target = varTarget.local;
//
//                // Inline useless function assignments through refactoring
//                if (assignment.values.get(0) instanceof LocalVariable local && local.decl.equals(target)) {
//                    code.remove(assignment);
//                    i -= 1; // Keep 'i' changes on the same line to make it clear why they occur.
//                    state.localRemaps.put(target, local.decl); // If L0_1 = L1_1, then replace L0_1 with L1_1 until its "used" (redefined)
//                    continue;
//                }
//
//                /* If the local was previously used in a function, Create a new local to separate it */
//                if (state.localUsed.containsKey(target) && state.localUsed.get(target)) {
//                    var newLocal = new Local("svar" + decompiler.bytecode.depth + "_" + newLocalCounter++, target.begin, target.end);
//                    newLocal.needsDeclaring = true;
//
//                    // Update state
//                    state.localRemaps.remove(target);
//                    state.localRemaps.put(target, newLocal);
//                    state.localUsed.remove(target);
//                    state.localUsed.put(target, false);
//                }
//            }
//
//            statement.remapLocals(state.localRemaps, state.lastLocalRemaps);
//        }
    }

    private static int handleActionStatement(Statement statement, int statementIdx, List<Statement> code, Decompiler decompiler) {
        var actions = statement.getActionVars();
        var actionAssignStatements = new ArrayList<AssignmentStatement>(actions.size());

        for (var backwardsSearchIdx = 1; backwardsSearchIdx <= actions.size(); backwardsSearchIdx++) {
            var codeIdx = statementIdx - backwardsSearchIdx;
            actionAssignStatements.add((AssignmentStatement) code.get(codeIdx));
        }

        if (actions.size() != actionAssignStatements.size())
            throw new IllegalStateException(); // Safety: if this doesn't match the decompile is wrong

        for (var i = 0; i < actionAssignStatements.size(); i++) {
            var assignment = actionAssignStatements.get(i);
            var local = actions.get(actions.size() - (1 + i));
            statement.inlineLocal(local, assignment.values.get(0));
            decompiler.deadLocals.add(((VariableTarget) assignment.targets.get(0)).local);
        }

        code.removeAll(actionAssignStatements);
        return statementIdx - actionAssignStatements.size(); // Go x assignment many lines otherwise we will miss stuff
    }
}
