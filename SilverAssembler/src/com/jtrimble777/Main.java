package com.jtrimble777;

import com.jtrimble777.assembler.Assembler;
import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Must provide a command, one of [assemble | execute | hex | unhex]");
            return;
        }

        String command = args[0];
        System.out.println("Running in command mode '"+command+"'");
        switch (command) {
            case "assemble":

                if (args.length < 2) {
                    System.out.println("The assemble command requires positional argument "
                        + "<filename>");
                    return;
                }
                String sourceFile = args[1];
                if (!sourceFile.contains(".")) {
                    sourceFile += ".s";
                }
                String destFile = sourceFile.replaceAll("\\.s", ".o");
                if (args.length > 2) {
                    switch (args[2]) {
                        case "-o":
                            if (args.length < 4) {
                                System.out.println("The -o flag for assemble requires "
                                    + "positional argument <out_filename>");
                                return;
                            }
                            destFile = args[3];
                            if (!destFile.contains(".")) {
                                destFile += ".o";
                            }
                            break;
                    }
                }

                assemble(sourceFile, destFile);
                break;

        }
    }

    private static void assemble(String sfnm, String dfnm) throws IOException {
        File sourceFile = new File(sfnm);
        File outFile = new File(dfnm);

        Assembler assembler = new Assembler(sourceFile, true);
        assembler.assemble();

        assembler.writeToFile(outFile);
    }
}
