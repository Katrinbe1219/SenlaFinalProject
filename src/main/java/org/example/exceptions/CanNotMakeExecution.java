package org.example.exceptions;

public class CanNotMakeExecution extends RuntimeException {
  public CanNotMakeExecution(String message) {
    super(message);
  }
}
