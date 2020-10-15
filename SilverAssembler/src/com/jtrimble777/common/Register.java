package com.jtrimble777.common;

public enum Register {
  NUL(0, "0"),
  RV0(1, "v0"),
  RV1(2, "v0"),
  ARG0(3, "a0"),
  ARG1(4, "a1"),
  ARG2(5, "a2"),
  ARG3(6, "a3"),
  OS0(7, "k0"),
  OS1(8, "k1"),
  TEMP0(9, "t0"),
  TEMP1(10, "t1"),
  TEMP2(11, "t2"),
  TEMP3(12, "t3"),
  TEMP4(13, "t4"),
  TEMP5(14, "t5"),
  TEMP6(15, "t6"),
  TEMP7(16, "t7"),
  SVD0(17, "s0"),
  SVD1(18, "s1"),
  SVD2(19, "s2"),
  SVD3(20, "s3"),
  SVD4(21, "s4"),
  SVD5(22, "s5"),
  SVD6(23, "s6"),
  SVD7(24, "s7"),
  SP(25, "sp"),
  TPSTK(26, "fp"),
  RET(27, "ra"),
  BASE(28, "bp"),
  LO(29, "lo"),
  HI(30, "hi"),
  FLAGS(31, "flg");


  private int index;
  private String name;

  Register(int index, String name) {
    this.index = index;
    this.name = name;
  }

  public int getIndex() {
    return index;
  }

  public static Register match(String name) {
    for (Register r : values()) {
      if (r.name.equalsIgnoreCase(name)) {
        return r;
      }
    }

    return null;
  }
}
