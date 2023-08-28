package me.hydos.unluac.decompile.operation;

import java.util.Arrays;
import java.util.List;

import me.hydos.unluac.decompile.Registers;
import me.hydos.unluac.decompile.block.Block;
import me.hydos.unluac.decompile.expression.Expression;
import me.hydos.unluac.decompile.statement.Assignment;
import me.hydos.unluac.decompile.statement.Statement;
import me.hydos.unluac.decompile.target.UpvalueTarget;

public class UpvalueSet extends Operation {

  private UpvalueTarget target;
  private Expression value;
  
  public UpvalueSet(int line, String upvalue, Expression value) {
    super(line);
    target = new UpvalueTarget(upvalue);
    this.value = value;
  }

  @Override
  public List<Statement> process(Registers r, Block block) {
    return Arrays.asList(new Assignment(target, value, line));
  }
  
}
