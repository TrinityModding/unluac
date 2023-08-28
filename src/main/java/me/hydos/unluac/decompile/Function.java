package me.hydos.unluac.decompile;

import me.hydos.unluac.Version;
import me.hydos.unluac.decompile.expression.ConstantExpression;
import me.hydos.unluac.decompile.expression.GlobalExpression;
import me.hydos.unluac.parse.LFunction;

public class Function {

    private final Version version;
    private final Constant[] constants;
    private final CodeExtract extract;

    public Function(LFunction function) {
        version = function.header.version;
        constants = new Constant[function.constants.length];
        for (var i = 0; i < constants.length; i++) {
            constants[i] = new Constant(function.constants[i]);
        }
        extract = function.header.extractor;
    }

    public boolean isConstant(int register) {
        return extract.is_k(register);
    }

    public int constantIndex(int register) {
        return extract.get_k(register);
    }

    public ConstantExpression getGlobalName(int constantIndex) {
        var constant = constants[constantIndex];
        if (!constant.isIdentifierPermissive(version)) throw new IllegalStateException();
        return new ConstantExpression(constant, true, constantIndex);
    }

    public ConstantExpression getConstantExpression(int constantIndex) {
        var constant = constants[constantIndex];
        return new ConstantExpression(constant, constant.isIdentifier(version), constantIndex);
    }

    public GlobalExpression getGlobalExpression(int constantIndex) {
        return new GlobalExpression(getGlobalName(constantIndex), constantIndex);
    }

    public Version getVersion() {
        return version;
    }

}
