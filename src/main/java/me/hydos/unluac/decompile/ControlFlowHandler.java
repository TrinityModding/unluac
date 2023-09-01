package me.hydos.unluac.decompile;

import me.hydos.unluac.Version;
import me.hydos.unluac.decompile.block.*;
import me.hydos.unluac.decompile.condition.*;
import me.hydos.unluac.bytecode.BFunction;
import me.hydos.unluac.util.Stack;

import java.util.*;

/**
 * Handles taking bytecode lua and giving us a more lua styled output
 */
public class ControlFlowHandler {

    public static final boolean verbose = false;

    // static only
    private ControlFlowHandler() {
    }

    public static Result process(NewDecompiler d, Registers r) {
        var state = new State();
        state.function = d.bytecode;
        state.r = r;
        state.bytecodeReader = d.reader;
        state.labels = new boolean[d.reader.length + 1];
        find_reverse_targets(state);
        find_branches(state);
        combine_branches(state);
        resolve_lines(state);
        initialize_blocks(state);
        find_fixed_blocks(state);
        find_while_loops(state, d.declarations);
        find_repeat_loops(state);
        find_if_break(state, d.declarations);
        find_set_blocks(state);
        find_pseudo_goto_statements(state, d.declarations);
        find_do_blocks(state, d.declarations);
        Collections.sort(state.blocks);
        return new Result(state);
    }

    @Deprecated
    public static Result process(Decompiler d, Registers r) {
        var state = new State();
        state.d = d;
        state.function = d.function;
        state.r = r;
        state.bytecodeReader = d.bytecodeReader;
        state.labels = new boolean[d.bytecodeReader.length + 1];
        find_reverse_targets(state);
        find_branches(state);
        combine_branches(state);
        resolve_lines(state);
        initialize_blocks(state);
        find_fixed_blocks(state);
        find_while_loops(state, Arrays.stream(d.declList).toList());
        find_repeat_loops(state);
        find_if_break(state, Arrays.stream(d.declList).toList());
        find_set_blocks(state);
        find_pseudo_goto_statements(state, Arrays.stream(d.declList).toList());
        find_do_blocks(state, Arrays.stream(d.declList).toList());
        Collections.sort(state.blocks);
        return new Result(state);
    }

    private static void find_reverse_targets(State state) {
        var code = state.bytecodeReader;
        var reverse_targets = state.reverse_targets = new boolean[state.bytecodeReader.length + 1];
        for (var line = 1; line <= code.length; line++) {
            if (is_jmp(state, line)) {
                var target = code.target(line);
                if (target <= line) {
                    reverse_targets[target] = true;
                }
            }
        }
    }

    private static void resolve_lines(State state) {
        var resolved = new int[state.bytecodeReader.length + 1];
        Arrays.fill(resolved, -1);
        for (var line = 1; line <= state.bytecodeReader.length; line++) {
            var r = line;
            var b = state.branches[line];
            while (b != null && b.type == Branch.Type.jump) {
                if (resolved[r] >= 1) {
                    r = resolved[r];
                    break;
                } else if (resolved[r] == -2) {
                    r = b.targetSecond;
                    break;
                } else {
                    resolved[r] = -2;
                    r = b.targetSecond;
                    b = state.branches[r];
                }
            }
            if (r == line && state.bytecodeReader.op(line) == Op.JMP52 && is_close(state, line)) {
                r = line + 1;
            }
            resolved[line] = r;
        }
        state.resolved = resolved;
    }

    private static int find_loadboolblock(State state, int target) {
        if (target < 1) {
            return -1;
        }
        var loadboolblock = -1;
        var op = state.bytecodeReader.op(target);
        if (op == Op.LOADBOOL) {
            if (state.bytecodeReader.C(target) != 0) {
                loadboolblock = target;
            } else if (target - 1 >= 1 && state.bytecodeReader.op(target - 1) == Op.LOADBOOL && state.bytecodeReader.C(target - 1) != 0) {
                loadboolblock = target - 1;
            }
        } else if (op == Op.LFALSESKIP) {
            loadboolblock = target;
        } else if (target - 1 >= 1 && op == Op.LOADTRUE && state.bytecodeReader.op(target - 1) == Op.LFALSESKIP) {
            loadboolblock = target - 1;
        }
        return loadboolblock;
    }

    private static void handle_loadboolblock(State state, boolean[] skip, int loadboolblock, Condition c, int line, int target) {
        boolean loadboolvalue;
        var op = state.bytecodeReader.op(target);
        if (op == Op.LOADBOOL) {
            loadboolvalue = state.bytecodeReader.B(target) != 0;
        } else if (op == Op.LFALSESKIP) {
            loadboolvalue = false;
        } else if (op == Op.LOADTRUE) {
            loadboolvalue = true;
        } else {
            throw new IllegalStateException();
        }
        var final_line = -1;
        if (loadboolblock - 1 >= 1 && is_jmp(state, loadboolblock - 1)) {
            var boolskip_target = state.bytecodeReader.target(loadboolblock - 1);
            var boolskip_target_redirected = -1;
            if (is_jmp_raw(state, loadboolblock + 2)) {
                boolskip_target_redirected = state.bytecodeReader.target(loadboolblock + 2);
            }
            if (boolskip_target == loadboolblock + 2 || boolskip_target == boolskip_target_redirected) {
                skip[loadboolblock - 1] = true;
                final_line = loadboolblock - 2;
            }
        }
        var inverse = false;
        if (loadboolvalue) {
            inverse = true;
            c = c.inverse();
        }
        var constant = is_jmp(state, line);
        Branch b;
        var begin = line + 2;

        if (constant) {
            begin--;
            b = new Branch(line, line, Branch.Type.testset, c, begin, loadboolblock + 2, null);
        } else if (line + 2 == loadboolblock) {
            b = new Branch(loadboolblock, loadboolblock, Branch.Type.finalset, c, begin, loadboolblock + 2, null);
        } else {
            b = new Branch(line, line, Branch.Type.testset, c, begin, loadboolblock + 2, null);
        }
        b.target = state.bytecodeReader.A(loadboolblock);
        b.inverseValue = inverse;
        insert_branch(state, b);

        if (final_line != -1) {
            if (constant && final_line < begin) {
                final_line++;
            }
            var finalc = new FinalSetCondition(final_line, b.target);
            var finalb = new Branch(final_line, final_line, Branch.Type.finalset, finalc, final_line, loadboolblock + 2, finalc);
            finalb.target = b.target;
            insert_branch(state, finalb);
            b.finalset = finalc;
        }
    }

    private static void handle_test(State state, boolean[] skip, int line, Condition c, int target, boolean invert) {
        var code = state.bytecodeReader;
        var loadboolblock = find_loadboolblock(state, target);
        if (loadboolblock >= 1) {
            if (invert) c = c.inverse();
            handle_loadboolblock(state, skip, loadboolblock, c, line, target);
        } else {
            var ploadboolblock = target - 2 >= 1 ? find_loadboolblock(state, target - 2) : -1;
            if (ploadboolblock != -1 && ploadboolblock == target - 2 && code.A(target - 2) == c.register() && !has_statement(state, line + 2, target - 3)) {
                handle_testset(state, skip, line, c, target, c.register(), invert);
            } else {
                if (invert) c = c.inverse();
                var b = new Branch(line, line, Branch.Type.test, c, line + 2, target, null);
                b.target = code.A(line);
                if (invert) b.inverseValue = true;
                insert_branch(state, b);
            }
        }
        skip[line + 1] = true;
    }

    private static void handle_testset(State state, boolean[] skip, int line, Condition c, int target, int register, boolean invert) {
        if (find_loadboolblock(state, target) == -1) {
            if (invert) c = c.inverse();
            var b = new Branch(line, line, Branch.Type.test, c, line + 2, target, null);
            b.target = state.bytecodeReader.A(line);
            if (invert) b.inverseValue = true;
            insert_branch(state, b);
            skip[line + 1] = true;
            return;
        }
        var b = new Branch(line, line, Branch.Type.testset, c, line + 2, target, null);
        b.target = register;
        if (invert) b.inverseValue = true;
        skip[line + 1] = true;
        insert_branch(state, b);
        var final_line = target - 1;
        int branch_line;
        var loadboolblock = find_loadboolblock(state, target - 2);
        if (loadboolblock != -1 && state.bytecodeReader.A(loadboolblock) == register) {
            final_line = loadboolblock;
            if (loadboolblock - 2 >= 1 && is_jmp(state, loadboolblock - 1) &&
                (state.bytecodeReader.target(loadboolblock - 1) == target || is_jmp_raw(state, target) && state.bytecodeReader.target(loadboolblock - 1) == state.bytecodeReader.target(target))
            ) {
                final_line = loadboolblock - 2;
            }
            branch_line = final_line;
        } else {
            branch_line = Math.max(final_line, line + 2);
        }
        var finalc = new FinalSetCondition(final_line, register);
        var finalb = new Branch(branch_line, branch_line, Branch.Type.finalset, finalc, final_line, target, finalc);
        finalb.target = register;
        insert_branch(state, finalb);
        b.finalset = finalc;
    }

    private static void process_condition(State state, boolean[] skip, int line, Condition c, boolean invert) {
        var target = state.bytecodeReader.target(line + 1);
        if (invert) {
            c = c.inverse();
        }
        var loadboolblock = find_loadboolblock(state, target);
        if (loadboolblock >= 1) {
            handle_loadboolblock(state, skip, loadboolblock, c, line, target);
        } else {
            var b = new Branch(line, line, Branch.Type.comparison, c, line + 2, target, null);
            if (invert) {
                b.inverseValue = true;
            }
            insert_branch(state, b);
        }
        skip[line + 1] = true;
    }

    private static void find_branches(State state) {
        var code = state.bytecodeReader;
        state.branches = new Branch[state.bytecodeReader.length + 1];
        state.setbranches = new Branch[state.bytecodeReader.length + 1];
        state.finalsetbranches = new ArrayList<>(state.bytecodeReader.length + 1);
        for (var i = 0; i <= state.bytecodeReader.length; i++) state.finalsetbranches.add(null);
        var skip = new boolean[code.length + 1];
        for (var line = 1; line <= code.length; line++) {
            if (!skip[line]) {
                switch (code.op(line)) {
                    case EQ, LT, LE -> {
                        var op = BinaryCondition.Operator.EQ;
                        if (code.op(line) == Op.LT) op = BinaryCondition.Operator.LT;
                        if (code.op(line) == Op.LE) op = BinaryCondition.Operator.LE;
                        var left = new BinaryCondition.Operand(Condition.OperandType.RK, code.B(line));
                        var right = new BinaryCondition.Operand(Condition.OperandType.RK, code.C(line));
                        Condition c = new BinaryCondition(op, line, left, right);
                        process_condition(state, skip, line, c, code.A(line) != 0);
                    }
                    case EQ54, LT54, LE54 -> {
                        var op = BinaryCondition.Operator.EQ;
                        if (code.op(line) == Op.LT54) op = BinaryCondition.Operator.LT;
                        if (code.op(line) == Op.LE54) op = BinaryCondition.Operator.LE;
                        var left = new Condition.Operand(Condition.OperandType.R, code.A(line));
                        var right = new Condition.Operand(Condition.OperandType.R, code.B(line));
                        Condition c = new BinaryCondition(op, line, left, right);
                        process_condition(state, skip, line, c, code.k(line));
                    }
                    case EQK -> {
                        var op = BinaryCondition.Operator.EQ;
                        var right = new Condition.Operand(Condition.OperandType.R, code.A(line));
                        var left = new Condition.Operand(Condition.OperandType.K, code.B(line));
                        Condition c = new BinaryCondition(op, line, left, right);
                        process_condition(state, skip, line, c, code.k(line));
                    }
                    case EQI, LTI, LEI, GTI, GEI -> {
                        var op = BinaryCondition.Operator.EQ;
                        if (code.op(line) == Op.LTI) op = BinaryCondition.Operator.LT;
                        if (code.op(line) == Op.LEI) op = BinaryCondition.Operator.LE;
                        if (code.op(line) == Op.GTI) op = BinaryCondition.Operator.GT;
                        if (code.op(line) == Op.GEI) op = BinaryCondition.Operator.GE;
                        Condition.OperandType operandType;
                        if (code.C(line) != 0) {
                            operandType = Condition.OperandType.F;
                        } else {
                            operandType = Condition.OperandType.I;
                        }
                        var left = new Condition.Operand(Condition.OperandType.R, code.A(line));
                        var right = new Condition.Operand(operandType, code.sB(line));
                        if (op == BinaryCondition.Operator.EQ) {
                            var temp = left;
                            left = right;
                            right = temp;
                        }
                        Condition c = new BinaryCondition(op, line, left, right);
                        process_condition(state, skip, line, c, code.k(line));
                    }
                    case TEST50 -> {
                        Condition c = new TestCondition(line, code.B(line));
                        var target = code.target(line + 1);
                        if (code.A(line) == code.B(line)) {
                            handle_test(state, skip, line, c, target, code.C(line) != 0);
                        } else {
                            handle_testset(state, skip, line, c, target, code.A(line), code.C(line) != 0);
                        }
                    }
                    case TEST -> {
                        Condition c;
                        var target = code.target(line + 1);
                        c = new TestCondition(line, code.A(line));
                        handle_test(state, skip, line, c, target, code.C(line) != 0);
                    }
                    case TEST54 -> {
                        Condition c;
                        var target = code.target(line + 1);
                        c = new TestCondition(line, code.A(line));
                        handle_test(state, skip, line, c, target, code.k(line));
                    }
                    case TESTSET -> {
                        Condition c = new TestCondition(line, code.B(line));
                        var target = code.target(line + 1);
                        handle_testset(state, skip, line, c, target, code.A(line), code.C(line) != 0);
                    }
                    case TESTSET54 -> {
                        Condition c = new TestCondition(line, code.B(line));
                        var target = code.target(line + 1);
                        handle_testset(state, skip, line, c, target, code.A(line), code.k(line));
                    }
                    case JMP, JMP52, JMP54 -> {
                        if (is_jmp(state, line)) {
                            var target = code.target(line);
                            var loadboolblock = find_loadboolblock(state, target);
                            if (loadboolblock >= 1) {
                                handle_loadboolblock(state, skip, loadboolblock, new ConstantCondition(-1, false), line, target);
                            } else {
                                var b = new Branch(line, line, Branch.Type.jump, null, target, target, null);
                                insert_branch(state, b);
                            }
                        }
                    }
                    default -> {
                    }
                }
            }
        }
        link_branches(state);
    }

    private static void combine_branches(State state) {
        Branch b;

        b = state.end_branch;
        while (b != null) {
            b = combine_left(state, b).previous;
        }
    }

    private static void initialize_blocks(State state) {
        state.blocks = new LinkedList<>();
    }

    private static void find_fixed_blocks(State state) {
        var blocks = state.blocks;
        var r = state.r;
        var code = state.bytecodeReader;
        var tforTarget = state.function.header.version.tfortarget.get();
        var forTarget = state.function.header.version.fortarget.get();
        blocks.add(new OuterBlock(state.function, state.bytecodeReader.length));

        var loop = new boolean[state.bytecodeReader.length + 1];

        var b = state.begin_branch;
        while (b != null) {
            if (b.type == Branch.Type.jump) {
                var line = b.line;
                var target = b.targetFirst;
                if (code.op(target) == tforTarget && !loop[target]) {
                    loop[target] = true;
                    var A = code.A(target);
                    var C = code.C(target);
                    if (C == 0) throw new IllegalStateException();
                    remove_branch(state, state.branches[line]);
                    if (state.branches[target + 1] != null) {
                        remove_branch(state, state.branches[target + 1]);
                    }

                    var forvarClose = false;
                    var innerClose = false;
                    var close = target - 1;
                    if (close >= line + 1 && is_close(state, close) && code.A(close) == A + 3) {
                        forvarClose = true;
                        close--;
                    }
                    if (close >= line + 1 && is_close(state, close) && code.A(close) <= A + 3 + C) {
                        innerClose = true;
                    }

                    var block = TForBlock.make51(state.function, line + 1, target + 2, A, C, forvarClose, innerClose);
                    block.handleVariableDeclarations(r);
                    blocks.add(block);
                } else if (code.op(target) == forTarget && !loop[target]) {
                    loop[target] = true;
                    var A = code.A(target);

                    ForBlock block = new ForBlock50(
                            state.function, line + 1, target + 1, A,
                            get_close_type(state, target - 1), target - 1
                    );

                    block.handleVariableDeclarations(r);

                    blocks.add(block);
                    remove_branch(state, b);
                }
            }
            b = b.next;
        }

        for (var line = 1; line <= code.length; line++) {
            switch (code.op(line)) {
                case FORPREP, FORPREP54 -> {

                    var A = code.A(line);
                    var target = code.target(line);

                    var forvarClose = false;
                    var closeLine = target - 1;
                    if (closeLine >= line + 1 && is_close(state, closeLine) && code.A(closeLine) == A + 3) {
                        forvarClose = true;
                        closeLine--;
                    }

                    ForBlock block = new ForBlock51(
                            state.function, line + 1, target + 1, A,
                            get_close_type(state, closeLine), closeLine, forvarClose
                    );

                    block.handleVariableDeclarations(r);
                    blocks.add(block);
                }
                case TFORPREP -> {
                    var target = code.target(line);
                    var A = code.A(target);
                    var C = code.C(target);

                    var innerClose = false;
                    var close = target - 1;
                    if (close >= line + 1 && is_close(state, close) && code.A(close) == A + 3 + C) {
                        innerClose = true;
                    }

                    var block = TForBlock.make50(state.function, line + 1, target + 2, A, C + 1, innerClose);
                    block.handleVariableDeclarations(r);
                    blocks.add(block);
                    remove_branch(state, state.branches[target + 1]);
                }
                case TFORPREP54 -> {
                    var target = code.target(line);
                    var A = code.A(line);
                    var C = code.C(target);

                    var forvarClose = false;
                    var close = target - 1;
                    if (close >= line + 1 && is_close(state, close) && code.A(close) == A + 4) {
                        forvarClose = true;
                        close--;
                    }

                    var block = TForBlock.make54(state.function, line + 1, target + 2, A, C, forvarClose);
                    block.handleVariableDeclarations(r);
                    blocks.add(block);
                }
                default -> {
                }
            }
        }
    }

    private static void unredirect(State state, int begin, int end, int line, int target) {
        var b = state.begin_branch;
        while (b != null) {
            if (b.line >= begin && b.line < end && b.targetSecond == target) {
                if (b.type == Branch.Type.finalset) {
                    b.targetFirst = line - 1;
                    b.targetSecond = line;
                    if (b.finalset != null) {
                        b.finalset.line = line - 1;
                    }
                } else {
                    b.targetSecond = line;
                    if (b.targetFirst == target) {
                        b.targetFirst = line;
                    }
                }
            }
            b = b.next;
        }
    }

    private static void find_while_loops(State state, List<Declaration> declList) {
        var blocks = state.blocks;
        var j = state.end_branch;
        while (j != null) {
            if (j.type == Branch.Type.jump && j.targetFirst <= j.line && !splits_decl(j.targetFirst, j.targetFirst, j.line + 1, declList)) {
                var line = j.targetFirst;
                var loopback = line;
                var end = j.line + 1;
                var b = state.begin_branch;
                var extent = -1;
                while (b != null) {
                    if (is_conditional(b) && b.line >= loopback && b.line < j.line && state.resolved[b.targetSecond] == state.resolved[end] && extent <= b.line) {
                        break;
                    }
                    if (b.line >= loopback) {
                        extent = Math.max(extent, b.targetSecond);
                    }
                    b = b.next;
                }
                if (b != null) {
                    var reverse = state.reverse_targets[loopback];
                    state.reverse_targets[loopback] = false;
                    if (has_statement(state, loopback, b.line - 1)) {
                        b = null;
                    }
                    state.reverse_targets[loopback] = reverse;
                }
                if (state.function.header.version.whileformat.get() == Version.WhileFormat.BOTTOM_CONDITION) {
                    b = null; // while loop aren't this style
                }
                Block loop = null;
                if (b != null) {
                    b.targetSecond = end;
                    remove_branch(state, b);
                    //System.err.println("while " + b.targetFirst + " " + b.targetSecond);
                    loop = new WhileBlock51(
                            state.function, b.cond, b.targetFirst, b.targetSecond, loopback,
                            get_close_type(state, end - 2), end - 2
                    );
                    unredirect(state, loopback, end, j.line, loopback);
                }
                if (loop == null && j.line - 5 >= 1 && state.bytecodeReader.op(j.line - 3) == Op.CLOSE
                    && is_jmp_raw(state, j.line - 2) && state.bytecodeReader.target(j.line - 2) == end
                    && state.bytecodeReader.op(j.line - 1) == Op.CLOSE
                ) {
                    b = j.previous;
                    while (b != null && !(is_conditional(b) && b.line2 == j.line - 5)) {
                        b = b.previous;
                    }
                    if (b != null) {
                        var skip = state.branches[j.line - 2];
                        if (skip == null) throw new IllegalStateException();
                        var scopeEnd = j.line - 3;
                        if (state.function.header.version.closeinscope.get()) {
                            scopeEnd = j.line - 2;
                        }
                        // TODO: make this work better with new close system
                        loop = new RepeatBlock(
                                state.function, b.cond, j.targetFirst, j.line + 1,
                                CloseType.NONE, -1,
                                true, scopeEnd
                        );
                        remove_branch(state, b);
                        remove_branch(state, skip);
                    }
                }
                if (loop == null) {
                    var repeat = false;
                    if (state.function.header.version.whileformat.get() == Version.WhileFormat.BOTTOM_CONDITION) {
                        repeat = true;
                        if (loopback - 1 >= 1 && state.branches[loopback - 1] != null) {
                            var head = state.branches[loopback - 1];
                            if (head.type == Branch.Type.jump && head.targetFirst == j.line) {
                                remove_branch(state, head);
                                repeat = false;
                            }
                        }
                    }
                    loop = new AlwaysLoop(state.function, loopback, end, get_close_type(state, end - 2), end - 2, repeat);
                    unredirect(state, loopback, end, j.line, loopback);
                }
                remove_branch(state, j);
                blocks.add(loop);
            }
            j = j.previous;
        }
    }

    private static void find_repeat_loops(State state) {
        var blocks = state.blocks;
        var b = state.begin_branch;
        while (b != null) {
            if (is_conditional(b)) {
                if (b.targetSecond < b.targetFirst) {
                    Block block = null;
                    if (state.function.header.version.whileformat.get() == Version.WhileFormat.BOTTOM_CONDITION) {
                        var head = b.targetSecond - 1;
                        if (head >= 1 && state.branches[head] != null && state.branches[head].type == Branch.Type.jump) {
                            var headb = state.branches[head];
                            if (headb.targetSecond <= b.line) {
                                if (has_statement(state, headb.targetSecond, b.line - 1)) {
                                    headb = null;
                                }
                                if (headb != null) {
                                    block = new WhileBlock50(
                                            state.function, b.cond.inverse(), head + 1, b.targetFirst, headb.targetFirst,
                                            get_close_type(state, headb.targetFirst - 1), headb.targetFirst - 1
                                    );
                                    remove_branch(state, headb);
                                    unredirect(state, 1, headb.line, headb.line, headb.targetSecond);
                                }
                            }
                        }
                    }
                    if (block == null) {
                        if (state.function.header.version.extendedrepeatscope.get()) {
                            var statementLine = b.line - 1;
                            while (statementLine >= 1 && !is_statement(state, statementLine)) {
                                statementLine--;
                            }
                            block = new RepeatBlock(
                                    state.function, b.cond, b.targetSecond, b.targetFirst,
                                    get_close_type(state, statementLine), statementLine,
                                    true, statementLine
                            );
                        } else if (state.function.header.version.closesemantics.get() == Version.CloseSemantics.JUMP) {
                            block = new RepeatBlock(
                                    state.function, b.cond, b.targetSecond, b.targetFirst,
                                    get_close_type(state, b.targetFirst), b.targetFirst,
                                    false, -1
                            );
                        } else {
                            block = new RepeatBlock(
                                    state.function, b.cond, b.targetSecond, b.targetFirst,
                                    CloseType.NONE, -1,
                                    false, -1
                            );
                        }
                    }
                    remove_branch(state, b);
                    blocks.add(block);
                }
            }
            b = b.next;
        }
    }

    private static boolean splits_decl(int line, int begin, int end, List<Declaration> declList) {
        for (var decl : declList) {
            if (decl.isSplitBy(line, begin, end)) {
                return true;
            }
        }
        return false;
    }

    private static int stack_reach(State state, Stack<Branch> stack) {
        for (var i = 0; i < stack.size(); i++) {
            var b = stack.peek(i);
            var breakable = enclosing_breakable_block(state, b.line);
            if (breakable != null && breakable.end == b.targetSecond) {
                // next
            } else {
                return b.targetSecond;
            }
        }
        return Integer.MAX_VALUE;
    }

    private static Block resolve_if_stack(State state, Stack<Branch> stack, int line) {
        Block block = null;
        if (!stack.isEmpty() && stack_reach(state, stack) <= line) {
            var top = stack.pop();
            var literalEnd = state.bytecodeReader.target(top.targetFirst - 1);
            if (state.function.header.version.useifbreakrewrite.get() && state.function.header.version.usegoto.get() && top.targetFirst + 1 == top.targetSecond && is_jmp(state, top.targetFirst)) {
                // If this were actually an if statement, it would have been rewritten. It hasn't been, so it isn't...
                block = new IfThenEndBlock(state.function, state.r, top.cond.inverse(), top.targetFirst - 1, top.targetFirst - 1);
                block.addStatement(new Goto(state.function, top.targetFirst - 1, top.targetSecond));
                state.labels[top.targetSecond] = true;
            } else {
                block = new IfThenEndBlock(
                        state.function, state.r, top.cond, top.targetFirst, top.targetSecond,
                        get_close_type(state, top.targetSecond - 1), top.targetSecond - 1,
                        literalEnd != top.targetSecond
                );
            }
            state.blocks.add(block);
            remove_branch(state, top);
        }
        return block;
    }

    private static void resolve_else(State state, Stack<Branch> stack, Stack<Branch> hanging, Stack<ElseEndBlock> elseStack, Branch top, Branch b, int tailTargetSecond) {
        while (!elseStack.isEmpty() && elseStack.peek().end == tailTargetSecond && elseStack.peek().begin >= top.targetFirst) {
            elseStack.pop().end = b.line;
        }

        var replace = new Stack<Branch>();
        while (!hanging.isEmpty() && hanging.peek().targetSecond == tailTargetSecond && hanging.peek().line > top.line) {
            var hanger = hanging.pop();
            hanger.targetSecond = b.line;
            var breakable = enclosing_breakable_block(state, hanger.line);
            if (breakable != null && hanger.targetSecond >= breakable.end) {
                replace.push(hanger);
            } else {
                stack.push(hanger);
                var if_block = resolve_if_stack(state, stack, b.line);
                if (if_block == null) throw new IllegalStateException();
            }
        }
        while (!replace.isEmpty()) {
            hanging.push(replace.pop());
        }

        unredirect_finalsets(state, tailTargetSecond, b.line, top.targetFirst);

        var restore = new Stack<Branch>();
        while (!stack.isEmpty() && stack.peek().line > top.line && stack.peek().targetSecond == b.targetSecond) {
            stack.peek().targetSecond = b.line;
            restore.push(stack.pop());
        }
        while (!restore.isEmpty()) {
            stack.push(restore.pop());
        }

        b.targetSecond = tailTargetSecond;
        state.blocks.add(new IfThenElseBlock(
                state.function, top.cond, top.targetFirst, top.targetSecond, b.targetSecond,
                get_close_type(state, top.targetSecond - 2), top.targetSecond - 2
        ));
        var elseBlock = new ElseEndBlock(
                state.function, top.targetSecond, b.targetSecond,
                get_close_type(state, b.targetSecond - 1), b.targetSecond - 1
        );
        state.blocks.add(elseBlock);
        elseStack.push(elseBlock);
        remove_branch(state, b);
    }

    private static boolean is_hanger_resolvable(State state, List<Declaration> declList, Branch hanging, Branch resolver) {
        return hanging.targetSecond == resolver.targetFirst
               && enclosing_block(state, hanging.line) == enclosing_block(state, resolver.line)
               && !splits_decl(hanging.line, hanging.targetFirst, resolver.line, declList)
               && !(
                state.function.header.version.useifbreakrewrite.get()
                && hanging.targetFirst == resolver.line - 1
                && is_jmp(state, resolver.line - 1)
        );
    }

    private static boolean is_hanger_resolvable(State state, List<Declaration> declList, Branch hanging, Stack<Branch> resolvers) {
        for (var i = 0; i < resolvers.size(); i++) {
            if (is_hanger_resolvable(state, declList, hanging, resolvers.peek(i))) {
                return true;
            }
        }
        return false;
    }

    private static void resolve_hanger(State state, Stack<Branch> stack, Branch hanger, Branch b) {
        hanger.targetSecond = b.line;
        stack.push(hanger);
        var if_block = resolve_if_stack(state, stack, b.line);
        if (if_block == null) throw new IllegalStateException();
    }

    private static void resolve_hangers(State state, List<Declaration> declList, Stack<Branch> stack, Stack<Branch> hanging, Branch b) {
        while (!hanging.isEmpty() && is_hanger_resolvable(state, declList, hanging.peek(), b)) {
            resolve_hanger(state, stack, hanging.pop(), b);
        }
    }

    private static void find_if_break(State state, List<Declaration> declList) {
        var stack = new Stack<Branch>();
        var hanging = new Stack<Branch>();
        var elseStack = new Stack<ElseEndBlock>();
        var b = state.begin_branch;
        var hangingResolver = new Stack<Branch>();

        while (b != null) {
            while (resolve_if_stack(state, stack, b.line2) != null) {
            }

            while (!elseStack.isEmpty() && elseStack.peek().end <= b.line) {
                elseStack.pop();
            }

            while (!hangingResolver.isEmpty() && !enclosing_block(state, hangingResolver.peek().line).contains(b.line)) {
                resolve_hangers(state, declList, stack, hanging, hangingResolver.pop());
            }

            if (is_conditional(b)) {
                var unprotected = enclosing_unprotected_block(state, b.line);
                if (b.targetFirst > b.targetSecond) throw new IllegalStateException();
                if (unprotected != null && !unprotected.contains(b.targetSecond)) {
                    if (b.targetSecond == unprotected.getUnprotectedTarget()) {
                        b.targetSecond = unprotected.getUnprotectedLine();
                    }
                }

                var breakable = enclosing_breakable_block(state, b.line);
                if (!stack.isEmpty() && stack.peek().targetSecond < b.targetSecond
                    || breakable != null && !breakable.contains(b.targetSecond)
                ) {
                    hanging.push(b);
                } else {
                    stack.push(b);
                }
            } else if (b.type == Branch.Type.jump) {
                var line = b.line;

                var enclosing = enclosing_block(state, b.line);

                var tailTargetSecond = b.targetSecond;
                var unprotected = enclosing_unprotected_block(state, b.line);
                if (unprotected != null && !unprotected.contains(b.targetSecond)) {
                    if (tailTargetSecond == state.resolved[unprotected.getUnprotectedTarget()]) {
                        tailTargetSecond = unprotected.getUnprotectedLine();
                    }
                }

                var handled = false;

                var breakable = enclosing_breakable_block(state, line);
                if (breakable != null && (b.targetFirst == breakable.end || b.targetFirst == state.resolved[breakable.end])) {
                    var block = new Break(state.function, b.line, b.targetFirst);
                    if (!hanging.isEmpty() && hanging.peek().targetSecond == b.targetFirst
                        && enclosing_block(state, hanging.peek().line) == enclosing
                        && (stack.isEmpty()
                            || stack.peek().line < hanging.peek().line
                            || hanging.peek().line > stack.peek().line)
                    ) {
                        hangingResolver.push(b);
                    }
                    unredirect_finalsets(state, b.targetFirst, line, breakable.begin);
                    state.blocks.add(block);
                    remove_branch(state, b);
                    handled = true;
                }

                if (!handled && state.function.header.version.usegoto.get() && breakable != null && !breakable.contains(b.targetFirst) && state.resolved[b.targetFirst] != state.resolved[breakable.end]) {
                    var block = new Goto(state.function, b.line, b.targetFirst);
                    if (!hanging.isEmpty() && hanging.peek().targetSecond == b.targetFirst
                        && enclosing_block(state, hanging.peek().line) == enclosing
                        && (stack.isEmpty() || hanging.peek().line > stack.peek().line)
                    ) {
                        hangingResolver.push(b);
                    }
                    unredirect_finalsets(state, b.targetFirst, line, 1);
                    state.blocks.add(block);
                    state.labels[b.targetFirst] = true;
                    remove_branch(state, b);
                    handled = true;
                }

                if (!handled && !stack.isEmpty() && stack.peek().targetSecond - 1 == b.line && enclosing.contains(b.line, b.targetSecond) && b.targetSecond > b.line) {
                    var top = stack.peek();
                    while (top != null && top.targetSecond - 1 == b.line && splits_decl(top.line, top.targetFirst, top.targetSecond, declList)) {
                        var if_block = resolve_if_stack(state, stack, top.targetSecond);
                        if (if_block == null) throw new IllegalStateException();
                        top = stack.isEmpty() ? null : stack.peek();
                    }
                    if (top != null && top.targetSecond - 1 == b.line) {
                        if (top.targetSecond != b.targetSecond) {
                            resolve_else(state, stack, hanging, elseStack, top, b, tailTargetSecond);
                            stack.pop();
                        } else if (!splits_decl(top.line, top.targetFirst, top.targetSecond - 1, declList)) {
                            // "empty else" case
                            b.targetSecond = tailTargetSecond;
                            state.blocks.add(new IfThenElseBlock(
                                    state.function, top.cond, top.targetFirst, top.targetSecond, b.targetSecond,
                                    get_close_type(state, top.targetSecond - 2), top.targetSecond - 2
                            ));
                            remove_branch(state, b);
                            stack.pop();
                        }
                    }
                    handled = true; // TODO: should this always count as handled?
                }

                if (
                        !handled
                        && breakable != null
                        && line + 1 < state.branches.length && state.branches[line + 1] != null
                        && state.branches[line + 1].type == Branch.Type.jump
                ) {
                    for (var i = 0; i < hanging.size(); i++) {
                        var hanger = hanging.peek(i);
                        if (
                                state.resolved[hanger.targetSecond] == state.resolved[breakable.end]
                                && line + 1 < state.branches.length && state.branches[line + 1] != null
                                && state.branches[line + 1].targetFirst == hanger.targetSecond
                                && !splits_decl(hanger.line, hanger.targetFirst, b.line, declList) // if else
                                && !splits_decl(b.line, b.line + 1, b.line + 2, declList) // else break
                                && !splits_decl(hanger.line, hanger.targetFirst, b.line + 2, declList) // full
                        ) {
                            // resolve intervening hangers
                            for (var j = i; j > 0; j--) {
                                while (!is_hanger_resolvable(state, declList, hanging.peek(), hangingResolver.peek())) {
                                    hangingResolver.pop();
                                }
                                resolve_hanger(state, stack, hanging.pop(), hangingResolver.peek());
                            }

                            // else break
                            var top = hanging.pop();
                            if (!hangingResolver.isEmpty() && hangingResolver.peek().targetFirst == top.targetSecond) {
                                hangingResolver.pop();
                            }
                            top.targetSecond = line + 1;
                            resolve_else(state, stack, hanging, elseStack, top, b, tailTargetSecond);
                            handled = true;
                            break;
                        } else if (!is_hanger_resolvable(state, declList, hanger, hangingResolver)) {
                            break;
                        }
                    }
                }

                if (
                        !handled
                        && breakable != null && breakable.isSplitable()
                        && state.resolved[b.targetFirst] == breakable.getUnprotectedTarget()
                        && line + 1 < state.branches.length && state.branches[line + 1] != null
                        && state.branches[line + 1].type == Branch.Type.jump
                        && state.resolved[state.branches[line + 1].targetFirst] == state.resolved[breakable.end]
                ) {
                    // split while condition (else break)
                    var split = breakable.split(b.line, get_close_type(state, b.line - 1));
                    Collections.addAll(state.blocks, split);
                    remove_branch(state, b);
                    handled = true;
                }

                if (
                        !handled
                        && !stack.isEmpty() && stack.peek().targetSecond == b.targetFirst
                        && line + 1 < state.branches.length && state.branches[line + 1] != null
                        && state.branches[line + 1].type == Branch.Type.jump
                        && state.branches[line + 1].targetFirst == b.targetFirst
                ) {
                    // empty else (redirected)
                    var top = stack.peek();
                    if (!splits_decl(top.line, top.targetFirst, b.line, declList)) {
                        top.targetSecond = line + 1;
                        b.targetSecond = line + 1;
                        state.blocks.add(new IfThenElseBlock(
                                state.function, top.cond, top.targetFirst, top.targetSecond, b.targetSecond,
                                get_close_type(state, line - 1), line - 1
                        ));
                        remove_branch(state, b);
                        stack.pop();
                    }
                    handled = true; // TODO:
                }

                if (
                        !handled
                        && !hanging.isEmpty() && hanging.peek().targetSecond == b.targetFirst
                        && line + 1 < state.branches.length && state.branches[line + 1] != null
                        && state.branches[line + 1].type == Branch.Type.jump
                        && state.branches[line + 1].targetFirst == b.targetFirst
                ) {
                    // empty else (redirected)
                    var top = hanging.peek();
                    if (!splits_decl(top.line, top.targetFirst, b.line, declList)) {
                        if (!hangingResolver.isEmpty() && hangingResolver.peek().targetFirst == top.targetSecond) {
                            hangingResolver.pop();
                        }
                        top.targetSecond = line + 1;
                        b.targetSecond = line + 1;
                        state.blocks.add(new IfThenElseBlock(
                                state.function, top.cond, top.targetFirst, top.targetSecond, b.targetSecond,
                                get_close_type(state, line - 1), line - 1
                        ));
                        remove_branch(state, b);
                        hanging.pop();
                    }
                    handled = true; // TODO:
                }

                if (!handled) {
                    state.function.header.version.usegoto.get();
                    var block = new Goto(state.function, b.line, b.targetFirst);
                    if (!hanging.isEmpty() && hanging.peek().targetSecond == b.targetFirst && enclosing_block(state, hanging.peek().line) == enclosing) {
                        hangingResolver.push(b);
                    }
                    state.blocks.add(block);
                    state.labels[b.targetFirst] = true;
                    remove_branch(state, b);
                }
            }
            b = b.next;
        }
        while (!hangingResolver.isEmpty()) {
            resolve_hangers(state, declList, stack, hanging, hangingResolver.pop());
        }
        while (!hanging.isEmpty()) {
            // if break (or if goto)
            var top = hanging.pop();
            var breakable = enclosing_breakable_block(state, top.line);
            if (breakable != null && breakable.end == top.targetSecond) {
                Block block = new IfThenEndBlock(state.function, state.r, top.cond.inverse(), top.targetFirst - 1, top.targetFirst - 1);
                block.addStatement(new Break(state.function, top.targetFirst - 1, top.targetSecond));
                state.blocks.add(block);
            } else {
                state.function.header.version.usegoto.get();
                state.function.header.version.useifbreakrewrite.get();
                Block block = new IfThenEndBlock(state.function, state.r, top.cond.inverse(), top.targetFirst - 1, top.targetFirst - 1);
                block.addStatement(new Goto(state.function, top.targetFirst - 1, top.targetSecond));
                state.blocks.add(block);
                state.labels[top.targetSecond] = true;
            }
            remove_branch(state, top);
        }
        while (resolve_if_stack(state, stack, Integer.MAX_VALUE) != null) {
        }
    }

    private static void unredirect_finalsets(State state, int target, int line, int begin) {
        var b = state.begin_branch;
        while (b != null) {
            if (b.type == Branch.Type.finalset) {
                if (b.targetSecond == target && b.line < line && b.line >= begin) {
                    b.targetFirst = line - 1;
                    b.targetSecond = line;
                    if (b.finalset != null) {
                        b.finalset.line = line - 1;
                    }
                }
            }
            b = b.next;
        }
    }

    private static void find_set_blocks(State state) {
        var blocks = state.blocks;
        var b = state.begin_branch;
        while (b != null) {
            if (is_assignment(b) || b.type == Branch.Type.finalset) {
                if (b.finalset != null) {
                    var c = b.finalset;
                    var op = state.bytecodeReader.op(c.line);
                    if (c.line >= 2 && (op == Op.MMBIN || op == Op.MMBINI || op == Op.MMBINK || op == Op.EXTRAARG)) {
                        c.line--;
                        if (b.targetFirst == c.line + 1) {
                            b.targetFirst = c.line;
                        }
                    }
                    while (state.bytecodeReader.isUpvalueDeclaration(c.line)) {
                        c.line--;
                        if (b.targetFirst == c.line + 1) {
                            b.targetFirst = c.line;
                        }
                    }

                    if (is_jmp_raw(state, c.line)) {
                        c.type = FinalSetCondition.Type.REGISTER;
                    } else {
                        c.type = FinalSetCondition.Type.VALUE;
                    }
                }
                if (b.cond == b.finalset) {
                    remove_branch(state, b);
                } else {
                    Block block = new SetBlock(state.function, b.cond, b.target, b.targetFirst, b.targetSecond, state.r);
                    blocks.add(block);
                    remove_branch(state, b);
                }
            }
            b = b.next;
        }
    }

    private static Block enclosing_block(State state, int line) {
        Block enclosing = null;
        for (var block : state.blocks) {
            if (block.contains(line)) {
                if (enclosing == null || enclosing.contains(block)) {
                    enclosing = block;
                }
            }
        }
        return enclosing;
    }

    private static Block enclosing_breakable_block(State state, int line) {
        Block enclosing = null;
        for (var block : state.blocks) {
            if (block.contains(line) && block.breakable()) {
                if (enclosing == null || enclosing.contains(block)) {
                    enclosing = block;
                }
            }
        }
        return enclosing;
    }

    private static Block enclosing_unprotected_block(State state, int line) {
        Block enclosing = null;
        for (var block : state.blocks) {
            if (block.contains(line) && block.isUnprotected()) {
                if (enclosing == null || enclosing.contains(block)) {
                    enclosing = block;
                }
            }
        }
        return enclosing;
    }

    private static void find_pseudo_goto_statements(State state, List<Declaration> declList) {
        var b = state.begin_branch;
        while (b != null) {
            if (b.type == Branch.Type.jump && b.targetFirst > b.line) {
                var end = b.targetFirst;
                Block smallestEnclosing = null;
                for (var block : state.blocks) {
                    if (block.contains(b.line) && block.contains(end - 1)) {
                        if (smallestEnclosing == null || smallestEnclosing.contains(block)) {
                            smallestEnclosing = block;
                        }
                    }
                }
                if (smallestEnclosing != null) {
                    // Should always find the outer block at least...
                    Block wrapping = null;
                    for (var block : state.blocks) {
                        if (block != smallestEnclosing && smallestEnclosing.contains(block) && block.contains(b.line)) {
                            if (wrapping == null || block.contains(wrapping)) {
                                wrapping = block;
                            }
                        }
                    }
                    var begin = smallestEnclosing.begin;
                    if (wrapping != null) {
                        begin = Math.max(wrapping.begin - 1, smallestEnclosing.begin);
                        //beginMax = begin;
                    }
                    var lowerBound = Integer.MIN_VALUE;
                    var upperBound = Integer.MAX_VALUE;
                    final var scopeAdjust = -1;
                    for (var decl : declList) {
                        //if(decl.begin >= begin && decl.begin < end) {

                        //}
                        if (decl.end >= begin && decl.end <= end + scopeAdjust) {
                            if (decl.begin < begin) {
                                upperBound = Math.min(decl.begin, upperBound);
                            }
                        }
                        if (decl.begin >= begin && decl.begin <= end + scopeAdjust && decl.end > end + scopeAdjust) {
                            lowerBound = Math.max(decl.begin + 1, lowerBound);
                            begin = decl.begin + 1;
                        }
                    }
                    if (lowerBound > upperBound) {
                        throw new IllegalStateException();
                    }
                    begin = Math.max(lowerBound, begin);
                    begin = Math.min(upperBound, begin);
                    var breakable = enclosing_breakable_block(state, b.line);
                    if (breakable != null) {
                        begin = Math.max(breakable.begin, begin);
                    }
                    var containsBreak = false;
                    var loop = new OnceLoop(state.function, begin, end);
                    for (var block : state.blocks) {
                        if (loop.contains(block) && block instanceof Break) {
                            containsBreak = true;
                            break;
                        }
                    }
                    if (containsBreak) {
                        // TODO: close type
                        state.blocks.add(new IfThenElseBlock(state.function, FixedCondition.TRUE, begin, b.line + 1, end, CloseType.NONE, -1));
                        state.blocks.add(new ElseEndBlock(state.function, b.line + 1, end, CloseType.NONE, -1));
                        remove_branch(state, b);
                    } else {
                        state.blocks.add(loop);
                        var b2 = b;
                        while (b2 != null) {
                            if (b2.type == Branch.Type.jump && b2.targetFirst > b2.line && b2.targetFirst == b.targetFirst) {
                                var breakStatement = new Break(state.function, b2.line, b2.targetFirst);
                                state.blocks.add(breakStatement);
                                breakStatement.comment = "pseudo-goto";
                                remove_branch(state, b2);
                                if (b.next == b2) {
                                    b = b2;
                                }
                            }
                            b2 = b2.next;
                        }
                    }
                }
            }
            b = b.next;
        }
    }

    private static void find_do_blocks(State state, List<Declaration> declList) {
        List<Block> newBlocks = new ArrayList<>();
        for (var block : state.blocks) {
            if (block.hasCloseLine() && block.getCloseLine() >= 1) {
                var closeLine = block.getCloseLine();
                var enclosing = enclosing_block(state, closeLine);
                if ((enclosing == block || enclosing.contains(block)) && is_close(state, closeLine)) {
                    var register = get_close_value(state, closeLine);
                    var close = true;
                    Declaration closeDecl = null;
                    for (var decl : declList) {
                        if (!decl.forLoop && !decl.forLoopExplicit && block.contains(decl.begin)) {
                            if (decl.register < register) {
                                close = false;
                            } else if (decl.register == register) {
                                closeDecl = decl;
                            }
                        }
                    }
                    if (close) {
                        block.useClose();
                    } else if (closeDecl != null) {
                        Block inner = new DoEndBlock(state.function, closeDecl.begin, closeDecl.end + 1);
                        inner.closeRegister = register;
                        newBlocks.add(inner);
                        strictScopeCheck(state);
                    }
                }
            }
        }
        state.blocks.addAll(newBlocks);

        for (var decl : declList) {
            var begin = decl.begin;
            if (!decl.forLoop && !decl.forLoopExplicit) {
                var needsDoEnd = true;
                for (var block : state.blocks) {
                    if (block.contains(decl.begin)) {
                        if (block.scopeEnd() == decl.end) {
                            block.useScope();
                            needsDoEnd = false;
                            break;
                        } else if (block.scopeEnd() < decl.end) {
                            begin = Math.min(begin, block.begin);
                        }
                    }
                }
                if (needsDoEnd) {
                    // Without accounting for the order of declarations, we might
                    // create another do..end block later that would eliminate the
                    // need for this one. But order of decls should fix this.
                    state.blocks.add(new DoEndBlock(state.function, begin, decl.end + 1));
                    strictScopeCheck(state);
                }
            }
        }
    }

    private static void strictScopeCheck(State state) {
        if (state.function.header.config.strict_scope) {
            throw new RuntimeException("Violation of strict scope rule");
        }
    }

    private static boolean is_conditional(Branch b) {
        return b.type == Branch.Type.comparison || b.type == Branch.Type.test;
    }

    private static boolean is_assignment(Branch b) {
        return b.type == Branch.Type.testset;
    }

    private static boolean is_assignment(Branch b, int r) {
        return b.type == Branch.Type.testset || b.type == Branch.Type.test && b.target == r;
    }

    private static boolean adjacent(State state, Branch branch0, Branch branch1) {
        if (branch1.finalset != null && branch0.finalset == branch1.finalset) {
            // With redirects, there can be real statements between a finalset and paired branches.
            return true;
        } else if (branch0 == null || branch1 == null) {
            return false;
        } else {
            var adjacent = branch0.targetFirst <= branch1.line;
            if (adjacent) {
                adjacent = !has_statement(state, branch0.targetFirst, branch1.line - 1);
                adjacent = adjacent && !state.reverse_targets[branch1.line];
            }
            return adjacent;
        }
    }

    private static Branch combine_left(State state, Branch branch1) {
        if (is_conditional(branch1)) {
            return combine_conditional(state, branch1);
        } else if (is_assignment(branch1) || branch1.type == Branch.Type.finalset) {
            return combine_assignment(state, branch1);
        } else {
            return branch1;
        }
    }

    private static Branch combine_conditional(State state, Branch branch1) {
        var branch0 = branch1.previous;
        var branchn = branch1;
        while (branch0 != null && branch0.line > branch1.line) {
            branch0 = branch0.previous;
        }
        while (branch0 != null && branchn == branch1 && adjacent(state, branch0, branch1)) {
            branchn = combine_conditional_helper(state, branch0, branch1);
            if (branch0.targetSecond > branch1.targetFirst) break;
            branch0 = branch0.previous;
        }
        return branchn;
    }

    private static Branch combine_conditional_helper(State state, Branch branch0, Branch branch1) {
        if (is_conditional(branch0) && is_conditional(branch1)) {
            var branch0TargetSecond = branch0.targetSecond;
            if (is_jmp(state, branch1.targetFirst) && state.bytecodeReader.target(branch1.targetFirst) == branch0TargetSecond) {
                // Handle redirected target
                branch0TargetSecond = branch1.targetFirst;
            }
            if (branch0TargetSecond == branch1.targetFirst) {
                // Combination if not branch0 or branch1 then
                branch0 = combine_conditional(state, branch0);
                Condition c = new OrCondition(branch0.cond.inverse(), branch1.cond);
                var branchn = new Branch(branch0.line, branch1.line2, Branch.Type.comparison, c, branch1.targetFirst, branch1.targetSecond, branch1.finalset);
                branchn.inverseValue = branch1.inverseValue;
                if (verbose) System.err.println("conditional or " + branchn.line);
                replace_branch(state, branch0, branch1, branchn);
                return combine_conditional(state, branchn);
            } else if (branch0TargetSecond == branch1.targetSecond) {
                // Combination if branch0 and branch1 then
                branch0 = combine_conditional(state, branch0);
                Condition c = new AndCondition(branch0.cond, branch1.cond);
                var branchn = new Branch(branch0.line, branch1.line2, Branch.Type.comparison, c, branch1.targetFirst, branch1.targetSecond, branch1.finalset);
                branchn.inverseValue = branch1.inverseValue;
                if (verbose) System.err.println("conditional and " + branchn.line);
                replace_branch(state, branch0, branch1, branchn);
                return combine_conditional(state, branchn);
            }
        }
        return branch1;
    }

    private static Branch combine_assignment(State state, Branch branch1) {
        var branch0 = branch1.previous;
        var branchn = branch1;
        while (branch0 != null && branchn == branch1) {
            branchn = combine_assignment_helper(state, branch0, branch1);
            if (branch1.cond == branch1.finalset) {
                // keep searching for the first branch paired with a raw finalset
            } else if (branch0.cond == branch0.finalset) {
                // ignore duped finalset
            } else if (branch0.targetSecond > branch1.targetFirst) {
                break;
            }
            branch0 = branch0.previous;
        }
        return branchn;
    }

    private static Branch combine_assignment_helper(State state, Branch branch0, Branch branch1) {
        if (adjacent(state, branch0, branch1)) {
            var register = branch1.target;
            if (branch1.target == -1) {
                throw new IllegalStateException();
            }
            //System.err.println("blah " + branch1.line + " " + branch0.line);
            if (is_conditional(branch0) && is_assignment(branch1)) {
                //System.err.println("bridge cand " + branch1.line + " " + branch0.line);
                if (branch0.targetSecond == branch1.targetFirst) {
                    var inverse = branch0.inverseValue;
                    if (verbose)
                        System.err.println("bridge " + (inverse ? "or" : "and") + " " + branch1.line + " " + branch0.line);
                    branch0 = combine_conditional(state, branch0);
                    if (inverse != branch0.inverseValue) throw new IllegalStateException();
                    Condition c;
                    if (!branch1.inverseValue) {
                        //System.err.println("bridge or " + branch0.line + " " + branch0.inverseValue);
                        c = new OrCondition(branch0.cond.inverse(), branch1.cond);
                    } else {
                        //System.err.println("bridge and " + branch0.line + " " + branch0.inverseValue);
                        c = new AndCondition(branch0.cond, branch1.cond);
                    }
                    var branchn = new Branch(branch0.line, branch1.line2, branch1.type, c, branch1.targetFirst, branch1.targetSecond, branch1.finalset);
                    branchn.inverseValue = branch1.inverseValue;
                    branchn.target = register;
                    replace_branch(state, branch0, branch1, branchn);
                    return combine_assignment(state, branchn);
                } else if (branch0.targetSecond == branch1.targetSecond) {
          /*
          Condition c = new AndCondition(branch0.cond, branch1.cond);
          Branch branchn = new Branch(branch0.line, Branch.Type.comparison, c, branch1.targetFirst, branch1.targetSecond);
          replace_branch(state, branch0, branch1, branchn);
          return branchn;
          */
                }
            }

            if (is_assignment(branch0, register) && is_assignment(branch1) && branch0.inverseValue == branch1.inverseValue) {
                if (branch0.targetSecond == branch1.targetSecond) {
                    Condition c;
                    //System.err.println("preassign " + branch1.line + " " + branch0.line + " " + branch0.targetSecond);
                    if (verbose)
                        System.err.println("assign " + (branch0.inverseValue ? "or" : "and") + " " + branch1.line + " " + branch0.line);
                    if (is_conditional(branch0)) {
                        branch0 = combine_conditional(state, branch0);
                        if (branch0.inverseValue) {
                            branch0.cond = branch0.cond.inverse(); // inverse has been double handled; undo it
                        }
                    } else {
                        var inverse = branch0.inverseValue;
                        branch0 = combine_assignment(state, branch0);
                        if (inverse != branch0.inverseValue) throw new IllegalStateException();
                    }
                    if (branch0.inverseValue) {
                        //System.err.println("assign and " + branch1.line + " " + branch0.line);
                        c = new OrCondition(branch0.cond, branch1.cond);
                    } else {
                        //System.err.println("assign or " + branch1.line + " " + branch0.line);
                        c = new AndCondition(branch0.cond, branch1.cond);
                    }
                    var branchn = new Branch(branch0.line, branch1.line2, branch1.type, c, branch1.targetFirst, branch1.targetSecond, branch1.finalset);
                    branchn.inverseValue = branch1.inverseValue;
                    branchn.target = register;
                    replace_branch(state, branch0, branch1, branchn);
                    return combine_assignment(state, branchn);
                }
            }
            if (is_assignment(branch0, register) && branch1.type == Branch.Type.finalset) {
                if (branch0.targetSecond == branch1.targetSecond) {
                    Condition c;
                    //System.err.println("final preassign " + branch1.line + " " + branch0.line);
                    if (branch0.finalset != null && branch0.finalset != branch1.finalset) {
                        var b = branch0.next;
                        while (b != null) {
                            if (b.cond == branch0.finalset) {
                                remove_branch(state, b);
                                break;
                            }
                            b = b.next;
                        }
                    }

                    if (is_conditional(branch0)) {
                        branch0 = combine_conditional(state, branch0);
                        if (branch0.inverseValue) {
                            branch0.cond = branch0.cond.inverse(); // inverse has been double handled; undo it
                        }
                    } else {
                        var inverse = branch0.inverseValue;
                        branch0 = combine_assignment(state, branch0);
                        if (inverse != branch0.inverseValue) throw new IllegalStateException();
                    }
                    if (verbose)
                        System.err.println("final assign " + (branch0.inverseValue ? "or" : "and") + " " + branch1.line + " " + branch0.line);

                    if (branch0.inverseValue) {
                        //System.err.println("final assign or " + branch1.line + " " + branch0.line);
                        c = new OrCondition(branch0.cond, branch1.cond);
                    } else {
                        //System.err.println("final assign and " + branch1.line + " " + branch0.line);
                        c = new AndCondition(branch0.cond, branch1.cond);
                    }
                    var branchn = new Branch(branch0.line, branch1.line2, Branch.Type.finalset, c, branch1.targetFirst, branch1.targetSecond, branch1.finalset);
                    branchn.target = register;
                    replace_branch(state, branch0, branch1, branchn);
                    return combine_assignment(state, branchn);
                }
            }
        }
        return branch1;
    }

    private static void raw_add_branch(State state, Branch b) {
        if (b.type == Branch.Type.finalset) {
            var list = state.finalsetbranches.get(b.line);
            if (list == null) {
                list = new LinkedList<>();
                state.finalsetbranches.set(b.line, list);
            }
            list.add(b);
        } else if (b.type == Branch.Type.testset) {
            state.setbranches[b.line] = b;
        } else {
            state.branches[b.line] = b;
        }
    }

    private static void raw_remove_branch(State state, Branch b) {
        if (b.type == Branch.Type.finalset) {
            var list = state.finalsetbranches.get(b.line);
            if (list == null) {
                throw new IllegalStateException();
            }
            list.remove(b);
        } else if (b.type == Branch.Type.testset) {
            state.setbranches[b.line] = null;
        } else {
            state.branches[b.line] = null;
        }
    }

    private static void replace_branch(State state, Branch branch0, Branch branch1, Branch branchn) {
        remove_branch(state, branch0);
        raw_remove_branch(state, branch1);
        branchn.previous = branch1.previous;
        if (branchn.previous == null) {
            state.begin_branch = branchn;
        } else {
            branchn.previous.next = branchn;
        }
        branchn.next = branch1.next;
        if (branchn.next == null) {
            state.end_branch = branchn;
        } else {
            branchn.next.previous = branchn;
        }
        raw_add_branch(state, branchn);
    }

    private static void remove_branch(State state, Branch b) {
        raw_remove_branch(state, b);
        var prev = b.previous;
        var next = b.next;
        if (prev != null) {
            prev.next = next;
        } else {
            state.begin_branch = next;
        }
        if (next != null) {
            next.previous = prev;
        } else {
            state.end_branch = prev;
        }
    }

    private static void insert_branch(State state, Branch b) {
        raw_add_branch(state, b);
    }

    private static void link_branches(State state) {
        Branch previous = null;
        for (var index = 0; index < state.branches.length; index++) {
            for (var array = 0; array < 3; array++) {
                if (array == 0) {
                    var list = state.finalsetbranches.get(index);
                    if (list != null) {
                        for (var b : list) {
                            b.previous = previous;
                            if (previous != null) {
                                previous.next = b;
                            } else {
                                state.begin_branch = b;
                            }
                            previous = b;
                        }
                    }
                } else {
                    Branch[] branches;
                    if (array == 1) {
                        branches = state.setbranches;
                    } else {
                        branches = state.branches;
                    }
                    var b = branches[index];
                    if (b != null) {
                        b.previous = previous;
                        if (previous != null) {
                            previous.next = b;
                        } else {
                            state.begin_branch = b;
                        }
                        previous = b;
                    }
                }
            }
        }
        state.end_branch = previous;
    }

    private static boolean is_jmp_raw(State state, int line) {
        var op = state.bytecodeReader.op(line);
        return op == Op.JMP || op == Op.JMP52 || op == Op.JMP54;
    }

    private static boolean is_jmp(State state, int line) {
        var code = state.bytecodeReader;
        var op = code.op(line);
        if (op == Op.JMP || op == Op.JMP54) {
            return true;
        } else if (op == Op.JMP52) {
            return !is_close(state, line);
        } else {
            return false;
        }
    }

    private static boolean is_close(State state, int line) {
        var code = state.bytecodeReader;
        var op = code.op(line);
        if (op == Op.CLOSE) {
            return true;
        } else if (op == Op.JMP52) {
            var target = code.target(line);
            if (target == line + 1) {
                return code.A(line) != 0;
            } else {
                if (line + 1 <= code.length && code.op(line + 1) == Op.JMP52) {
                    return target == code.target(line + 1) && code.A(line) != 0;
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    private static int get_close_value(State state, int line) {
        var code = state.bytecodeReader;
        var op = code.op(line);
        if (op == Op.CLOSE) {
            return code.A(line);
        } else if (op == Op.JMP52) {
            return code.A(line) - 1;
        } else {
            throw new IllegalStateException();
        }
    }

    private static CloseType get_close_type(State state, int line) {
        if (line < 1 || !is_close(state, line)) {
            return CloseType.NONE;
        } else {
            var op = state.bytecodeReader.op(line);
            if (op == Op.CLOSE) {
                return state.function.header.version.closesemantics.get() == Version.CloseSemantics.LUA54 ? CloseType.CLOSE54 : CloseType.CLOSE;
            } else {
                return CloseType.JMP;
            }
        }
    }

    private static boolean has_statement(State state, int begin, int end) {
        for (var line = begin; line <= end; line++) {
            if (is_statement(state, line)) {
                return true;
            }
        }
        return state.d.hasStatement(begin, end);
    }

    private static boolean is_statement(State state, int line) {
        if (state.reverse_targets[line]) return true;
        var r = state.r;
        if (!r.getNewLocals(line).isEmpty()) return true;
        var code = state.bytecodeReader;
        if (code.isUpvalueDeclaration(line)) return false;
        switch (code.op(line)) {
            case MOVE, LOADI, LOADF, LOADK, LOADKX, LOADBOOL, LOADFALSE, LOADTRUE, LFALSESKIP, GETGLOBAL, GETUPVAL, GETTABUP, GETTABUP54, GETTABLE, GETTABLE54, GETI, GETFIELD, NEWTABLE50, NEWTABLE, NEWTABLE54, ADD, SUB, MUL, DIV, IDIV, MOD, POW, BAND, BOR, BXOR, SHL, SHR, UNM, NOT, LEN, BNOT, CONCAT, CONCAT54, CLOSURE, TESTSET, TESTSET54 -> {
                return r.isLocal(code.A(line), line);
            }
            case ADD54, SUB54, MUL54, DIV54, IDIV54, MOD54, POW54, BAND54, BOR54, BXOR54, SHL54, SHR54, ADDK, SUBK, MULK, DIVK, IDIVK, MODK, POWK, BANDK, BORK, BXORK, ADDI, SHLI, SHRI -> {
                return false; // only count following MMBIN* instruction
            }
            case MMBIN, MMBINI, MMBINK -> {
                if (line <= 1) throw new IllegalStateException();
                return r.isLocal(code.A(line - 1), line - 1);
            }
            case LOADNIL -> {
                for (var register = code.A(line); register <= code.B(line); register++) {
                    if (r.isLocal(register, line)) {
                        return true;
                    }
                }
                return false;
            }
            case LOADNIL52 -> {
                for (var register = code.A(line); register <= code.A(line) + code.B(line); register++) {
                    if (r.isLocal(register, line)) {
                        return true;
                    }
                }
                return false;
            }
            case SETGLOBAL, SETUPVAL, SETTABUP, SETTABUP54, TAILCALL, TAILCALL54, RETURN, RETURN54, RETURN0, RETURN1, FORLOOP, FORLOOP54, FORPREP, FORPREP54, TFORCALL, TFORCALL54, TFORLOOP, TFORLOOP52, TFORLOOP54, TFORPREP, TFORPREP54, CLOSE, TBC -> { // TODO: ?
                return true;
            }
            case TEST50 -> {
                return code.A(line) != code.B(line) && r.isLocal(code.A(line), line);
            }
            case SELF, SELF54 -> {
                return r.isLocal(code.A(line), line) || r.isLocal(code.A(line) + 1, line);
            }
            case EQ, LT, LE, EQ54, LT54, LE54, EQK, EQI, LTI, LEI, GTI, GEI, TEST, TEST54, SETLIST50, SETLISTO, SETLIST, SETLIST52, SETLIST54, VARARGPREP, EXTRAARG, EXTRABYTE -> {
                return false;
            } // TODO: CLOSE?
            case JMP, JMP52, JMP54 -> {
                if (line == 1) {
                    return true;
                } else {
                    var prev = line >= 2 ? code.op(line - 1) : null;
                    var next = line + 1 <= code.length ? code.op(line + 1) : null;
                    if (prev == Op.EQ) return false;
                    if (prev == Op.LT) return false;
                    if (prev == Op.LE) return false;
                    if (prev == Op.EQ54) return false;
                    if (prev == Op.LT54) return false;
                    if (prev == Op.LE54) return false;
                    if (prev == Op.EQK) return false;
                    if (prev == Op.EQI) return false;
                    if (prev == Op.LTI) return false;
                    if (prev == Op.LEI) return false;
                    if (prev == Op.GTI) return false;
                    if (prev == Op.GEI) return false;
                    if (prev == Op.TEST50) return false;
                    if (prev == Op.TEST) return false;
                    if (prev == Op.TEST54) return false;
                    if (prev == Op.TESTSET) return false;
                    if (prev == Op.TESTSET54) return false;
                    if (next == Op.LOADBOOL && code.C(line + 1) != 0) return false;
                    return next != Op.LFALSESKIP;
                }
            }
            case CALL -> {
                var a = code.A(line);
                var c = code.C(line);
                if (c == 1) {
                    return true;
                }
                if (c == 0) c = r.registers - a + 1;
                for (var register = a; register < a + c - 1; register++) {
                    if (r.isLocal(register, line)) {
                        return true;
                    }
                }
                return false;
            }
            case VARARG -> {
                var a = code.A(line);
                var b = code.B(line);
                if (b == 0) b = r.registers - a + 1;
                for (var register = a; register < a + b - 1; register++) {
                    if (r.isLocal(register, line)) {
                        return true;
                    }
                }
                return false;
            }
            case VARARG54 -> {
                var a = code.A(line);
                var c = code.C(line);
                if (c == 0) c = r.registers - a + 1;
                for (var register = a; register < a + c - 1; register++) {
                    if (r.isLocal(register, line)) {
                        return true;
                    }
                }
                return false;
            }
            case SETTABLE, SETTABLE54, SETI, SETFIELD -> {
                // special case -- this is actually ambiguous and must be resolved by the decompiler check
                return false;
            }
            case DEFAULT, DEFAULT54 -> throw new IllegalStateException();
        }
        throw new IllegalStateException("Illegal opcode: " + code.op(line));
    }

    private static class Branch implements Comparable<Branch> {

        public Branch previous;
        public Branch next;
        public final int line;
        public final int line2;
        public int target;
        public final Type type;
        public Condition cond;
        public int targetFirst;
        public int targetSecond;
        public boolean inverseValue;
        public FinalSetCondition finalset;
        public Branch(int line, int line2, Type type, Condition cond, int targetFirst, int targetSecond, FinalSetCondition finalset) {
            this.line = line;
            this.line2 = line2;
            this.type = type;
            this.cond = cond;
            this.targetFirst = targetFirst;
            this.targetSecond = targetSecond;
            this.inverseValue = false;
            this.target = -1;
            this.finalset = finalset;
        }

        @Override
        public int compareTo(Branch other) {
            return this.line - other.line;
        }

        private enum Type {
            comparison,
            test,
            testset,
            finalset,
            jump
        }
    }

    private static class State {
        public Decompiler d;
        public BFunction function;
        public Registers r;
        public BytecodeReader bytecodeReader;
        public Branch begin_branch;
        public Branch end_branch;
        public Branch[] branches;
        public Branch[] setbranches;
        public ArrayList<List<Branch>> finalsetbranches;
        public boolean[] reverse_targets;
        public int[] resolved;
        public boolean[] labels;
        public List<Block> blocks;
    }

    public static class Result {

        public final List<Block> blocks;
        public final boolean[] labels;
        public Result(State state) {
            blocks = state.blocks;
            labels = state.labels;
        }
    }

}
