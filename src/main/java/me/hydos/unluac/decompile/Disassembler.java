package me.hydos.unluac.decompile;

import me.hydos.unluac.Version;
import me.hydos.unluac.assemble.Directive;
import me.hydos.unluac.bytecode.BFunction;
import me.hydos.unluac.util.StringUtils;

public class Disassembler {

    private final BFunction function;
    private final BytecodeReader bytecodeReader;
    private final String name;
    private final String parent;

    public Disassembler(BFunction function) {
        this(function, "main", null);
    }

    private Disassembler(BFunction function, String name, String parent) {
        this.function = function;
        this.bytecodeReader = new BytecodeReader(function);
        this.name = name;
        this.parent = parent;
    }

    public void disassemble(Output out) {
        disassemble(out, 0);
    }

    private void disassemble(Output out, int level) {
        final var print_flags = PrintFlag.DISASSEMBLER;
        if (parent == null) {
            out.println(".version\t" + function.header.version.getName());
            out.println();

            for (var directive : function.header.lheader_type.get_directives()) {
                directive.disassemble(out, function.header.lheader);
            }
            out.println();

            if (function.header.opmap != function.header.version.getOpcodeMap()) {
                var opmap = function.header.opmap;
                for (var opcode = 0; opcode < opmap.size(); opcode++) {
                    var op = opmap.get(opcode);
                    if (op != null) {
                        out.println(Directive.OP.token + "\t" + opcode + "\t" + op.name);
                    }
                }
                out.println();
            }
        }

        String fullname;
        if (parent == null) {
            fullname = name;
        } else {
            fullname = parent + "/" + name;
        }
        out.println(".function\t" + fullname);
        out.println();

        for (var directive : function.header.function.get_directives()) {
            directive.disassemble(out, function, print_flags);
        }
        out.println();

        if (function.locals.length > 0) {
            for (var local = 1; local <= function.locals.length; local++) {
                var l = function.locals[local - 1];
                out.println(".local\t" + l.name.toPrintString(print_flags) + "\t" + l.start + "\t" + l.end);
            }
            out.println();
        }

        if (function.upvalues.length > 0) {
            for (var upvalue = 1; upvalue <= function.upvalues.length; upvalue++) {
                var u = function.upvalues[upvalue - 1];
                out.println(".upvalue\t" + StringUtils.toPrintString(u.name) + "\t" + u.idx + "\t" + u.instack);
            }
            out.println();
        }

        if (function.constants.length > 0) {
            for (var constant = 1; constant <= function.constants.length; constant++) {
                out.println(".constant\tk" + (constant - 1) + "\t" + function.constants[constant - 1].toPrintString(print_flags));
            }
            out.println();
        }

        var label = new boolean[function.code.length];
        for (var line = 1; line <= function.code.length; line++) {
            var op = bytecodeReader.op(line);
            if (op != null && op.hasJump()) {
                var target = bytecodeReader.target(line);
                if (target >= 1 && target <= label.length) {
                    label[target - 1] = true;
                }
            }
        }

        var abslineinfoindex = 0;
        var upvalue_count = 0;

        for (var line = 1; line <= function.code.length; line++) {
            if (label[line - 1]) {
                out.println(".label\t" + "l" + line);
            }
            if (function.abslineinfo != null && abslineinfoindex < function.abslineinfo.length && function.abslineinfo[abslineinfoindex].pc == line - 1) {
                var info = function.abslineinfo[abslineinfoindex++];
                out.println(".abslineinfo\t" + info.pc + "\t" + info.line);
            }
            if (line <= function.lines.length) {
                out.print(".line\t" + function.lines[line - 1] + "\t");
            }
            var op = bytecodeReader.op(line);
            String cpLabel = null;
            if (op != null && op.hasJump()) {
                var target = bytecodeReader.target(line);
                if (target >= 1 && target <= bytecodeReader.length) {
                    cpLabel = "l" + target;
                }
            }
            if (op == null) {
                out.println(Op.defaultToString(print_flags, function, bytecodeReader.codepoint(line), function.header.version, bytecodeReader.getDecoder(), upvalue_count > 0));
            } else {
                out.println(op.codePointToString(print_flags, function, bytecodeReader.codepoint(line), bytecodeReader.getDecoder(), cpLabel, upvalue_count > 0));
            }
            if (upvalue_count > 0) {
                upvalue_count--;
            } else {
                if (op == Op.CLOSURE && function.header.version.upvaluedeclarationtype.get() == Version.UpvalueDeclarationType.INLINE) {
                    var f = bytecodeReader.Bx(line);
                    if (f >= 0 && f < function.functions.length) {
                        var closed = function.functions[f];
                        if (closed.numUpvalues > 0) {
                            upvalue_count = closed.numUpvalues;
                        }
                    }
                }
            }
            //out.println("\t" + code.opcode(line) + " " + code.A(line) + " " + code.B(line) + " " + code.C(line) + " " + code.Bx(line) + " " + code.sBx(line) + " " + code.codepoint(line));
        }
        for (var line = function.code.length + 1; line <= function.lines.length; line++) {
            if (function.abslineinfo != null && abslineinfoindex < function.abslineinfo.length && function.abslineinfo[abslineinfoindex].pc == line - 1) {
                var info = function.abslineinfo[abslineinfoindex++];
                out.println(".abslineinfo\t" + info.pc + "\t" + info.line);
            }
            out.println(".line\t" + function.lines[line - 1]);
        }
        if (function.abslineinfo != null) {
            while (abslineinfoindex < function.abslineinfo.length) {
                var info = function.abslineinfo[abslineinfoindex++];
                out.println(".abslineinfo\t" + info.pc + "\t" + info.line);
            }
        }
        out.println();

        var subindex = 0;
        for (var child : function.functions) {
            new Disassembler(child, "f" + subindex, fullname).disassemble(out, level + 1);
            subindex++;
        }
    }

}
