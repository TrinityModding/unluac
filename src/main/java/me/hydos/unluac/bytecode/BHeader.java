package me.hydos.unluac.bytecode;

import me.hydos.unluac.Configuration;
import me.hydos.unluac.Configuration.Mode;
import me.hydos.unluac.Version;
import me.hydos.unluac.assemble.Tokenizer;
import me.hydos.unluac.decompile.core.BytecodeDecoder;
import me.hydos.unluac.decompile.core.Op;
import me.hydos.unluac.decompile.core.OpcodeMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;


public class BHeader {

    private static final byte[] signature = {
            0x1B, 0x4C, 0x75, 0x61,
    };

    public final boolean debug = false;

    public final Configuration config;
    public final Version version;
    public final LHeader lheader;
    public final LHeaderType lheader_type;
    public final BIntegerType integer;
    public final BIntegerType sizeT;
    public final LBooleanType bool;
    public final LNumberType number;
    public final LNumberType linteger;
    public final LNumberType lfloat;
    public final LStringType string;
    public final LConstantType constant;
    public final LAbsLineInfoType abslineinfo;
    public final LLocalType local;
    public final LUpvalueType upvalue;
    public final LFunctionType function;
    public final BytecodeDecoder extractor;
    public final OpcodeMap opmap;

    public final BFunction main;

    public BHeader(Version version, LHeader lheader) {
        this(version, lheader, null);
    }

    public BHeader(Version version, LHeader lheader, BFunction main) {
        this.config = null;
        this.version = version;
        this.lheader = lheader;
        this.lheader_type = version.getLHeaderType();
        integer = lheader.integer;
        sizeT = lheader.sizeT;
        bool = lheader.bool;
        number = lheader.number;
        linteger = lheader.linteger;
        lfloat = lheader.lfloat;
        string = lheader.string;
        constant = lheader.constant;
        abslineinfo = lheader.abslineinfo;
        local = lheader.local;
        upvalue = lheader.upvalue;
        function = lheader.function;
        extractor = lheader.extractor;
        opmap = version.getOpcodeMap();
        this.main = main;
    }

    public BHeader(ByteBuffer buffer, Configuration config) {
        this.config = config;
        // 4 byte Lua signature
        for (var b : signature) {
            if (buffer.get() != b) {
                throw new IllegalStateException("The input file does not have the signature of a valid Lua file.");
            }
        }

        var versionNumber = 0xFF & buffer.get();
        var major = versionNumber >> 4;
        var minor = versionNumber & 0x0F;

        version = Version.getVersion(config, major, minor);
        if (version == null) {
            throw new IllegalStateException("The input chunk's Lua version is " + major + "." + minor + "; unluac can only handle Lua 5.0 - Lua 5.4.");
        }

        lheader_type = version.getLHeaderType();
        lheader = lheader_type.parse(buffer, this);
        integer = lheader.integer;
        sizeT = lheader.sizeT;
        bool = lheader.bool;
        number = lheader.number;
        linteger = lheader.linteger;
        lfloat = lheader.lfloat;
        string = lheader.string;
        constant = lheader.constant;
        abslineinfo = lheader.abslineinfo;
        local = lheader.local;
        upvalue = lheader.upvalue;
        function = lheader.function;
        extractor = lheader.extractor;

        if (config.opmap != null) {
            try {
                var t = new Tokenizer(new FileInputStream(new File(config.opmap)));
                String tok;
                Map<Integer, Op> useropmap = new HashMap<>();
                while ((tok = t.next()) != null) {
                    if (tok.equals(".op")) {
                        tok = t.next();
                        if (tok == null) throw new IllegalStateException("Unexpected end of opmap file.");
                        int opcode;
                        try {
                            opcode = Integer.parseInt(tok);
                        } catch (NumberFormatException e) {
                            throw new IllegalStateException("Excepted number in opmap file, got \"" + tok + "\".");
                        }
                        tok = t.next();
                        if (tok == null) throw new IllegalStateException("Unexpected end of opmap file.");
                        var op = version.getOpcodeMap().get(tok);
                        if (op == null)
                            throw new IllegalStateException("Unknown op name \"" + tok + "\" in opmap file.");
                        useropmap.put(opcode, op);
                    } else {
                        throw new IllegalStateException("Unexpected token \"" + tok + "\" + in opmap file.");
                    }
                }
                opmap = new OpcodeMap(useropmap);
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage());
            }
        } else {
            opmap = version.getOpcodeMap();
        }

        var upvalues = -1;
        if (versionNumber >= 0x53) {
            upvalues = 0xFF & buffer.get();
            if (debug) {
                System.out.println("-- main chunk upvalue count: " + upvalues);
            }
            // TODO: check this value
        }
        main = function.parse(buffer, this);
        if (upvalues >= 0) {
            if (main.numUpvalues != upvalues) {
                throw new IllegalStateException("The main chunk has the wrong number of upvalues: " + main.numUpvalues + " (" + upvalues + " expected)");
            }
        }
        if (main.numUpvalues >= 1 && versionNumber >= 0x52 && (main.upvalues[0].name == null || main.upvalues[0].name.isEmpty()) && config.mode == Mode.DECOMPILE) {
            main.upvalues[0].name = "_ENV";
        }
        main.setDepth(1);
    }

    public void write(OutputStream out) throws IOException {
        out.write(signature);
        var major = version.getVersionMajor();
        var minor = version.getVersionMinor();
        var versionNumber = (major << 4) | minor;
        out.write(versionNumber);
        version.getLHeaderType().write(out, this, lheader);
        if (version.useupvaluecountinheader.get()) {
            out.write(main.numUpvalues);
        }
        function.write(out, this, main);
    }

}
