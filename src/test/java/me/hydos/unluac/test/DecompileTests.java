package me.hydos.unluac.test;

import me.hydos.unluac.Configuration;
import me.hydos.unluac.Main;
import me.hydos.unluac.bytecode.BFunction;
import me.hydos.unluac.bytecode.BHeader;
import me.hydos.unluac.decompile.Decompiler;
import me.hydos.unluac.decompile.NewDecompiler;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.ThrowingConsumer;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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


            var newDecompiler = new NewDecompiler(readBytecode(Files.readAllBytes(compiledSrc), new Configuration()), null, -1);
            var oldDecompiler = new Decompiler(readBytecode(Files.readAllBytes(compiledSrc), new Configuration()));

            var newResult = newDecompiler.getResult();
            var oldResult = oldDecompiler.decompile();
            System.out.println("ok");

            // FIXME: old decompile test. We have the lua source tree kinda now so we can do better
/*            Main.decompile(compiledSrc.toAbsolutePath().toString(), decompiledSrc.toAbsolutePath().toString(), new Configuration());
            Main.disassemble(compiledSrc.toAbsolutePath().toString(), disassembledSrc.toAbsolutePath().toString());
            // Extra (not mandatory) step: check for 1 to 1 source
            if (compareFiles(originalSrc, decompiledSrc)) return;
            System.out.println("Warning: code does is not completely identical. You can most likely ignore this");
            // Load and execute the compiled Lua bytecode in another thread
            runTest(decompiledSrc);*/
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

    private static BFunction readBytecode(byte[] bytecode, Configuration config) {
        var header = new BHeader(ByteBuffer.wrap(bytecode).order(ByteOrder.LITTLE_ENDIAN), config);
        return header.main;
    }
}
