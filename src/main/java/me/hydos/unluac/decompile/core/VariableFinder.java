package me.hydos.unluac.decompile.core;

import java.util.ArrayList;
import java.util.List;

public class VariableFinder {

    static int lc = 0;

    private VariableFinder() {
    }

    private static boolean isConstantReference(Decompiler d, int value) {
        return d.bytecode.header.extractor.is_k(value);
    }

    public static Local[] process(Decompiler d, int args, int registers) {
        var code = d.reader;
        var states = new RegisterStates(registers, code.length());
        var skip = new boolean[code.length()];
        for (var line = 1; line <= code.length(); line++) {
            states.nextLine(line);
            if (skip[line - 1]) continue;
            var A = code.A(line);
            var B = code.B(line);
            var C = code.C(line);
            switch (code.op(line)) {
                case MOVE -> {
                    states.setWritten(A, line);
                    states.setRead(B, line);
                    if (A < B) {
                        states.setLocalWrite(A, A, line);
                    } else if (B < A) {
                        states.setLocalRead(B, line);
                    }
                }
                case LOADK, LOADBOOL, GETUPVAL, GETGLOBAL, NEWTABLE, NEWTABLE50 -> states.setWritten(A, line);
                case LOADNIL -> {
                    var maximum = B;
                    var register = code.A(line);
                    while (register <= maximum) {
                        states.setWritten(register, line);
                        register++;
                    }
                }
                case LOADNIL52 -> {
                    var maximum = A + B;
                    var register = code.A(line);
                    while (register <= maximum) {
                        states.setWritten(register, line);
                        register++;
                    }
                }
                case GETTABLE -> {
                    states.setWritten(A, line);
                    if (!isConstantReference(d, code.B(line))) states.setRead(B, line);
                    if (!isConstantReference(d, code.C(line))) states.setRead(C, line);
                }
                case SETGLOBAL, SETUPVAL -> states.setRead(A, line);
                case SETTABLE, ADD, SUB, MUL, DIV, MOD, POW -> {
                    states.setWritten(A, line);
                    if (!isConstantReference(d, code.B(line))) states.setRead(B, line);
                    if (!isConstantReference(d, code.C(line))) states.setRead(C, line);
                }
                case SELF -> {
                    states.setWritten(A, line);
                    states.setWritten(A + 1, line);
                    states.setRead(B, line);
                    if (!isConstantReference(d, code.C(line))) states.setRead(C, line);
                }
                case UNM, NOT, LEN -> {
                    states.get(code.A(line), line).written = true;
                    states.get(code.B(line), line).read = true;
                }
                case CONCAT -> {
                    states.setWritten(A, line);
                    for (var register = B; register <= C; register++) {
                        states.setRead(register, line);
                        states.setTemporaryRead(register, line);
                    }
                }
                case SETLIST -> states.setTemporaryRead(A + 1, line);
                case JMP, JMP52 -> {
                }
                case EQ, LT, LE -> {
                    if (!isConstantReference(d, code.B(line))) states.setRead(B, line);
                    if (!isConstantReference(d, code.C(line))) states.setRead(C, line);
                }
                case TEST -> states.setRead(A, line);
                case TESTSET -> {
                    states.setWritten(A, line);
                    states.setLocalWrite(A, A, line);
                    states.setRead(B, line);
                }
                case CLOSURE -> {
                    var f = d.bytecode.functions[code.Bx(line)];
                    for (var upvalue : f.upvalues) {
                        if (upvalue.instack) {
                            states.setLocalRead(upvalue.idx, line);
                        }
                    }
                    states.get(code.A(line), line).written = true;
                }
                case CALL, TAILCALL -> {
                    if (code.op(line) != Op.TAILCALL) {
                        if (C >= 2) {
                            for (var register = A; register <= A + C - 2; register++) {
                                states.setWritten(register, line);
                            }
                        }
                    }
                    for (var register = A; register <= A + B - 1; register++) {
                        states.setRead(register, line);
                        states.setTemporaryRead(register, line);
                    }
                    if (C >= 2) {
                        var nline = line + 1;
                        var register = A + C - 2;
                        while (register >= A && nline <= code.length()) {
                            if (code.op(nline) == Op.MOVE && code.B(nline) == register) {
                                states.setWritten(code.A(nline), nline);
                                states.setRead(code.B(nline), nline);
                                states.setLocalWrite(code.A(nline), code.A(nline), nline);
                                skip[nline - 1] = true;
                            }
                            register--;
                            nline++;
                        }
                    }
                }
                case RETURN -> {
                    if (B == 0) B = registers - code.A(line) + 1;
                    for (var register = A; register <= A + B - 2; register++) {
                        states.get(register, line).read = true;
                    }
                }
                default -> {
                }
            }
        }
        for (var register = 0; register < registers; register++) {
            states.setWritten(register, 1);
        }
        for (var line = 1; line <= code.length(); line++) {
            for (var register = 0; register < registers; register++) {
                var s = states.get(register, line);
                if (s.written) {
                    if (s.read_count >= 2 || (line >= 2 && s.read_count == 0)) {
                        states.setLocalWrite(register, register, line);
                    }
                }
            }
        }
        for (var line = 1; line <= code.length(); line++) {
            for (var register = 0; register < registers; register++) {
                var s = states.get(register, line);
                if (s.written && s.temporary) {
                    List<Integer> ancestors = new ArrayList<>();
                    for (var read = 0; read < registers; read++) {
                        var r = states.get(read, line);
                        if (r.read && !r.local) {
                            ancestors.add(read);
                        }
                    }
                    int pline;
                    for (pline = line - 1; pline >= 1; pline--) {
                        var any_written = false;
                        for (var pregister = 0; pregister < registers; pregister++) {
                            if (states.get(pregister, pline).written && ancestors.contains(pregister)) {
                                any_written = true;
                                ancestors.remove((Object) pregister);
                            }
                        }
                        if (!any_written) {
                            break;
                        }
                        for (var pregister = 0; pregister < registers; pregister++) {
                            var a = states.get(pregister, pline);
                            if (a.read && !a.local) {
                                ancestors.add(pregister);
                            }
                        }
                    }
                    for (int ancestor : ancestors) {
                        if (pline >= 1) {
                            states.setLocalRead(ancestor, pline);
                        }
                    }
                }
            }
        }
    /*
    for(int register = 0; register < registers; register++) {
      for(int line = 1; line <= code.length(); line++) {
        RegisterState s = states.get(register, line);
        if(s.written || line == 1) {
          System.out.println("WRITE r:" + register + " l:" + line + " .. " + s.last_read);
          if(s.local) System.out.println("  LOCAL");
          if(s.temporary) System.out.println("  TEMPORARY");
          System.out.println("  READ_COUNT " + s.read_count);
        }
      }
    }
    //*/
        List<Local> declList = new ArrayList<>(registers);
        for (var register = 0; register < registers; register++) {
            var id = "L";
            var local = false;
            var temporary = false;
            var read = 0;
            var written = 0;
            var start = 0;
            if (register < args) {
                local = true;
                id = "A";
            }
            var is_arg = false;
            if (register == args) {
                switch (d.bytecodeVersion.varargtype.get()) {
                    case ARG, HYBRID -> {
                        if ((d.bytecode.vararg & 1) != 0) {
                            local = true;
                            is_arg = true;
                        }
                    }
                    case ELLIPSIS -> {
                    }
                }
            }
            if (!local && !temporary) {
                for (var line = 1; line <= code.length(); line++) {
                    var state = states.get(register, line);
                    if (state.local) {
                        temporary = false;
                        local = true;
                    }
                    if (state.temporary) {
                        start = line + 1;
                        temporary = true;
                    }
                    if (state.read) {
                        written = 0;
                        read++;
                    }
                    if (state.written) {
                        if (written > 0 && read == 0) {
                            temporary = false;
                            local = true;
                        }
                        read = 0;
                        written++;
                    }
                }
            }
            if (!local && !temporary) {
                if (read >= 2 || read == 0 && written != 0) {
                    local = true;
                }
            }
            if (local && temporary) {
                //throw new IllegalStateException();
            }
            if (local) {
                String name;
                if (is_arg) {
                    name = "arg";
                } else {
                    name = id + register + "_" + lc++;
                }
                var decl = new Local(name, start, code.length() + d.bytecodeVersion.outerblockscopeadjustment.get());
                decl.register = register;
                declList.add(decl);
            }
        }
        //DEBUG
    /*
    for(Declaration decl : declList) {
      System.out.println("decl: " + decl.name + " " + decl.begin + " " + decl.end);
    }*/
        return declList.toArray(new Local[0]);
    }

    static class RegisterState {

        int last_written;
        int last_read;
        int read_count;
        boolean temporary;
        boolean local;
        boolean read;
        boolean written;
        public RegisterState() {
            last_written = 1;
            last_read = -1;
            read_count = 0;
            temporary = false;
            local = false;
            read = false;
            written = false;
        }
    }

    static class RegisterStates {

        private final int registers;
        private final int lines;
        private final RegisterState[][] states;

        RegisterStates(int registers, int lines) {
            this.registers = registers;
            this.lines = lines;
            states = new RegisterState[lines][registers];
            for (var line = 0; line < lines; line++) {
                for (var register = 0; register < registers; register++) {
                    states[line][register] = new RegisterState();
                }
            }
        }

        public RegisterState get(int register, int line) {
            return states[line - 1][register];
        }

        public void setWritten(int register, int line) {
            get(register, line).written = true;
            get(register, line + 1).last_written = line;
        }

        public void setRead(int register, int line) {
            get(register, line).read = true;
            get(register, get(register, line).last_written).read_count++;
            get(register, get(register, line).last_written).last_read = line;
        }

        public void setLocalRead(int register, int line) {
            for (var r = 0; r <= register; r++) {
                get(r, get(r, line).last_written).local = true;
            }
        }

        public void setLocalWrite(int register_min, int register_max, int line) {
            for (var r = 0; r < register_min; r++) {
                get(r, get(r, line).last_written).local = true;
            }
            for (var r = register_min; r <= register_max; r++) {
                get(r, line).local = true;
            }
        }

        public void setTemporaryRead(int register, int line) {
            for (var r = register; r < registers; r++) {
                get(r, get(r, line).last_written).temporary = true;
            }
        }

        public void setTemporaryWrite(int register_min, int register_max, int line) {
            for (var r = register_max + 1; r < registers; r++) {
                get(r, get(r, line).last_written).temporary = true;
            }
            for (var r = register_min; r <= register_max; r++) {
                get(r, line).temporary = true;
            }
        }

        public void nextLine(int line) {
            if (line + 1 < lines) {
                for (var r = 0; r < registers; r++) {
                    if (get(r, line).last_written > get(r, line + 1).last_written) {
                        get(r, line + 1).last_written = get(r, line).last_written;
                    }
                }
            }
        }

    }

}
