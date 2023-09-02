package me.hydos.unluac.decompile;

public interface OutputProvider {

    void print(String s);

    void print(byte b);

    void println();

    default void println(String s) {
        print(s + "\n");
    }
}
