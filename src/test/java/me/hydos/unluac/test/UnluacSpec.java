package me.hydos.unluac.test;

import me.hydos.unluac.Configuration;
import me.hydos.unluac.Main;

import java.io.IOException;

public class UnluacSpec {

    public boolean disassemble;

    public UnluacSpec() {
        disassemble = false;
    }

    public void run(String in, String out, Configuration config) throws IOException {
        if (!disassemble) {
            Main.decompile(in, out, config);
        } else {
            Main.disassemble(in, out);
        }
    }

}
