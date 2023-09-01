package me.hydos.unluac.decompile;

import me.hydos.unluac.Version;
import me.hydos.unluac.bytecode.BFunction;

import java.util.Objects;

public enum Op {
    // Lua 5.0 Opcodes
    NEWTABLE50("newtable", OpVer.LUA50, OperandFormat.AR, OperandFormat.B, OperandFormat.C),
    SETLIST50("setlist", OpVer.LUA50, OperandFormat.AR, OperandFormat.Bx),
    SETLISTO("setlisto", OpVer.LUA50, OperandFormat.AR, OperandFormat.Bx),
    TFORPREP("tforprep", OpVer.LUA50, OperandFormat.AR, OperandFormat.sBxJ),
    TEST50("test", OpVer.LUA50, OperandFormat.AR, OperandFormat.BR, OperandFormat.C),
    // Lua 5.1 Opcodes
    MOVE("move", OpVer.LUA50 | OpVer.LUA51 | OpVer.LUA52 | OpVer.LUA53 | OpVer.LUA54, OperandFormat.AR, OperandFormat.BR),
    LOADK("loadk", OpVer.LUA50 | OpVer.LUA51 | OpVer.LUA52 | OpVer.LUA53 | OpVer.LUA54, OperandFormat.AR, OperandFormat.BxK),
    LOADBOOL("loadbool", OpVer.LUA50 | OpVer.LUA51 | OpVer.LUA52 | OpVer.LUA53, OperandFormat.AR, OperandFormat.B, OperandFormat.C),
    LOADNIL("loadnil", OpVer.LUA50 | OpVer.LUA51, OperandFormat.AR, OperandFormat.BR),
    GETUPVAL("getupval", OpVer.LUA50 | OpVer.LUA51 | OpVer.LUA52 | OpVer.LUA53 | OpVer.LUA54, OperandFormat.AR, OperandFormat.BU),
    GETGLOBAL("getglobal", OpVer.LUA50 | OpVer.LUA51, OperandFormat.AR, OperandFormat.BxK),
    GETTABLE("gettable", OpVer.LUA50 | OpVer.LUA51 | OpVer.LUA52 | OpVer.LUA53, OperandFormat.AR, OperandFormat.BR, OperandFormat.CRK),
    SETGLOBAL("setglobal", OpVer.LUA50 | OpVer.LUA51, OperandFormat.AR, OperandFormat.BxK),
    SETUPVAL("setupval", OpVer.LUA50 | OpVer.LUA51 | OpVer.LUA52 | OpVer.LUA53 | OpVer.LUA54, OperandFormat.AR, OperandFormat.BU),
    SETTABLE("settable", OpVer.LUA50 | OpVer.LUA51 | OpVer.LUA52 | OpVer.LUA53, OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
    NEWTABLE("newtable", OpVer.LUA51 | OpVer.LUA52 | OpVer.LUA53, OperandFormat.AR, OperandFormat.B, OperandFormat.C),
    SELF("self", OpVer.LUA50 | OpVer.LUA51 | OpVer.LUA52 | OpVer.LUA53, OperandFormat.AR, OperandFormat.BR, OperandFormat.CRK),
    ADD("add", OpVer.LUA50 | OpVer.LUA51 | OpVer.LUA52 | OpVer.LUA53, OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
    SUB("sub", OpVer.LUA50 | OpVer.LUA51 | OpVer.LUA52 | OpVer.LUA53, OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
    MUL("mul", OpVer.LUA50 | OpVer.LUA51 | OpVer.LUA52 | OpVer.LUA53, OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
    DIV("div", OpVer.LUA50 | OpVer.LUA51 | OpVer.LUA52 | OpVer.LUA53, OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
    MOD("mod", OpVer.LUA51 | OpVer.LUA52 | OpVer.LUA53, OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
    POW("pow", OpVer.LUA50 | OpVer.LUA51 | OpVer.LUA52 | OpVer.LUA53, OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
    UNM("unm", OpVer.LUA50 | OpVer.LUA51 | OpVer.LUA52 | OpVer.LUA53, OperandFormat.AR, OperandFormat.BR),
    NOT("not", OpVer.LUA50 | OpVer.LUA51 | OpVer.LUA52 | OpVer.LUA53, OperandFormat.AR, OperandFormat.BR),
    LEN("len", OpVer.LUA51 | OpVer.LUA52 | OpVer.LUA53, OperandFormat.AR, OperandFormat.BR),
    CONCAT("concat", OpVer.LUA50 | OpVer.LUA51 | OpVer.LUA52 | OpVer.LUA53, OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
    JMP("jmp", OpVer.LUA50 | OpVer.LUA51, OperandFormat.sBxJ),
    EQ("eq", OpVer.LUA50 | OpVer.LUA51 | OpVer.LUA52 | OpVer.LUA53, OperandFormat.A, OperandFormat.BRK, OperandFormat.CRK),
    LT("lt", OpVer.LUA50 | OpVer.LUA51 | OpVer.LUA52 | OpVer.LUA53, OperandFormat.A, OperandFormat.BRK, OperandFormat.CRK),
    LE("le", OpVer.LUA50 | OpVer.LUA51 | OpVer.LUA52 | OpVer.LUA53, OperandFormat.A, OperandFormat.BRK, OperandFormat.CRK),
    TEST("test", OpVer.LUA51 | OpVer.LUA52 | OpVer.LUA53, OperandFormat.AR, OperandFormat.C),
    TESTSET("testset", OpVer.LUA51 | OpVer.LUA52 | OpVer.LUA53, OperandFormat.AR, OperandFormat.BR, OperandFormat.C),
    CALL("call", OpVer.LUA50 | OpVer.LUA51 | OpVer.LUA52 | OpVer.LUA53 | OpVer.LUA54, OperandFormat.AR, OperandFormat.B, OperandFormat.C),
    TAILCALL("tailcall", OpVer.LUA50 | OpVer.LUA51 | OpVer.LUA52 | OpVer.LUA53, OperandFormat.AR, OperandFormat.B),
    RETURN("return", OpVer.LUA50 | OpVer.LUA51 | OpVer.LUA52 | OpVer.LUA53, OperandFormat.AR, OperandFormat.B),
    FORLOOP("forloop", OpVer.LUA50 | OpVer.LUA51 | OpVer.LUA52 | OpVer.LUA53, OperandFormat.AR, OperandFormat.sBxJ),
    FORPREP("forprep", OpVer.LUA51 | OpVer.LUA52 | OpVer.LUA53, OperandFormat.AR, OperandFormat.sBxJ),
    TFORLOOP("tforloop", OpVer.LUA50 | OpVer.LUA51, OperandFormat.AR, OperandFormat.C),
    SETLIST("setlist", OpVer.LUA51, OperandFormat.AR, OperandFormat.B, OperandFormat.C),
    CLOSE("close", OpVer.LUA50 | OpVer.LUA51 | OpVer.LUA54, OperandFormat.AR),
    CLOSURE("closure", OpVer.LUA50 | OpVer.LUA51 | OpVer.LUA52 | OpVer.LUA53 | OpVer.LUA54, OperandFormat.AR, OperandFormat.BxF),
    VARARG("vararg", OpVer.LUA51 | OpVer.LUA52 | OpVer.LUA53, OperandFormat.AR, OperandFormat.B),
    // Lua 5.2 Opcodes
    JMP52("jmp", OpVer.LUA52 | OpVer.LUA53, OperandFormat.A, OperandFormat.sBxJ),
    LOADNIL52("loadnil", OpVer.LUA52 | OpVer.LUA53 | OpVer.LUA54, OperandFormat.AR, OperandFormat.B),
    LOADKX("loadkx", OpVer.LUA52 | OpVer.LUA53 | OpVer.LUA54, OperandFormat.AR),
    GETTABUP("gettabup", OpVer.LUA52 | OpVer.LUA53, OperandFormat.AR, OperandFormat.BU, OperandFormat.CRK),
    SETTABUP("settabup", OpVer.LUA52 | OpVer.LUA53, OperandFormat.AU, OperandFormat.BRK, OperandFormat.CRK),
    SETLIST52("setlist", OpVer.LUA52 | OpVer.LUA53, OperandFormat.AR, OperandFormat.B, OperandFormat.C),
    TFORCALL("tforcall", OpVer.LUA52 | OpVer.LUA53, OperandFormat.AR, OperandFormat.C),
    TFORLOOP52("tforloop", OpVer.LUA52 | OpVer.LUA53, OperandFormat.AR, OperandFormat.sBxJ),
    EXTRAARG("extraarg", OpVer.LUA52 | OpVer.LUA53 | OpVer.LUA54, OperandFormat.Ax),
    // Lua 5.3 Opcodes
    IDIV("idiv", OpVer.LUA53, OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
    BAND("band", OpVer.LUA53, OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
    BOR("bor", OpVer.LUA53, OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
    BXOR("bxor", OpVer.LUA53, OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
    SHL("shl", OpVer.LUA53, OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
    SHR("shr", OpVer.LUA53, OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
    BNOT("bnot", OpVer.LUA53 | OpVer.LUA54, OperandFormat.AR, OperandFormat.BR),
    // Lua 5.4 Opcodes
    LOADI("loadi", OpVer.LUA54, OperandFormat.AR, OperandFormat.sBxI),
    LOADF("loadf", OpVer.LUA54, OperandFormat.AR, OperandFormat.sBxF),
    LOADFALSE("loadfalse", OpVer.LUA54, OperandFormat.AR),
    LFALSESKIP("lfalseskip", OpVer.LUA54, OperandFormat.AR),
    LOADTRUE("loadtrue", OpVer.LUA54, OperandFormat.AR),
    GETTABUP54("gettabup", OpVer.LUA54, OperandFormat.AR, OperandFormat.BU, OperandFormat.CKS),
    GETTABLE54("gettable", OpVer.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CR),
    GETI("geti", OpVer.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CI),
    GETFIELD("getfield", OpVer.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CKS),
    SETTABUP54("settabup", OpVer.LUA54, OperandFormat.AU, OperandFormat.BK, OperandFormat.CRK54),
    SETTABLE54("settable", OpVer.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CRK54),
    SETI("seti", OpVer.LUA54, OperandFormat.AR, OperandFormat.BI, OperandFormat.CRK54),
    SETFIELD("setfield", OpVer.LUA54, OperandFormat.AR, OperandFormat.BKS, OperandFormat.CRK54),
    NEWTABLE54("newtable", OpVer.LUA54, OperandFormat.AR, OperandFormat.B, OperandFormat.C, OperandFormat.k),
    SELF54("self", OpVer.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CRK54),
    ADDI("addi", OpVer.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CsI),
    ADDK("addk", OpVer.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CK),
    SUBK("subk", OpVer.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CK),
    MULK("mulk", OpVer.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CK),
    MODK("modk", OpVer.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CK),
    POWK("powk", OpVer.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CK),
    DIVK("divk", OpVer.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CK),
    IDIVK("idivk", OpVer.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CK),
    BANDK("bandk", OpVer.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CKI),
    BORK("bork", OpVer.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CKI),
    BXORK("bxork", OpVer.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CKI),
    SHRI("shri", OpVer.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CsI),
    SHLI("shli", OpVer.LUA54, OperandFormat.AR, OperandFormat.CsI, OperandFormat.BR),
    ADD54("add", OpVer.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CR),
    SUB54("sub", OpVer.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CR),
    MUL54("mul", OpVer.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CR),
    MOD54("mod", OpVer.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CR),
    POW54("pow", OpVer.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CR),
    DIV54("div", OpVer.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CR),
    IDIV54("idiv", OpVer.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CR),
    BAND54("band", OpVer.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CR),
    BOR54("bor", OpVer.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CR),
    BXOR54("bxor", OpVer.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CR),
    SHL54("shl", OpVer.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CR),
    SHR54("shr", OpVer.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.CR),
    MMBIN("mmbin", OpVer.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.C),
    MMBINI("mmbini", OpVer.LUA54, OperandFormat.AR, OperandFormat.BsI, OperandFormat.C, OperandFormat.k),
    MMBINK("mmbink", OpVer.LUA54, OperandFormat.AR, OperandFormat.BK, OperandFormat.C, OperandFormat.k),
    CONCAT54("concat", OpVer.LUA54, OperandFormat.AR, OperandFormat.B),
    TBC("tbc", OpVer.LUA54, OperandFormat.AR),
    JMP54("jmp", OpVer.LUA54, OperandFormat.sJ),
    EQ54("eq", OpVer.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.k, OperandFormat.C),
    LT54("lt", OpVer.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.k, OperandFormat.C),
    LE54("le", OpVer.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.k, OperandFormat.C),
    EQK("eqk", OpVer.LUA54, OperandFormat.AR, OperandFormat.BK, OperandFormat.k, OperandFormat.C),
    EQI("eqi", OpVer.LUA54, OperandFormat.AR, OperandFormat.BsI, OperandFormat.k, OperandFormat.C),
    LTI("lti", OpVer.LUA54, OperandFormat.AR, OperandFormat.BsI, OperandFormat.k, OperandFormat.C),
    LEI("lei", OpVer.LUA54, OperandFormat.AR, OperandFormat.BsI, OperandFormat.k, OperandFormat.C),
    GTI("gti", OpVer.LUA54, OperandFormat.AR, OperandFormat.BsI, OperandFormat.k, OperandFormat.C),
    GEI("gei", OpVer.LUA54, OperandFormat.AR, OperandFormat.BsI, OperandFormat.k, OperandFormat.C),
    TEST54("test", OpVer.LUA54, OperandFormat.AR, OperandFormat.k),
    TESTSET54("testset", OpVer.LUA54, OperandFormat.AR, OperandFormat.BR, OperandFormat.k),
    TAILCALL54("tailcall", OpVer.LUA54, OperandFormat.AR, OperandFormat.B, OperandFormat.C, OperandFormat.k),
    RETURN54("return", OpVer.LUA54, OperandFormat.AR, OperandFormat.B, OperandFormat.C, OperandFormat.k),
    RETURN0("return0", OpVer.LUA54, OperandFormat.AR, OperandFormat.B, OperandFormat.C, OperandFormat.k),
    RETURN1("return1", OpVer.LUA54, OperandFormat.AR, OperandFormat.B, OperandFormat.C, OperandFormat.k),
    FORLOOP54("forloop", OpVer.LUA54, OperandFormat.AR, OperandFormat.BxJn),
    FORPREP54("forprep", OpVer.LUA54, OperandFormat.AR, OperandFormat.BxJ),
    TFORPREP54("tforprep", OpVer.LUA54, OperandFormat.AR, OperandFormat.BxJ),
    TFORCALL54("tforcall", OpVer.LUA54, OperandFormat.AR, OperandFormat.C),
    TFORLOOP54("tforloop", OpVer.LUA54, OperandFormat.AR, OperandFormat.BxJn),
    SETLIST54("setlist", OpVer.LUA54, OperandFormat.AR, OperandFormat.B, OperandFormat.C, OperandFormat.k),
    VARARG54("vararg", OpVer.LUA54, OperandFormat.AR, OperandFormat.C),
    VARARGPREP("varargprep", OpVer.LUA54, OperandFormat.A),
    // Special
    EXTRABYTE("extrabyte", OpVer.LUA50 | OpVer.LUA51 | OpVer.LUA52 | OpVer.LUA53 | OpVer.LUA54, OperandFormat.x),
    DEFAULT("default", 0, OperandFormat.AR, OperandFormat.BRK, OperandFormat.CRK),
    DEFAULT54("default", 0, OperandFormat.AR, OperandFormat.BR, OperandFormat.C, OperandFormat.k);

    public final String name;
    public final int versions;
    public final OperandFormat[] operands;

    Op(String name, int versions, OperandFormat f1) {
        this.name = name;
        this.versions = versions;
        this.operands = new OperandFormat[]{f1};
    }

    Op(String name, int versions, OperandFormat f1, OperandFormat f2) {
        this.name = name;
        this.versions = versions;
        this.operands = new OperandFormat[]{f1, f2};
    }

    Op(String name, int versions, OperandFormat f1, OperandFormat f2, OperandFormat f3) {
        this.name = name;
        this.versions = versions;
        this.operands = new OperandFormat[]{f1, f2, f3};
    }

    Op(String name, int versions, OperandFormat f1, OperandFormat f2, OperandFormat f3, OperandFormat f4) {
        this.name = name;
        this.versions = versions;
        this.operands = new OperandFormat[]{f1, f2, f3, f4};
    }

    /**
     * {@link Op#SETLIST} sometimes uses an extra byte without tagging it.
     * This means that the value in the extra byte can be detected as any other opcode unless it is recognized.
     */
    public boolean hasExtraByte(int codepoint, BytecodeDecoder ex) {
        if (this == Op.SETLIST) {
            return ex.C.extract(codepoint) == 0;
        } else {
            return false;
        }
    }

    public int jumpField(int codepoint, BytecodeDecoder ex) {
        return switch (this) {
            case FORPREP54, TFORPREP54 -> ex.Bx.extract(codepoint);
            case FORLOOP54, TFORLOOP54 -> -ex.Bx.extract(codepoint);
            case JMP, FORLOOP, FORPREP, JMP52, TFORLOOP52, TFORPREP -> ex.sBx.extract(codepoint);
            case JMP54 -> ex.sJ.extract(codepoint);
            default -> throw new IllegalStateException();
        };
    }

    /**
     * Returns the target register of the instruction at the given
     * line or -1 if the instruction does not have a unique target.
     */
    public int target(int codepoint, BytecodeDecoder ex) {
        switch (this) {
            case MOVE, LOADI, LOADF, LOADK, LOADKX, LOADBOOL, LOADFALSE, LFALSESKIP, LOADTRUE, GETUPVAL, GETTABUP, GETTABUP54, GETGLOBAL, GETTABLE, GETTABLE54, GETI, GETFIELD, NEWTABLE50, NEWTABLE, NEWTABLE54, ADD, SUB, MUL, DIV, IDIV, MOD, POW, BAND, BOR, BXOR, SHL, SHR, ADD54, SUB54, MUL54, DIV54, IDIV54, MOD54, POW54, BAND54, BOR54, BXOR54, SHL54, SHR54, ADDK, SUBK, MULK, DIVK, IDIVK, MODK, POWK, BANDK, BORK, BXORK, ADDI, SHLI, SHRI, UNM, NOT, LEN, BNOT, CONCAT, CONCAT54, CLOSURE, TEST50, TESTSET, TESTSET54 -> {
                return ex.A.extract(codepoint);
            }
            case MMBIN, MMBINI, MMBINK -> {
                return -1; // depends on previous instruction
            }
            case LOADNIL -> {
                if (ex.A.extract(codepoint) == ex.B.extract(codepoint)) {
                    return ex.A.extract(codepoint);
                } else {
                    return -1;
                }
            }
            case LOADNIL52 -> {
                if (ex.B.extract(codepoint) == 0) {
                    return ex.A.extract(codepoint);
                } else {
                    return -1;
                }
            }
            case SETGLOBAL, SETUPVAL, SETTABUP, SETTABUP54, SETTABLE, SETTABLE54, SETI, SETFIELD, JMP, JMP52, JMP54, TAILCALL, TAILCALL54, RETURN, RETURN54, RETURN0, RETURN1, FORLOOP, FORLOOP54, FORPREP, FORPREP54, TFORPREP, TFORPREP54, TFORCALL, TFORCALL54, TFORLOOP, TFORLOOP52, TFORLOOP54, TBC, CLOSE, EXTRAARG, SELF, SELF54, EQ, LT, LE, EQ54, LT54, LE54, EQK, EQI, LTI, LEI, GTI, GEI, TEST, TEST54, SETLIST50, SETLISTO, SETLIST, SETLIST52, SETLIST54, VARARGPREP -> {
                return -1;
            }
            case CALL -> {
                var a = ex.A.extract(codepoint);
                var c = ex.C.extract(codepoint);
                if (c == 2) {
                    return a;
                } else {
                    return -1;
                }
            }
            case VARARG -> {
                var a = ex.A.extract(codepoint);
                var b = ex.B.extract(codepoint);
                if (b == 2) {
                    return a;
                } else {
                    return -1;
                }
            }
            case VARARG54 -> {
                var a = ex.A.extract(codepoint);
                var c = ex.C.extract(codepoint);
                if (c == 2) {
                    return a;
                } else {
                    return -1;
                }
            }
            case EXTRABYTE -> {
                return -1;
            }
            case DEFAULT, DEFAULT54 -> throw new IllegalStateException();
        }
        throw new IllegalStateException(this.name());
    }

    private static String fixedOperand(int field) {
        return Integer.toString(field);
    }

    private static String registerOperand(int field) {
        return "r" + field;
    }

    private static String upvalueOperand(int field) {
        return "u" + field;
    }

    private static String constantOperand(int field) {
        return "k" + field;
    }

    private static String functionOperand(int field) {
        return "f" + field;
    }

    public boolean hasJump() {
        for (OperandFormat operand : operands) {
            var format = operand.format;
            if (format == OperandFormat.Format.JUMP || format == OperandFormat.Format.JUMP_NEGATIVE) {
                return true;
            }
        }
        return false;
    }

    public String codePointToString(int flags, BFunction function, int codepoint, BytecodeDecoder ex, String label, boolean upvalue) {
        return toStringHelper(flags, function, name, operands, codepoint, ex, label, upvalue);
    }

    public static String defaultToString(int flags, BFunction function, int codepoint, Version version, BytecodeDecoder ex, boolean upvalue) {
        return toStringHelper(flags, function, String.format("op%02d", ex.op.extract(codepoint)), version.getDefaultOp().operands, codepoint, ex, null, upvalue);
    }

    private static String toStringHelper(int flags, BFunction function, String name, OperandFormat[] operands, int codepoint, BytecodeDecoder ex, String label, boolean upvalue) {
        var constant = -1;
        var width = 10;
        var b = new StringBuilder();
        b.append(name);
        b.append(" ".repeat(Math.max(0, width - name.length())));
        var parameters = new String[operands.length];
        for (var i = 0; i < operands.length; ++i) {
            var field = switch (operands[i].field) {
                case A -> ex.A;
                case B -> ex.B;
                case C -> ex.C;
                case k -> ex.k;
                case Ax -> ex.Ax;
                case sJ -> ex.sJ;
                case Bx -> ex.Bx;
                case sBx -> ex.sBx;
                case x -> ex.x;
                default -> throw new IllegalStateException();
            };
            var x = field.extract(codepoint);
            switch (operands[i].format) {
                case IMMEDIATE_INTEGER, IMMEDIATE_FLOAT, RAW -> parameters[i] = fixedOperand(x);
                case IMMEDIATE_SIGNED_INTEGER -> parameters[i] = fixedOperand(x - field.max() / 2);
                case REGISTER -> parameters[i] = registerOperand(x);
                case UPVALUE -> parameters[i] = upvalueOperand(x);
                case REGISTER_K -> {
                    if (ex.is_k(x)) {
                        constant = ex.get_k(x);
                        parameters[i] = constantOperand(constant);
                    } else {
                        parameters[i] = registerOperand(x);
                    }
                }
                case REGISTER_K54 -> {
                    if (ex.k.extract(codepoint) != 0) {
                        constant = x;
                        parameters[i] = constantOperand(x);
                    } else {
                        parameters[i] = registerOperand(x);
                    }
                }
                case CONSTANT, CONSTANT_INTEGER, CONSTANT_STRING -> {
                    constant = x;
                    parameters[i] = constantOperand(x);
                }
                case FUNCTION -> parameters[i] = functionOperand(x);
                case JUMP -> {
                    if (label != null) {
                        parameters[i] = label;
                    } else {
                        parameters[i] = fixedOperand(x + operands[i].offset);
                    }
                }
                case JUMP_NEGATIVE -> {
                    parameters[i] = Objects.requireNonNullElseGet(label, () -> fixedOperand(-x));
                }
                default -> throw new IllegalStateException();
            }
        }
        for (var parameter : parameters) {
            b.append(' ');
            b.append(" ".repeat(Math.max(0, 5 - parameter.length())));
            b.append(parameter);
        }
        if (upvalue) {
            b.append(" ; upvalue declaration");
        } else if (function != null && constant >= 0) {
            b.append(" ; ");
            b.append(constantOperand(constant));
            if (constant < function.constants.length) {
                b.append(" = ");
                b.append(function.constants[constant].toPrintString(flags | PrintFlag.SHORT));
            } else {
                b.append(" out of range");
            }
        }
        return b.toString();
    }

    public static class OpVer {
        public static final int LUA50 = 1;
        public static final int LUA51 = 2;
        public static final int LUA52 = 4;
        public static final int LUA53 = 8;
        public static final int LUA54 = 16;
    }
}
