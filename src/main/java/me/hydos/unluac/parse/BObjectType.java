package me.hydos.unluac.parse;

import me.hydos.unluac.Version;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

abstract public class BObjectType<T extends BObject> {

    abstract public T parse(ByteBuffer buffer, BHeader header);

    abstract public void write(OutputStream out, BHeader header, T object) throws IOException;

    public final BList<T> parseList(ByteBuffer buffer, BHeader header) {
        return parseList(buffer, header, Version.ListLengthMode.STRICT, null);
    }

    public final BList<T> parseList(ByteBuffer buffer, BHeader header, Version.ListLengthMode mode) {
        return parseList(buffer, header, mode, null);
    }

    public final BList<T> parseList(ByteBuffer buffer, BHeader header, Version.ListLengthMode mode, BInteger knownLength) {
        var length = header.integer.parse(buffer, header);
        switch (mode) {
            case STRICT -> {
            }
            case ALLOW_NEGATIVE -> {
                if (length.signum() < 0) length = new BInteger(0);
            }
            case IGNORE -> {
                if (knownLength == null) throw new IllegalStateException();
                if (length.signum() != 0) length = knownLength;
            }
        }
        return parseList(buffer, header, length);
    }

    public final BList<T> parseList(final ByteBuffer buffer, final BHeader header, BInteger length) {
        final List<T> values = new ArrayList<>();
        length.iterate(() -> values.add(parse(buffer, header)));
        return new BList<>(length, values);
    }

    public final void writeList(OutputStream out, BHeader header, T[] array) throws IOException {
        header.integer.write(out, header, new BInteger(array.length));
        for (var object : array) {
            write(out, header, object);
        }
    }

    public final void writeList(OutputStream out, BHeader header, BList<T> blist) throws IOException {
        header.integer.write(out, header, blist.length);
        var it = blist.iterator();
        while (it.hasNext()) {
            write(out, header, it.next());
        }
    }

}
