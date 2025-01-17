package me.hydos.unluac.test;

import me.hydos.unluac.Configuration;
import me.hydos.unluac.Main;
import me.hydos.unluac.bytecode.BFunction;
import me.hydos.unluac.bytecode.BHeader;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.ThrowingConsumer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

class DecompileTests {
    private static final String MAIN = "F:/PokemonScarlet/arc/script/lua/bin/release/main/main.blua";
    private static final LuaSpec COMPILER_SPEC = new LuaSpec(0x54, true);
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    private static final List<String> TESTS = List.of(
            "class",
            "jump01",
            "assignmentTests",
            "action1",
            "action2"
    );

    @TestFactory
    Stream<DynamicTest> generateTestCases() {
        ThrowingConsumer<String> testExecutor = input -> {
            var originalSrc = Paths.get("src/test/resources/" + input + ".lua");
            var compiledSrc = Paths.get(".gradle/test" + input + ".blua");
            var decompiledSrc = Paths.get("src/test/resources/" + input + ".decompiled.lua");
            var disassembledSrc = Paths.get("src/test/resources/" + input + ".disassembled.lua");

            // Compile the src with native lua
            Files.deleteIfExists(compiledSrc);
            LuaCompiler.compile(COMPILER_SPEC, originalSrc, compiledSrc);
            // Decompile the binary lua
            Main.disassemble(compiledSrc.toAbsolutePath().toString(), disassembledSrc.toAbsolutePath().toString());
            Main.decompile(compiledSrc.toAbsolutePath().toString(), decompiledSrc.toAbsolutePath().toString(), new Configuration());
        };

        return DynamicTest.stream(TESTS.stream(), input -> input, testExecutor);
    }

    private static BFunction readBytecode(byte[] bytecode, Configuration config) {
        var header = new BHeader(ByteBuffer.wrap(bytecode).order(ByteOrder.LITTLE_ENDIAN), config);
        return header.main;
    }
}
