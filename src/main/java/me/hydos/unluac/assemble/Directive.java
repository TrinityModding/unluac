package me.hydos.unluac.assemble;

import me.hydos.unluac.decompile.core.Output;
import me.hydos.unluac.bytecode.BFunction;
import me.hydos.unluac.bytecode.LHeader;

import java.util.HashMap;
import java.util.Map;

enum DirectiveType {
    HEADER,
    NEWFUNCTION,
    FUNCTION,
    INSTRUCTION
}

public enum Directive {
    FORMAT(".format", DirectiveType.HEADER),
    ENDIANNESS(".endianness", DirectiveType.HEADER),
    INT_SIZE(".int_size", DirectiveType.HEADER),
    SIZE_T_SIZE(".size_t_size", DirectiveType.HEADER),
    INSTRUCTION_SIZE(".instruction_size", DirectiveType.HEADER),
    SIZE_OP(".size_op", DirectiveType.HEADER),
    SIZE_A(".size_a", DirectiveType.HEADER),
    SIZE_B(".size_b", DirectiveType.HEADER),
    SIZE_C(".size_c", DirectiveType.HEADER),
    NUMBER_FORMAT(".number_format", DirectiveType.HEADER),
    INTEGER_FORMAT(".integer_format", DirectiveType.HEADER),
    FLOAT_FORMAT(".float_format", DirectiveType.HEADER),
    OP(".op", DirectiveType.HEADER),
    FUNCTION(".function", DirectiveType.NEWFUNCTION),
    SOURCE(".source", DirectiveType.FUNCTION),
    LINEDEFINED(".linedefined", DirectiveType.FUNCTION),
    LASTLINEDEFINED(".lastlinedefined", DirectiveType.FUNCTION),
    NUMPARAMS(".numparams", DirectiveType.FUNCTION),
    IS_VARARG(".is_vararg", DirectiveType.FUNCTION),
    MAXSTACKSIZE(".maxstacksize", DirectiveType.FUNCTION),
    LABEL(".label", DirectiveType.FUNCTION),
    CONSTANT(".constant", DirectiveType.FUNCTION),
    LINE(".line", DirectiveType.FUNCTION),
    ABSLINEINFO(".abslineinfo", DirectiveType.FUNCTION),
    LOCAL(".local", DirectiveType.FUNCTION),
    UPVALUE(".upvalue", DirectiveType.FUNCTION),
    ;

    static final Map<String, Directive> lookup;

    static {
        lookup = new HashMap<>();
        for (var d : Directive.values()) {
            lookup.put(d.token, d);
        }
    }

    public final String token;
    public final DirectiveType type;

    Directive(String token, DirectiveType type) {
        this.token = token;
        this.type = type;
    }

    public void disassemble(Output out, LHeader header) {
        out.print(this.token + "\t");
        switch (this) {
            case FORMAT -> out.println(String.valueOf(header.format));
            case ENDIANNESS -> out.println(header.endianness.toString());
            case INT_SIZE -> out.println(String.valueOf(header.integer.getSize()));
            case SIZE_T_SIZE -> out.println(String.valueOf(header.sizeT.getSize()));
            case INSTRUCTION_SIZE -> out.println("4");
            case SIZE_OP -> out.println(String.valueOf(header.extractor.op.size));
            case SIZE_A -> out.println(String.valueOf(header.extractor.A.size));
            case SIZE_B -> out.println(String.valueOf(header.extractor.B.size));
            case SIZE_C -> out.println(String.valueOf(header.extractor.C.size));
            case NUMBER_FORMAT ->
                    out.println((header.number.integral ? "integer" : "float") + "\t" + header.number.size);
            case INTEGER_FORMAT -> out.println(String.valueOf(header.linteger.size));
            case FLOAT_FORMAT -> out.println(String.valueOf(header.lfloat.size));
            default -> throw new IllegalStateException();
        }
    }

    public void disassemble(Output out, BFunction function, int print_flags) {
        out.print(this.token + "\t");
        switch (this) {
            case SOURCE -> out.println(function.name.toPrintString(print_flags));
            case LINEDEFINED -> out.println(String.valueOf(function.linedefined));
            case LASTLINEDEFINED -> out.println(String.valueOf(function.lastlinedefined));
            case NUMPARAMS -> out.println(String.valueOf(function.paramCount));
            case IS_VARARG -> out.println(String.valueOf(function.vararg));
            case MAXSTACKSIZE -> out.println(String.valueOf(function.maximumStackSize));
            default -> throw new IllegalStateException();
        }
    }

}
