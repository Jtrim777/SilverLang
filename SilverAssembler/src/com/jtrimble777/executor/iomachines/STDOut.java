package com.jtrimble777.executor.iomachines;

public class STDOut extends IOMachine {

  public STDOut() {
    super(0, 1);
  }

  @Override
  public void operate(byte[] memory) {
    if (memory[memoryMapping] == 0) {
      return;
    } else {
      char c = (char)memory[memoryMapping];
      System.out.print(c);
      memory[memoryMapping] = 0;
    }
  }
}
