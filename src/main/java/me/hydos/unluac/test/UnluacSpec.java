package me.hydos.unluac.test;

import java.io.IOException;

import me.hydos.unluac.Configuration;
import me.hydos.unluac.Main;

public class UnluacSpec {
  
  public UnluacSpec() {
    disassemble = false;
  }
  
  public void run(String in, String out, Configuration config) throws IOException {
    if(!disassemble) {
      Main.decompile(in, out, config);
    } else {
      Main.disassemble(in, out);
    }
  }
  
  public boolean disassemble;
  
}
