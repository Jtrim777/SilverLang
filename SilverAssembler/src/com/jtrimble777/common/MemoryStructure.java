package com.jtrimble777.common;

public class MemoryStructure {
  // 255 bytes of reserved space
  public static final int RSRVD1_BEGIN = 0;
  public static final int RSRVD1_END = RSRVD1_BEGIN + 0xFF;

  // 32 KiB of Global Data
  public static final int DATA_BEGIN = RSRVD1_END + 1;
  public static final int DATA_END = DATA_BEGIN + 0x8000;

  // 64 KiB of Program Text
  public static final int PROGRAM_TEXT_BEGIN = DATA_END + 1;
  public static final int PROGRAM_TEXT_END = PROGRAM_TEXT_BEGIN + 0x10000;

  // ~900 KiB of Dynamic Data
  public static final int DYNAMIC_DATA_BEGIN = PROGRAM_TEXT_END + 1;
  public static final int DYNAMIC_DATA_END = DYNAMIC_DATA_BEGIN + 0xE2800;

  // 4 KiB of OS Data
  public static final int OS_DATA_BEGIN = DYNAMIC_DATA_END + 1;
  public static final int OS_DATA_END = OS_DATA_BEGIN + 0x1000;

  // 16 KiB of OS Code
  public static final int OS_BEGIN = OS_DATA_END + 1;
  public static final int OS_END = OS_BEGIN + 0x4000;

  // 512 B OS Table
  public static final int OS_TABLE = OS_END + 1;
  public static final int OS_TABLE_END = OS_TABLE + 0x200;

  public static final int MMIO = OS_TABLE_END + 1;
  public static final int MMIO_END = MMIO + 0x100;
}
