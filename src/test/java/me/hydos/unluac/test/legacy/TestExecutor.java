package me.hydos.unluac.test.legacy;

import me.hydos.unluac.Configuration;
import me.hydos.unluac.Main;
import me.hydos.unluac.assemble.AssemblerException;
import me.hydos.unluac.test.legacy.cmd.LuaCompiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

public class TestExecutor {
    private static final Path WORKING_DIR = Paths.get("build/test");
    private static final Path LUA_COMP = WORKING_DIR.resolve("luac.out");
    private static final Path DECOMPILED = WORKING_DIR.resolve("unluac.out");
    private static final Path UNLUAC_COMP = WORKING_DIR.resolve("test.out");
    private final List<UnLuaCTest> tests;

    public TestExecutor(List<UnLuaCTest> files) {
        this.tests = files;
    }

    private TestResult test(LuaSpec spec, UnLuaCSpec uspec, Path file, Configuration config) {
        try {
            LuaCompiler.compile(spec, file, LUA_COMP);

            uspec.run(LUA_COMP, DECOMPILED, config);
            if (!uspec.disassemble) LuaCompiler.compile(spec, DECOMPILED, UNLUAC_COMP);
            else Main.assemble(DECOMPILED.toAbsolutePath().toString(), UNLUAC_COMP.toAbsolutePath().toString());
            Compare compare;
            if (!uspec.disassemble) compare = new Compare(Compare.Mode.NORMAL);
            else compare = new Compare(Compare.Mode.FULL);

            Files.delete(file);
            if(!compare.bytecodeEqual(LUA_COMP, UNLUAC_COMP)) {
                Main.disassemble(LUA_COMP.toAbsolutePath().toString(), Paths.get("expected.lua").toAbsolutePath().toString());
                Main.disassemble(UNLUAC_COMP.toAbsolutePath().toString(), Paths.get("result.lua").toAbsolutePath().toString());
                throw new RuntimeException("Bytecode does not match on test " + file.getFileName().toString());
            }
            return TestResult.OK;
        } catch (IOException e) {
            throw new RuntimeException(file.getFileName() + " test invalid", e);
        } catch (AssemblerException e) {
            throw new RuntimeException(file.getFileName() + " test failed", e);
        }
    }

    private TestResult testc(LuaSpec spec, UnLuaCSpec uspec, Path file, Configuration config) {
        try {
            uspec.run(file, DECOMPILED, config);
            LuaCompiler.compile(spec, DECOMPILED, UNLUAC_COMP);
            var compare = new Compare(Compare.Mode.NORMAL);

            if(!compare.bytecodeEqual(file, UNLUAC_COMP))
                throw new RuntimeException("Bytecode does not match on test " + file.getFileName().toString());
            else return TestResult.OK;
        } catch (IOException e) {
            return TestResult.FAILED;
        }
    }

    public void run(LuaSpec spec, UnLuaCSpec uspec, Configuration base) throws IOException {
        Files.createDirectories(WORKING_DIR);

        for (var test : tests) {
            if (spec.compatible(test.name)) {
                var config = configure(test, base);
                var result = test(spec, uspec, getTestFile(test.name), config);
                switch (result) {
                    case OK -> System.out.println("Passed: " + test.name);
                    case SKIPPED -> System.out.println("Skipped: " + test.name);
                    case FAILED -> throw new RuntimeException("Failed: " + test.name);
                }
            }
        }
    }

    private Path getTestFile(String name) {
        try {
            var file = WORKING_DIR.resolve(name + ".lua");
            Files.deleteIfExists(file);
            Files.createFile(file);
            Files.write(file, Objects.requireNonNull(TestExecutor.class.getResourceAsStream("/" + name + ".lua"), name + ".lua" + " was not found :(").readAllBytes());
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Configuration configure(UnLuaCTest testfile, Configuration config) {
        Configuration modified = null;
        if (testfile.getFlag(UnLuaCTest.RELAXED_SCOPE) && config.strict_scope) {
            modified = new Configuration(config);
            modified.strict_scope = false;
        }
        return modified != null ? modified : config;
    }
}
