package me.hydos.unluac.parse;

public class LFunction extends BObject {

    public final BHeader header;
    public final LString name;
    public final int linedefined;
    public final int lastlinedefined;
    public LFunction parent;
    public final int[] code;
    public final int[] lines;
    public final LAbsLineInfo[] abslineinfo;
    public final LLocal[] locals;
    public final LObject[] constants;
    public final LUpvalue[] upvalues;
    public final LFunction[] functions;
    public final int maximumStackSize;
    public final int numUpvalues;
    public final int numParams;
    public final int vararg;
    public boolean stripped;
    public int level;

    public LFunction(BHeader header, LString name, int linedefined, int lastlinedefined, int[] code, int[] lines, LAbsLineInfo[] abslineinfo, LLocal[] locals, LObject[] constants, LUpvalue[] upvalues, LFunction[] functions, int maximumStackSize, int numUpValues, int numParams, int vararg) {
        this.header = header;
        this.name = name;
        this.linedefined = linedefined;
        this.lastlinedefined = lastlinedefined;
        this.code = code;
        this.lines = lines;
        this.abslineinfo = abslineinfo;
        this.locals = locals;
        this.constants = constants;
        this.upvalues = upvalues;
        this.functions = functions;
        this.maximumStackSize = maximumStackSize;
        this.numUpvalues = numUpValues;
        this.numParams = numParams;
        this.vararg = vararg;
        this.stripped = false;
    }

    public void setLevel(int level) {
        this.level = level;
        for (var f : functions) {
            f.setLevel(level + 1);
        }
    }

}
