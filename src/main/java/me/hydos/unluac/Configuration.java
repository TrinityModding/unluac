package me.hydos.unluac;

import me.hydos.unluac.decompile.FileOutputProvider;
import me.hydos.unluac.decompile.Output;

import java.io.FileOutputStream;
import java.io.IOException;

public class Configuration {

    public boolean rawstring;
    public Mode mode;
    public VariableMode variable;
    public boolean strict_scope;
    public boolean luaj;
    public String opmap;
    public String output;

    public Configuration() {
        rawstring = false;
        mode = Mode.DECOMPILE;
        variable = VariableMode.DEFAULT;
        strict_scope = false;
        luaj = false;
        opmap = null;
        output = null;
    }

    public Configuration(Configuration other) {
        rawstring = other.rawstring;
        mode = other.mode;
        variable = other.variable;
        strict_scope = other.strict_scope;
        opmap = other.opmap;
        output = other.output;
    }

    public Output getOutput() {
        if (output != null) {
            try {
                return new Output(new FileOutputProvider(new FileOutputStream(output)));
            } catch (IOException e) {
                Main.error(e.getMessage(), false);
                return null;
            }
        } else {
            return new Output();
        }
    }

    public enum Mode {
        DECOMPILE,
        DISASSEMBLE,
        ASSEMBLE,
        HELP,
        VERSION,
    }

    public enum VariableMode {
        NODEBUG,
        DEFAULT,
        FINDER,
    }

}
