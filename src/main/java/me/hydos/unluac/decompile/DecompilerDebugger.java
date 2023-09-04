package me.hydos.unluac.decompile;

import me.hydos.unluac.decompile.core.Decompiler;
import me.hydos.unluac.decompile.core.Output;
import me.hydos.unluac.decompile.core.OutputProvider;

import java.util.function.BiConsumer;

/**
 * Unused as I use it in evaluation to verify what I'm working with and how it looks
 */
public class DecompilerDebugger {

    public static String print(Decompiler decompiler, BiConsumer<Decompiler, Output> print) {
        var string = new StringBuilder();
        var provider = new OutputProvider() {

            @Override
            public void print(String s) {
                string.append(s);
            }

            @Override
            public void print(byte b) {
                string.append(b);
            }

            @Override
            public void println() {
                string.append("\n");
            }
        };

        print.accept(decompiler, new Output(provider));
        return string.toString();
    }
}
