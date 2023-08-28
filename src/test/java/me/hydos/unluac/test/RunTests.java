package me.hydos.unluac.test;

import me.hydos.unluac.Configuration;

import java.io.IOException;

public class RunTests {

    public static void main(String[] args) throws IOException {
        var result = true;
        var report = new TestReport();
        var config = new Configuration();
        config.strict_scope = true;
        for (var spec : new LuaSpec[]{
                new LuaSpec(0x50),
                new LuaSpec(0x51),
                new LuaSpec(0x51, 4),
                new LuaSpec(0x52),
                new LuaSpec(0x53),
                new LuaSpec(0x54),
        }) {
            var uspec = new UnluacSpec();
            System.out.print(spec.id());
            result = result & TestFiles.suite.run(spec, uspec, report, config);
            System.out.println();
        }
        report.report(System.out);
        if (result) {
            System.exit(0);
        } else {
            System.exit(1);
        }
    }

}
