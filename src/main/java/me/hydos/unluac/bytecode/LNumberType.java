package me.hydos.unluac.bytecode;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;


public class LNumberType extends BObjectType<LNumber> {

    public final int size;
    public final boolean integral;
    public final NumberMode mode;
    public LNumberType(int size, boolean integral, NumberMode mode) {
        this.size = size;
        this.integral = integral;
        this.mode = mode;
        if (!(size == 4 || size == 8)) {
            throw new IllegalStateException("The input chunk has an unsupported Lua number size: " + size);
        }
    }

    public double convert(double number) {
        if (integral) {
            switch (size) {
                case 4 -> {
                    return (int) number;
                }
                case 8 -> {
                    return (long) number;
                }
            }
        } else {
            switch (size) {
                case 4 -> {
                    return (float) number;
                }
                case 8 -> {
                    return number;
                }
            }
        }
        throw new IllegalStateException("The input chunk has an unsupported Lua number format");
    }

    @Override
    public LNumber parse(ByteBuffer buffer, BHeader header) {
        LNumber value = null;
        if (integral) {
            switch (size) {
                case 4 -> value = new LIntNumber(buffer.getInt());
                case 8 -> value = new LLongNumber(buffer.getLong());
            }
        } else {
            value = switch (size) {
                case 4 -> new LFloatNumber(buffer.getFloat(), mode);
                case 8 -> new LDoubleNumber(buffer.getDouble(), mode);
                default -> value;
            };
        }
        if (value == null) {
            throw new IllegalStateException("The input chunk has an unsupported Lua number format");
        }
        if (header.debug) {
            System.out.println("-- parsed <number> " + value);
        }
        return value;
    }

    @Override
    public void write(OutputStream out, BHeader header, LNumber n) throws IOException {
        var bits = n.bits();
        if (header.lheader.endianness == LHeader.LEndianness.LITTLE) {
            for (var i = 0; i < size; i++) {
                out.write((byte) (bits & 0xFF));
                bits = bits >>> 8;
            }
        } else {
            for (var i = size - 1; i >= 0; i--) {
                out.write((byte) ((bits >> (i * 8)) & 0xFF));
            }
        }
    }

    public LNumber createNaN(long bits) {
        if (integral) {
            throw new IllegalStateException();
        } else {
            switch (size) {
                case 4 -> {
                    var fbits = Float.floatToRawIntBits(Float.NaN);
                    if (bits < 0) {
                        bits ^= 0x8000000000000000L;
                        fbits ^= 0x80000000;
                    }
                    fbits |= (bits >> LFloatNumber.NAN_SHIFT_OFFSET);
                    return new LFloatNumber(Float.intBitsToFloat(fbits), mode);
                }
                case 8 -> {
                    return new LDoubleNumber(Double.longBitsToDouble(Double.doubleToRawLongBits(Double.NaN) ^ bits), mode);
                }
                default -> throw new IllegalStateException();
            }
        }
    }

    public LNumber create(double x) {
        if (integral) {
            return switch (size) {
                case 4 -> new LIntNumber((int) x);
                case 8 -> new LLongNumber((long) x);
                default -> throw new IllegalStateException();
            };
        } else {
            return switch (size) {
                case 4 -> new LFloatNumber((float) x, mode);
                case 8 -> new LDoubleNumber(x, mode);
                default -> throw new IllegalStateException();
            };
        }
    }

    public LNumber create(BigInteger x) {
        if (integral) {
            return switch (size) {
                case 4 -> new LIntNumber(x.intValueExact());
                case 8 -> new LLongNumber(x.longValueExact());
                default -> throw new IllegalStateException();
            };
        } else {
            return switch (size) {
                case 4 -> new LFloatNumber(x.floatValue(), mode);
                case 8 -> new LDoubleNumber(x.doubleValue(), mode);
                default -> throw new IllegalStateException();
            };
        }
    }

    public enum NumberMode {
        MODE_NUMBER, // Used for Lua 5.0 - 5.2 where numbers can represent integers or floats
        MODE_FLOAT, // Used for floats in Lua 5.3+
        MODE_INTEGER, // Used for integers in Lua 5.3+
    }

}
