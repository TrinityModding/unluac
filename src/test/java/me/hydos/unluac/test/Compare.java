package me.hydos.unluac.test;

import me.hydos.unluac.Configuration;
import me.hydos.unluac.parse.BHeader;
import me.hydos.unluac.parse.LFunction;
import me.hydos.unluac.parse.LLocal;
import me.hydos.unluac.parse.LObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

public class Compare {

    private final Mode mode;

    public Compare(Mode mode) {
        this.mode = mode;
    }

    /**
     * Determines if two files of lua bytecode are the same
     * (except possibly for line numbers).
     */
    public boolean bytecodeEqual(Path file1, Path file2) {
        var main1 = file_to_function(file1);
        var main2 = file_to_function(file2);
        return function_equal(main1, main2);
    }

    public boolean function_equal(LFunction f1, LFunction f2) {
        if (f1.maximumStackSize != f2.maximumStackSize) {
            return false;
        }
        if (f1.numParams != f2.numParams) {
            return false;
        }
        if (f1.numUpvalues != f2.numUpvalues) {
            return false;
        }
        if (f1.vararg != f2.vararg) {
            return false;
        }
        if (f1.code.length != f2.code.length) {
            return false;
        }
        for (var i = 0; i < f1.code.length; i++) {
            if (f1.code[i] != f2.code[i]) {
                return false;
            }
        }
        if (f1.constants.length != f2.constants.length) {
            return false;
        }
        for (var i = 0; i < f1.constants.length; i++) {
            if (!object_equal(f1.constants[i], f2.constants[i])) {
                return false;
            }
        }
        if (f1.locals.length != f2.locals.length) {
            return false;
        }
        for (var i = 0; i < f1.locals.length; i++) {
            if (!local_equal(f1.locals[i], f2.locals[i])) {
                return false;
            }
        }
        if (f1.upvalues.length != f2.upvalues.length) {
            return false;
        }
        for (var i = 0; i < f1.upvalues.length; i++) {
            if (!f1.upvalues[i].equals(f2.upvalues[i])) {
                return false;
            }
        }
        if (f1.functions.length != f2.functions.length) {
            return false;
        }
        for (var i = 0; i < f1.functions.length; i++) {
            if (!function_equal(f1.functions[i], f2.functions[i])) {
                return false;
            }
        }
        if (mode == Mode.FULL) {
            if (!f1.name.equals(f2.name)) {
                return false;
            }
            if (f1.linedefined != f2.linedefined) {
                return false;
            }
            if (f1.lastlinedefined != f2.lastlinedefined) {
                return false;
            }
            if (f1.lines.length != f2.lines.length) {
                return false;
            }
            for (var i = 0; i < f1.lines.length; i++) {
                if (f1.lines[i] != f2.lines[i]) {
                    return false;
                }
            }
            if ((f1.abslineinfo == null) != (f2.abslineinfo == null)) {
                return false;
            }
            if (f1.abslineinfo != null) {
                if (f1.abslineinfo.length != f2.abslineinfo.length) {
                    return false;
                }
                for (var i = 0; i < f1.abslineinfo.length; i++) {
                    if (!f1.abslineinfo[i].equals(f2.abslineinfo[i])) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean object_equal(LObject o1, LObject o2) {
        return o1.equals(o2);
    }

    public boolean local_equal(LLocal l1, LLocal l2) {
        if (l1.start != l2.start) return false;
        if (l1.end != l2.end) return false;
        return l1.name.equals(l2.name);
    }

    public LFunction file_to_function(Path path) {
        try {
            return new BHeader(
                    ByteBuffer.wrap(Files.readAllBytes(path)).order(ByteOrder.LITTLE_ENDIAN),
                    new Configuration()
            ).main;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public enum Mode {
        NORMAL,
        FULL,
    }
}
