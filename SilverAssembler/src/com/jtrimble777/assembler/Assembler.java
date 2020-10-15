package com.jtrimble777.assembler;

import com.jtrimble777.common.InstructionSet;
import com.jtrimble777.common.Register;
import com.jtrimble777.common.Syscall;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Assembler {

  private Map<String, SegmentData> segments;
  private int dataSegmentLength;
  private Map<String, Integer> labels;
  private List<Integer> program;
  private List<Byte> data;
  private int programLength = 0;
  private boolean assembled = false;
  private boolean verbose = false;

  public Assembler(File source, boolean verbose) throws FileNotFoundException {
    segments = new HashMap<>();
    dataSegmentLength = 0;
    labels = new HashMap<>();
    program = new ArrayList<>();
    data = new ArrayList<>();
    this.verbose = verbose;

    labels.putAll(Syscall.getMapping());

    List<String> fileLines = new ArrayList<>();
    Scanner fileReader = new Scanner(source);
    while (fileReader.hasNextLine()) {
      fileLines.add(fileReader.nextLine());
    }
    fileReader.close();

    String activeSegmentName = "";
    List<String> activeSegment = new ArrayList<>();
    int ssln = 0;
    boolean inSegment = false;
    int lineNum = 0;
    for (String line : fileLines) {
      if (line.contains("{")) {
        if (inSegment) {
          throw new AssemblyException(lineNum, line, "Nested segments are not allowed");
        }
        inSegment = true;
        ssln = lineNum;
        activeSegmentName = line.substring(0, line.indexOf("{")).strip();
        if (!validateSegmentName(activeSegmentName)) {
          throw new AssemblyException(lineNum, line, "The segment name was not valid");
        } else if (segments.containsKey(activeSegmentName)) {
          throw new AssemblyException(lineNum, line,
              "A segment with this name has already been defined");
        }
      } else if (line.contains("}")) {
        if (!inSegment) {
          throw new AssemblyException(lineNum, line,
              "Cannot end a segment when not inside a segment");
        }

        String preLine = line.substring(0, line.indexOf("}")).strip();
        if (preLine.length() > 0) {
          activeSegment.add(preLine);
        }
        inSegment = false;
        log("Discovered segment called \""+activeSegmentName+"\" starting at line " + ssln);
        segments.put(activeSegmentName, new SegmentData(activeSegment, ssln));
        activeSegment = new ArrayList<>();
      } else if (inSegment) {
        String sline = line.strip();
        if (sline.length() > 0 && !sline.startsWith("//")) {
          activeSegment.add(sline);
        }
      } else if (!line.strip().startsWith("//")) {
        throw new AssemblyException(lineNum, line, "Instructions are not valid outside a segment");
      }

      lineNum++;
    }
    if (inSegment) {
      throw new AssemblyException(lineNum - 1, fileLines.get(lineNum - 1),
          "Unexpected EOF inside block " + activeSegmentName);
    }
  }

  public void assemble() {
    if (!segments.containsKey("main")) {
      throw new AssemblyException("No main segment found in file");
    }

    int nonFuncSegs = 1;
    if (segments.containsKey("data")) {
      this.processDataSegment();
      nonFuncSegs += 1;
    }

    if (segments.size() > nonFuncSegs) {
      for (String sname : segments.keySet()) {
        if (sname.equals("data") || sname.equals("main")) {
          continue;
        }

        this.defineFunction(sname);
      }
    }
    defineMain();

    resolveLabels();

    processFunction("main");
    processFunctions();

    assembled = true;
  }

  public List<Integer> getProgram() {
    if (!assembled) {
      throw new IllegalStateException("Program cannot be fetched before it is assembled");
    }
    return program;
  }

  public List<Byte> getData() {
    if (!assembled) {
      throw new IllegalStateException("Data cannot be fetched before it is assembled");
    }
    return data;
  }

  public void writeToFile(File out) throws IOException {
    if (!assembled) {
      throw new IllegalStateException("Program cannot be exported before it is assembled");
    }
    ExecutableGenerator.writeExecutable(out, this.program, this.data);
  }

  private void processDataSegment() {
    log("Parsing data segment...");
    SegmentData data = segments.get("data");

    int ln = 0;
    while (ln < data.lines.size()) {
      String line = data.lines.get(ln);

      String oline = "";
      boolean inString = false;
      boolean lastWasSlash = false;
      for (char c : line.toCharArray()) {
        if (c == '"') {
          lastWasSlash = false;
          inString = !inString;
          oline += "\"";
          continue;
        }

        if (inString) {
          switch (c) {
            case ' ':
              oline += "%_";
              continue;
            case '%':
              oline += "%%";
            default:
              oline += c;
          }
        } else {
          if (c == '/') {
            if (lastWasSlash) {
              oline = oline.substring(0, oline.length() - 1);
              break;
            } else {
              lastWasSlash = true;
              oline += "/";
            }
          } else {
            lastWasSlash = false;
            oline += c;
          }
        }
      }
      line = oline;

      if (inString) {
        throw new AssemblyException(ln + data.startingLineNum, line,
            "Unterminated string literal");
      }

      String[] pts = line.split(" ");
      if (pts.length != 3) {
        throw new AssemblyException(ln + data.startingLineNum, line,
            "All data definitions must consist of a type, a label, and a value");
      }

      log(String.format("\tParsing data definition :: Type: %s | Name: %s | Parsed Value: %s",
          pts[0], pts[1], oline));

      DataTypes type;
      try {
        type = DataTypes.match(pts[0]);
      } catch (Exception e) {
        throw new AssemblyException(ln + data.startingLineNum, line,
            "Unknown data type " + pts[0]);
      }

      String trueLiteral = pts[2].contains("\"") ?
          pts[2].replace("%%", "%").replace("%_", " ") :
          pts[2];

      int size;
      try {
        size = type.computeSize(trueLiteral);
      } catch (IllegalArgumentException e) {
        throw new AssemblyException(ln + data.startingLineNum, line,
            "The value provided to the reserve keyword was not a valid size");
      }

      if (!pts[1].equals("_")) {
        if (!validateSegmentName(pts[1])) {
          throw new AssemblyException(ln + data.startingLineNum, line,
              pts[1] + " is not a valid label name");
        } else if (labels.containsKey(pts[1])) {
          throw new AssemblyException(ln + data.startingLineNum, line,
              "Cannot define a label with name " + pts[1] + ": That label is already defined");
        }

        labels.put(pts[1], dataSegmentLength);
      }
      dataSegmentLength += size;

      if (type != DataTypes.RESERVE) {
        try {
          List<Byte> thisData = new ArrayList<>(type.createObject(trueLiteral));
          while (thisData.size() < size) {
            thisData.add((byte)0);
          }
          this.data.addAll(thisData);
        } catch (AssemblyException e) {
          throw new AssemblyException(ln + data.startingLineNum, line,
              "Error parsing literal for label " + pts[1] + " of type " + type.name() + ": "
                  + e.getMessage());
        }
      }

      ln++;
    }
  }

  private void defineFunction(String name) {
    if (labels.containsKey(name)) {
      throw new AssemblyException("Cannot process function " + name + ": A label with the same "
          + "name is already defined");
    }
    labels.put(name, this.programLength);
    int fxLen = 0;
    int ln = 0;

    SegmentData segment = this.segments.get(name);

    for (String line : segment.lines) {
      String tline = line;
      if (line.contains("//")) {
        tline = line.substring(0, line.indexOf("//")).strip();
      }

      String[] pts = tline.split(" ");
      InstructionSet instr = InstructionSet.match(pts[0]);
      if (instr == null) {
        throw new AssemblyException(ln + segment.startingLineNum, tline,
            "Unknown instruction " + pts[0]);
      }
      fxLen += instr.trueSize();
      ln++;
    }

    this.programLength += fxLen;
  }

  private void defineMain() {
    int fxLen = 0;
    int ln = 0;

    SegmentData segment = this.segments.get("main");

    for (String line : segment.lines) {
      String tline = line;
      if (line.contains("//")) {
        tline = line.substring(0, line.indexOf("//")).strip();
      }

      String[] pts = tline.split(" ");
      InstructionSet instr = InstructionSet.match(pts[0]);
      if (instr == null) {
        throw new AssemblyException(ln + segment.startingLineNum, tline,
            "Unknown instruction " + pts[0]);
      }
      fxLen += instr.trueSize();
      ln++;
    }

    for (String lkey : this.labels.keySet()) {
      if (segments.containsKey(lkey)) {
        int base = labels.get(lkey);
        labels.put(lkey, base + fxLen);
      }
    }

    this.programLength += fxLen;
  }

  private void resolveLabels() {
    for (String label : labels.keySet()) {
      if (InstructionSet.match(label) != null) {
        throw new AssemblyException(
            "Label " + label + " defined in function conflicts with assembly "
                + "instruction of the same name");
      }
    }

    for (String sName : segments.keySet()) {
      if (sName.equals("data")) {
        continue;
      }

      SegmentData segment = segments.get(sName);

      int ln = 0;
      while (ln < segment.lines.size()) {
        String tline = segment.lines.get(ln);
        if (tline.contains("//")) {
          tline = tline.substring(0, tline.indexOf("//")).strip();
        }
        List<String> components = Arrays.asList(tline.split(" "));
        for (int ci = 0; ci < components.size(); ci++) {
          if (labels.containsKey(components.get(ci))) {
            components.set(ci, "" + labels.get(components.get(ci)));
          }

          if (ci > 0) {
            String comp = components.get(ci);
            if (!comp.startsWith("$")) {
              try {
                int x = parseInt(comp);
                components.set(ci, "" + x);
              } catch (NumberFormatException e) {
                throw new AssemblyException(segment.startingLineNum + ln, tline,
                    "Unknown symbol " + comp
                        + "; Was not an instruction, register, value, or defined label");
              }
            } else if (comp.length() > 1) {
              Register reg = Register.match(comp.substring(1));
              if (reg != null) {
                components.set(ci, "" + reg.getIndex());
              } else {
                throw new AssemblyException(segment.startingLineNum + ln, tline,
                    "Unknown register " + comp + "; Was not found in the register map");
              }
            } else {
              throw new AssemblyException(segment.startingLineNum + ln, tline,
                  "Unknown symbol $; Not a valid label");
            }
          }
        }
        segment.lines.set(ln, String.join(" ", components));
        ln++;
      }
    }
  }

  private void processFunctions() {
    for (String sname : segments.keySet()) {
      if (!sname.equals("main") && !sname.equals("data")) {
        processFunction(sname);
      }
    }
  }

  private void processFunction(String name) {
    SegmentData data = this.segments.get(name);
    log("Processing function "+name+"...");

    int ln = 0;
    while (ln < data.lines.size()) {
      String line = data.lines.get(ln);

      String instr;
      String[] args;
      try {
        instr = line.substring(0, line.indexOf(" "));
        args = line.substring(line.indexOf(" ") + 1).split(" ");
      } catch (IndexOutOfBoundsException e) {
        instr = line;
        args = new String[0];
      }

      try {
        InstructionSet cmdType = InstructionSet.match(instr);
        log("\t:: Resolving command "+cmdType.name()+" with arguments "+Arrays.toString(args));
        List<Integer> cmds = cmdType.encode(args);
        for (Integer cmd : cmds) {
          log("\t\t> 0x" + String.format("%08X", cmd));
        }
        program.addAll(cmds);
      } catch (IllegalArgumentException e) {
        throw new AssemblyException(data.startingLineNum + ln, name, line, e.getMessage());
      } catch (NullPointerException e) {
        throw new AssemblyException(data.startingLineNum + ln, name, line,
            "No such instruction " + instr);
      }
      ln++;
    }
  }

  private static boolean validateSegmentName(String name) {
    return name.matches("^[a-zA-Z_][a-zA-Z0-9_\\-]*") && name.length() > 1;
  }

  private static int parseInt(String val) {
    if (val.startsWith("0x")) {
      return Integer.parseInt(val.substring(2), 16);
    } else if (val.startsWith("0b")) {
      return Integer.parseInt(val.substring(2), 2);
    } else if (val.startsWith("0o")) {
      return Integer.parseInt(val.substring(2), 8);
    } else if (val.startsWith("0d")) {
      return Integer.parseInt(val.substring(2));
    } else {
      return Integer.parseInt(val);
    }
  }

  private void log(String msg) {
    if (this.verbose) {
      System.out.println(msg);
    }
  }

  private static class SegmentData {

    List<String> lines;
    int startingLineNum;

    public SegmentData(List<String> lines, int ln) {
      this.lines = lines;
      this.startingLineNum = ln;
    }
  }
}
