package me.hydos.unluac.bytecode;

import me.hydos.unluac.Version;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;


public abstract class LStringType extends BObjectType<LString> {

    protected final ThreadLocal<StringBuilder> b = ThreadLocal.withInitial(StringBuilder::new);

    public static LStringType get(Version.StringType type) {
        return switch (type) {
            case LUA50 -> new LStringType50();
            case LUA53 -> new LStringType53();
            case LUA54 -> new LStringType54();
            default -> throw new IllegalStateException();
        };
    }

}

class LStringType50 extends LStringType {

    @Override
    public LString parse(final ByteBuffer buffer, BHeader header) {
        var sizeT = header.sizeT.parse(buffer, header);
        final var b = this.b.get();
        b.setLength(0);
        sizeT.iterate(() -> b.append((char) (0xFF & buffer.get())));
        if (b.length() == 0) {
            return LString.NULL;
        } else {
            var last = b.charAt(b.length() - 1);
            b.delete(b.length() - 1, b.length());
            var s = b.toString();
            if (header.debug) {
                System.out.println("-- parsed <string> \"" + s + "\"");
            }
            return new LString(s, last);
        }
    }

    @Override
    public void write(OutputStream out, BHeader header, LString string) throws IOException {
        var len = string.value.length();
        if (string == LString.NULL) {
            header.sizeT.write(out, header, header.sizeT.create(0));
        } else {
            header.sizeT.write(out, header, header.sizeT.create(len + 1));
            for (var i = 0; i < len; i++) {
                out.write(string.value.charAt(i));
            }
            out.write(0);
        }
    }
}

class LStringType53 extends LStringType {

    @Override
    public LString parse(final ByteBuffer buffer, BHeader header) {
        BInteger sizeT;
        var size = 0xFF & buffer.get();
        if (size == 0) {
            return LString.NULL;
        } else if (size == 0xFF) {
            sizeT = header.sizeT.parse(buffer, header);
        } else {
            sizeT = new BInteger(size);
        }
        final var b = this.b.get();
        b.setLength(0);
        sizeT.iterate(new Runnable() {

            boolean first = true;

            @Override
            public void run() {
                if (!first) {
                    b.append((char) (0xFF & buffer.get()));
                } else {
                    first = false;
                }
            }

        });
        var s = b.toString();
        if (header.debug) {
            System.out.println("-- parsed <string> \"" + s + "\"");
        }
        return new LString(s);
    }

    @Override
    public void write(OutputStream out, BHeader header, LString string) throws IOException {
        var len = string.value.length() + 1;
        if (len < 0xFF) {
            out.write((byte) len);
        } else {
            out.write(0xFF);
            header.sizeT.write(out, header, header.sizeT.create(len));
        }
        for (var i = 0; i < string.value.length(); i++) {
            out.write(string.value.charAt(i));
        }
    }
}

class LStringType54 extends LStringType {

    @Override
    public LString parse(final ByteBuffer buffer, BHeader header) {
        var sizeT = header.sizeT.parse(buffer, header);
        if (sizeT.asInt() == 0) {
            return LString.NULL;
        }
        final var b = this.b.get();
        b.setLength(0);
        sizeT.iterate(new Runnable() {

            boolean first = true;

            @Override
            public void run() {
                if (!first) {
                    b.append((char) (0xFF & buffer.get()));
                } else {
                    first = false;
                }
            }

        });
        var s = b.toString();
        if (header.debug) {
            System.out.println("-- parsed <string> \"" + s + "\"");
        }
        return new LString(s);
    }

    @Override
    public void write(OutputStream out, BHeader header, LString string) throws IOException {
        if (string == LString.NULL) {
            header.sizeT.write(out, header, header.sizeT.create(0));
        } else {
            header.sizeT.write(out, header, header.sizeT.create(string.value.length() + 1));
            for (var i = 0; i < string.value.length(); i++) {
                out.write(string.value.charAt(i));
            }
        }
    }
}

