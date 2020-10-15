package com.jtrimble777.assembler;

public class AssemblyException extends RuntimeException {
  public AssemblyException(int lineNum, String line, String problem) {
    super(String.format("Error assembling program on line #%d (\"%s\"):\n\t%s",
        lineNum, line, problem));
  }

  public AssemblyException(int lineNum, String fname, String line, String problem) {
    super(String.format("Error assembling program on line #%d in segment %s (\"%s\"):\n\t%s",
        lineNum, fname, line, problem));
  }

  public AssemblyException(String problem) {
    super("Error parsing program: "+problem);
  }
}
