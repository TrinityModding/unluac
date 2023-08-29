package me.hydos.unluac.parse;

import java.nio.ByteBuffer;

public class LSourceLines {

    public static LSourceLines parse(ByteBuffer buffer) {
        var number = buffer.getInt();
        while (number-- > 0) {
            buffer.getInt();
        }
        return null;
    }

}