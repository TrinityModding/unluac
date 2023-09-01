package me.hydos.unluac.bytecode;

import me.hydos.unluac.Version;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public abstract class LUpvalueType extends BObjectType<LUpvalue> {

    public static LUpvalueType get(Version.UpvalueType type) {
        return switch (type) {
            case LUA50 -> new LUpvalueType50();
            case LUA54 -> new LUpvalueType54();
            default -> throw new IllegalStateException();
        };
    }

}

class LUpvalueType50 extends LUpvalueType {

    @Override
    public LUpvalue parse(ByteBuffer buffer, BHeader header) {
        var upvalue = new LUpvalue();
        upvalue.instack = buffer.get() != 0;
        upvalue.idx = 0xFF & buffer.get();
        upvalue.kind = -1;
        return upvalue;
    }

    @Override
    public void write(OutputStream out, BHeader header, LUpvalue object) throws IOException {
        out.write((byte) (object.instack ? 1 : 0));
        out.write(object.idx);
    }
}

class LUpvalueType54 extends LUpvalueType {

    @Override
    public LUpvalue parse(ByteBuffer buffer, BHeader header) {
        var upvalue = new LUpvalue();
        upvalue.instack = buffer.get() != 0;
        upvalue.idx = 0xFF & buffer.get();
        upvalue.kind = 0xFF & buffer.get();
        return upvalue;
    }

    @Override
    public void write(OutputStream out, BHeader header, LUpvalue object) throws IOException {
        out.write((byte) (object.instack ? 1 : 0));
        out.write(object.idx);
        out.write(object.kind);
    }
}
