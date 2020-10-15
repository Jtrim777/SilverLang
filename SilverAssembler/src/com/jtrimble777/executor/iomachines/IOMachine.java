package com.jtrimble777.executor.iomachines;

import com.jtrimble777.common.MemoryStructure;

public abstract class IOMachine {
  protected int memoryMapping;
  protected int mappedLen;

  public IOMachine(int memoryMapping, int mappedLen) {
    this.memoryMapping = memoryMapping + MemoryStructure.MMIO;
    this.mappedLen = mappedLen;
  }

  public abstract void operate(byte[] memory);
}
