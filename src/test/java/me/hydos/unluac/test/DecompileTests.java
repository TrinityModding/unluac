package me.hydos.unluac.test;

import me.hydos.unluac.Configuration;
import me.hydos.unluac.Main;
import me.hydos.unluac.test.legacy.LuaSpec;
import me.hydos.unluac.test.legacy.cmd.LuaCompiler;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.ThrowingConsumer;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

class DecompileTests {
    private static final LuaSpec COMPILER_SPEC = new LuaSpec(0x54);
    private static final List<String> TESTS = List.of(
            "51_adjust02"
    );

    @TestFactory
    Stream<DynamicTest> generateTestCases() {
        ThrowingConsumer<String> testExecutor = input -> {
            var originalSrc = Paths.get("build/" + input + ".lua");
            var compiledSrc = Paths.get("build/" + input + ".blua");
            var decompiledSrc = Paths.get("build/" + input + "_decompiled.lua");

            // Save the src
            Files.deleteIfExists(originalSrc);
            Files.createFile(originalSrc);
            Files.write(originalSrc, Objects.requireNonNull(DecompileTests.class.getResourceAsStream("/" + input + ".lua"), input + ".lua doesnt exist").readAllBytes());
            // Compile the src
            LuaCompiler.compile(COMPILER_SPEC, originalSrc, compiledSrc);
            // Decompile the binary lua
            Main.decompile(compiledSrc.toAbsolutePath().toString(), decompiledSrc.toAbsolutePath().toString(), new Configuration());
            // Load and execute the compiled Lua bytecode
            try {
                var runtime = JsePlatform.standardGlobals();
                var chunk = runtime.loadfile(decompiledSrc.toAbsolutePath().toString());
                chunk.call();
            } catch (LuaError e) {
                throw new RuntimeException("Error loading or executing Lua bytecode", e);
            }
            // Extra (not mandatory) step: check for 1 to 1 source
            if (Files.mismatch(originalSrc, decompiledSrc) != -1)
                System.out.println("Warning: code does is not completely identical. You can most likely ignore this");
        };

        return DynamicTest.stream(TESTS.stream(), input -> input, testExecutor);
    }
}
