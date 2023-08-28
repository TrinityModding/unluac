package me.hydos.unluac.test;

import me.hydos.unluac.Configuration;

import java.io.IOException;

public class RunTest {

    public static void main(String[] args) throws IOException {
        var spec = new LuaSpec(0x54);
        var uspec = new UnluacSpec();
        //uspec.disassemble = true;
        var config = new Configuration();
        config.strict_scope = true;

        if (TestFiles.suite.run(spec, uspec, args[0], false, config)) {
            System.exit(0);
        } else {
            System.exit(1);
        }
    }
}
