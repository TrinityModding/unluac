package me.hydos.unluac.decompile.operation;

import java.util.Arrays;
import java.util.List;

import me.hydos.unluac.decompile.Registers;
import me.hydos.unluac.decompile.block.Block;
import me.hydos.unluac.decompile.expression.FunctionCall;
import me.hydos.unluac.decompile.statement.FunctionCallStatement;
import me.hydos.unluac.decompile.statement.Statement;

public class CallOperation extends Operation {

  private FunctionCall call;
  
  public CallOperation(int line, FunctionCall call) {
    super(line);
    this.call = call;
  }

  @Override
  public List<Statement> process(Registers r, Block block) {
    return Arrays.asList(new FunctionCallStatement(call));
  }
  
}
