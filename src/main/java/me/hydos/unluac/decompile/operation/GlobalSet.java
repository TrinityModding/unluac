package me.hydos.unluac.decompile.operation;

import java.util.Arrays;
import java.util.List;

import me.hydos.unluac.decompile.Registers;
import me.hydos.unluac.decompile.block.Block;
import me.hydos.unluac.decompile.expression.ConstantExpression;
import me.hydos.unluac.decompile.expression.Expression;
import me.hydos.unluac.decompile.statement.Assignment;
import me.hydos.unluac.decompile.statement.Statement;
import me.hydos.unluac.decompile.target.GlobalTarget;

public class GlobalSet extends Operation {

  private ConstantExpression global;
  private Expression value;
  
  public GlobalSet(int line, ConstantExpression global, Expression value) {
    super(line);
    this.global = global;
    this.value = value;
  }

  @Override
  public List<Statement> process(Registers r, Block block) {
    return Arrays.asList(new Assignment(new GlobalTarget(global), value, line));
  }
  
}
