package me.hydos.unluac.decompile.block;

import me.hydos.unluac.decompile.CloseType;
import me.hydos.unluac.decompile.Registers;
import me.hydos.unluac.bytecode.BFunction;

public class ForBlock50 extends ForBlock {

    public ForBlock50(BFunction function, int begin, int end, int register, CloseType closeType, int closeLine) {
        super(function, begin, end, register, closeType, closeLine, false);
    }

    @Override
    public void resolve(Registers r) {
        target = r.getTarget(register, begin - 1);
        start = r.getValue(register, begin - 2);
        stop = r.getValue(register + 1, begin - 1);
        step = r.getValue(register + 2, begin - 1);
    }

    @Override
    public void handleVariableDeclarations(Registers r) {
        r.setExplicitLoopVariable(register, begin - 1, end - 1);
        r.setInternalLoopVariable(register + 1, begin - 1, end - 1);
        r.setInternalLoopVariable(register + 2, begin - 1, end - 1);
    }

}
