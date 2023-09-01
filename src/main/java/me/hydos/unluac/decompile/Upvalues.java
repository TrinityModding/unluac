package me.hydos.unluac.decompile;

import me.hydos.unluac.decompile.expression.UpvalueExpression;
import me.hydos.unluac.bytecode.BFunction;
import me.hydos.unluac.bytecode.LUpvalue;

import java.util.Arrays;
import java.util.List;

public class Upvalues {

    private final LUpvalue[] upvalues;

    @Deprecated
    public Upvalues(BFunction func, Declaration[] parentDecls, int line) {
        this(func, parentDecls == null ? null : Arrays.stream(parentDecls).toList(), line);
    }

    public Upvalues(BFunction func, List<Declaration> parentDecls, int line) {
        this.upvalues = func.upvalues;
        for (var upvalue : upvalues) {
            if (upvalue.name == null || upvalue.name.isEmpty()) {
                if (upvalue.instack) {
                    if (parentDecls != null) {
                        for (var decl : parentDecls) {
                            if (decl.register == upvalue.idx && line >= decl.begin && line < decl.end) {
                                upvalue.name = decl.name;
                                break;
                            }
                        }
                    }
                } else {
                    var parentvals = func.parent.upvalues;
                    if (upvalue.idx >= 0 && upvalue.idx < parentvals.length) {
                        upvalue.name = parentvals[upvalue.idx].name;
                    }
                }
            }
        }
    }

    public String getName(int index) {
        if (index < upvalues.length && upvalues[index].name != null && !upvalues[index].name.isEmpty()) {
            return upvalues[index].name;
        } else {
            //TODO: SET ERROR
            return "_UPVALUE" + index + "_";
        }
    }

    public UpvalueExpression getExpression(int index) {
        return new UpvalueExpression(getName(index));
    }

}
