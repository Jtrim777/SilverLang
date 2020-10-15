package com.jtrimble777.executor.iomachines;

import java.util.ArrayList;
import java.util.List;

public class STDIn extends IOMachine {

  private List<Byte> buffer;

  public STDIn() {
    super(1, 2);
    buffer = new ArrayList<>();
  }

  @Override
  public void operate(byte[] memory) {

  }
}
