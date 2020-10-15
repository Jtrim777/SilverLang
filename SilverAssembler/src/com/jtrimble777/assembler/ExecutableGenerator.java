package com.jtrimble777.assembler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExecutableGenerator {

  /*
  File format:
  Main Header:
  Entry Point (4 bytes), Data Header Start (2 bytes)(0 for n/a), Program Headers Start (2 bytes)

  Data Header:
  Start File Pos (4 bytes), Length (4 bytes)

  Program Header(s):
  Start File Pos (4 bytes), Length (4 bytes), Offset (4 bytes) [Position to load in mem; Rel to text start]

  Sections...
   */
  public static void writeExecutable(File dest, List<Integer> program, List<Byte> data)
    throws IOException {
    List<Byte> executable = new ArrayList<>();

    // Main Header
    executable.addAll(lob(0));
    executable.addAll(lob2(data.size() > 0 ? 8 : 0,
        data.size() > 0 ? 16 : 8));

    // Data header
    if (data.size() > 0 ){
      executable.addAll(lob(28, data.size()));
    }

    // Program header
    executable.addAll(lob(data.size() > 0 ? (28 + data.size()) : 20,
        program.size(),
        0));

    executable.addAll(data);
    executable.addAll(program.stream()
        .flatMap(ExecutableGenerator::as4Bytes)
        .collect(Collectors.toList()));

    FileOutputStream fos = new FileOutputStream(dest);
    fos.write(primitivize(executable));
  }

  private static byte[] primitivize(List<Byte> input) {
    byte[] out = new byte[input.size()];

    for(int i=0; i<out.length; i++) {
      out[i] = input.get(i);
    }

    return out;
  }

  private static List<Byte> lob(Integer... src){
    return Arrays.stream(src)
        .flatMap(ExecutableGenerator::as4Bytes)
        .collect(Collectors.toList());
  }

  private static Stream<Byte> as4Bytes(int i) {
    byte b1 = (byte)(i >> 24);
    byte b2 = (byte)((i >> 16) % 256);
    byte b3 = (byte)((i >> 8) % 256);
    byte b4 = (byte)(i  % 256);
    System.out.println(String.format("Converting integer command %08X to [%02X, %02X, %02X, %02X]",
        i, b1, b2, b3, b4));

    return List.of(b1, b2, b3, b4).stream();
  }

  private static List<Byte> lob2(Integer... src){
    return Arrays.stream(src)
        .flatMap(ExecutableGenerator::as2Bytes)
        .collect(Collectors.toList());
  }

  private static Stream<Byte> as2Bytes(int i) {
    byte b1 = (byte)((i >> 8) % 256);
    byte b2 = (byte)(i  % 256);

    return List.of(b1, b2).stream();
  }
}
