package me.hydos.unluac.test.cmd;

import me.hydos.unluac.test.LuaSpec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Uses lua on the command line to verify compilation
 */
public class LuaCompiler {

    public static void compile(LuaSpec spec, Path in, Path out) throws IOException {
        try {
            var command = new ArrayList<>(List.of(spec.getLuaCName()));
            command.addAll(spec.getArgs());
            command.add("-o");
            command.add(out.toAbsolutePath().toString());
            command.add(in.toAbsolutePath().toString());
            new ProcessBuilder(command)
                    .directory(null)
                    .inheritIO()
                    .start()
                    .waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException("luac failed on file: " + in);
        }
    }
}
