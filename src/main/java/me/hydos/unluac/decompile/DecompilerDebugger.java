package me.hydos.unluac.decompile;

import java.util.function.BiConsumer;

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
