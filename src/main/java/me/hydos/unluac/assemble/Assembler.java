package me.hydos.unluac.assemble;

import me.hydos.unluac.Configuration;
import me.hydos.unluac.Version;
import me.hydos.unluac.decompile.core.BytecodeDecoder;
import me.hydos.unluac.decompile.core.Op;
import me.hydos.unluac.decompile.core.OpcodeMap;
import me.hydos.unluac.bytecode.*;
import me.hydos.unluac.bytecode.LNumberType.NumberMode;
import me.hydos.unluac.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.*;

class AssemblerLabel {

    public String name;
    public int code_index;

}

class AssemblerConstant {

    public String name;
    public Type type;
    public boolean booleanValue;
    public double numberValue;
    public String stringValue;
    public BigInteger integerValue;
    public long nanValue;
    enum Type {
        NIL,
        BOOLEAN,
        NUMBER,
        INTEGER,
        FLOAT,
        STRING,
        LONGSTRING,
        NAN,
    }
}

class AssemblerAbsLineInfo {

    public int pc;
    public int line;

}

class AssemblerLocal {

    public String name;
    public int begin;
    public int end;

}

class AssemblerUpvalue {

    public String name;
    public int index;
    public boolean instack;

}

class AssemblerFunction {

    public final AssemblerChunk chunk;
    public final AssemblerFunction parent;
    public final String name;
    public final List<AssemblerFunction> children;
    public boolean hasSource;
    public String source;
    public boolean hasLineDefined;
    public int linedefined;
    public boolean hasLastLineDefined;
    public int lastlinedefined;
    public boolean hasMaxStackSize;
    public int maxStackSize;
    public boolean hasNumParams;
    public int numParams;
    public boolean hasVararg;
    public int vararg;
    public final List<AssemblerLabel> labels;
    public final List<AssemblerConstant> constants;
    public final List<AssemblerUpvalue> upvalues;
    public final List<Integer> code;
    public final List<Integer> lines;
    public final List<AssemblerAbsLineInfo> abslineinfo;
    public final List<AssemblerLocal> locals;
    public final List<FunctionFixup> f_fixup;
    public final List<JumpFixup> j_fixup;

    public AssemblerFunction(AssemblerChunk chunk, AssemblerFunction parent, String name) {
        this.chunk = chunk;
        this.parent = parent;
        this.name = name;
        children = new ArrayList<>();

        hasSource = false;
        hasLineDefined = false;
        hasLastLineDefined = false;
        hasMaxStackSize = false;
        hasNumParams = false;
        hasVararg = false;

        labels = new ArrayList<>();
        constants = new ArrayList<>();
        upvalues = new ArrayList<>();
        code = new ArrayList<>();
        lines = new ArrayList<>();
        abslineinfo = new ArrayList<>();
        locals = new ArrayList<>();

        f_fixup = new ArrayList<>();
        j_fixup = new ArrayList<>();
    }

    public AssemblerFunction addChild(String name) {
        var child = new AssemblerFunction(chunk, this, name);
        children.add(child);
        return child;
    }

    public AssemblerFunction getInnerParent(String[] parts, int index) throws AssemblerException {
        if (index + 1 == parts.length) return this;
        for (var child : children) {
            if (child.name.equals(parts[index])) {
                return child.getInnerParent(parts, index + 1);
            }
        }
        throw new AssemblerException("Can't find outer function");
    }

    public void processFunctionDirective(Assembler a, Directive d) throws AssemblerException, IOException {
        switch (d) {
            case SOURCE -> {
                if (hasSource) throw new AssemblerException("Duplicate .source directive");
                hasSource = true;
                source = a.getString();
            }
            case LINEDEFINED -> {
                if (hasLineDefined) throw new AssemblerException("Duplicate .linedefined directive");
                hasLineDefined = true;
                linedefined = a.getInteger();
            }
            case LASTLINEDEFINED -> {
                if (hasLastLineDefined) throw new AssemblerException("Duplicate .lastlinedefined directive");
                hasLastLineDefined = true;
                lastlinedefined = a.getInteger();
            }
            case MAXSTACKSIZE -> {
                if (hasMaxStackSize) throw new AssemblerException("Duplicate .maxstacksize directive");
                hasMaxStackSize = true;
                maxStackSize = a.getInteger();
            }
            case NUMPARAMS -> {
                if (hasNumParams) throw new AssemblerException("Duplicate .numparams directive");
                hasNumParams = true;
                numParams = a.getInteger();
            }
            case IS_VARARG -> {
                if (hasVararg) throw new AssemblerException("Duplicate .is_vararg directive");
                hasVararg = true;
                vararg = a.getInteger();
            }
            case LABEL -> {
                var name = a.getAny();
                var label = new AssemblerLabel();
                label.name = name;
                label.code_index = code.size();
                labels.add(label);
            }
            case CONSTANT -> {
                var name = a.getName();
                var value = a.getAny();
                var constant = new AssemblerConstant();
                constant.name = name;
                if (value.equals("nil")) {
                    constant.type = AssemblerConstant.Type.NIL;
                } else if (value.equals("true")) {
                    constant.type = AssemblerConstant.Type.BOOLEAN;
                    constant.booleanValue = true;
                } else if (value.equals("false")) {
                    constant.type = AssemblerConstant.Type.BOOLEAN;
                    constant.booleanValue = false;
                } else if (value.startsWith("\"")) {
                    constant.type = AssemblerConstant.Type.STRING;
                    constant.stringValue = StringUtils.fromPrintString(value);
                } else if (value.startsWith("L\"")) {
                    constant.type = AssemblerConstant.Type.LONGSTRING;
                    constant.stringValue = StringUtils.fromPrintString(value.substring(1));
                } else if (value.equals("null")) {
                    constant.type = AssemblerConstant.Type.STRING;
                    constant.stringValue = null;
                } else if (value.equals("NaN")) {
                    constant.type = AssemblerConstant.Type.NAN;
                    constant.nanValue = 0;
                } else {
                    try {
                        if (value.startsWith("NaN+") || value.startsWith("NaN-")) {
                            var bits = Long.parseUnsignedLong(value.substring(4), 16);
                            if (bits < 0 || (bits & Double.doubleToRawLongBits(Double.NaN)) != 0) {
                                throw new AssemblerException("Unrecognized NaN value: " + value);
                            }
                            if (value.startsWith("NaN-")) {
                                bits ^= 0x8000000000000000L;
                            }
                            constant.type = AssemblerConstant.Type.NAN;
                            constant.nanValue = bits;
                        } else if (chunk.number != null) { // TODO: better check
                            constant.numberValue = Double.parseDouble(value);
                            constant.type = AssemblerConstant.Type.NUMBER;
                        } else {
                            if (value.contains(".") || value.contains("E") || value.contains("e")) {
                                constant.numberValue = Double.parseDouble(value);
                                constant.type = AssemblerConstant.Type.FLOAT;
                            } else {
                                constant.integerValue = new BigInteger(value);
                                constant.type = AssemblerConstant.Type.INTEGER;
                            }
                        }
                    } catch (NumberFormatException e) {
                        throw new AssemblerException("Unrecognized constant value: " + value);
                    }
                }
                constants.add(constant);
            }
            case LINE -> {
                lines.add(a.getInteger());
            }
            case ABSLINEINFO -> {
                var info = new AssemblerAbsLineInfo();
                info.pc = a.getInteger();
                info.line = a.getInteger();
                abslineinfo.add(info);
            }
            case LOCAL -> {
                var local = new AssemblerLocal();
                local.name = a.getString();
                local.begin = a.getInteger();
                local.end = a.getInteger();
                locals.add(local);
            }
            case UPVALUE -> {
                var upvalue = new AssemblerUpvalue();
                upvalue.name = a.getString();
                upvalue.index = a.getInteger();
                upvalue.instack = a.getBoolean();
                upvalues.add(upvalue);
            }
            default -> throw new IllegalStateException("Unhandled directive: " + d);
        }
    }

    public void processOp(Assembler a, BytecodeDecoder extract, Op op, int opcode) throws AssemblerException, IOException {
        if (!hasMaxStackSize) throw new AssemblerException("Expected .maxstacksize before code");
        if (opcode >= 0 && !extract.op.check(opcode)) throw new IllegalStateException("Invalid opcode: " + opcode);
        var codepoint = opcode >= 0 ? extract.op.encode(opcode) : 0;
        for (var operand : op.operands) {
            var field = switch (operand.field) {
                case A -> extract.A;
                case B -> extract.B;
                case C -> extract.C;
                case k -> extract.k;
                case Ax -> extract.Ax;
                case sJ -> extract.sJ;
                case Bx -> extract.Bx;
                case sBx -> extract.sBx;
                case x -> extract.x;
                default -> throw new IllegalStateException("Unhandled field: " + operand.field);
            };
            int x;
            switch (operand.format) {
                case RAW, IMMEDIATE_INTEGER, IMMEDIATE_FLOAT -> x = a.getInteger();
                case IMMEDIATE_SIGNED_INTEGER -> {
                    x = a.getInteger();
                    x += field.max() / 2;
                }
                case REGISTER -> {
                    x = a.getRegister();
                    //TODO: stack warning
                }
                case REGISTER_K -> {
                    var rk = a.getRegisterK54();
                    x = rk.x;
                    if (rk.constant) {
                        x += chunk.version.rkoffset.get();
                    }
                    //TODO: stack warning
                }
                case REGISTER_K54 -> {
                    var rk = a.getRegisterK54();
                    codepoint |= extract.k.encode(rk.constant ? 1 : 0);
                    x = rk.x;
                }
                case CONSTANT, CONSTANT_INTEGER, CONSTANT_STRING -> {
                    x = a.getConstant();
                }
                case UPVALUE -> {
                    x = a.getUpvalue();
                }
                case FUNCTION -> {
                    var fix = new FunctionFixup();
                    fix.code_index = code.size();
                    fix.function = a.getAny();
                    fix.field = field;
                    f_fixup.add(fix);
                    x = 0;
                }
                case JUMP -> {
                    var fix = new JumpFixup();
                    fix.code_index = code.size();
                    fix.label = a.getAny();
                    fix.field = field;
                    fix.negate = false;
                    j_fixup.add(fix);
                    x = 0;
                }
                case JUMP_NEGATIVE -> {
                    var fix = new JumpFixup();
                    fix.code_index = code.size();
                    fix.label = a.getAny();
                    fix.field = field;
                    fix.negate = true;
                    j_fixup.add(fix);
                    x = 0;
                }
                default -> throw new IllegalStateException("Unhandled operand format: " + operand.format);
            }
            if (!field.check(x)) {
                throw new AssemblerException("Operand " + operand.field + " out of range");
            }
            codepoint |= field.encode(x);
        }
        code.add(codepoint);
    }

    public void fixup(BytecodeDecoder extract) throws AssemblerException {
        for (var fix : f_fixup) {
            int codepoint = code.get(fix.code_index);
            var x = -1;
            for (var f = 0; f < children.size(); f++) {
                var child = children.get(f);
                if (fix.function.equals(child.name)) {
                    x = f;
                    break;
                }
            }
            if (x == -1) {
                throw new AssemblerException("Unknown function: " + fix.function);
            }
            codepoint = fix.field.clear(codepoint);
            codepoint |= fix.field.encode(x);
            code.set(fix.code_index, codepoint);
        }

        for (var fix : j_fixup) {
            int codepoint = code.get(fix.code_index);
            var x = 0;
            var found = false;
            for (var label : labels) {
                if (fix.label.equals(label.name)) {
                    x = label.code_index - fix.code_index - 1;
                    if (fix.negate) x = -x;
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new AssemblerException("Unknown label: " + fix.label);
            }
            codepoint = fix.field.clear(codepoint);
            codepoint |= fix.field.encode(x);
            code.set(fix.code_index, codepoint);
        }

        for (var f : children) {
            f.fixup(extract);
        }
    }

    static class FunctionFixup {

        int code_index;
        String function;
        BytecodeDecoder.Field field;

    }

    static class JumpFixup {

        int code_index;
        String label;
        BytecodeDecoder.Field field;
        boolean negate;

    }

}

class AssemblerChunk {

    public final Set<Directive> processed_directives;
    public final Version version;
    public int format;
    public LHeader.LEndianness endianness;
    public int int_size;
    public BIntegerType integer;
    public int size_t_size;
    public BIntegerType sizeT;
    public int instruction_size;
    public int op_size;
    public int a_size;
    public int b_size;
    public int c_size;
    public Map<Integer, Op> useropmap;
    public boolean number_integral;
    public int number_size;
    public LNumberType number;
    public LNumberType linteger;
    public LNumberType lfloat;
    public AssemblerFunction main;
    public AssemblerFunction current;
    public BytecodeDecoder extract;

    public AssemblerChunk(Version version) {
        this.version = version;
        processed_directives = new HashSet<>();

        main = null;
        current = null;
        extract = null;
    }

    public void processHeaderDirective(Assembler a, Directive d) throws AssemblerException, IOException {
        if (d != Directive.OP && processed_directives.contains(d)) {
            throw new AssemblerException("Duplicate " + d.name() + " directive");
        }
        processed_directives.add(d);
        switch (d) {
            case FORMAT -> format = a.getInteger();
            case ENDIANNESS -> {
                var endiannessName = a.getName();
                switch (endiannessName) {
                    case "LITTLE" -> endianness = LHeader.LEndianness.LITTLE;
                    case "BIG" -> endianness = LHeader.LEndianness.BIG;
                    default -> throw new AssemblerException("Unknown endianness \"" + endiannessName + "\"");
                }
            }
            case INT_SIZE -> {
                int_size = a.getInteger();
                integer = BIntegerType.create50Type(true, int_size, version.allownegativeint.get());
            }
            case SIZE_T_SIZE -> {
                size_t_size = a.getInteger();
                sizeT = BIntegerType.create50Type(false, size_t_size, false);
            }
            case INSTRUCTION_SIZE -> instruction_size = a.getInteger();
            case SIZE_OP -> op_size = a.getInteger();
            case SIZE_A -> a_size = a.getInteger();
            case SIZE_B -> b_size = a.getInteger();
            case SIZE_C -> c_size = a.getInteger();
            case NUMBER_FORMAT -> {
                var numberTypeName = a.getName();
                switch (numberTypeName) {
                    case "integer" -> number_integral = true;
                    case "float" -> number_integral = false;
                    default -> throw new AssemblerException("Unknown number_format \"" + numberTypeName + "\"");
                }
                number_size = a.getInteger();
                number = new LNumberType(number_size, number_integral, NumberMode.MODE_NUMBER);
            }
            case INTEGER_FORMAT -> linteger = new LNumberType(a.getInteger(), true, NumberMode.MODE_INTEGER);
            case FLOAT_FORMAT -> lfloat = new LNumberType(a.getInteger(), false, NumberMode.MODE_FLOAT);
            case OP -> {
                if (useropmap == null) {
                    useropmap = new HashMap<>();
                }
                var opcode = a.getInteger();
                var name = a.getName();
                var op = version.getOpcodeMap().get(name);
                if (op == null) {
                    throw new AssemblerException("Unknown op name \"" + name + "\"");
                }
                useropmap.put(opcode, op);
            }
            default -> throw new IllegalStateException("Unhandled directive: " + d);
        }
    }

    public BytecodeDecoder getCodeExtract() {
        if (extract == null) {
            extract = new BytecodeDecoder(version, op_size, a_size, b_size, c_size);
        }
        return extract;
    }

    public void processNewFunction(Assembler a) throws AssemblerException, IOException {
        var name = a.getName();
        var parts = name.split("/");
        if (main == null) {
            if (parts.length != 1)
                throw new AssemblerException("First (main) function declaration must not have a \"/\" in the name");
            main = new AssemblerFunction(this, null, name);
            current = main;
        } else {
            if (parts.length == 1 || !parts[0].equals(main.name))
                throw new AssemblerException("Function \"" + name + "\" isn't contained in the main function");
            var parent = main.getInnerParent(parts, 1);
            current = parent.addChild(parts[parts.length - 1]);
        }
    }

    public void processFunctionDirective(Assembler a, Directive d) throws AssemblerException, IOException {
        if (current == null) {
            throw new AssemblerException("Misplaced function directive before declaration of any function");
        }
        current.processFunctionDirective(a, d);
    }

    public void processOp(Assembler a, Op op, int opcode) throws AssemblerException, IOException {
        if (current == null) {
            throw new AssemblerException("Misplaced code before declaration of any function");
        }
        current.processOp(a, getCodeExtract(), op, opcode);
    }

    public void fixup() throws AssemblerException {
        main.fixup(getCodeExtract());
    }

    public void write(OutputStream out) throws AssemblerException, IOException {
        var bool = new LBooleanType();
        var string = version.getLStringType();
        var constant = version.getLConstantType();
        var abslineinfo = new LAbsLineInfoType();
        var local = new LLocalType();
        var upvalue = version.getLUpvalueType();
        var function = version.getLFunctionType();
        var extract = getCodeExtract();

        if (integer == null) {
            integer = BIntegerType.create54();
            sizeT = integer;
        }

        var lheader = new LHeader(format, endianness, integer, sizeT, bool, number, linteger, lfloat, string, constant, abslineinfo, local, upvalue, function, extract);
        var header = new BHeader(version, lheader);
        var main = convert_function(header, this.main);
        header = new BHeader(version, lheader, main);

        header.write(out);
    }

    private BFunction convert_function(BHeader header, AssemblerFunction function) {
        int i;
        var code = new int[function.code.size()];
        i = 0;
        for (int codepoint : function.code) {
            code[i++] = codepoint;
        }
        var lines = new int[function.lines.size()];
        i = 0;
        for (int line : function.lines) {
            lines[i++] = line;
        }
        var abslineinfo = new LAbsLineInfo[function.abslineinfo.size()];
        i = 0;
        for (var info : function.abslineinfo) {
            abslineinfo[i++] = new LAbsLineInfo(info.pc, info.line);
        }
        var locals = new LLocal[function.locals.size()];
        i = 0;
        for (var local : function.locals) {
            locals[i++] = new LLocal(convert_string(local.name), new BInteger(local.begin), new BInteger(local.end));
        }
        var constants = new LObject[function.constants.size()];
        i = 0;
        for (var constant : function.constants) {
            LObject object;
            switch (constant.type) {
                case NIL -> object = LNil.NIL;
                case BOOLEAN -> object = constant.booleanValue ? LBoolean.LTRUE : LBoolean.LFALSE;
                case NUMBER -> object = header.number.create(constant.numberValue);
                case INTEGER -> object = header.linteger.create(constant.integerValue);
                case FLOAT -> object = header.lfloat.create(constant.numberValue);
                case STRING -> object = convert_string(constant.stringValue);
                case LONGSTRING -> object = convert_long_string(constant.stringValue);
                case NAN -> {
                    if (header.number != null) {
                        object = header.number.createNaN(constant.nanValue);
                    } else {
                        object = header.lfloat.createNaN(constant.nanValue);
                    }
                }
                default -> throw new IllegalStateException();
            }
            constants[i++] = object;
        }
        var upvalues = new LUpvalue[function.upvalues.size()];
        i = 0;
        for (var upvalue : function.upvalues) {
            var lup = new LUpvalue();
            lup.bname = convert_string(upvalue.name);
            lup.idx = upvalue.index;
            lup.instack = upvalue.instack;
            upvalues[i++] = lup;
        }
        var functions = new BFunction[function.children.size()];
        i = 0;
        for (var f : function.children) {
            functions[i++] = convert_function(header, f);
        }
        return new BFunction(
                header,
                convert_string(function.source),
                function.linedefined,
                function.lastlinedefined,
                code,
                lines,
                abslineinfo,
                locals,
                constants,
                upvalues,
                functions,
                function.maxStackSize,
                function.upvalues.size(),
                function.numParams,
                function.vararg
        );
    }

    private LString convert_string(String string) {
        if (string == null) {
            return LString.NULL;
        } else {
            return new LString(string, '\0');
        }
    }

    private LString convert_long_string(String string) {
        return new LString(string, '\0', true);
    }

}

public class Assembler {

    private final Configuration config;
    private final Tokenizer t;
    private final OutputStream out;
    private Version version;

    public Assembler(Configuration config, InputStream in, OutputStream out) {
        this.config = config;
        t = new Tokenizer(in);
        this.out = out;
    }

    public void assemble() throws AssemblerException, IOException {

        var tok = t.next();
        if (!tok.equals(".version"))
            throw new AssemblerException("First directive must be .version, instead was \"" + tok + "\"");
        tok = t.next();

        int major;
        int minor;
        var parts = tok.split("\\.");
        if (parts.length == 2) {
            try {
                major = Integer.parseInt(parts[0]);
                minor = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                throw new AssemblerException("Unsupported version " + tok);
            }
        } else {
            throw new AssemblerException("Unsupported version " + tok);
        }
        if (major < 0 || major > 0xF || minor < 0 || minor > 0xF) {
            throw new AssemblerException("Unsupported version " + tok);
        }

        version = Version.getVersion(config, major, minor);

        if (version == null) {
            throw new AssemblerException("Unsupported version " + tok);
        }

        Map<String, Op> oplookup = null;
        Map<Op, Integer> opcodelookup = null;

        var chunk = new AssemblerChunk(version);
        var opinit = false;

        while ((tok = t.next()) != null) {
            var d = Directive.lookup.get(tok);
            if (d != null) {
                switch (d.type) {
                    case HEADER -> chunk.processHeaderDirective(this, d);
                    case NEWFUNCTION -> {
                        if (!opinit) {
                            opinit = true;
                            OpcodeMap opmap;
                            if (chunk.useropmap != null) {
                                opmap = new OpcodeMap(chunk.useropmap);
                            } else {
                                opmap = version.getOpcodeMap();
                            }
                            oplookup = new HashMap<>();
                            opcodelookup = new HashMap<>();
                            for (var i = 0; i < opmap.size(); i++) {
                                var op = opmap.get(i);
                                if (op != null) {
                                    oplookup.put(op.name, op);
                                    opcodelookup.put(op, i);
                                }
                            }

                            oplookup.put(Op.EXTRABYTE.name, Op.EXTRABYTE);
                            opcodelookup.put(Op.EXTRABYTE, -1);
                        }
                        chunk.processNewFunction(this);
                    }
                    case FUNCTION -> chunk.processFunctionDirective(this, d);
                    default -> throw new IllegalStateException();
                }

            } else {
                var op = oplookup.get(tok);
                if (op != null) {
                    // TODO:
                    chunk.processOp(this, op, opcodelookup.get(op));
                } else {
                    throw new AssemblerException("Unexpected token \"" + tok + "\"");
                }
            }

        }

        chunk.fixup();

        chunk.write(out);

    }

    String getAny() throws AssemblerException, IOException {
        var s = t.next();
        if (s == null) throw new AssemblerException("Unexcepted end of file");
        return s;
    }

    String getName() throws AssemblerException, IOException {
        var s = t.next();
        if (s == null) throw new AssemblerException("Unexcepted end of file");
        return s;
    }

    String getString() throws AssemblerException, IOException {
        var s = t.next();
        if (s == null) throw new AssemblerException("Unexcepted end of file");
        return StringUtils.fromPrintString(s);
    }

    int getInteger() throws AssemblerException, IOException {
        var s = t.next();
        if (s == null) throw new AssemblerException("Unexcepted end of file");
        int i;
        try {
            i = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new AssemblerException("Excepted number, got \"" + s + "\"");
        }
        return i;
    }

    boolean getBoolean() throws AssemblerException, IOException {
        var s = t.next();
        if (s == null) throw new AssemblerException("Unexcepted end of file");
        boolean b;
        if (s.equals("true")) {
            b = true;
        } else if (s.equals("false")) {
            b = false;
        } else {
            throw new AssemblerException("Expected boolean, got \"" + s + "\"");
        }
        return b;
    }

    int getRegister() throws AssemblerException, IOException {
        var s = t.next();
        if (s == null) throw new AssemblerException("Unexcepted end of file");
        int r;
        if (s.length() >= 2 && s.charAt(0) == 'r') {
            try {
                r = Integer.parseInt(s.substring(1));
            } catch (NumberFormatException e) {
                throw new AssemblerException("Excepted register, got \"" + s + "\"");
            }
        } else {
            throw new AssemblerException("Excepted register, got \"" + s + "\"");
        }
        return r;
    }

    RKInfo getRegisterK54() throws AssemblerException, IOException {
        var s = t.next();
        if (s == null) throw new AssemblerException("Unexcepted end of file");
        var rk = new RKInfo();
        if (s.length() >= 2 && s.charAt(0) == 'r') {
            rk.constant = false;
            try {
                rk.x = Integer.parseInt(s.substring(1));
            } catch (NumberFormatException e) {
                throw new AssemblerException("Excepted register, got \"" + s + "\"");
            }
        } else if (s.length() >= 2 && s.charAt(0) == 'k') {
            rk.constant = true;
            try {
                rk.x = Integer.parseInt(s.substring(1));
            } catch (NumberFormatException e) {
                throw new AssemblerException("Excepted constant, got \"" + s + "\"");
            }
        } else {
            throw new AssemblerException("Excepted register or constant, got \"" + s + "\"");
        }
        return rk;
    }

    int getConstant() throws AssemblerException, IOException {
        var s = t.next();
        if (s == null) throw new AssemblerException("Unexpected end of file");
        int k;
        if (s.length() >= 2 && s.charAt(0) == 'k') {
            try {
                k = Integer.parseInt(s.substring(1));
            } catch (NumberFormatException e) {
                throw new AssemblerException("Excepted constant, got \"" + s + "\"");
            }
        } else {
            throw new AssemblerException("Excepted constant, got \"" + s + "\"");
        }
        return k;
    }

    int getUpvalue() throws AssemblerException, IOException {
        var s = t.next();
        if (s == null) throw new AssemblerException("Unexcepted end of file");
        int u;
        if (s.length() >= 2 && s.charAt(0) == 'u') {
            try {
                u = Integer.parseInt(s.substring(1));
            } catch (NumberFormatException e) {
                throw new AssemblerException("Excepted register, got \"" + s + "\"");
            }
        } else {
            throw new AssemblerException("Excepted register, got \"" + s + "\"");
        }
        return u;
    }

    static class RKInfo {
        int x;
        boolean constant;
    }

}
