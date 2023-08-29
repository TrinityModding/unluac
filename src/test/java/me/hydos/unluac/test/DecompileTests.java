package me.hydos.unluac.test;

import me.hydos.unluac.Configuration;
import me.hydos.unluac.Main;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.ThrowingConsumer;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

class DecompileTests {
    private static final LuaSpec COMPILER_SPEC = new LuaSpec(0x54, true);
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    private static final List<String> TESTS = List.of(
//            "class",
            "jump01"
    );

    @TestFactory
    Stream<DynamicTest> generateTestCases() {
        ThrowingConsumer<String> testExecutor = input -> {
            var originalSrc = Paths.get("src/test/resources/" + input + ".lua");
            var compiledSrc = Paths.get(".gradle/test" + input + ".blua");
            var decompiledSrc = Paths.get("src/test/resources/" + input + ".decompiled.lua");
            var disassembledSrc = Paths.get("src/test/resources/" + input + ".disassembled.lua");

            // Compile the src with native lua
            LuaCompiler.compile(COMPILER_SPEC, originalSrc, compiledSrc);
            // Decompile the binary lua
            Main.decompile(compiledSrc.toAbsolutePath().toString(), decompiledSrc.toAbsolutePath().toString(), new Configuration());
            Main.disassemble(compiledSrc.toAbsolutePath().toString(), disassembledSrc.toAbsolutePath().toString());
            // Extra (not mandatory) step: check for 1 to 1 source
            if (compareFiles(originalSrc, decompiledSrc)) return;
            System.out.println("Warning: code does is not completely identical. You can most likely ignore this");
            // Load and execute the compiled Lua bytecode in another thread
            runTest(decompiledSrc);
        };

        return DynamicTest.stream(TESTS.stream(), input -> input, testExecutor);
    }

    private boolean compareFiles(Path aPath, Path bPath) {
        try {
            var aSrc = Files.readString(aPath).replace("\r", "")
                    .replace("\n", "")
                    .replace(" ", "");
            var bSrc = Files.readString(bPath).replace("\r", "")
                    .replace("\n", "")
                    .replace(" ", "");
            return aSrc.equals(bSrc);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void runTest(Path decompiledSrc) {
        var exception = new AtomicReference<RuntimeException>(null);
        var future = EXECUTOR.submit(() -> {
            try {
                var runtime = JsePlatform.standardGlobals();
                var chunk = runtime.loadfile(decompiledSrc.toAbsolutePath().toString());
                chunk.call();
            } catch (LuaError e) {
                exception.set(new RuntimeException("Error loading or executing Lua bytecode", e));
            }
        });

        var startTime = System.currentTimeMillis();
        while (!future.isDone()) if (System.currentTimeMillis() - startTime > 1000) break;
        if (exception.get() != null) throw exception.get();
    }
}
