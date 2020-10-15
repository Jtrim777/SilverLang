package com.jtrimble777.executor;

import com.jtrimble777.common.MemoryStructure;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ProgramLoader {
  public static LoadedProgram loadProgram(File src) throws IOException {
    Scanner scanner = new Scanner(src);
    FileInputStream fin = new FileInputStream(src);
    byte[] fileContents = new byte[(int) src.length()];
    fin.read(fileContents);
    fin.close();

    byte[] mainHeader;
    try {
      mainHeader = readHeader(scanner, 8);
    } catch (NoSuchElementException e) {
      throw new ProgramLoadingException(src, "Could not read file header, not enough bytes");
    }

    int entryPoint = toInt(mainHeader[0], mainHeader[1], mainHeader[2], mainHeader[3]);
    int dataHeaderPos = toInt(mainHeader[4], mainHeader[5]);
    int textHeaderPos = toInt(mainHeader[6], mainHeader[7]);

    Map<Integer, byte[]> out = new HashMap<>();
    if (dataHeaderPos != 0) {
      byte[] dataHeader = readHeader(scanner, 8);
      int dStart = toInt(dataHeader[0], dataHeader[1], dataHeader[2], dataHeader[3]);
      int dLen = toInt(dataHeader[4], dataHeader[5], dataHeader[6], dataHeader[7]);

      addSegment(MemoryStructure.DATA_BEGIN, dStart, dLen, fileContents, out, src);
    }

    int readI = textHeaderPos;
    int minTextSeg = 0x1000000;
    List<byte[]> textHeaders = new ArrayList<>();
    while (readI<minTextSeg) {
      byte[] th = readHeader(scanner, 12);
      int start = toInt(th[0], th[1], th[2], th[3]);
      if (start < minTextSeg) {
        minTextSeg = start;
      }
      textHeaders.add(th);
      readI += 12;
    }

    for (byte[] th : textHeaders) {
      int start = toInt(th[0], th[1], th[2], th[3]);
      int len = toInt(th[4], th[5], th[6], th[7]);
      int offset = toInt(th[8], th[9], th[10], th[11]);

      addSegment(MemoryStructure.PROGRAM_TEXT_BEGIN + offset,
          start, len, fileContents, out, src);
    }

    return new LoadedProgram(out, entryPoint);
  }

  private static byte[] readHeader(Scanner src, int length) {
    byte[] out = new byte[length];

    for (int i=0; i<length; i++) {
      out[i] = src.nextByte();
    }

    return out;
  }

  private static void addSegment(int pos, int fPos, int len, byte[] sdata, Map<Integer, byte[]> place, File src) {
    try {
      place.put(pos, Arrays.copyOfRange(sdata, fPos, fPos+len));
    } catch (Exception e) {
      throw new ProgramLoadingException(src,
          String.format("Could not read range of file from %08X to %08X", fPos, fPos+len));
    }
  }

  private static int toInt(byte... components) {
    if (components.length < 1) {
      throw new IllegalArgumentException("Cannot call with less than 1 argument");
    }

    int shift = (components.length * 8) - 8;
    int out = 0;
    for (byte b : components) {
      out += ((int)b) << shift;
      shift -= 8;
    }

    return out;
  }

  public static class LoadedProgram {
    public Map<Integer, byte[]> segments;
    public int entryPoint;

    public LoadedProgram(Map<Integer, byte[]> segments, int entryPoint) {
      this.segments = segments;
      this.entryPoint = entryPoint;
    }
  }
}
