package me.hydos.unluac.decompile;

public class Validator {

    //static only
    private Validator() {
    }

    public static void process(Decompiler d) {
        var code = d.bytecodeReader;
        for (var line = 1; line <= code.length; line++) {
            switch (code.op(line)) {
                case EQ: {
				  /* TODO
					AssertionManager.assertCritical(
						line + 1 <= code.length && code.isJMP(line + 1),
						"ByteCode validation failed; EQ instruction is not followed by JMP"
					);
					break;*/
                }
                case LT: {
                    break;
                }
                default:
                    break;
            }
        }
    }

}
