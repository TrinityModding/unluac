package me.hydos.unluac.bytecode;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class LAbsLineInfoType extends BObjectType<LAbsLineInfo> {

    @Override
    public LAbsLineInfo parse(ByteBuffer buffer, BHeader header) {
        var pc = header.integer.parse(buffer, header).asInt();
        var line = header.integer.parse(buffer, header).asInt();
        return new LAbsLineInfo(pc, line);
    }

    @Override
    public void write(OutputStream out, BHeader header, LAbsLineInfo object) throws IOException {
        header.integer.write(out, header, new BInteger(object.pc));
        header.integer.write(out, header, new BInteger(object.line));
    }

}
