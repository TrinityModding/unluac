package me.hydos.unluac.parse;

import me.hydos.unluac.Version;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;


public abstract class LConstantType extends BObjectType<LObject> {

    public static LConstantType get(Version.ConstantType type) {
        return switch (type) {
            case LUA50 -> new LConstantType50();
            case LUA53 -> new LConstantType53();
            case LUA54 -> new LConstantType54();
            default -> throw new IllegalStateException();
        };
    }

}

class LConstantType50 extends LConstantType {

    @Override
    public LObject parse(ByteBuffer buffer, BHeader header) {
        var type = 0xFF & buffer.get();
        if (header.debug) {
            System.out.print("-- parsing <constant>, type is ");
            switch (type) {
                case 0 -> System.out.println("<nil>");
                case 1 -> System.out.println("<boolean>");
                case 3 -> System.out.println("<number>");
                case 4 -> System.out.println("<string>");
                default -> System.out.println("illegal " + type);
            }
        }
        return switch (type) {
            case 0 -> LNil.NIL;
            case 1 -> header.bool.parse(buffer, header);
            case 3 -> header.number.parse(buffer, header);
            case 4 -> header.string.parse(buffer, header);
            default -> throw new IllegalStateException();
        };
    }

    @Override
    public void write(OutputStream out, BHeader header, LObject object) throws IOException {
        if (object instanceof LNil) {
            out.write(0);
        } else if (object instanceof LBoolean) {
            out.write(1);
            header.bool.write(out, header, (LBoolean) object);
        } else if (object instanceof LNumber) {
            out.write(3);
            header.number.write(out, header, (LNumber) object);
        } else if (object instanceof LString) {
            out.write(4);
            header.string.write(out, header, (LString) object);
        } else {
            throw new IllegalStateException();
        }
    }

}

class LConstantType53 extends LConstantType {

    @Override
    public LObject parse(ByteBuffer buffer, BHeader header) {
        var type = 0xFF & buffer.get();
        if (header.debug) {
            System.out.print("-- parsing <constant>, type is ");
            switch (type) {
                case 0 -> System.out.println("<nil>");
                case 1 -> System.out.println("<boolean>");
                case 3 -> System.out.println("<float>");
                case 0x13 -> System.out.println("<integer>");
                case 4 -> System.out.println("<short string>");
                case 0x14 -> System.out.println("<long string>");
                default -> System.out.println("illegal " + type);
            }
        }
        switch (type) {
            case 0 -> {
                return LNil.NIL;
            }
            case 1 -> {
                return header.bool.parse(buffer, header);
            }
            case 3 -> {
                return header.lfloat.parse(buffer, header);
            }
            case 0x13 -> {
                return header.linteger.parse(buffer, header);
            }
            case 4 -> {
                return header.string.parse(buffer, header);
            }
            case 0x14 -> {
                var s = header.string.parse(buffer, header);
                s.islong = true;
                return s;
            }
            default -> throw new IllegalStateException();
        }
    }

    @Override
    public void write(OutputStream out, BHeader header, LObject object) throws IOException {
        if (object instanceof LNil) {
            out.write(0);
        } else if (object instanceof LBoolean) {
            out.write(1);
            header.bool.write(out, header, (LBoolean) object);
        } else if (object instanceof LNumber n) {
            if (!n.integralType()) {
                out.write(3);
                header.lfloat.write(out, header, (LNumber) object);
            } else {
                out.write(0x13);
                header.linteger.write(out, header, (LNumber) object);
            }
        } else if (object instanceof LString s) {
            out.write(s.islong ? 0x14 : 4);
            header.string.write(out, header, s);
        } else {
            throw new IllegalStateException();
        }
    }

}

class LConstantType54 extends LConstantType {

    @Override
    public LObject parse(ByteBuffer buffer, BHeader header) {
        var type = 0xFF & buffer.get();
        switch (type) {
            case 0 -> {
                return LNil.NIL;
            }
            case 1 -> {
                return LBoolean.LFALSE;
            }
            case 0x11 -> {
                return LBoolean.LTRUE;
            }
            case 3 -> {
                return header.linteger.parse(buffer, header);
            }
            case 0x13 -> {
                return header.lfloat.parse(buffer, header);
            }
            case 4 -> {
                return header.string.parse(buffer, header);
            }
            case 0x14 -> {
                var s = header.string.parse(buffer, header);
                s.islong = true;
                return s;
            }
            default -> throw new IllegalStateException();
        }
    }

    @Override
    public void write(OutputStream out, BHeader header, LObject object) throws IOException {
        if (object instanceof LNil) {
            out.write(0);
        } else if (object instanceof LBoolean) {
            if (((LBoolean) object).value()) {
                out.write(0x11);
            } else {
                out.write(1);
            }
        } else if (object instanceof LNumber n) {
            if (!n.integralType()) {
                out.write(0x13);
                header.lfloat.write(out, header, (LNumber) object);
            } else {
                out.write(3);
                header.linteger.write(out, header, (LNumber) object);
            }
        } else if (object instanceof LString s) {
            out.write(s.islong ? 0x14 : 4);
            header.string.write(out, header, s);
        } else {
            throw new IllegalStateException();
        }
    }

}
