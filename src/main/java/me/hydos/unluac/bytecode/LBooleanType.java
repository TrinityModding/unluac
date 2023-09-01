package me.hydos.unluac.bytecode;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;


public class LBooleanType extends BObjectType<LBoolean> {

    @Override
    public LBoolean parse(ByteBuffer buffer, BHeader header) {
        int value = buffer.get();
        if ((value & 0xFFFFFFFE) != 0) {
            throw new IllegalStateException();
        } else {
            var bool = value == 0 ? LBoolean.LFALSE : LBoolean.LTRUE;
            if (header.debug) {
                System.out.println("-- parsed <boolean> " + bool);
            }
            return bool;
        }
    }

    @Override
    public void write(OutputStream out, BHeader header, LBoolean object) throws IOException {
        var value = object.value() ? 1 : 0;
        out.write(value);
    }

}
