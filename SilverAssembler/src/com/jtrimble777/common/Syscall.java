package com.jtrimble777.common;

import java.util.HashMap;
import java.util.Map;

public enum Syscall {
  PRINT(0),
  READ(1),
  EXIT(2);

  private int index;

  Syscall(int index) {
    this.index = index;
  }

  public static Map<String, Integer> getMapping() {
    Map<String, Integer> mapping = new HashMap<>();
    for (Syscall sys : values()) {
      mapping.put("OS_"+sys.name(), sys.index);
    }

    return mapping;
  }
}
