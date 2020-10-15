package com.jtrimble777.assembler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public enum DataTypes {
  INT(4, (s) -> {
    int x = Integer.parseInt(s);
    return as4Bytes(x);
  }),
  BYTE(1, (s) -> {
    byte b = Byte.parseByte(s);
    return Collections.singletonList(b);
  }),
  WORD(4, INT.converter),
  HALFWORD(2, (s) -> {
    int i = Integer.parseInt(s);
    return as2Bytes(i);
  }),
  CHAR(1, BYTE.converter),
  ADDR(4, INT.converter),
  STRING((s) -> roundToWord(s.length() + 1), (s) -> {
    if (!s.startsWith("\"") || !s.endsWith("\"")) {
      throw new AssemblyException("Illegal string literal does not start/end with double quoutes");
    }

    String trueString = unescape(s.substring(1, s.length()-1));

    List<Byte> out = new ArrayList<>();
    for (char c : trueString.toCharArray()) {
      out.add((byte) c);
    }
    out.add((byte)0);

    return out;
  }),
  RESERVE(DataTypes::computeReserveSize, (s) -> new ArrayList<>());

  private int size;
  private Function<String, Integer> sizeComputer;
  private Function<String, List<Byte>> converter;

  DataTypes(int size, Function<String, List<Byte>> conv) {
    this.size = size;
    this.sizeComputer = (s) -> size;
    this.converter = conv;
  }

  DataTypes(Function<String, Integer> sizeComputer, Function<String, List<Byte>> conv) {
    this.size = -1;
    this.sizeComputer = sizeComputer;
    this.converter = conv;
  }

  public int size() {
    return size;
  }

  public int computeSize(String desc) {
    return this.sizeComputer.apply(desc);
  }

  public List<Byte> createObject(String sval) {
    return this.converter.apply(sval);
  }

  public static DataTypes match(String name) {
    return valueOf(name.toUpperCase());
  }

  private static int roundToWord(int base) {
    int start = base/4;
    if (base % 4 == 0) {
      return base;
    } else {
      return (start+1)*4;
    }
  }

  private static int computeReserveSize(String descriptor) {
    String tdesc = descriptor;
    int mux = 4;
    if (descriptor.endsWith("b")) {
      mux = 1;
      tdesc = tdesc.substring(0, tdesc.length()-1);
    } else if (descriptor.endsWith("h")) {
      mux = 2;
      tdesc = tdesc.substring(0, tdesc.length()-1);
    } else if (descriptor.endsWith("w")) {
      tdesc = tdesc.substring(0, tdesc.length()-1);
    }

    return mux * Integer.parseInt(tdesc);
  }

  private static List<Byte> as4Bytes(int i) {
    byte b1 = (byte)(i >> 24);
    byte b2 = (byte)((i >> 16) % 256);
    byte b3 = (byte)((i >> 8) % 256);
    byte b4 = (byte)(i  % 256);

    return List.of(b1, b2, b3, b4);
  }

  private static List<Byte> as2Bytes(int i) {
    byte b1 = (byte)((i >> 8) % 256);
    byte b2 = (byte)(i  % 256);

    return List.of(b1, b2);
  }

  private static String unescape(String raw) {
    String out = "";
    boolean escaped = false;

    for (char c : raw.toCharArray()) {
      if (escaped) {
        switch (c) {
          case '\\':
            out += '\\';
            escaped = false;
            break;
          case 'n':
            out += '\n';
            escaped = false;
            break;
          case 't':
            out += '\t';
            escaped = false;
            break;
          case 'b':
            out += '\b';
            escaped = false;
            break;
          case 'r':
            out += '\r';
            escaped = false;
            break;
          case 'f':
            out += '\f';
            escaped = false;
            break;
          case '\'':
            out += '\'';
            escaped = false;
            break;
          case '"':
            out += '"';
            escaped = false;
            break;
          default:
            throw new AssemblyException("Invalid string escape sequence \"\\"+c+"\"");
        }
      } else {
        if (c == '\\') {
          escaped = true;
        } else {
          out += c;
        }
      }
    }

    return out;
  }
}
