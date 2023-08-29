package me.hydos.unluac.decompile;

import me.hydos.unluac.Configuration;
import me.hydos.unluac.Version;
import me.hydos.unluac.decompile.block.Block;
import me.hydos.unluac.decompile.block.DoEndBlock;
import me.hydos.unluac.decompile.block.OuterBlock;
import me.hydos.unluac.decompile.expression.*;
import me.hydos.unluac.decompile.operation.*;
import me.hydos.unluac.decompile.statement.Assignment;
import me.hydos.unluac.decompile.statement.Label;
import me.hydos.unluac.decompile.statement.Statement;
import me.hydos.unluac.decompile.target.*;
import me.hydos.unluac.parse.LFunction;
import me.hydos.unluac.parse.LUpvalue;
import me.hydos.unluac.util.Stack;

import java.util.*;

public class Decompiler {

    public final LFunction function;
    public final Code code;
    public final Declaration[] declList;

    private final int registers;
    private final int length;
    private final Upvalues upvalues;

    private final Function f;
    private final LFunction[] functions;
    private final int params;
    private final int vararg;

    public Decompiler(LFunction function) {
        this(function, null, -1);
    }

    public Decompiler(LFunction function, Declaration[] parentDecls, int line) {
        this.f = new Function(function);
        this.function = function;
        registers = function.maximumStackSize;
        length = function.code.length;
        code = new Code(function);
        if (function.stripped || getConfiguration().variable == Configuration.VariableMode.NODEBUG) {
            if (getConfiguration().variable == Configuration.VariableMode.FINDER) {
                declList = VariableFinder.process(this, function.numParams, function.maximumStackSize);
            } else {
                declList = new Declaration[function.maximumStackSize];
                var scopeEnd = length + function.header.version.outerblockscopeadjustment.get();
                int i;
                for (i = 0; i < Math.min(function.numParams, function.maximumStackSize); i++) {
                    declList[i] = new Declaration("A" + i + "_" + function.level, 0, scopeEnd);
                }
                if (getVersion().varargtype.get() != Version.VarArgType.ELLIPSIS && (function.vararg & 1) != 0 && i < function.maximumStackSize) {
                    declList[i++] = new Declaration("arg", 0, scopeEnd);
                }
                for (; i < function.maximumStackSize; i++) {
                    declList[i] = new Declaration("L" + i + "_" + function.level, 0, scopeEnd);
                }
            }
        } else if (function.locals.length >= function.numParams) {
            declList = new Declaration[function.locals.length];
            for (var i = 0; i < declList.length; i++) {
                declList[i] = new Declaration(function.locals[i], code);
            }
        } else {
            declList = new Declaration[function.numParams];
            for (var i = 0; i < declList.length; i++) {
                declList[i] = new Declaration("_ARG_" + i + "_", 0, length - 1);
            }
        }
        upvalues = new Upvalues(function, parentDecls, line);
        functions = function.functions;
        params = function.numParams;
        vararg = function.vararg;
    }

    public Configuration getConfiguration() {
        return function.header.config;
    }

    public Version getVersion() {
        return function.header.version;
    }

    public boolean getNoDebug() {
        return function.header.config.variable == Configuration.VariableMode.NODEBUG ||
               function.stripped && function.header.config.variable == Configuration.VariableMode.DEFAULT;
    }

    public State decompile() {
        var state = new State();
        state.r = new Registers(registers, length, declList, f, getNoDebug());
        var result = ControlFlowHandler.process(this, state.r);
        var blocks = result.blocks;
        state.outer = blocks.get(0);
        state.labels = result.labels;
        processSequence(state, blocks, code.length);
        for (var block : blocks) {
            block.resolve(state.r);
        }
        handleUnusedConstants(state.outer);
        return state;
    }

    public void print(State state) {
        print(state, new Output());
    }

    public void print(State state, OutputProvider out) {
        print(state, new Output(out));
    }

    public void print(State state, Output out) {
        handleInitialDeclares(out);
        state.outer.print(this, out);
    }

    private void handleUnusedConstants(Block outer) {
        Set<Integer> unusedConstants = new HashSet<>(function.constants.length);
        outer.walk(new Walker() {

            private int nextConstant = 0;

            @Override
            public void visitExpression(Expression expression) {
                if (expression.isConstant()) {
                    var index = expression.getConstantIndex();
                    if (index >= 0) {
                        while (index > nextConstant) {
                            unusedConstants.add(nextConstant++);
                        }
                        if (index == nextConstant) {
                            nextConstant++;
                        }
                    }
                }
            }

        });
        outer.walk(new Walker() {

            private int nextConstant = 0;

            @Override
            public void visitStatement(Statement statement) {
                if (unusedConstants.contains(nextConstant)) {
                    if (statement.useConstant(f, nextConstant)) {
                        nextConstant++;
                    }
                }
            }

            @Override
            public void visitExpression(Expression expression) {
                if (expression.isConstant()) {
                    var index = expression.getConstantIndex();
                    if (index >= nextConstant) {
                        nextConstant = index + 1;
                    }
                }
            }

        });
    }

    private void handleInitialDeclares(Output out) {
        List<Declaration> initdecls = new ArrayList<>(declList.length);
        var initdeclcount = params;
        switch (getVersion().varargtype.get()) {
            case ARG, HYBRID -> initdeclcount += vararg & 1;
            case ELLIPSIS -> {
            }
        }
        for (var i = initdeclcount; i < declList.length; i++) {
            if (declList[i].begin == 0) {
                initdecls.add(declList[i]);
            }
        }
        if (initdecls.size() > 0) {
            out.print("local ");
            out.print(initdecls.get(0).name);
            for (var i = 1; i < initdecls.size(); i++) {
                out.print(", ");
                out.print(initdecls.get(i).name);
            }
            out.println();
        }
    }

    private int fb2int50(int fb) {
        return (fb & 7) << (fb >> 3);
    }

    private int fb2int(int fb) {
        var exponent = (fb >> 3) & 0x1f;
        if (exponent == 0) {
            return fb;
        } else {
            return ((fb & 7) + 8) << (exponent - 1);
        }
    }

    /**
     * Decodes values from the Lua TMS enumeration used for the MMBIN family of operations.
     */
    private Expression.BinaryOperation decodeBinOp(int tm) {
        return switch (tm) {
            case 6 -> Expression.BinaryOperation.ADD;
            case 7 -> Expression.BinaryOperation.SUB;
            case 8 -> Expression.BinaryOperation.MUL;
            case 9 -> Expression.BinaryOperation.MOD;
            case 10 -> Expression.BinaryOperation.POW;
            case 11 -> Expression.BinaryOperation.DIV;
            case 12 -> Expression.BinaryOperation.IDIV;
            case 13 -> Expression.BinaryOperation.BAND;
            case 14 -> Expression.BinaryOperation.BOR;
            case 15 -> Expression.BinaryOperation.BXOR;
            case 16 -> Expression.BinaryOperation.SHL;
            case 17 -> Expression.BinaryOperation.SHR;
            default -> throw new IllegalStateException();
        };
    }

    private void handle50BinOp(List<Operation> operations, State state, int line, Expression.BinaryOperation op) {
        operations.add(new RegisterSet(line, code.A(line), Expression.make(op, state.r.getKExpression(code.B(line), line), state.r.getKExpression(code.C(line), line))));
    }

    private void handle54BinOp(List<Operation> operations, State state, int line, Expression.BinaryOperation op) {
        operations.add(new RegisterSet(line, code.A(line), Expression.make(op, state.r.getExpression(code.B(line), line), state.r.getExpression(code.C(line), line))));
    }

    private void handle54BinKOp(List<Operation> operations, State state, int line, Expression.BinaryOperation op) {
        if (line + 1 > code.length || code.op(line + 1) != Op.MMBINK) throw new IllegalStateException();
        var left = state.r.getExpression(code.B(line), line);
        Expression right = f.getConstantExpression(code.C(line));
        if (code.k(line + 1)) {
            var temp = left;
            left = right;
            right = temp;
        }
        operations.add(new RegisterSet(line, code.A(line), Expression.make(op, left, right)));
    }

    private void handleUnaryOp(List<Operation> operations, State state, int line, Expression.UnaryOperation op) {
        operations.add(new RegisterSet(line, code.A(line), Expression.make(op, state.r.getExpression(code.B(line), line))));
    }

    private void handleSetList(List<Operation> operations, State state, int line, int stack, int count, int offset) {
        var table = state.r.getValue(stack, line);
        for (var i = 1; i <= count; i++) {
            operations.add(new TableSet(line, table, ConstantExpression.createInteger(offset + i), state.r.getExpression(stack + i, line), false, state.r.getUpdated(stack + i, line)));
        }
    }

    private List<Operation> processLine(State state, int line) {
        var r = state.r;
        var skip = state.skip;
        List<Operation> operations = new LinkedList<>();
        var A = code.A(line);
        var B = code.B(line);
        var C = code.C(line);
        var Bx = code.Bx(line);
        switch (code.op(line)) {
            case MOVE -> operations.add(new RegisterSet(line, A, r.getExpression(B, line)));
            case LOADI -> operations.add(new RegisterSet(line, A, ConstantExpression.createInteger(code.sBx(line))));
            case LOADF -> operations.add(new RegisterSet(line, A, ConstantExpression.createDouble(code.sBx(line))));
            case LOADK -> operations.add(new RegisterSet(line, A, f.getConstantExpression(Bx)));
            case LOADKX -> {
                if (line + 1 > code.length || code.op(line + 1) != Op.EXTRAARG) throw new IllegalStateException();
                operations.add(new RegisterSet(line, A, f.getConstantExpression(code.Ax(line + 1))));
            }
            case LOADBOOL -> operations.add(new RegisterSet(line, A, ConstantExpression.createBoolean(B != 0)));
            case LOADFALSE, LFALSESKIP ->
                    operations.add(new RegisterSet(line, A, ConstantExpression.createBoolean(false)));
            case LOADTRUE -> operations.add(new RegisterSet(line, A, ConstantExpression.createBoolean(true)));
            case LOADNIL -> operations.add(new LoadNil(line, A, B));
            case LOADNIL52 -> operations.add(new LoadNil(line, A, A + B));
            case GETGLOBAL -> operations.add(new RegisterSet(line, A, f.getGlobalExpression(Bx)));
            case SETGLOBAL -> operations.add(new GlobalSet(line, f.getGlobalName(Bx), r.getExpression(A, line)));
            case GETUPVAL -> operations.add(new RegisterSet(line, A, upvalues.getExpression(B)));
            case SETUPVAL -> operations.add(new UpvalueSet(line, upvalues.getName(B), r.getExpression(A, line)));
            case GETTABUP ->
                    operations.add(new RegisterSet(line, A, new TableReference(upvalues.getExpression(B), r.getKExpression(C, line))));
            case GETTABUP54 ->
                    operations.add(new RegisterSet(line, A, new TableReference(upvalues.getExpression(B), f.getConstantExpression(C))));
            case GETTABLE ->
                    operations.add(new RegisterSet(line, A, new TableReference(r.getExpression(B, line), r.getKExpression(C, line))));
            case GETTABLE54 ->
                    operations.add(new RegisterSet(line, A, new TableReference(r.getExpression(B, line), r.getExpression(C, line))));
            case GETI ->
                    operations.add(new RegisterSet(line, A, new TableReference(r.getExpression(B, line), ConstantExpression.createInteger(C))));
            case GETFIELD ->
                    operations.add(new RegisterSet(line, A, new TableReference(r.getExpression(B, line), f.getConstantExpression(C))));
            case SETTABLE ->
                    operations.add(new TableSet(line, r.getExpression(A, line), r.getKExpression(B, line), r.getKExpression(C, line), true, line));
            case SETTABLE54 ->
                    operations.add(new TableSet(line, r.getExpression(A, line), r.getExpression(B, line), r.getKExpression54(C, code.k(line), line), true, line));
            case SETI ->
                    operations.add(new TableSet(line, r.getExpression(A, line), ConstantExpression.createInteger(B), r.getKExpression54(C, code.k(line), line), true, line));
            case SETFIELD ->
                    operations.add(new TableSet(line, r.getExpression(A, line), f.getConstantExpression(B), r.getKExpression54(C, code.k(line), line), true, line));
            case SETTABUP ->
                    operations.add(new TableSet(line, upvalues.getExpression(A), r.getKExpression(B, line), r.getKExpression(C, line), true, line));
            case SETTABUP54 ->
                    operations.add(new TableSet(line, upvalues.getExpression(A), f.getConstantExpression(B), r.getKExpression54(C, code.k(line), line), true, line));
            case NEWTABLE50 ->
                    operations.add(new RegisterSet(line, A, new TableLiteral(fb2int50(B), C == 0 ? 0 : 1 << C)));
            case NEWTABLE -> operations.add(new RegisterSet(line, A, new TableLiteral(fb2int(B), fb2int(C))));
            case NEWTABLE54 -> {
                if (code.op(line + 1) != Op.EXTRAARG) throw new IllegalStateException();
                var arraySize = C;
                if (code.k(line)) {
                    arraySize += code.Ax(line + 1) * (code.getExtractor().C.max() + 1);
                }
                operations.add(new RegisterSet(line, A, new TableLiteral(arraySize, B == 0 ? 0 : (1 << (B - 1)))));
            }
            case SELF -> {
                // We can later determine if : syntax was used by comparing subexpressions with ==
                var common = r.getExpression(B, line);
                operations.add(new RegisterSet(line, A + 1, common));
                operations.add(new RegisterSet(line, A, new TableReference(common, r.getKExpression(C, line))));
            }
            case SELF54 -> {
                // We can later determine if : syntax was used by comparing subexpressions with ==
                var common = r.getExpression(B, line);
                operations.add(new RegisterSet(line, A + 1, common));
                operations.add(new RegisterSet(line, A, new TableReference(common, r.getKExpression54(C, code.k(line), line))));
            }
            case ADD -> handle50BinOp(operations, state, line, Expression.BinaryOperation.ADD);
            case SUB -> handle50BinOp(operations, state, line, Expression.BinaryOperation.SUB);
            case MUL -> handle50BinOp(operations, state, line, Expression.BinaryOperation.MUL);
            case DIV -> handle50BinOp(operations, state, line, Expression.BinaryOperation.DIV);
            case IDIV -> handle50BinOp(operations, state, line, Expression.BinaryOperation.IDIV);
            case MOD -> handle50BinOp(operations, state, line, Expression.BinaryOperation.MOD);
            case POW -> handle50BinOp(operations, state, line, Expression.BinaryOperation.POW);
            case BAND -> handle50BinOp(operations, state, line, Expression.BinaryOperation.BAND);
            case BOR -> handle50BinOp(operations, state, line, Expression.BinaryOperation.BOR);
            case BXOR -> handle50BinOp(operations, state, line, Expression.BinaryOperation.BXOR);
            case SHL -> handle50BinOp(operations, state, line, Expression.BinaryOperation.SHL);
            case SHR -> handle50BinOp(operations, state, line, Expression.BinaryOperation.SHR);
            case ADD54 -> handle54BinOp(operations, state, line, Expression.BinaryOperation.ADD);
            case SUB54 -> handle54BinOp(operations, state, line, Expression.BinaryOperation.SUB);
            case MUL54 -> handle54BinOp(operations, state, line, Expression.BinaryOperation.MUL);
            case DIV54 -> handle54BinOp(operations, state, line, Expression.BinaryOperation.DIV);
            case IDIV54 -> handle54BinOp(operations, state, line, Expression.BinaryOperation.IDIV);
            case MOD54 -> handle54BinOp(operations, state, line, Expression.BinaryOperation.MOD);
            case POW54 -> handle54BinOp(operations, state, line, Expression.BinaryOperation.POW);
            case BAND54 -> handle54BinOp(operations, state, line, Expression.BinaryOperation.BAND);
            case BOR54 -> handle54BinOp(operations, state, line, Expression.BinaryOperation.BOR);
            case BXOR54 -> handle54BinOp(operations, state, line, Expression.BinaryOperation.BXOR);
            case SHL54 -> handle54BinOp(operations, state, line, Expression.BinaryOperation.SHL);
            case SHR54 -> handle54BinOp(operations, state, line, Expression.BinaryOperation.SHR);
            case ADDI -> {
                if (line + 1 > code.length || code.op(line + 1) != Op.MMBINI) throw new IllegalStateException();
                var op = decodeBinOp(code.C(line + 1));
                var immediate = code.sC(line);
                var swap = false;
                if (code.k(line + 1)) {
                    if (op != Expression.BinaryOperation.ADD) {
                        throw new IllegalStateException();
                    }
                    swap = true;
                } else {
                    if (op == Expression.BinaryOperation.ADD) {
                        // do nothing
                    } else if (op == Expression.BinaryOperation.SUB) {
                        immediate = -immediate;
                    } else {
                        throw new IllegalStateException();
                    }
                }
                var left = r.getExpression(B, line);
                Expression right = ConstantExpression.createInteger(immediate);
                if (swap) {
                    var temp = left;
                    left = right;
                    right = temp;
                }
                operations.add(new RegisterSet(line, A, Expression.make(op, left, right)));
            }
            case ADDK -> handle54BinKOp(operations, state, line, Expression.BinaryOperation.ADD);
            case SUBK -> handle54BinKOp(operations, state, line, Expression.BinaryOperation.SUB);
            case MULK -> handle54BinKOp(operations, state, line, Expression.BinaryOperation.MUL);
            case DIVK -> handle54BinKOp(operations, state, line, Expression.BinaryOperation.DIV);
            case IDIVK -> handle54BinKOp(operations, state, line, Expression.BinaryOperation.IDIV);
            case MODK -> handle54BinKOp(operations, state, line, Expression.BinaryOperation.MOD);
            case POWK -> handle54BinKOp(operations, state, line, Expression.BinaryOperation.POW);
            case BANDK -> handle54BinKOp(operations, state, line, Expression.BinaryOperation.BAND);
            case BORK -> handle54BinKOp(operations, state, line, Expression.BinaryOperation.BOR);
            case BXORK -> handle54BinKOp(operations, state, line, Expression.BinaryOperation.BXOR);
            case SHRI -> {
                if (line + 1 > code.length || code.op(line + 1) != Op.MMBINI) throw new IllegalStateException();
                var immediate = code.sC(line);
                var op = decodeBinOp(code.C(line + 1));
                if (op == Expression.BinaryOperation.SHR) {
                    // okay
                } else if (op == Expression.BinaryOperation.SHL) {
                    immediate = -immediate;
                } else {
                    throw new IllegalStateException();
                }
                operations.add(new RegisterSet(line, A, Expression.make(op, r.getExpression(B, line), ConstantExpression.createInteger(immediate))));
            }
            case SHLI -> operations.add(new RegisterSet(line, A, Expression.make(Expression.BinaryOperation.SHL, ConstantExpression.createInteger(code.sC(line)), r.getExpression(B, line))));
            case MMBIN, MMBINI, MMBINK -> {
            }
            /* Do nothing ... handled with preceding operation. */
            case UNM -> handleUnaryOp(operations, state, line, Expression.UnaryOperation.UNM);
            case NOT -> handleUnaryOp(operations, state, line, Expression.UnaryOperation.NOT);
            case LEN -> handleUnaryOp(operations, state, line, Expression.UnaryOperation.LEN);
            case BNOT -> handleUnaryOp(operations, state, line, Expression.UnaryOperation.BNOT);
            case CONCAT -> {
                var value = r.getExpression(C, line);
                //Remember that CONCAT is right associative.
                while (C-- > B) {
                    value = Expression.make(Expression.BinaryOperation.CONCAT, r.getExpression(C, line), value);
                }
                operations.add(new RegisterSet(line, A, value));
            }
            case CONCAT54 -> {
                if (B < 2) throw new IllegalStateException();
                B--;
                var value = r.getExpression(A + B, line);
                while (B-- > 0) {
                    value = Expression.make(Expression.BinaryOperation.CONCAT, r.getExpression(A + B, line), value);
                }
                operations.add(new RegisterSet(line, A, value));
            }
            case JMP, JMP52, JMP54, EQ, LT, LE, EQ54, LT54, LE54, EQK, EQI, LTI, LEI, GTI, GEI, TEST, TEST54 -> {
            }
            /* Do nothing ... handled with branches */
            case TEST50 -> {
                if (getNoDebug() && A != B) {
                    operations.add(new RegisterSet(line, A, Expression.make(Expression.BinaryOperation.OR, r.getExpression(B, line), initialExpression(state, A, line))));
                }
            }
            case TESTSET, TESTSET54 -> {
                if (getNoDebug()) {
                    operations.add(new RegisterSet(line, A, Expression.make(Expression.BinaryOperation.OR, r.getExpression(B, line), initialExpression(state, A, line))));
                }
            }
            case CALL -> {
                var multiple = (C >= 3 || C == 0);
                if (B == 0) B = registers - A;
                if (C == 0) C = registers - A + 1;
                var function = r.getExpression(A, line);
                var arguments = new Expression[B - 1];
                for (var register = A + 1; register <= A + B - 1; register++) {
                    arguments[register - A - 1] = r.getExpression(register, line);
                }
                var value = new FunctionCall(function, arguments, multiple);
                if (C == 1) {
                    operations.add(new CallOperation(line, value));
                } else {
                    if (C == 2 && !multiple) {
                        operations.add(new RegisterSet(line, A, value));
                    } else {
                        operations.add(new MultipleRegisterSet(line, A, A + C - 2, value));
                    }
                }
            }
            case TAILCALL, TAILCALL54 -> {
                if (B == 0) B = registers - A;
                var function = r.getExpression(A, line);
                var arguments = new Expression[B - 1];
                for (var register = A + 1; register <= A + B - 1; register++) {
                    arguments[register - A - 1] = r.getExpression(register, line);
                }
                var value = new FunctionCall(function, arguments, true);
                operations.add(new ReturnOperation(line, value));
                skip[line + 1] = true;
            }
            case RETURN, RETURN54 -> {
                if (B == 0) B = registers - A + 1;
                var values = new Expression[B - 1];
                for (var register = A; register <= A + B - 2; register++) {
                    values[register - A] = r.getExpression(register, line);
                }
                operations.add(new ReturnOperation(line, values));
            }
            case RETURN0 -> operations.add(new ReturnOperation(line, new Expression[0]));
            case RETURN1 -> operations.add(new ReturnOperation(line, new Expression[]{r.getExpression(A, line)}));
            case FORLOOP, FORLOOP54, FORPREP, FORPREP54, TFORPREP, TFORPREP54, TFORCALL, TFORCALL54, TFORLOOP, TFORLOOP52, TFORLOOP54 -> {
            }
            /* Do nothing ... handled with branches */
            case SETLIST50 -> {
                handleSetList(operations, state, line, A, 1 + Bx % 32, Bx - Bx % 32);
            }
            case SETLISTO -> {
                handleSetList(operations, state, line, A, registers - A - 1, Bx - Bx % 32);
            }
            case SETLIST -> {
                if (C == 0) {
                    C = code.codepoint(line + 1);
                    skip[line + 1] = true;
                }
                if (B == 0) {
                    B = registers - A - 1;
                }
                handleSetList(operations, state, line, A, B, (C - 1) * 50);
            }
            case SETLIST52 -> {
                if (C == 0) {
                    if (line + 1 > code.length || code.op(line + 1) != Op.EXTRAARG) throw new IllegalStateException();
                    C = code.Ax(line + 1);
                    skip[line + 1] = true;
                }
                if (B == 0) {
                    B = registers - A - 1;
                }
                handleSetList(operations, state, line, A, B, (C - 1) * 50);
            }
            case SETLIST54 -> {
                if (code.k(line)) {
                    if (line + 1 > code.length || code.op(line + 1) != Op.EXTRAARG) throw new IllegalStateException();
                    C += code.Ax(line + 1) * (code.getExtractor().C.max() + 1);
                    skip[line + 1] = true;
                }
                if (B == 0) {
                    B = registers - A - 1;
                }
                handleSetList(operations, state, line, A, B, C);
            }
            case TBC -> r.getDeclaration(A, line).tbc = true;
            case CLOSE -> {
            }
            case CLOSURE -> {
                var f = functions[Bx];
                operations.add(new RegisterSet(line, A, new ClosureExpression(f, line + 1)));
                if (function.header.version.upvaluedeclarationtype.get() == Version.UpvalueDeclarationType.INLINE) {
                    // Handle upvalue declarations
                    for (var i = 0; i < f.numUpvalues; i++) {
                        var upvalue = f.upvalues[i];
                        switch (code.op(line + 1 + i)) {
                            case MOVE -> upvalue.instack = true;
                            case GETUPVAL -> upvalue.instack = false;
                            default -> throw new IllegalStateException();
                        }
                        upvalue.idx = code.B(line + 1 + i);
                        skip[line + 1 + i] = true;
                    }
                }
            }
            case VARARGPREP -> {
            }
            /* Do nothing ... internal operation */
            case VARARG -> {
                var multiple = (B != 2);
                if (B == 1) throw new IllegalStateException();
                if (B == 0) B = registers - A + 1;
                Expression value = new Vararg(multiple);
                operations.add(new MultipleRegisterSet(line, A, A + B - 2, value));
            }
            case VARARG54 -> {
                var multiple = (C != 2);
                if (C == 1) throw new IllegalStateException();
                if (C == 0) C = registers - A + 1;
                Expression value = new Vararg(multiple);
                operations.add(new MultipleRegisterSet(line, A, A + C - 2, value));
            }
            case EXTRAARG, EXTRABYTE -> {
            }
            /* Do nothing ... handled by previous instruction */
            case DEFAULT, DEFAULT54 -> throw new IllegalStateException();
        }
        return operations;
    }

    private Expression initialExpression(State state, int register, int line) {
        if (line == 1) {
            if (register < function.numParams) throw new IllegalStateException();
            return ConstantExpression.createNil(line);
        } else {
            return state.r.getExpression(register, line - 1);
        }
    }

    private Assignment processOperation(State state, Operation operation, int line, int nextLine, Block block) {
        var r = state.r;
        var skip = state.skip;
        Assignment assign = null;
        var stmts = operation.process(r, block);
        if (stmts.size() == 1) {
            var stmt = stmts.get(0);
            if (stmt instanceof Assignment) {
                assign = (Assignment) stmt;
            }
            //System.out.println("-- added statemtent @" + line);
            if (assign != null) {
                var declare = false;
                for (var newLocal : r.getNewLocals(line, block.closeRegister)) {
                    if (assign.getFirstTarget().isDeclaration(newLocal)) {
                        declare = true;
                        break;
                    }
                }
                //System.out.println("-- checking for multiassign @" + nextLine);
                while (!declare && nextLine < block.end) {
                    var op = code.op(nextLine);
                    if (isMoveIntoTarget(r, nextLine)) {
                        //System.out.println("-- found multiassign @" + nextLine);
                        var target = getMoveIntoTargetTarget(r, nextLine, line + 1);
                        var value = getMoveIntoTargetValue(r, nextLine, line + 1); //updated?
                        assign.addFirst(target, value, nextLine);
                        skip[nextLine] = true;
                        nextLine++;
                    } else if (op == Op.MMBIN || op == Op.MMBINI || op == Op.MMBINK || code.isUpvalueDeclaration(nextLine)) {
                        // skip
                        nextLine++;
                    } else {
                        break;
                    }
                }
            }
        }
        for (var stmt : stmts) {
            block.addStatement(stmt);
        }
        return assign;
    }

    public boolean hasStatement(int begin, int end) {
        if (begin <= end) {
            var state = new State();
            state.r = new Registers(registers, length, declList, f, getNoDebug());
            state.outer = new OuterBlock(function, code.length);
            Block scoped = new DoEndBlock(function, begin, end + 1);
            state.labels = new boolean[code.length + 1];
            var blocks = Arrays.asList(state.outer, scoped);
            processSequence(state, blocks, code.length);
            return !scoped.isEmpty();
        } else {
            return false;
        }
    }

    private void processSequence(State state, List<Block> blocks, int end) {
        var r = state.r;
        var blockContainerIndex = 0;
        var blockStatementIndex = 0;
        List<Block> blockContainers = new ArrayList<>(blocks.size());
        List<Block> blockStatements = new ArrayList<>(blocks.size());
        for (var block : blocks) {
            if (block.isContainer()) {
                blockContainers.add(block);
            } else {
                blockStatements.add(block);
            }
        }
        var blockStack = new Stack<Block>();
        blockStack.push(blockContainers.get(blockContainerIndex++));

        state.skip = new boolean[code.length + 1];
        var skip = state.skip;
        var labels_handled = new boolean[code.length + 1];

        var line = 1;
        while (true) {
            var nextline = line;
            List<Operation> operations = null;
            List<Declaration> prevLocals = null;
            List<Declaration> newLocals = null;

            // Handle container blocks
            if (blockStack.peek().end <= line) {
                var endingBlock = blockStack.pop();
                var operation = endingBlock.process(this);
                if (blockStack.isEmpty()) return;
                if (operation == null) throw new IllegalStateException();
                operations = List.of(operation);
                prevLocals = r.getNewLocals(line - 1);
            } else {
                var locals = r.getNewLocals(line, blockStack.peek().closeRegister);
                while (blockContainerIndex < blockContainers.size() && blockContainers.get(blockContainerIndex).begin <= line) {
                    var next = blockContainers.get(blockContainerIndex++);
                    if (!locals.isEmpty() && next.allowsPreDeclare() &&
                        (locals.get(0).end > next.scopeEnd() || locals.get(0).register < next.closeRegister)
                    ) {
                        var declaration = new Assignment();
                        var declareEnd = locals.get(0).end;
                        declaration.declare(locals.get(0).begin);
                        while (!locals.isEmpty() && locals.get(0).end == declareEnd && (next.closeRegister == -1 || locals.get(0).register < next.closeRegister)) {
                            var decl = locals.get(0);
                            declaration.addLast(new VariableTarget(decl), ConstantExpression.createNil(line), line);
                            locals.remove(0);
                        }
                        blockStack.peek().addStatement(declaration);
                    }

                    if (!next.hasHeader()) {
                        if (!labels_handled[line] && state.labels[line]) {
                            blockStack.peek().addStatement(new Label(line));
                            labels_handled[line] = true;
                        }
                    }

                    blockStack.push(next);
                }

                if (!labels_handled[line] && state.labels[line]) {
                    blockStack.peek().addStatement(new Label(line));
                    labels_handled[line] = true;
                }

            }

            var block = blockStack.peek();

            r.startLine(line);

            // Handle other sources of operations (after pushing any new container block)
            if (operations == null) {
                if (blockStatementIndex < blockStatements.size() && blockStatements.get(blockStatementIndex).begin <= line) {
                    var blockStatement = blockStatements.get(blockStatementIndex++);
                    var operation = blockStatement.process(this);
                    operations = Collections.singletonList(operation);
                } else {
                    // After all blocks are handled for a line, we will reach here
                    nextline = line + 1;
                    if (!skip[line] && line >= 1 && line <= end) {
                        operations = processLine(state, line);
                    } else {
                        operations = Collections.emptyList();
                    }
                    if (line >= 1 && line <= end) {
                        newLocals = r.getNewLocals(line, block.closeRegister);
                    }
                }
            }

            // Need to capture the assignment (if any) to attach local variable declarations
            Assignment assignment = null;

            for (var operation : operations) {
                var operationAssignment = processOperation(state, operation, line, nextline, block);
                if (operationAssignment != null) {
                    assignment = operationAssignment;
                }
            }

            // Some declarations may be swallowed by assignment blocks.
            // These are restored via prevLocals
            var locals = newLocals;
            if (assignment != null && prevLocals != null) {
                locals = prevLocals;
            }
            if (locals != null && !locals.isEmpty()) {
                var scopeEnd = -1;
                if (assignment == null) {
                    // Create a new Assignment to hold the declarations
                    assignment = new Assignment();
                    block.addStatement(assignment);
                } else {
                    for (var decl : locals) {
                        if (assignment.assigns(decl)) {
                            scopeEnd = decl.end;
                            break;
                        }
                    }
                }

                assignment.declare(locals.get(0).begin);
                for (var decl : locals) {
                    if ((scopeEnd == -1 || decl.end == scopeEnd) && decl.register >= block.closeRegister) {
                        assignment.addLast(new VariableTarget(decl), r.getValue(decl.register, line + 1), r.getUpdated(decl.register, line - 1));
                    }
                }
            }

            line = nextline;
        }
    }

    private boolean isMoveIntoTarget(Registers r, int line) {
        if (code.isUpvalueDeclaration(line)) return false;
        switch (code.op(line)) {
            case MOVE -> {
                return r.isAssignable(code.A(line), line) && !r.isLocal(code.B(line), line);
            }
            case SETUPVAL, SETGLOBAL -> {
                return !r.isLocal(code.A(line), line);
            }
            case SETTABLE, SETTABUP -> {
                var C = code.C(line);
                if (f.isConstant(C)) {
                    return false;
                } else {
                    return !r.isLocal(C, line);
                }
            }
            case SETTABLE54, SETI, SETFIELD, SETTABUP54 -> {
                if (code.k(line)) {
                    return false;
                } else {
                    return !r.isLocal(code.C(line), line);
                }
            }
            default -> {
                return false;
            }
        }
    }

    private Target getMoveIntoTargetTarget(Registers r, int line, int previous) {
        switch (code.op(line)) {
            case MOVE -> {
                return r.getTarget(code.A(line), line);
            }
            case SETUPVAL -> {
                return new UpvalueTarget(upvalues.getName(code.B(line)));
            }
            case SETGLOBAL -> {
                return new GlobalTarget(f.getGlobalName(code.Bx(line)));
            }
            case SETTABLE -> {
                return new TableTarget(r.getExpression(code.A(line), previous), r.getKExpression(code.B(line), previous));
            }
            case SETTABLE54 -> {
                return new TableTarget(r.getExpression(code.A(line), previous), r.getExpression(code.B(line), previous));
            }
            case SETI -> {
                return new TableTarget(r.getExpression(code.A(line), previous), ConstantExpression.createInteger(code.B(line)));
            }
            case SETFIELD -> {
                return new TableTarget(r.getExpression(code.A(line), previous), f.getConstantExpression(code.B(line)));
            }
            case SETTABUP -> {
                var A = code.A(line);
                var B = code.B(line);
                return new TableTarget(upvalues.getExpression(A), r.getKExpression(B, previous));
            }
            case SETTABUP54 -> {
                var A = code.A(line);
                var B = code.B(line);
                return new TableTarget(upvalues.getExpression(A), f.getConstantExpression(B));
            }
            default -> throw new IllegalStateException();
        }
    }

    private Expression getMoveIntoTargetValue(Registers r, int line, int previous) {
        var A = code.A(line);
        var B = code.B(line);
        var C = code.C(line);
        switch (code.op(line)) {
            case MOVE -> {
                return r.getValue(B, previous);
            }
            case SETUPVAL, SETGLOBAL -> {
                return r.getExpression(A, previous);
            }
            case SETTABLE, SETTABUP -> {
                if (f.isConstant(C)) {
                    throw new IllegalStateException();
                } else {
                    return r.getExpression(C, previous);
                }
            }
            case SETTABLE54, SETI, SETFIELD, SETTABUP54 -> {
                if (code.k(line)) {
                    throw new IllegalStateException();
                } else {
                    return r.getExpression(C, previous);
                }
            }
            default -> throw new IllegalStateException();
        }
    }

    public static class State {
        private Registers r;
        private boolean[] skip;
        private Block outer;
        private boolean[] labels;
    }

}