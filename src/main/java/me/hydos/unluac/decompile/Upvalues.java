package me.hydos.unluac.decompile;

import me.hydos.unluac.decompile.expression.UpvalueExpression;
import me.hydos.unluac.parse.LFunction;
import me.hydos.unluac.parse.LUpvalue;

public class Upvalues {

    private final LUpvalue[] upvalues;

    public Upvalues(LFunction func, Declaration[] parentDecls, int line) {
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
