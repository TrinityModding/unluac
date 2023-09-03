package me.hydos.unluac.test;

import me.hydos.unluac.Configuration;
import me.hydos.unluac.Main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TempTests {
    private static final LuaSpec COMPILER_SPEC = new LuaSpec(0x54, true);

    public static void main(String[] args) throws IOException {
        var input = "assignmentTests";
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
    }
}
