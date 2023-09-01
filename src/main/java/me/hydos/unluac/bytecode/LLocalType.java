package me.hydos.unluac.bytecode;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;


public class LLocalType extends BObjectType<LLocal> {

    @Override
    public LLocal parse(ByteBuffer buffer, BHeader header) {
        var name = header.string.parse(buffer, header);
        var start = header.integer.parse(buffer, header);
        var end = header.integer.parse(buffer, header);
        if (header.debug) {
            System.out.print("-- parsing local, name: ");
            System.out.print(name);
            System.out.print(" from " + start.asInt() + " to " + end.asInt());
            System.out.println();
        }
        return new LLocal(name, start, end);
    }

    @Override
    public void write(OutputStream out, BHeader header, LLocal object) throws IOException {
        header.string.write(out, header, object.name);
        header.integer.write(out, header, new BInteger(object.start));
        header.integer.write(out, header, new BInteger(object.end));
    }

}
