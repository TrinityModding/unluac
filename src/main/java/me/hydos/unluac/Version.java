package me.hydos.unluac;

import me.hydos.unluac.decompile.core.Op;
import me.hydos.unluac.decompile.core.OpcodeMap;
import me.hydos.unluac.bytecode.*;

import java.util.HashSet;
import java.util.Set;

public class Version {

    public final Setting<VarArgType> varargtype;
    public final Setting<Boolean> useupvaluecountinheader;
    public final Setting<InstructionFormat> instructionformat;
    public final Setting<Integer> outerblockscopeadjustment;
    public final Setting<Boolean> extendedrepeatscope;
    public final Setting<Boolean> closeinscope;
    public final Setting<CloseSemantics> closesemantics;
    public final Setting<UpvalueDeclarationType> upvaluedeclarationtype;
    public final Setting<Op> fortarget;
    public final Setting<Op> tfortarget;
    public final Setting<WhileFormat> whileformat;
    public final Setting<Boolean> allowpreceedingsemicolon;
    public final Setting<Boolean> usenestinglongstrings;
    public final Setting<String> environmenttable;
    public final Setting<Boolean> useifbreakrewrite;
    public final Setting<Boolean> usegoto;
    public final Setting<Integer> rkoffset;
    public final Setting<Boolean> allownegativeint;
    public final Setting<ListLengthMode> constantslengthmode;
    public final Setting<ListLengthMode> functionslengthmode;
    public final Setting<ListLengthMode> locallengthmode;
    public final Setting<ListLengthMode> upvaluelengthmode;
    private final int major;
    private final int minor;
    private final String name;
    private final Set<String> reservedWords;
    private final LHeaderType lheadertype;
    private final LStringType lstringtype;
    private final LConstantType lconstanttype;
    private final LUpvalueType lupvaluetype;
    private final LFunctionType lfunctiontype;
    private final OpcodeMap opcodemap;
    private final Op defaultop;

    private Version(Configuration config, int major, int minor) {
        HeaderType headertype;
        StringType stringtype;
        ConstantType constanttype;
        UpvalueType upvaluetype;
        FunctionType functiontype;
        OpcodeMapType opcodemap;
        this.major = major;
        this.minor = minor;
        name = major + "." + minor;
        final var luaj = config.luaj;
        if (major == 5 && minor >= 0 && minor <= 4) {
            switch (minor) {
                case 0 -> {
                    varargtype = new Setting<>(VarArgType.ARG);
                    useupvaluecountinheader = new Setting<>(false);
                    headertype = HeaderType.LUA50;
                    stringtype = StringType.LUA50;
                    constanttype = ConstantType.LUA50;
                    upvaluetype = UpvalueType.LUA50;
                    functiontype = FunctionType.LUA50;
                    opcodemap = OpcodeMapType.LUA50;
                    defaultop = Op.DEFAULT;
                    instructionformat = new Setting<>(InstructionFormat.LUA50);
                    outerblockscopeadjustment = new Setting<>(-1);
                    extendedrepeatscope = new Setting<>(true);
                    closeinscope = new Setting<>(true);
                    closesemantics = new Setting<>(CloseSemantics.DEFAULT);
                    upvaluedeclarationtype = new Setting<>(UpvalueDeclarationType.INLINE);
                    fortarget = new Setting<>(Op.FORLOOP);
                    tfortarget = new Setting<>(null);
                    whileformat = new Setting<>(WhileFormat.BOTTOM_CONDITION);
                    allowpreceedingsemicolon = new Setting<>(false);
                    usenestinglongstrings = new Setting<>(true);
                    environmenttable = new Setting<>(null);
                    useifbreakrewrite = new Setting<>(false);
                    usegoto = new Setting<>(false);
                    rkoffset = new Setting<>(250);
                    allownegativeint = new Setting<>(false);
                    constantslengthmode = new Setting<>(ListLengthMode.STRICT);
                    functionslengthmode = new Setting<>(ListLengthMode.STRICT);
                    locallengthmode = new Setting<>(ListLengthMode.STRICT);
                    upvaluelengthmode = new Setting<>(ListLengthMode.STRICT);
                }
                case 1 -> {
                    varargtype = new Setting<>(VarArgType.HYBRID);
                    useupvaluecountinheader = new Setting<>(false);
                    headertype = HeaderType.LUA51;
                    stringtype = StringType.LUA50;
                    constanttype = ConstantType.LUA50;
                    upvaluetype = UpvalueType.LUA50;
                    functiontype = FunctionType.LUA51;
                    opcodemap = OpcodeMapType.LUA51;
                    defaultop = Op.DEFAULT;
                    instructionformat = new Setting<>(InstructionFormat.LUA51);
                    outerblockscopeadjustment = new Setting<>(-1);
                    extendedrepeatscope = new Setting<>(false);
                    closeinscope = new Setting<>(true);
                    closesemantics = new Setting<>(CloseSemantics.DEFAULT);
                    upvaluedeclarationtype = new Setting<>(UpvalueDeclarationType.INLINE);
                    fortarget = new Setting<>(null);
                    tfortarget = new Setting<>(Op.TFORLOOP);
                    whileformat = new Setting<>(WhileFormat.TOP_CONDITION);
                    allowpreceedingsemicolon = new Setting<>(false);
                    usenestinglongstrings = new Setting<>(false);
                    environmenttable = new Setting<>(null);
                    useifbreakrewrite = new Setting<>(false);
                    usegoto = new Setting<>(false);
                    rkoffset = new Setting<>(256);
                    allownegativeint = new Setting<>(luaj);
                    constantslengthmode = new Setting<>(luaj ? ListLengthMode.ALLOW_NEGATIVE : ListLengthMode.STRICT);
                    functionslengthmode = new Setting<>(luaj ? ListLengthMode.ALLOW_NEGATIVE : ListLengthMode.STRICT);
                    locallengthmode = new Setting<>(luaj ? ListLengthMode.ALLOW_NEGATIVE : ListLengthMode.STRICT);
                    upvaluelengthmode = new Setting<>(luaj ? ListLengthMode.ALLOW_NEGATIVE : ListLengthMode.STRICT);
                }
                case 2 -> {
                    varargtype = new Setting<>(VarArgType.ELLIPSIS);
                    useupvaluecountinheader = new Setting<>(false);
                    headertype = HeaderType.LUA52;
                    stringtype = StringType.LUA50;
                    constanttype = ConstantType.LUA50;
                    upvaluetype = UpvalueType.LUA50;
                    functiontype = FunctionType.LUA52;
                    opcodemap = OpcodeMapType.LUA52;
                    defaultop = Op.DEFAULT;
                    instructionformat = new Setting<>(InstructionFormat.LUA51);
                    outerblockscopeadjustment = new Setting<>(0);
                    extendedrepeatscope = new Setting<>(false);
                    closeinscope = new Setting<>(null);
                    closesemantics = new Setting<>(CloseSemantics.JUMP);
                    upvaluedeclarationtype = new Setting<>(UpvalueDeclarationType.HEADER);
                    fortarget = new Setting<>(null);
                    tfortarget = new Setting<>(Op.TFORCALL);
                    whileformat = new Setting<>(WhileFormat.TOP_CONDITION);
                    allowpreceedingsemicolon = new Setting<>(true);
                    usenestinglongstrings = new Setting<>(false);
                    environmenttable = new Setting<>("_ENV");
                    useifbreakrewrite = new Setting<>(true);
                    usegoto = new Setting<>(true);
                    rkoffset = new Setting<>(256);
                    allownegativeint = new Setting<>(luaj);
                    constantslengthmode = new Setting<>(luaj ? ListLengthMode.ALLOW_NEGATIVE : ListLengthMode.STRICT);
                    functionslengthmode = new Setting<>(luaj ? ListLengthMode.ALLOW_NEGATIVE : ListLengthMode.STRICT);
                    locallengthmode = new Setting<>(luaj ? ListLengthMode.ALLOW_NEGATIVE : ListLengthMode.STRICT);
                    upvaluelengthmode = new Setting<>(luaj ? ListLengthMode.ALLOW_NEGATIVE : ListLengthMode.STRICT);
                }
                case 3 -> {
                    varargtype = new Setting<>(VarArgType.ELLIPSIS);
                    useupvaluecountinheader = new Setting<>(true);
                    headertype = HeaderType.LUA53;
                    stringtype = StringType.LUA53;
                    constanttype = ConstantType.LUA53;
                    upvaluetype = UpvalueType.LUA50;
                    functiontype = FunctionType.LUA53;
                    opcodemap = OpcodeMapType.LUA53;
                    defaultop = Op.DEFAULT;
                    instructionformat = new Setting<>(InstructionFormat.LUA51);
                    outerblockscopeadjustment = new Setting<>(0);
                    extendedrepeatscope = new Setting<>(false);
                    closeinscope = new Setting<>(null);
                    closesemantics = new Setting<>(CloseSemantics.JUMP);
                    upvaluedeclarationtype = new Setting<>(UpvalueDeclarationType.HEADER);
                    fortarget = new Setting<>(null);
                    tfortarget = new Setting<>(Op.TFORCALL);
                    whileformat = new Setting<>(WhileFormat.TOP_CONDITION);
                    allowpreceedingsemicolon = new Setting<>(true);
                    usenestinglongstrings = new Setting<>(false);
                    environmenttable = new Setting<>("_ENV");
                    useifbreakrewrite = new Setting<>(true);
                    usegoto = new Setting<>(true);
                    rkoffset = new Setting<>(256);
                    allownegativeint = new Setting<>(true);
                    constantslengthmode = new Setting<>(ListLengthMode.STRICT);
                    functionslengthmode = new Setting<>(ListLengthMode.STRICT);
                    locallengthmode = new Setting<>(ListLengthMode.STRICT);
                    upvaluelengthmode = new Setting<>(ListLengthMode.STRICT);
                }
                case 4 -> {
                    varargtype = new Setting<>(VarArgType.ELLIPSIS);
                    useupvaluecountinheader = new Setting<>(true);
                    headertype = HeaderType.LUA54;
                    stringtype = StringType.LUA54;
                    constanttype = ConstantType.LUA54;
                    upvaluetype = UpvalueType.LUA54;
                    functiontype = FunctionType.LUA54;
                    opcodemap = OpcodeMapType.LUA54;
                    defaultop = Op.DEFAULT54;
                    instructionformat = new Setting<>(InstructionFormat.LUA54);
                    outerblockscopeadjustment = new Setting<>(0);
                    extendedrepeatscope = new Setting<>(false);
                    closeinscope = new Setting<>(false);
                    closesemantics = new Setting<>(CloseSemantics.LUA54);
                    upvaluedeclarationtype = new Setting<>(UpvalueDeclarationType.HEADER);
                    fortarget = new Setting<>(null);
                    tfortarget = new Setting<>(null);
                    whileformat = new Setting<>(WhileFormat.TOP_CONDITION);
                    allowpreceedingsemicolon = new Setting<>(true);
                    usenestinglongstrings = new Setting<>(false);
                    environmenttable = new Setting<>("_ENV");
                    useifbreakrewrite = new Setting<>(true);
                    usegoto = new Setting<>(true);
                    rkoffset = new Setting<>(null);
                    allownegativeint = new Setting<>(true);
                    constantslengthmode = new Setting<>(ListLengthMode.STRICT);
                    functionslengthmode = new Setting<>(ListLengthMode.STRICT);
                    locallengthmode = new Setting<>(ListLengthMode.STRICT);
                    upvaluelengthmode = new Setting<>(ListLengthMode.IGNORE);
                }
                default -> throw new IllegalStateException();
            }
        } else {
            throw new IllegalStateException();
        }

        reservedWords = new HashSet<>();
        reservedWords.add("and");
        reservedWords.add("break");
        reservedWords.add("do");
        reservedWords.add("else");
        reservedWords.add("elseif");
        reservedWords.add("end");
        reservedWords.add("false");
        reservedWords.add("for");
        reservedWords.add("function");
        reservedWords.add("if");
        reservedWords.add("in");
        reservedWords.add("local");
        reservedWords.add("nil");
        reservedWords.add("not");
        reservedWords.add("or");
        reservedWords.add("repeat");
        reservedWords.add("return");
        reservedWords.add("then");
        reservedWords.add("true");
        reservedWords.add("until");
        reservedWords.add("while");
        if (usegoto.get()) {
            reservedWords.add("goto");
        }

        this.lheadertype = LHeaderType.get(headertype);
        this.lstringtype = LStringType.get(stringtype);
        this.lconstanttype = LConstantType.get(constanttype);
        this.lupvaluetype = LUpvalueType.get(upvaluetype);
        this.lfunctiontype = LFunctionType.get(functiontype);
        this.opcodemap = new OpcodeMap(opcodemap);
    }

    public static Version getVersion(Configuration config, int major, int minor) {
        return new Version(config, major, minor);
    }

    public int getVersionMajor() {
        return major;
    }

    public int getVersionMinor() {
        return minor;
    }

    public String getName() {
        return name;
    }

    public boolean isEnvironmentTable(String name) {
        var env = environmenttable.get();
        if (env != null) {
            return name.equals(env);
        } else {
            return false;
        }
    }

    public boolean isReserved(String name) {
        return reservedWords.contains(name);
    }

    public LHeaderType getLHeaderType() {
        return lheadertype;
    }

    public LStringType getLStringType() {
        return lstringtype;
    }

    public LConstantType getLConstantType() {
        return lconstanttype;
    }

    public LUpvalueType getLUpvalueType() {
        return lupvaluetype;
    }

    public LFunctionType getLFunctionType() {
        return lfunctiontype;
    }

    public OpcodeMap getOpcodeMap() {
        return opcodemap;
    }

    public Op getDefaultOp() {
        return defaultop;
    }

    public enum VarArgType {
        ARG,
        HYBRID,
        ELLIPSIS,
    }

    public enum HeaderType {
        LUA50,
        LUA51,
        LUA52,
        LUA53,
        LUA54,
    }

    public enum StringType {
        LUA50,
        LUA53,
        LUA54,
    }

    public enum ConstantType {
        LUA50,
        LUA53,
        LUA54,
    }

    public enum UpvalueType {
        LUA50,
        LUA54
    }

    public enum FunctionType {
        LUA50,
        LUA51,
        LUA52,
        LUA53,
        LUA54,
    }

    public enum OpcodeMapType {
        LUA50,
        LUA51,
        LUA52,
        LUA53,
        LUA54,
    }

    public enum UpvalueDeclarationType {
        INLINE,
        HEADER,
    }

    public enum InstructionFormat {
        LUA50,
        LUA51,
        LUA54,
    }

    public enum WhileFormat {
        TOP_CONDITION,
        BOTTOM_CONDITION,
    }

    public enum CloseSemantics {
        DEFAULT,
        JUMP,
        LUA54,
    }

    public enum ListLengthMode {
        STRICT, // Negative is illegal
        ALLOW_NEGATIVE, // Negative treated as zero
        IGNORE, // List length is already known; only accept 0 or else ignore
    }

    public static class Setting<T> {

        private final T value;

        public Setting(T value) {
            this.value = value;
        }

        public T get() {
            return value;
        }

    }

}
