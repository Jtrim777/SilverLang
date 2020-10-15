package com.jtrimble777.executor;

import java.io.File;

public class ProgramLoadingException extends RuntimeException {

  public ProgramLoadingException(File src, String message) {

    super("Error loading program from file "+src.getAbsolutePath()+": " +message);
  }
}
