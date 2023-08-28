package me.hydos.unluac.decompile.block;

import me.hydos.unluac.decompile.CloseType;
import me.hydos.unluac.decompile.statement.Statement;
import me.hydos.unluac.decompile.Decompiler;
import me.hydos.unluac.decompile.Output;
import me.hydos.unluac.parse.LFunction;

public class DoEndBlock extends ContainerBlock {

  public DoEndBlock(LFunction function, int begin, int end) {
    super(function, begin, end, CloseType.NONE, -1, 1);
  }

  @Override
  public boolean breakable() {
    return false;
  }
  
  @Override
  public boolean hasHeader() {
    return false;
  }
  
  @Override
  public boolean isUnprotected() {
    return false;
  }
  
  public boolean allowsPreDeclare() {
    return true;
  }
  
  @Override
  public int getLoopback() {
    throw new IllegalStateException();
  }
  
  @Override
  public void print(Decompiler d, Output out) {
    out.println("do");
    out.indent();
    Statement.printSequence(d, out, statements);
    out.dedent();
    out.print("end");
  }
  
}
