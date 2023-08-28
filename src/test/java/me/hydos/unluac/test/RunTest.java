package me.hydos.unluac.test;

import java.io.IOException;

import me.hydos.unluac.Configuration;

public class RunTest {

  public static void main(String[] args) throws IOException {
    LuaSpec spec = new LuaSpec(0x54);
    UnluacSpec uspec = new UnluacSpec();
    //uspec.disassemble = true;
    Configuration config = new Configuration();
    config.strict_scope = true;
    if(TestFiles.suite.run(spec, uspec, args[0], false, config)) {
      System.exit(0);
    } else {
      System.exit(1);
    }
  }
}
