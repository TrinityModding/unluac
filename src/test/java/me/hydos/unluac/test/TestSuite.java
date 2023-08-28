package me.hydos.unluac.test;

import me.hydos.unluac.Configuration;
import me.hydos.unluac.Main;
import me.hydos.unluac.assemble.AssemblerException;
import me.hydos.unluac.test.cmd.LuaCompiler;

import java.io.File;
import java.io.IOException;

public class TestSuite {

    private static final String working_dir = "build/test";
    private static final String compiled = "luac.out";
    private static final String decompiled = "unluac.out";
    private static final String recompiled = "test.out";

    private final String name;
    private final String path;
    private final TestFile[] files;
    private final String ext = ".lua";

    public TestSuite(String name, String path, TestFile[] files) {
        this.name = name;
        this.path = path;
        this.files = files;
    }

    public String testName(LuaSpec spec, String file) {
        if (name == null) return spec.id() + ": " + file;
        else return spec.id() + ": " + name + "/" + file.replace('\\', '/');
    }

    private TestResult test(LuaSpec spec, UnluacSpec uspec, String file, Configuration config) {
        try {
            LuaCompiler.compile(spec, file, working_dir + compiled);
        } catch (IOException e) {
            return TestResult.SKIPPED;
        }
        try {
            uspec.run(working_dir + compiled, working_dir + decompiled, config);
            if (!uspec.disassemble) {
                LuaCompiler.compile(spec, working_dir + decompiled, working_dir + recompiled);
            } else {
                Main.assemble(working_dir + decompiled, working_dir + recompiled);
            }
            Compare compare;
            if (!uspec.disassemble) {
                compare = new Compare(Compare.Mode.NORMAL);
            } else {
                compare = new Compare(Compare.Mode.FULL);
            }
            return compare.bytecode_equal(working_dir + compiled, working_dir + recompiled) ? TestResult.OK : TestResult.FAILED;
        } catch (IOException e) {
            return TestResult.FAILED;
        } catch (RuntimeException | AssemblerException e) {
            e.printStackTrace();
            return TestResult.FAILED;
        }
    }

    private TestResult testc(LuaSpec spec, UnluacSpec uspec, String file, Configuration config) {
        try {
            uspec.run(file, working_dir + decompiled, config);
            LuaCompiler.compile(spec, working_dir + decompiled, working_dir + recompiled);
            var compare = new Compare(Compare.Mode.NORMAL);
            return compare.bytecode_equal(file, working_dir + recompiled) ? TestResult.OK : TestResult.FAILED;
        } catch (IOException e) {
            return TestResult.FAILED;
        } catch (RuntimeException e) {
            e.printStackTrace();
            return TestResult.FAILED;
        }
    }

    public boolean run(LuaSpec spec, UnluacSpec uspec, TestReport report, Configuration base) throws IOException {
        var failed = 0;
        var working = new File(working_dir);
        if (!working.exists()) {
            working.mkdir();
        }
        for (var testfile : files) {
            var name = testfile.name;
            if (spec.compatible(name)) {
                var config = configure(testfile, base);
                var result = test(spec, uspec, path + name + ext, config);
                report.result(testName(spec, name), result);
                switch (result) {
                    case OK -> System.out.print(".");
                    case SKIPPED -> System.out.print(",");
                    default -> {
                        System.out.print("!");
                        failed++;
                    }
                }
            }
        }
        return failed == 0;
    }

    public boolean run(LuaSpec spec, UnluacSpec uspec, String file, boolean compiled, Configuration config) throws IOException {
        var passed = 0;
        var skipped = 0;
        var failed = 0;
        var working = new File(working_dir);
        if (!working.exists()) {
            working.mkdir();
        }
        {
            String full;
            if (!file.contains("/")) {
                full = path + file + ext;
            } else {
                full = file;
            }
            TestResult result;
            if (!compiled) {
                result = test(spec, uspec, full, config);
            } else {
                result = testc(spec, uspec, full, config);
            }
            switch (result) {
                case OK -> {
                    System.out.println("Passed: " + file);
                    passed++;
                }
                case SKIPPED -> {
                    System.out.println("Skipped: " + file);
                    skipped++;
                }
                default -> {
                    System.out.println("Failed: " + file);
                    failed++;
                }
            }
        }
        if (failed == 0 && skipped == 0) {
            System.out.println(spec.getLuaCName() + ": All tests passed!");
        } else {
            System.out.println(spec.getLuaCName() + ": Failed " + failed + " of " + (failed + passed) + " tests, skipped " + skipped + " tests.");
        }
        return failed == 0;
    }

    private Configuration configure(TestFile testfile, Configuration config) {
        Configuration modified = null;
        if (testfile.getFlag(TestFile.RELAXED_SCOPE) && config.strict_scope) {
            modified = new Configuration(config);
            modified.strict_scope = false;
        }
        return modified != null ? modified : config;
    }
}
