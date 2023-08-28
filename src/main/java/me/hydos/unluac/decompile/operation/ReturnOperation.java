package me.hydos.unluac.decompile.operation;

import java.util.Arrays;
import java.util.List;

import me.hydos.unluac.decompile.Registers;
import me.hydos.unluac.decompile.block.Block;
import me.hydos.unluac.decompile.expression.Expression;
import me.hydos.unluac.decompile.statement.Return;
import me.hydos.unluac.decompile.statement.Statement;

public class ReturnOperation extends Operation {

  private Expression[] values;
  
  public ReturnOperation(int line, Expression value) {
    super(line);
    values = new Expression[1];
    values[0] = value;
  }
  
  public ReturnOperation(int line, Expression[] values) {
    super(line);
    this.values = values;
  }

  @Override
  public List<Statement> process(Registers r, Block block) {    
    return Arrays.asList(new Return(values));
  }
  
}
