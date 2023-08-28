package me.hydos.unluac.assemble;

import java.io.IOException;
import java.io.InputStream;

public class Tokenizer {

    private final StringBuilder b;
    private final InputStream in;

    public Tokenizer(InputStream in) {
        this.in = in;
        b = new StringBuilder();
    }

    public String next() throws IOException {
        b.setLength(0);

        var inToken = false;
        var inString = false;
        var inComment = false;
        var isLPrefix = false;
        var inEscape = false;

        for (; ; ) {
            var code = in.read();
            if (code == -1) break;
            var c = (char) code;
            //if(c == '\n') System.out.println("line");
            if (inString) {
                if (c == '\\' && !inEscape) {
                    inEscape = true;
                    b.append(c);
                } else if (c == '"' && !inEscape) {
                    b.append(c);
                    break;
                } else {
                    inEscape = false;
                    b.append(c);
                }
            } else if (inComment) {
                if (c == '\n' || c == '\r') {
                    inComment = false;
                    if (inToken) {
                        break;
                    }
                }
            } else if (c == ';') {
                inComment = true;
            } else if (Character.isWhitespace(c)) {
                if (inToken) {
                    break;
                }
            } else {
                if ((!inToken || isLPrefix) && c == '"') {
                    inString = true;
                } else isLPrefix = !inToken && c == 'L';
                inToken = true;
                b.append(c);
            }
        }

        //System.out.println("token: <" + b.toString() + ">");

        if (b.length() == 0) {
            return null;
        } else {
            return b.toString();
        }
    }

}
