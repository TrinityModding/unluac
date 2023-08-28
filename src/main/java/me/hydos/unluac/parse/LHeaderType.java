package me.hydos.unluac.parse;

import me.hydos.unluac.Version;
import me.hydos.unluac.assemble.Directive;
import me.hydos.unluac.decompile.CodeExtract;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;

abstract public class LHeaderType extends BObjectType<LHeader> {

    public static final LHeaderType TYPE50 = new LHeaderType50();
    public static final LHeaderType TYPE51 = new LHeaderType51();
    public static final LHeaderType TYPE52 = new LHeaderType52();
    public static final LHeaderType TYPE53 = new LHeaderType53();
    public static final LHeaderType TYPE54 = new LHeaderType54();
    protected static final int TEST_INTEGER = 0x5678;
    protected static final double TEST_FLOAT = 370.5;
    private static final byte[] luacTail = {
            0x19, (byte) 0x93, 0x0D, 0x0A, 0x1A, 0x0A,
    };

    public static LHeaderType get(Version.HeaderType type) {
        return switch (type) {
            case LUA50 -> TYPE50;
            case LUA51 -> TYPE51;
            case LUA52 -> TYPE52;
            case LUA53 -> TYPE53;
            case LUA54 -> TYPE54;
            default -> throw new IllegalStateException();
        };
    }

    @Override
    public LHeader parse(ByteBuffer buffer, BHeader header) {
        var version = header.version;
        var s = new LHeaderParseState();
        parse_main(buffer, header, s);
        var bool = new LBooleanType();
        var string = version.getLStringType();
        var constant = version.getLConstantType();
        var abslineinfo = new LAbsLineInfoType();
        var local = new LLocalType();
        var upvalue = version.getLUpvalueType();
        var function = version.getLFunctionType();
        var extract = new CodeExtract(header.version, s.sizeOp, s.sizeA, s.sizeB, s.sizeC);
        return new LHeader(s.format, s.endianness, s.integer, s.sizeT, bool, s.number, s.linteger, s.lfloat, string, constant, abslineinfo, local, upvalue, function, extract);
    }

    abstract public List<Directive> get_directives();

    abstract protected void parse_main(ByteBuffer buffer, BHeader header, LHeaderParseState s);

    protected void parse_format(ByteBuffer buffer, BHeader header, LHeaderParseState s) {
        // 1 byte Lua "format"
        var format = 0xFF & buffer.get();
        if (format != 0) {
            throw new IllegalStateException("The input chunk reports a non-standard lua format: " + format);
        }
        s.format = format;
        if (header.debug) {
            System.out.println("-- format: " + format);
        }
    }

    protected void write_format(OutputStream out, LHeader object) throws IOException {
        out.write(object.format);
    }

    protected void parse_endianness(ByteBuffer buffer, BHeader header, LHeaderParseState s) {
        // 1 byte endianness
        var endianness = 0xFF & buffer.get();
        switch (endianness) {
            case 0 -> {
                s.endianness = LHeader.LEndianness.BIG;
                buffer.order(ByteOrder.BIG_ENDIAN);
            }
            case 1 -> {
                s.endianness = LHeader.LEndianness.LITTLE;
                buffer.order(ByteOrder.LITTLE_ENDIAN);
            }
            default -> throw new IllegalStateException("The input chunk reports an invalid endianness: " + endianness);
        }
        if (header.debug) {
            System.out.println("-- endianness: " + endianness + (endianness == 0 ? " (big)" : " (little)"));
        }
    }

    protected void write_endianness(OutputStream out, LHeader object) throws IOException {
        var value = switch (object.endianness) {
            case BIG -> 0;
            case LITTLE -> 1;
            default -> throw new IllegalStateException();
        };
        out.write(value);
    }

    protected void parse_int_size(ByteBuffer buffer, BHeader header, LHeaderParseState s) {
        // 1 byte int size
        var intSize = 0xFF & buffer.get();
        if (header.debug) {
            System.out.println("-- int size: " + intSize);
        }
        s.integer = new BIntegerType50(true, intSize, header.version.allownegativeint.get());
    }

    protected void write_int_size(OutputStream out, LHeader object) throws IOException {
        out.write(object.integer.getSize());
    }

    protected void parse_size_t_size(ByteBuffer buffer, BHeader header, LHeaderParseState s) {
        // 1 byte sizeT size
        var sizeTSize = 0xFF & buffer.get();
        if (header.debug) {
            System.out.println("-- size_t size: " + sizeTSize);
        }
        s.sizeT = new BIntegerType50(false, sizeTSize, false);
    }

    protected void write_size_t_size(OutputStream out, LHeader object) throws IOException {
        out.write(object.sizeT.getSize());
    }

    protected void parse_instruction_size(ByteBuffer buffer, BHeader header) {
        // 1 byte instruction size
        var instructionSize = 0xFF & buffer.get();
        if (header.debug) {
            System.out.println("-- instruction size: " + instructionSize);
        }
        if (instructionSize != 4) {
            throw new IllegalStateException("The input chunk reports an unsupported instruction size: " + instructionSize + " bytes");
        }
    }

    protected void write_instruction_size(OutputStream out) throws IOException {
        out.write(4);
    }

    protected void parse_number_size(ByteBuffer buffer, BHeader header, LHeaderParseState s) {
        var lNumberSize = 0xFF & buffer.get();
        if (header.debug) {
            System.out.println("-- Lua number size: " + lNumberSize);
        }
        s.lNumberSize = lNumberSize;
    }

    protected void write_number_size(OutputStream out, LHeader object) throws IOException {
        out.write(object.number.size);
    }

    protected void parse_number_integrality(ByteBuffer buffer, BHeader header, LHeaderParseState s) {
        var lNumberIntegralityCode = 0xFF & buffer.get();
        if (header.debug) {
            System.out.println("-- Lua number integrality code: " + lNumberIntegralityCode);
        }
        if (lNumberIntegralityCode > 1) {
            throw new IllegalStateException("The input chunk reports an invalid code for lua number integrality: " + lNumberIntegralityCode);
        }
        s.lNumberIntegrality = (lNumberIntegralityCode == 1);
    }

    protected void write_number_integrality(OutputStream out, LHeader object) throws IOException {
        out.write((byte) (object.number.integral ? 1 : 0));
    }

    protected void parse_integer_size(ByteBuffer buffer, BHeader header, LHeaderParseState s) {
        var lIntegerSize = 0xFF & buffer.get();
        if (header.debug) {
            System.out.println("-- Lua integer size: " + lIntegerSize);
        }
        if (lIntegerSize < 2) {
            throw new IllegalStateException("The input chunk reports an integer size that is too small: " + lIntegerSize);
        }
        s.lIntegerSize = lIntegerSize;
    }

    protected void parse_float_size(ByteBuffer buffer, BHeader header, LHeaderParseState s) {
        var lFloatSize = 0xFF & buffer.get();
        if (header.debug) {
            System.out.println("-- Lua float size: " + lFloatSize);
        }
        s.lFloatSize = lFloatSize;
    }

    protected void parse_number_format_53(ByteBuffer buffer, BHeader header, LHeaderParseState s) {
        var endianness = new byte[s.lIntegerSize];
        buffer.get(endianness);

        var test_high = (byte) ((TEST_INTEGER >> 8) & 0xFF);
        var test_low = (byte) (TEST_INTEGER & 0xFF);

        if (endianness[0] == test_low && endianness[1] == test_high) {
            s.endianness = LHeader.LEndianness.LITTLE;
            buffer.order(ByteOrder.LITTLE_ENDIAN);
        } else if (endianness[s.lIntegerSize - 1] == test_low && endianness[s.lIntegerSize - 2] == test_high) {
            s.endianness = LHeader.LEndianness.BIG;
            buffer.order(ByteOrder.BIG_ENDIAN);
        } else {
            throw new IllegalStateException("The input chunk reports an invalid endianness: " + Arrays.toString(endianness));
        }
        s.linteger = new LNumberType(s.lIntegerSize, true, LNumberType.NumberMode.MODE_INTEGER);
        s.lfloat = new LNumberType(s.lFloatSize, false, LNumberType.NumberMode.MODE_FLOAT);
        var floatcheck = s.lfloat.parse(buffer, header).value();
        if (floatcheck != s.lfloat.convert(TEST_FLOAT)) {
            throw new IllegalStateException("The input chunk is using an unrecognized floating point format: " + floatcheck);
        }
    }

    protected void parse_extractor(ByteBuffer buffer, BHeader header, LHeaderParseState s) {
        s.sizeOp = 0xFF & buffer.get();
        s.sizeA = 0xFF & buffer.get();
        s.sizeB = 0xFF & buffer.get();
        s.sizeC = 0xFF & buffer.get();
        if (header.debug) {
            System.out.println("-- Lua opcode extractor sizeOp: " + s.sizeOp + ", sizeA: " + s.sizeA + ", sizeB: " + s.sizeB + ", sizeC: " + s.sizeC);
        }
    }

    protected void write_extractor(OutputStream out, LHeader object) throws IOException {
        out.write(object.extractor.op.size);
        out.write(object.extractor.A.size);
        out.write(object.extractor.B.size);
        out.write(object.extractor.C.size);
    }

    protected void parse_tail(ByteBuffer buffer) {
        for (var b : luacTail) {
            if (buffer.get() != b) {
                throw new IllegalStateException("The input file does not have the header tail of a valid Lua file (it may be corrupted).");
            }
        }
    }

    protected void write_tail(OutputStream out) throws IOException {
        for (var b : luacTail) {
            out.write(b);
        }
    }

    protected static class LHeaderParseState {
        BIntegerType integer;
        BIntegerType sizeT;
        LNumberType number;
        LNumberType linteger;
        LNumberType lfloat;

        int format;
        LHeader.LEndianness endianness;

        int lNumberSize;
        boolean lNumberIntegrality;

        int lIntegerSize;
        int lFloatSize;

        int sizeOp;
        int sizeA;
        int sizeB;
        int sizeC;
    }

}

class LHeaderType50 extends LHeaderType {

    private static final double TEST_NUMBER = 3.14159265358979323846E7;

    @Override
    public List<Directive> get_directives() {
        return Arrays.asList(Directive.ENDIANNESS,
                Directive.INT_SIZE,
                Directive.SIZE_T_SIZE,
                Directive.INSTRUCTION_SIZE,
                Directive.SIZE_OP,
                Directive.SIZE_A,
                Directive.SIZE_B,
                Directive.SIZE_C,
                Directive.NUMBER_FORMAT);
    }

    @Override
    protected void parse_main(ByteBuffer buffer, BHeader header, LHeaderParseState s) {
        s.format = 0;
        parse_endianness(buffer, header, s);
        parse_int_size(buffer, header, s);
        parse_size_t_size(buffer, header, s);
        parse_instruction_size(buffer, header);
        parse_extractor(buffer, header, s);
        parse_number_size(buffer, header, s);
        var lfloat = new LNumberType(s.lNumberSize, false, LNumberType.NumberMode.MODE_NUMBER);
        var linteger = new LNumberType(s.lNumberSize, true, LNumberType.NumberMode.MODE_NUMBER);
        buffer.mark();
        var floatcheck = lfloat.parse(buffer, header).value();
        buffer.reset();
        var intcheck = linteger.parse(buffer, header).value();
        if (floatcheck == lfloat.convert(TEST_NUMBER)) {
            s.number = lfloat;
        } else if (intcheck == linteger.convert(TEST_NUMBER)) {
            s.number = linteger;
        } else {
            throw new IllegalStateException("The input chunk is using an unrecognized number format: " + intcheck);
        }
    }

    @Override
    public void write(OutputStream out, BHeader header, LHeader object) throws IOException {
        write_endianness(out, object);
        write_int_size(out, object);
        write_size_t_size(out, object);
        write_instruction_size(out);
        write_extractor(out, object);
        write_number_size(out, object);
        object.number.write(out, header, object.number.create(TEST_NUMBER));
    }

}

class LHeaderType51 extends LHeaderType {

    @Override
    public List<Directive> get_directives() {
        return Arrays.asList(Directive.FORMAT,
                Directive.ENDIANNESS,
                Directive.INT_SIZE,
                Directive.SIZE_T_SIZE,
                Directive.INSTRUCTION_SIZE,
                Directive.NUMBER_FORMAT);
    }

    @Override
    protected void parse_main(ByteBuffer buffer, BHeader header, LHeaderParseState s) {
        parse_format(buffer, header, s);
        parse_endianness(buffer, header, s);
        parse_int_size(buffer, header, s);
        parse_size_t_size(buffer, header, s);
        parse_instruction_size(buffer, header);
        parse_number_size(buffer, header, s);
        parse_number_integrality(buffer, header, s);
        s.number = new LNumberType(s.lNumberSize, s.lNumberIntegrality, LNumberType.NumberMode.MODE_NUMBER);
    }

    @Override
    public void write(OutputStream out, BHeader header, LHeader object) throws IOException {
        write_format(out, object);
        write_endianness(out, object);
        write_int_size(out, object);
        write_size_t_size(out, object);
        write_instruction_size(out);
        write_number_size(out, object);
        write_number_integrality(out, object);
    }

}

class LHeaderType52 extends LHeaderType {

    @Override
    public List<Directive> get_directives() {
        return Arrays.asList(Directive.FORMAT,
                Directive.ENDIANNESS,
                Directive.INT_SIZE,
                Directive.SIZE_T_SIZE,
                Directive.INSTRUCTION_SIZE,
                Directive.NUMBER_FORMAT);
    }

    @Override
    protected void parse_main(ByteBuffer buffer, BHeader header, LHeaderParseState s) {
        parse_format(buffer, header, s);
        parse_endianness(buffer, header, s);
        parse_int_size(buffer, header, s);
        parse_size_t_size(buffer, header, s);
        parse_instruction_size(buffer, header);
        parse_number_size(buffer, header, s);
        parse_number_integrality(buffer, header, s);
        parse_tail(buffer);
        s.number = new LNumberType(s.lNumberSize, s.lNumberIntegrality, LNumberType.NumberMode.MODE_NUMBER);
    }

    @Override
    public void write(OutputStream out, BHeader header, LHeader object) throws IOException {
        write_format(out, object);
        write_endianness(out, object);
        write_int_size(out, object);
        write_size_t_size(out, object);
        write_instruction_size(out);
        write_number_size(out, object);
        write_number_integrality(out, object);
        write_tail(out);
    }

}

class LHeaderType53 extends LHeaderType {

    @Override
    public List<Directive> get_directives() {
        return Arrays.asList(Directive.FORMAT,
                Directive.INT_SIZE,
                Directive.SIZE_T_SIZE,
                Directive.INSTRUCTION_SIZE,
                Directive.INTEGER_FORMAT,
                Directive.FLOAT_FORMAT,
                Directive.ENDIANNESS);
    }

    @Override
    protected void parse_main(ByteBuffer buffer, BHeader header, LHeaderParseState s) {
        parse_format(buffer, header, s);
        parse_tail(buffer);
        parse_int_size(buffer, header, s);
        parse_size_t_size(buffer, header, s);
        parse_instruction_size(buffer, header);
        parse_integer_size(buffer, header, s);
        parse_float_size(buffer, header, s);
        parse_number_format_53(buffer, header, s);
    }

    @Override
    public void write(OutputStream out, BHeader header, LHeader object) throws IOException {
        write_format(out, object);
        write_tail(out);
        write_int_size(out, object);
        write_size_t_size(out, object);
        write_instruction_size(out);
        out.write(header.linteger.size);
        out.write(header.lfloat.size);
        header.linteger.write(out, header, header.linteger.create(TEST_INTEGER));
        header.lfloat.write(out, header, header.lfloat.create(TEST_FLOAT));
    }

}

class LHeaderType54 extends LHeaderType {

    @Override
    public List<Directive> get_directives() {
        return Arrays.asList(Directive.FORMAT,
                Directive.INSTRUCTION_SIZE,
                Directive.INTEGER_FORMAT,
                Directive.FLOAT_FORMAT,
                Directive.ENDIANNESS);
    }

    @Override
    protected void parse_main(ByteBuffer buffer, BHeader header, LHeaderParseState s) {
        parse_format(buffer, header, s);
        parse_tail(buffer);
        parse_instruction_size(buffer, header);
        parse_integer_size(buffer, header, s);
        parse_float_size(buffer, header, s);
        parse_number_format_53(buffer, header, s);
        s.integer = new BIntegerType54();
        s.sizeT = new BIntegerType54();
    }

    @Override
    public void write(OutputStream out, BHeader header, LHeader object) throws IOException {
        write_format(out, object);
        write_tail(out);
        write_instruction_size(out);
        out.write(header.linteger.size);
        out.write(header.lfloat.size);
        header.linteger.write(out, header, header.linteger.create(TEST_INTEGER));
        header.lfloat.write(out, header, header.lfloat.create(TEST_FLOAT));
    }

}
