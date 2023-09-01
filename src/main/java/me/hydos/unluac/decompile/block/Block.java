package me.hydos.unluac.decompile.block;

import me.hydos.unluac.bytecode.BFunction;
import me.hydos.unluac.decompile.CloseType;
import me.hydos.unluac.decompile.Decompiler;
import me.hydos.unluac.decompile.Registers;
import me.hydos.unluac.decompile.operation.Operation;
import me.hydos.unluac.decompile.statement.Statement;

import java.util.List;
import java.util.Objects;

abstract public class Block extends Statement implements Comparable<Block> {

    protected final BFunction function;
    private final int priority;
    public final int begin;
    public int end;
    public int closeRegister;
    public boolean loopRedirectAdjustment = false;
    protected boolean scopeUsed = false;

    public Block(BFunction function, int begin, int end, int priority) {
        this.function = function;
        this.begin = begin;
        this.end = end;
        this.closeRegister = -1;
        this.priority = priority;
    }

    public abstract void addStatement(Statement statement);

    public List<Statement> getStatements() {
        throw new RuntimeException("getStatements Not Implemented in " + getClass().getName());
    }

    public void resolve(Registers r) {
    }

    public boolean contains(Block block) {
        return contains(block.begin, block.end);
    }

    public boolean contains(int line) {
        return begin <= line && line < end;
    }

    public boolean contains(int begin, int end) {
        return this.begin <= begin && this.end >= end;
    }

    public int scopeEnd() {
        return end - 1;
    }

    public void useScope() {
        scopeUsed = true;
    }

    public boolean hasCloseLine() {
        return false;
    }

    public int getCloseLine() {
        throw new IllegalStateException();
    }

    public void useClose() {
        throw new IllegalStateException();
    }

    abstract public boolean hasHeader();

    /**
     * An unprotected block is one that ends in a JMP instruction.
     * If this is the case, any inner statement that tries to jump
     * to the end of this block will be redirected.
     * <p>
     * (One of the Lua compiler's few optimizations is that is changes
     * any JMP that targets another JMP to the ultimate target. This
     * is what I call redirection.)
     */
    abstract public boolean isUnprotected();

    public int getUnprotectedTarget() {
        throw new IllegalStateException(this.toString());
    }

    public int getUnprotectedLine() {
        throw new IllegalStateException(this.toString());
    }

    abstract public int getLoopback();

    abstract public boolean breakable();

    abstract public boolean isContainer();

    abstract public boolean isEmpty();

    public boolean allowsPreDeclare() {
        return false;
    }

    public boolean isSplitable() {
        return false;
    }

    public Block[] split(int line, CloseType closeType) {
        throw new IllegalStateException();
    }

    @Override
    public int compareTo(Block block) {
        if (this.begin < block.begin) {
            return -1;
        } else if (this.begin == block.begin) {
            if (this.end < block.end) {
                return 1;
            } else if (this.end == block.end) {
                return this.priority - block.priority;
            } else {
                return -1;
            }
        } else {
            return 1;
        }
    }

    public Operation process(Decompiler d) {
        final Statement statement = this;
        return new Operation(end - 1) {
            @Override
            public List<Statement> process(Registers r, Block block) {
                return List.of(statement);
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var block = (Block) o;
        return priority == block.priority && begin == block.begin && end == block.end && closeRegister == block.closeRegister && loopRedirectAdjustment == block.loopRedirectAdjustment && scopeUsed == block.scopeUsed && Objects.equals(function, block.function);
    }

    @Override
    public int hashCode() {
        return Objects.hash(function, priority, begin, end, closeRegister, loopRedirectAdjustment, scopeUsed);
    }
}
