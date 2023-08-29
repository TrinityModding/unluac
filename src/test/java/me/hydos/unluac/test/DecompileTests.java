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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

class DecompileTests {
    private static final LuaSpec COMPILER_SPEC = new LuaSpec(0x54);
    private static final List<String> TESTS = List.of(
            "assign",
            "literal",
            "number01",
            "number02",
            "number03",
            "number04",
            "multiassign",
            "multiassign02",
            "multiassign03",
            "multiassign04",
            "multiassign05",
            "multiassign06",
            "multiassign07",
            "multiassign08",
            "multiassign09",
            "multiassign10",
            "multiassign11",
            "multiassign12",
            "expression",
            "expression02",
            "functioncall",
            "self01",
            "literallist",
            "multiliteraltarget",
            "closure",
            "ellipsis03",
            "ifthen",
            "condition",
            "condition02",
            "condition03",
            "condition04",
            "nestedif",
            "nestedif02",
            "ifthenelse",
            "while",
            "while02",
            "while03",
            "while04",
            "while05",
            "while06",
            "while07",
            "while08",
            "repeat",
            "repeat02",
            "repeat03",
            "if01",
            "if02",
            "if03",
            "if04",
            "if05",
            "if06",
            "if07",
            "else01",
            "else02",
            "else03",
            "else04",
            "else05",
            "else06",
            "else07",
            "else08",
            "booleanassign01",
            "booleanassign02",
            "booleanassign03",
            "booleanassign04",
            "booleanassign05",
            "booleanassign06",
            "booleanassign07",
            "booleanassign08",
            "booleanassign09",
            "booleanassign10",
            "booleanassign11",
            "booleanassign12",
            "booleanassign13",
            "booleanassign14",
            "booleanassign15",
            "booleanassign16",
            "booleanassign17",
            "booleanassign18",
            "booleanassign19",
            "booleanassign20",
            "booleanassign21",
            "booleanassign22",
            "booleanassign23",
            "booleanassign24",
            "booleanassign25",
            "booleanassign26",
            "booleanassign27",
            "booleanassign28",
            "booleanassign29",
            "booleanselfassign01",
            "booleanexpression01",
            "booleanexpression02",
            "booleanexpression03",
            "booleanexpression04",
            "booleanexpression05",
            "booleanexpression06",
            "booleanexpression07",
            "booleanexpression08",
            "booleanexpression09",
            "booleanexpression10",
            "booleanexpression11",
            "booleanmultiassign01",
            "booleanmultiassign02",
            "compareassign01",
            "compareassign02",
            "compareexpression",
            "compareexpression02",
            "combinebexpression01",
            "combinebexpression02",
            "combinebexpression03",
            "combinebexpression04",
            "combinebexpression05",
            "combinebexpression06",
            "combinebexpression07",
            "combinebassign01",
            "combinebassign02",
            "combinebassign03",
            "combinebassign04",
            "combinebassign05",
            "combinebassign07",
            "complexassign01",
            "complexassign02",
            "complexassign03",
            "compare01",
            "compareorder01",
            "compareorder02",
            "compareorder03",
            "compareorder04",
            "compareorder05",
            "compareorder06",
            "compareorder07",
            "compareorder08",
            "table01",
            "table02",
            "table03",
            "table06",
            "table07",
            "table08",
            "localfunction01",
            "localfunction02",
            "localfunction03",
            "localfunction04",
            "declare",
            "declare02",
            "declare03",
            "declare04",
            "declare05",
            "adjust01",
            "adjust04",
            "adjust05",
            "adjust06",
            "final01",
            "final02",
            "doend01",
            "doend02",
            "doend03",
            "doend04",
            "doend05",
            "doend06",
            "doend07",
            "doend08",
            "control01",
            "control02",
            "control03",
            "control04",
            "control05",
            "control06",
            "loop01",
            "loop02",
            "loop03",
            "loop04",
            "method01",
            "method02",
            "inlinefunction01",
            "inlinefunction02",
            "inlineconstant01",
            "string01",
            "string02",
            "string04",
            "string05",
            "upvalue01",
            "upvalue02",
            "upvalue03",
            "upvalue04",
            "upvalue05",
            "upvalue06",
            "break01",
            "break02",
            "break03",
            "break04",
            "break05",
            "break06",
            "break07",
            "break08",
            "break09",
            "break10",
            "break11",
            "break12",
            "break13",
            "break14",
            "break15",
            "break16",
            "break17",
            "break19",
            "break20",
            "break21",
            "break22",
            "break23",
            "break24",
            "close01",
            "close02",
            "close03",
            "close04",
            "close05",
            "close06",
            "close07",
            "close08",
            "always01",
            "always02",
            "always03",
            "always04",
            "once01",
            "once02",
            "once03",
            "once04",
            "once05",
            "unused01",
            "report01a",
            "report01b",
            "report01c",
            "report01d",
            "report01_full",
            "report02",
            "report02a",
            "report02b",
            "report02c",
            "report02d",
            "report02e",
            "report03",
            "report04",
            "report05",
            "report06",
            "scope02",
            "scope03",
            "51_expression",
            "51_expression2",
            "51_expression03",
            "51_string03",
            "51_ellipsis",
            "51_ellipsis02",
            "51_adjust02",
            "51_adjust03",
            "51_method03",
            "52_loadkx01",
            "52_goto01",
            "52_goto02",
            "52_goto03",
            "52_goto04",
            "52_goto05",
            "52_goto06",
            "52_goto08",
            "53_expression",
            "53_expression02",
            "54_tbc01"
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
            // Compile the src with native lua
            LuaCompiler.compile(COMPILER_SPEC, originalSrc, compiledSrc);
            // Decompile the binary lua
            Main.decompile(compiledSrc.toAbsolutePath().toString(), decompiledSrc.toAbsolutePath().toString(), new Configuration());

            // Load and execute the compiled Lua bytecode in another thread
            var exception = new AtomicReference<RuntimeException>(null);
            var test = runTest(decompiledSrc, exception);
            test.join(2000); // If the test doesn't finish by now it passed
            if (exception.get() != null) throw exception.get();

            // Extra (not mandatory) step: check for 1 to 1 source
            if (Files.mismatch(originalSrc, decompiledSrc) != -1)
                System.out.println("Warning: code does is not completely identical. You can most likely ignore this");
        };

        return DynamicTest.stream(TESTS.stream(), input -> input, testExecutor);
    }

    private static Thread runTest(Path decompiledSrc, AtomicReference<RuntimeException> exception) {
        var test = new Thread(() -> {
            try {
                var runtime = JsePlatform.standardGlobals();
                var chunk = runtime.loadfile(decompiledSrc.toAbsolutePath().toString());
                chunk.call();
            } catch (LuaError e) {
                exception.set(new RuntimeException("Error loading or executing Lua bytecode", e));
            }
        });
        test.start();
        return test;
    }
}
