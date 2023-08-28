package me.hydos.unluac.test;

import me.hydos.unluac.Configuration;
import me.hydos.unluac.Main;

import java.io.IOException;
import java.nio.file.Path;

public class UnLuaCSpec {

    public boolean disassemble;

    public UnLuaCSpec() {
        this.disassemble = false;
    }

    // TODO: do this better
    public void run(Path in, Path out, Configuration config) throws IOException {
        if (!disassemble) {
            Main.decompile(in.toAbsolutePath().toString(), out.toAbsolutePath().toString(), config);
        } else {
            Main.disassemble(in.toAbsolutePath().toString(), out.toAbsolutePath().toString());
        }
    }
}
