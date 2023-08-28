package me.hydos.unluac.parse;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

abstract public class BIntegerType extends BObjectType<BInteger> {

    public static BIntegerType create50Type(boolean signed, int intSize, boolean allownegative) {
        return new BIntegerType50(signed, intSize, allownegative);
    }

    public static BIntegerType create54() {
        return new BIntegerType54();
    }

    public int getSize() {
        throw new IllegalStateException();
    }

    public BInteger create(int n) {
        return new BInteger(n);
    }

}

class BIntegerType50 extends BIntegerType {

    public final boolean signed;
    public final int intSize;
    public final boolean allownegative;

    public BIntegerType50(boolean signed, int intSize, boolean allownegative) {
        this.signed = signed;
        this.intSize = intSize;
        this.allownegative = allownegative;
    }

    protected BInteger raw_parse(ByteBuffer buffer) {
        BInteger value;
        if (signed && (intSize == 0 || intSize == 1 || intSize == 2 || intSize == 4)) {
            value = switch (intSize) {
                case 0 -> new BInteger(0);
                case 1 -> new BInteger(buffer.get());
                case 2 -> new BInteger(buffer.getShort());
                case 4 -> new BInteger(buffer.getInt());
                default -> throw new IllegalStateException();
            };
        } else {
            var bytes = new byte[intSize];
            var start = 0;
            var delta = 1;
            if (buffer.order() == ByteOrder.LITTLE_ENDIAN) {
                start = intSize - 1;
                delta = -1;
            }
            for (var i = start; i >= 0 && i < intSize; i += delta) {
                bytes[i] = buffer.get();
            }
            if (signed) {
                value = new BInteger(new BigInteger(bytes));
            } else {
                value = new BInteger(new BigInteger(1, bytes));
            }
        }

        if (!allownegative && value.signum() < 0) {
            throw new IllegalStateException("Illegal number");
        }

        return value;
    }

    protected void raw_write(OutputStream out, BHeader header, BInteger object) throws IOException {
        var bytes = object.littleEndianBytes(intSize);
        if (header.lheader.endianness == LHeader.LEndianness.LITTLE) {
            for (var b : bytes) {
                out.write(b);
            }
        } else {
            for (var i = bytes.length - 1; i >= 0; i--) {
                out.write(bytes[i]);
            }
        }
    }

    @Override
    public BInteger parse(ByteBuffer buffer, BHeader header) {
        var value = raw_parse(buffer);
        if (header.debug) {
            System.out.println("-- parsed <integer> " + value.asInt());
        }
        return value;
    }

    @Override
    public void write(OutputStream out, BHeader header, BInteger object) throws IOException {
        raw_write(out, header, object);
    }

    @Override
    public int getSize() {
        return intSize;
    }

}

class BIntegerType54 extends BIntegerType {

    public BIntegerType54() {

    }

    @Override
    public BInteger parse(ByteBuffer buffer, BHeader header) {
        long x = 0;
        byte b;
        do {
            b = buffer.get();
            x = (x << 7) | (b & 0x7F);
        } while ((b & 0x80) == 0);
        if (Integer.MIN_VALUE <= x && x <= Integer.MAX_VALUE) {
            return new BInteger((int) x);
        } else {
            return new BInteger(BigInteger.valueOf(x));
        }
    }

    @Override
    public void write(OutputStream out, BHeader header, BInteger object) throws IOException {
        var bytes = object.compressedBytes();
        for (var i = bytes.length - 1; i >= 1; i--) {
            out.write(bytes[i]);
        }
        out.write(bytes[0] | 0x80);
    }

}