package me.hydos.unluac;

import me.hydos.unluac.Configuration.Mode;
import me.hydos.unluac.assemble.Assembler;
import me.hydos.unluac.assemble.AssemblerException;
import me.hydos.unluac.decompile.Decompiler;
import me.hydos.unluac.decompile.Disassembler;
import me.hydos.unluac.decompile.Output;
import me.hydos.unluac.decompile.OutputProvider;
import me.hydos.unluac.bytecode.BHeader;
import me.hydos.unluac.bytecode.BFunction;
import me.hydos.unluac.util.FileUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;

@Deprecated // FIXME: combination of logic and CLI handling. Tisk tisk...
public class Main {
    public static final String VERSION = "1.2.3.511";

    public static void main(String[] args) {
        String fn = null;
        var config = new Configuration();
        for (var i = 0; i < args.length; i++) {
            var arg = args[i];
            if (arg.startsWith("-")) {
                // option
                switch (arg) {
                    case "--rawstring" -> config.rawstring = true;
                    case "--luaj" -> config.luaj = true;
                    case "--nodebug" -> config.variable = Configuration.VariableMode.NODEBUG;
                    case "--disassemble" -> config.mode = Mode.DISASSEMBLE;
                    case "--assemble" -> config.mode = Mode.ASSEMBLE;
                    case "--help" -> config.mode = Mode.HELP;
                    case "--version" -> config.mode = Mode.VERSION;
                    case "--output", "-o" -> {
                        if (i + 1 < args.length) {
                            config.output = args[i + 1];
                            i++;
                        } else {
                            error("option \"" + arg + "\" doesn't have an argument", true);
                        }
                    }
                    case "--opmap" -> {
                        if (i + 1 < args.length) {
                            config.opmap = args[i + 1];
                            i++;
                        } else {
                            error("option \"" + arg + "\" doesn't have an argument", true);
                        }
                    }
                    default -> error("unrecognized option: " + arg, true);
                }
            } else if (fn == null) {
                fn = arg;
            } else {
                error("too many arguments: " + arg, true);
            }
        }
        if (fn == null && config.mode != Mode.HELP && config.mode != Mode.VERSION) {
            error("no input file provided", true);
        } else {
            switch (config.mode) {
                case HELP -> help();
                case VERSION -> System.out.println(VERSION);
                case DECOMPILE -> {
                    BFunction lmain = null;
                    try {
                        lmain = file_to_function(fn, config);
                    } catch (IOException e) {
                        error(e.getMessage(), false);
                    }
                    var d = new Decompiler(lmain, null, -1);
                    var result = d.getResult();
                    d.print(result, config.getOutput());
                }
                case DISASSEMBLE -> {
                    BFunction lmain = null;
                    try {
                        lmain = file_to_function(fn, config);
                    } catch (IOException e) {
                        error(e.getMessage(), false);
                    }
                    var d = new Disassembler(lmain);
                    d.disassemble(config.getOutput());
                }
                case ASSEMBLE -> {
                    if (config.output == null) {
                        error("assembler mode requires an output file", true);
                    } else {
                        try {
                            var a = new Assembler(
                                    config,
                                    FileUtils.createSmartTextFileReader(new File(fn)),
                                    new FileOutputStream(config.output)
                            );
                            a.assemble();
                        } catch (IOException | AssemblerException e) {
                            error(e.getMessage(), false);
                        }
                    }
                }
                default -> throw new IllegalStateException();
            }
            System.exit(0);
        }
    }

    public static void error(String err, boolean usage) {
        print_unluac_string(System.err);
        System.err.print("  error: ");
        System.err.println(err);
        if (usage) {
            print_usage(System.err);
            System.err.println("For information about options, use option: --help");
        }
        System.exit(1);
    }

    public static void help() {
        print_unluac_string(System.out);
        print_usage(System.out);
        System.out.println("Available options are:");
        System.out.println("  --assemble       assemble given disassembly listing");
        System.out.println("  --disassemble    disassemble instead of decompile");
        System.out.println("  --nodebug        ignore debugging information in input file");
        System.out.println("  --opmap <file>   use opcode mapping specified in <file>");
        System.out.println("  --output <file>  output to <file> instead of stdout");
        System.out.println("  --rawstring      copy string bytes directly to output");
        System.out.println("  --luaj           emulate Luaj's permissive parser");
    }

    private static void print_unluac_string(PrintStream out) {
        out.println("unluac v" + VERSION);
    }

    private static void print_usage(PrintStream out) {
        out.println("  usage: java -jar unluac.jar [options] <file>");
    }

    private static BFunction file_to_function(String fn, Configuration config) throws IOException {
        var buffer = ByteBuffer.wrap(Files.readAllBytes(Paths.get(fn)))
                .order(ByteOrder.LITTLE_ENDIAN);
        var header = new BHeader(buffer, config);
        return header.main;
    }

    public static void decompile(String in, String out, Configuration config) throws IOException {
        var lmain = file_to_function(in, config);
        var d = new Decompiler(lmain, null, -1);
        var result = d.getResult();
        final var pout = new PrintStream(out);
        d.print(result, new Output(new OutputProvider() {

            @Override
            public void print(String s) {
                pout.print(s);
            }

            @Override
            public void print(byte b) {
                pout.write(b);
            }

            @Override
            public void println() {
                pout.println();
            }

        }));
        pout.flush();
        pout.close();
    }

    public static void assemble(String in, String out) throws IOException, AssemblerException {
        OutputStream outstream = new BufferedOutputStream(new FileOutputStream(new File(out)));
        var a = new Assembler(new Configuration(), FileUtils.createSmartTextFileReader(new File(in)), outstream);
        a.assemble();
        outstream.flush();
        outstream.close();
    }

    public static void disassemble(String in, String out) throws IOException {
        var lmain = file_to_function(in, new Configuration());
        var d = new Disassembler(lmain);
        final var pout = new PrintStream(out);
        d.disassemble(new Output(new OutputProvider() {

            @Override
            public void print(String s) {
                pout.print(s);
            }

            @Override
            public void print(byte b) {
                pout.print(b);
            }

            @Override
            public void println() {
                pout.println();
            }

        }));
        pout.flush();
        pout.close();
    }

}
