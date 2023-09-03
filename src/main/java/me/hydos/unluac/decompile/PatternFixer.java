package me.hydos.unluac.decompile;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import me.hydos.unluac.decompile.expression.FunctionCall;
import me.hydos.unluac.decompile.expression.LocalVariable;
import me.hydos.unluac.decompile.statement.AssignmentStatement;
import me.hydos.unluac.decompile.statement.FunctionCallStatement;
import me.hydos.unluac.decompile.statement.Statement;
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
                    if (localVar.decl.equals(varTarget.local)) {
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
                            if (localVarArg.decl.equals(varTarget.local)) {
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

    public static void rewriteStatements(Decompiler decompiler, Decompiler.State state, List<Statement> code) {

        for (var i = 0; i < code.size(); i++) {
            var statement = code.get(i);

            // Search for function calls and inline the function local and args
            if (statement instanceof AssignmentStatement assignmentStatement) {
                if (assignmentStatement.values.get(0) instanceof FunctionCall functionCall)
                    handleFunctionCall(functionCall, i, code, decompiler);
            } else if (statement instanceof FunctionCallStatement functionCallStatement)
                handleFunctionCall(functionCallStatement.call, i, code, decompiler);
        }

        var newLocalCounter = 0;
        for (var statement : code) {
            statement.fillUsageMap(state.localUsed, false);
            statement.remapLocals(state.localRemaps);

            // Keep track of usage after it has been (re)assigned.
            if (statement instanceof AssignmentStatement assignment && assignment.targets.size() == 1 && assignment.targets.get(0) instanceof VariableTarget varTarget) {
                var target = varTarget.local;

                /* If the local was previously used in a function, Create a new local to separate it */
                if (state.localUsed.containsKey(target) && state.localUsed.get(target)) {
                    var newLocal = new Local("var" + newLocalCounter++, target.begin, target.end);
                    newLocal.needsDeclaring = true;

                    // Update state
                    state.localRemaps.remove(target);
                    state.localRemaps.put(target, newLocal);
                    state.localUsed.remove(target);
                    state.localUsed.put(target, false);
                    statement.remapLocals(state.localRemaps);
                }
            }
        }
    }
}
