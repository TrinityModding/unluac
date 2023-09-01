package me.hydos.unluac.bytecode;

public class BFunction extends BObject {

    public final BHeader header;
    public final LString name;
    public final int linedefined;
    public final int lastlinedefined;
    public BFunction parent;
    public final int[] code;
    public final int[] lines;
    public final LAbsLineInfo[] abslineinfo;
    public final LLocal[] locals;
    public final LObject[] constants;
    public final LUpvalue[] upvalues;
    public final BFunction[] functions;
    public final int maximumStackSize;
    public final int numUpvalues;
    public final int paramCount;
    public final int vararg;
    public boolean stripped;

    /**
     * How far we have gone into the code. The more methods we are in, The higher this number gets
     */
    public int depth;

    public BFunction(BHeader header, LString name, int linedefined, int lastlinedefined, int[] code, int[] lines, LAbsLineInfo[] abslineinfo, LLocal[] locals, LObject[] constants, LUpvalue[] upvalues, BFunction[] functions, int maximumStackSize, int numUpValues, int paramCount, int vararg) {
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
        this.paramCount = paramCount;
        this.vararg = vararg;
        this.stripped = false;
    }

    public void setDepth(int depth) {
        this.depth = depth;
        for (var f : functions) {
            f.setDepth(depth + 1);
        }
    }

}
