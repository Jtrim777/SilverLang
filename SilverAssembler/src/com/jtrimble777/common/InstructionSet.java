package com.jtrimble777.common;

import static com.jtrimble777.common.InstructionType.*;
import static com.jtrimble777.common.InstructionType.InstructionComponent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum InstructionSet {

  LD(ITYPE, wrap("LD", "Dest Reg", "", "", "Mem Addr", AE_IVR(1, false))),
  LR(RTYPE, wrap("LR", "Addr Reg", "", "Dest Reg", "", AE_RAC(0))),
  ADD(RTYPE, wrap("ADD", "Reg A", "Reg B", "Dest Reg", "", AE_RABC(3))),
  ADI(ITYPE, wrap("ADI", "Src Reg", "", "Dest Reg", "Src Val", AE_IRVR(4, false))),
  ADS(RTYPE, wrap("ADS", "Reg A", "", "Dest Reg", "Value", AE_IRVR(5, true))),
  LRO(PSEUDO, 2, outerWrap("LRO", (args) -> List.of(
      ADI.encode(new String[]{args[0], args[1], "7"}).get(0),
      LR.encode("7", args[2]).get(0)
  ))),
  SV(ITYPE, wrap("SV", "Src Reg", "", "", "Mem Addr", AE_IRV(2, false))),
  SVR(RTYPE, wrap("SVR", "Src Reg", "", "Dest Reg", "", AE_RAC(1))),
  SI(PSEUDO, 2, outerWrap("SI", (args) -> pseudo(
      ADI.encode("0", args[0], "7"),
      SV.encode("7", args[1])
  ))),
  SIR(PSEUDO, 2, outerWrap("SIR", args -> pseudo(
      ADI.encode("0", args[0], "7"),
      SVR.encode("7", args[1])
  ))),
  MV(PSEUDO, 1, outerWrap("MV", args -> pseudo(
      ADD.encode("0", args[0], args[1])
  ))),
  MPC(RTYPE, wrap("MPC", "", "", "Dest Reg", "", AE_RC(2))),
  LUI(ITYPE, wrap("LUI", "", "", "Dest Reg", "Value", AE_IVR(3, false))),
  LI(PSEUDO, 2, outerWrap("LI", (args) -> ADI.encode(new String[]{"0", args[0], args[1]}))),
  NEG(RTYPE, wrap("NEG", "Src Reg", "", "Dest Reg", "", AE_RAC(4))),
  NGI(PSEUDO, 2, outerWrap("NGI", (args) -> pseudo(
      ADI.encode("0", args[0], "7"),
      NEG.encode("7", args[1])
  ))),
  SUB(PSEUDO, 4, outerWrap("SUB", args -> pseudo(
      ADD.encode("0", args[1], "7"),
      NEG.encode("7", "7"),
      ADI.encode("7", "1", "7"),
      ADD.encode(args[0], "7", args[2])
  ))),
  SBI(PSEUDO, 4, outerWrap("SBI", args -> pseudo(
      ADI.encode("0", args[1], "7"),
      NEG.encode("7", "7"),
      ADI.encode("7", "1", "7"),
      ADD.encode(args[0], "7", args[2])
  ))),
  SBS(PSEUDO, 4, outerWrap("SBI", args -> pseudo(
      ADS.encode("0", args[1], "7"),
      NEG.encode("7", "7"),
      ADI.encode("7", "1", "7"),
      ADD.encode(args[0], "7", args[2])
  ))),
  AND(RTYPE, wrap("AND", "Reg A", "Reg B", "Dest Reg", "", AE_RABC(5))),
  ANI(PSEUDO, 2, outerWrap("ANI", (args) -> pseudo(
      ADI.encode("0", args[1], "7"),
      AND.encode("7", args[0], args[2])
  ))),
  OR(RTYPE, wrap("OR", "Reg A", "Reg B", "Dest Reg", "", AE_RABC(6))),
  ORI(PSEUDO, 2, outerWrap("ORI", (args) -> pseudo(
      ADI.encode("0", args[1], "7"),
      OR.encode("7", args[0], args[2])
  ))),
  MUL(RTYPE, wrap("MUL", "Reg A", "Reg B", "", "", AE_RAB(7))),
  MLI(ITYPE, 2, outerWrap("MLI", (args) -> pseudo(
      ADI.encode("0", args[1], "7"),
      MUL.encode("7", args[0])
  ))),
  DIV(RTYPE, wrap("DIV", "Reg A", "Reg B", "", "", AE_RAB(8))),
  DVI(PSEUDO, 2, outerWrap("DVI", (args) -> pseudo(
      ADI.encode("0", args[1], "7"),
      DIV.encode(args[0], "7")
  ))),
  SLL(RTYPE, wrap("SLL", "Src Reg", "", "Dest Reg", "", AE_RAC(9))),
  SRL(RTYPE, wrap("SRL", "Src Reg", "", "Dest Reg", "", AE_RAC(10))),
  SRA(RTYPE, wrap("SRA", "Src Reg", "", "Dest Reg", "", AE_RAC(11))),
  CMP(RTYPE, wrap("CMP", "Reg A", "Reg B", "Dest Reg", "", AE_RABC(12))),
  LT(RTYPE, wrap("LT", "Reg A", "Reg B", "Dest Reg", "", AE_RABC(13))),
  PSH(PSEUDO, 2, outerWrap("PSH", (args) -> pseudo(
      ADS.encode("25", "-4", "25"),
      SVR.encode(args[0], "25")
  ))),
  PSI(PSEUDO, 2, outerWrap("PSI", (args) -> pseudo(
      ADI.encode("25", "-4", "25"),
      SIR.encode(args[0], "25")
  ))),
  POP(PSEUDO, 2, outerWrap("POP", (args) -> pseudo(
      LR.encode("25", args[0]),
      ADI.encode("25", "4", "25")
  ))),
  JT(STYPE, wrap("JT", "", "", "", "Dest", AE_SV(8, false))),
  JR(STYPE, wrap("JR", "Dest", "", "", "", AE_SR(9))),
  JIZ(STYPE, wrap("JIZ", "Cmp Reg", "", "", "Dest", AE_SRV(10, false))),
  JNZ(STYPE, wrap("JNZ", "Cmp Reg", "", "", "Dest", AE_SRV(11, false))),
  JRZ(RTYPE, wrap("JRZ", "Cmp Reg", "Dest", "", "", AE_RAC(14))),
  JRN(RTYPE, wrap("JRN", "Cmp Reg", "Dest", "", "", AE_RAC(15))),
  JAL(PSEUDO, 3, outerWrap("JAL", (args) -> pseudo(
      MPC.encode("27"),
      ADI.encode("27", "4", "27"),
      JT.encode(args[0])
  ))),
  JRL(PSEUDO, 3, outerWrap("JRL", (args) -> pseudo(
      MPC.encode("27"),
      ADI.encode("27", "4", "27"),
      JR.encode(args[0])
  ))),
  JZL(PSEUDO, 3, outerWrap("JZL", (args) -> pseudo(
      MPC.encode("27"),
      ADI.encode("27", "4", "27"),
      JIZ.encode(args[0], args[1])
  ))),
  JXL(PSEUDO, 3, outerWrap("JXL", (args) -> pseudo(
      MPC.encode("27"),
      ADI.encode("27", "4", "27"),
      JRZ.encode(args[0], args[1])
  ))),
  BIZ(PSEUDO, 3, outerWrap("BIZ", (args) -> pseudo(
      MPC.encode("7"),
      ADI.encode("7", args[1], "7"),
      JRZ.encode(args[1], "7")
  ))),
  BRZ(PSEUDO, 3, outerWrap("BRZ", (args) -> pseudo(
      MPC.encode("7"),
      ADD.encode("7", args[1], "7"),
      JRZ.encode(args[1], "7")
  ))),
  BZL(PSEUDO, 3, outerWrap("BZL", (args) -> pseudo(
      MPC.encode("27"),
      ADI.encode("27", "4", "27"),
      BIZ.encode(args[0], args[1])
  ))),
  BRL(PSEUDO, 3, outerWrap("BRL", (args) -> pseudo(
      MPC.encode("27"),
      ADI.encode("27", "4", "27"),
      BRZ.encode(args[0], args[1])
  ))),
  RET(PSEUDO, outerWrap("RET", (args) -> JR.encode("27"))),
  NO(PSEUDO, outerWrap("NO", (args) -> SLL.encode("0", "0"))),
  SYS(PSEUDO, 8, /*outerWrap("SYS", */(args) -> pseudo(
      ADI.encode("0", args[0], "7"),
      MLI.encode("7", "4"),
      ADI.encode("0", "29", "7"),
      LUI.encode(""+(MemoryStructure.OS_TABLE >> 16), "7"),
      ADI.encode("7", ""+(MemoryStructure.OS_TABLE % 65536), "7"),
      LR.encode("7", "8"),
      MPC.encode("27"),
      ADI.encode("27", "4", "27"),
      JR.encode("8")
  )/*)*/);

  private InstructionType type;
  private AEncoder translator;
  private int instructionLength = 1;

  InstructionSet(InstructionType type, AEncoder translator) {
    this.type = type;
    this.translator = translator;
  }

  InstructionSet(InstructionType type, int ic, AEncoder translator) {
    this.type = type;
    this.translator = translator;
  }

  public List<Integer> encode(String... args) {
    return this.translator.apply(args);
  }

  public int trueSize() {
    return instructionLength;
  }

  private static void assureValidRegister(int r) {
    if (r < 0 || r > 31) {
      throw new IllegalArgumentException("The value at position $R was not a valid register index");
    }
  }

  private static void assureValidImmediate(int v, boolean signed) {
    if (v > (signed ? 32767 : 65535) || v < (signed ? -32768 : 0)) {
      String ss = signed ? "signed" : "unsigned";
      throw new IllegalArgumentException("The value at position $V was not a valid "+ss+" immediate");
    }
  }

  private static void assureValidBigImmediate(int v, boolean signed) {
    if (v > (signed ? 4194303 : 8388607) || v < (signed ? -4194304 : 0)) {
      String ss = signed ? "signed" : "unsigned";
      throw new IllegalArgumentException("The value at position $V was not a valid "+ss+" immediate");
    }
  }

  private static AEncoder AE_IVR(int instr, boolean signed) {return (args) -> {
    int v, r;
    try {
      v = Integer.parseInt(args[0]);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("The value at position $V was not a number");
    }
    try {
      r = Integer.parseInt(args[1]);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("The value at position $RA was not a number");
    }

    assureValidRegister(r);
    assureValidImmediate(v, signed);

    Map<InstructionComponent, Integer> comps = Map.of(
        InstructionComponent.INSTRUCTION, instr,
        InstructionComponent.REGISTER_A, r,
        InstructionComponent.REGISTER_B, 0,
        InstructionComponent.IMMEDIATE, v
    );

    return Collections.singletonList(ITYPE.encode(comps));
  };}

  private static AEncoder AE_IRV(int instr, boolean signed) { return (args) -> {
    int v, r;
    try {
      v = Integer.parseInt(args[1]);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("The value at position $V was not a number");
    }
    try {
      r = Integer.parseInt(args[0]);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("The value at position $RA was not a number");
    }

    assureValidRegister(r);
    assureValidImmediate(v, signed);

    Map<InstructionComponent, Integer> comps = Map.of(
        InstructionComponent.INSTRUCTION, instr,
        InstructionComponent.REGISTER_A, r,
        InstructionComponent.REGISTER_B, 0,
        InstructionComponent.IMMEDIATE, v
    );

    return Collections.singletonList(ITYPE.encode(comps));
  };}

  private static AEncoder AE_IRVR(int instr, boolean signed) { return (args) -> {
    int v, r, r2;
    try {
      v = Integer.parseInt(args[1]);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("The value at position $V was not a number");
    }
    try {
      r = Integer.parseInt(args[0]);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("The value at position $RA was not a number");
    }
    try {
      r2 = Integer.parseInt(args[2]);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("The value at position $RB was not a number");
    }

    assureValidRegister(r);
    assureValidRegister(r2);
    assureValidImmediate(v, signed);

    Map<InstructionComponent, Integer> comps = Map.of(
        InstructionComponent.INSTRUCTION, instr,
        InstructionComponent.REGISTER_A, r,
        InstructionComponent.REGISTER_B, r2,
        InstructionComponent.IMMEDIATE, v
    );

    return Collections.singletonList(ITYPE.encode(comps));
  };}

  private static AEncoder AE_RAB(int instr) { return (args) -> {
    int ra, rb;
    try {
      ra = Integer.parseInt(args[0]);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("The value at position $RA was not a number");
    }
    try {
      rb = Integer.parseInt(args[1]);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("The value at position $RB was not a number");
    }

    assureValidRegister(ra);
    assureValidRegister(rb);

    Map<InstructionComponent, Integer> comps = Map.of(
        InstructionComponent.INSTRUCTION, instr,
        InstructionComponent.REGISTER_A, ra,
        InstructionComponent.REGISTER_B, rb,
        InstructionComponent.DEST_REGISTER, 0
    );

    return Collections.singletonList(RTYPE.encode(comps));
  };}

  private static AEncoder AE_RAC(int instr) { return (args) -> {
    int ra, rc;
    try {
      ra = Integer.parseInt(args[0]);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("The value at position $RA was not a number");
    }
    try {
      rc = Integer.parseInt(args[1]);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("The value at position $RC was not a number");
    }

    assureValidRegister(ra);
    assureValidRegister(rc);

    Map<InstructionComponent, Integer> comps = Map.of(
        InstructionComponent.INSTRUCTION, instr,
        InstructionComponent.REGISTER_A, ra,
        InstructionComponent.REGISTER_B, 0,
        InstructionComponent.DEST_REGISTER, rc
    );

    return Collections.singletonList(RTYPE.encode(comps));
  };}

  private static AEncoder AE_RC(int instr) { return (args) -> {
    int rc;
    try {
      rc = Integer.parseInt(args[0]);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("The value at position $RC was not a number");
    }
    assureValidRegister(rc);

    Map<InstructionComponent, Integer> comps = Map.of(
        InstructionComponent.INSTRUCTION, instr,
        InstructionComponent.REGISTER_A, 0,
        InstructionComponent.REGISTER_B, 0,
        InstructionComponent.DEST_REGISTER, rc
    );

    return Collections.singletonList(RTYPE.encode(comps));
  };}

  private static AEncoder AE_RABC(int instr) { return (args) -> {
    int ra, rb, rc;
    try {
      ra = Integer.parseInt(args[0]);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("The value at position $RA was not a number");
    }
    try {
      rb = Integer.parseInt(args[1]);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("The value at position $RB was not a number");
    }try {
      rc = Integer.parseInt(args[1]);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("The value at position $RC was not a number");
    }


    assureValidRegister(ra);
    assureValidRegister(rb);
    assureValidRegister(rc);

    Map<InstructionComponent, Integer> comps = Map.of(
        InstructionComponent.INSTRUCTION, instr,
        InstructionComponent.REGISTER_A, ra,
        InstructionComponent.REGISTER_B, rb,
        InstructionComponent.DEST_REGISTER, rc
    );

    return Collections.singletonList(RTYPE.encode(comps));
  };}

  private static AEncoder AE_SV(int instr, boolean signed) { return (args) -> {
    int v;
    try {
      v = Integer.parseInt(args[0]);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("The value at position $V was not a number");
    }

    assureValidBigImmediate(v, signed);

    Map<InstructionComponent, Integer> comps = Map.of(
        InstructionComponent.INSTRUCTION, instr,
        InstructionComponent.REGISTER_A, 0,
        InstructionComponent.IMMEDIATE, v
    );

    return Collections.singletonList(STYPE.encode(comps));
  };}

  private static AEncoder AE_SR(int instr) { return (args) -> {
    int r;
    try {
      r = Integer.parseInt(args[0]);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("The value at position $R was not a number");
    }

    assureValidRegister(r);

    Map<InstructionComponent, Integer> comps = Map.of(
        InstructionComponent.INSTRUCTION, instr,
        InstructionComponent.REGISTER_A, r,
        InstructionComponent.IMMEDIATE, 0
    );

    return Collections.singletonList(STYPE.encode(comps));
  };}

  private static AEncoder AE_SRV(int instr, boolean signed) { return (args) -> {
    int r;
    try {
      r = Integer.parseInt(args[0]);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("The value at position $R was not a number");
    }
    int v;
    try {
      v = Integer.parseInt(args[1]);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("The value at position $V was not a number");
    }

    assureValidRegister(r);
    assureValidBigImmediate(v, signed);

    Map<InstructionComponent, Integer> comps = Map.of(
        InstructionComponent.INSTRUCTION, instr,
        InstructionComponent.REGISTER_A, r,
        InstructionComponent.IMMEDIATE, v
    );

    return Collections.singletonList(STYPE.encode(comps));
  };}

  private static AEncoder AE_SVR(int instr, boolean signed) { return (args) -> {
    int r;
    try {
      r = Integer.parseInt(args[1]);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("The value at position $R was not a number");
    }
    int v;
    try {
      v = Integer.parseInt(args[0]);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("The value at position $V was not a number");
    }

    assureValidRegister(r);
    assureValidBigImmediate(v, signed);

    Map<InstructionComponent, Integer> comps = Map.of(
        InstructionComponent.INSTRUCTION, instr,
        InstructionComponent.REGISTER_A, r,
        InstructionComponent.IMMEDIATE, v
    );

    return Collections.singletonList(STYPE.encode(comps));
  };}

  private static AEncoder wrap(String name, String ra, String rb, String rc, String v, AEncoder base) {
    int acount = (ra.equals("") ? 0 : 1)
        + (rb.equals("") ? 0 : 1)
        + (rc.equals("") ? 0 : 1)
        + (v.equals("") ? 0 : 1);

    return (args) -> {
      if (args.length != acount) {
        throw new IllegalArgumentException(String.format("Error parsing %s: Expected %d arguments"
            + " but found %d", name, acount, args.length));
      }

      try {
        return base.apply(args);
      } catch (IllegalArgumentException e) {
        String msg = e.getMessage();
        msg = msg.replaceFirst("\\$RA", ra);
        msg = msg.replaceFirst("\\$RB", rb);
        msg = msg.replaceFirst("\\$RC", rc);
        msg = msg.replaceFirst("\\$V", v);

        throw new IllegalArgumentException("Error parsing "+name+": "+msg);
      }
    };
  }

  private static AEncoder outerWrap(String name, AEncoder inner) {
    return (args) -> {
      try {
        return inner.apply(args);
      } catch (IllegalArgumentException e) {
        String msg = e.getMessage().substring(e.getMessage().indexOf(":")+2);
        msg = "Error parsing " + name + ": " + msg;

        throw new IllegalArgumentException(msg);
      }
    };
  }

  private static List<Integer> pseudo(List<Integer>... parts) {
    return Arrays.stream(parts).map(e -> e.get(0)).collect(Collectors.toList());
  }

  public static InstructionSet match(String key) {
    try {
      return valueOf(key.toUpperCase());
    } catch (IllegalArgumentException | NullPointerException e) {
      return null;
    }
  }
}

interface AEncoder extends Function<String[], List<Integer>> {

}
