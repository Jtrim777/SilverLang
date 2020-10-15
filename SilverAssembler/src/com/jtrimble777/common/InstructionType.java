package com.jtrimble777.common;

import java.util.Map;
import java.util.function.Function;

public enum InstructionType {
  ITYPE((instr) -> {
    int i = instr >> 28;
    int a = (instr >> 23) % 32;
    int b = (instr >> 18) % 32;
    int v = instr % 65536;
    return Map.of(
        InstructionComponent.INSTRUCTION, i,
        InstructionComponent.REGISTER_A, a,
        InstructionComponent.REGISTER_B, b,
        InstructionComponent.IMMEDIATE, v
    );
  },
      (components) -> {
    int i = components.get(InstructionComponent.INSTRUCTION);
    int a = components.get(InstructionComponent.REGISTER_A);
    int b = components.get(InstructionComponent.REGISTER_B);
    int v = components.get(InstructionComponent.IMMEDIATE);
//    System.out.println(String.format("I = %d | RA = %d | RB = %d | V = %d", i,a,b,v));
    return (i << 28) + (a << 23) + (b << 18) + v;
  }),

  RTYPE((instr) -> {
    int i = (instr >> 8) % 32;
    int a = (instr >> 23) % 32;
    int b = (instr >> 18) % 32;
    int c = (instr >> 13) % 32;
    return Map.of(
        InstructionComponent.INSTRUCTION, i,
        InstructionComponent.REGISTER_A, a,
        InstructionComponent.REGISTER_B, b,
        InstructionComponent.DEST_REGISTER, c
    );
  },
      (components) -> {
    int i = components.get(InstructionComponent.INSTRUCTION);
    int a = components.get(InstructionComponent.REGISTER_A);
    int b = components.get(InstructionComponent.REGISTER_B);
    int c = components.get(InstructionComponent.DEST_REGISTER);
    return (a << 23) + (b << 18) + (c << 13) + (i << 8);
  }),

  STYPE((instr) -> {
    int i = (instr >> 28) % 32;
    int a = (instr >> 23) % 32;
    int v = instr % 8388608;
    return Map.of(
        InstructionComponent.INSTRUCTION, i,
        InstructionComponent.REGISTER_A, a,
        InstructionComponent.IMMEDIATE, v
    );
  },
      (components) -> {
    int i = components.get(InstructionComponent.INSTRUCTION);
    int a = components.get(InstructionComponent.REGISTER_A);
    int v = components.get(InstructionComponent.IMMEDIATE);
    return (i << 28) + (a << 23) + v;
  }),

  PSEUDO(null, null);

  private Function<Integer, Map<InstructionComponent, Integer>> decoder;
  private Function<Map<InstructionComponent, Integer>, Integer> encoder;

  InstructionType(Function<Integer, Map<InstructionComponent, Integer>> dec,
      Function<Map<InstructionComponent, Integer>, Integer> enc) {
    this.decoder = dec;
    this.encoder = enc;
  }

  public Integer encode(Map<InstructionComponent, Integer> inp) {
    return this.encoder.apply(inp);
  }

  public Map<InstructionComponent, Integer> decode(Integer inp) {
    return this.decoder.apply(inp);
  }

  public enum InstructionComponent {
    INSTRUCTION,
    REGISTER_A,
    REGISTER_B,
    DEST_REGISTER,
    IMMEDIATE
  }
}
