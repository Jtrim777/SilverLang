# SilverLang
An instruction set, assembler, and executor for a custom machine language, as well as a low level standard language

## SilverAssembler
Defines, in Java, the instruction set for this machine langauge, as well as providing an assembler for the language and a virtual machine to execute programs.

### Main Application
The SilverAssembler application can be run in several forms:
- assemble :: This command compiles an assembly language program (as defined [here](https://github/com/Jtrim777/SilverLang/blob/master/AssemblyProgram.md)) into 
an executable file (whose format is defined [here](https://github/com/Jtrim777/SilverLang/blob/master/ExecutableFormat.md))
  - Usage: `assemble [options] <source[.s]> [destination[.o]]`
    - `source` specifies the assembly file to assemble from. The file is presumed to have a ".s" extension unless another is specified or the `-xp` flag is used
    - `destination` specifies the filepath to save the executable to. The executable will have the extension ".o" unless another is specified or the `-xp` flag is used.
    If `destination` is not provided, the executable will be generated with the same filename as `source` but with a ".o" extension
    - Options:
      - `-v`         | Enables verbose assembly
      - `-xp`        | Use the exact filepath provided for source and destination files, do not add extensions
      - `-hx`        | Generate a hexdump of the assembled program, saved to `destination.hx`
      - `-l <path>`  | Prelink the executable at the provided path. The executable must have been generated with a linking table
      - `-tbl`       | Generate the executable with a linking table so that it can be used as a library
- boot :: This command starts the virtual machine (see [Using the VM](https://github/com/Jtrim777/SilverLang/blob/master/UsingTheVM.md))
  - Usage: `boot [options]`
    - Options:
      - `-os <path>` | Directs the VM to look for OS files at the provided path. See [OS Files](https://github/com/Jtrim777/SilverLang/blob/master/OSFiles.md)
      
