package com.jtrimble777.executor;

import com.jtrimble777.executor.iomachines.IOMachine;
import com.jtrimble777.executor.iomachines.STDIn;
import com.jtrimble777.executor.iomachines.STDOut;
import java.util.List;

public class SALMachine {
  private byte[] memory;
  private int[] registerFile;
  private int programCounter;
  private List<IOMachine> ioMachines;

  public SALMachine() {
    this.memory = new byte[1048576];
    this.registerFile = new int[32];
    this.programCounter = 0;
    this.ioMachines = List.of(new STDOut(), new STDIn());
  }


}
