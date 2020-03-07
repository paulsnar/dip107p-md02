package lv.paulsnar.edu.dip107p.md02.db;

import java.io.IOException;

class MalformedValueException extends IOException {
  private static final long serialVersionUID = -5007557726760996455L;

  public MalformedValueException() {
    super();
  }

  public MalformedValueException(String message) {
    super(message);
  }

  public MalformedValueException(String message, Throwable parent) {
    super(message, parent);
  }

  private static String appendOffsetToMessage(String message, long offset) {
    return String.format("%s (at file offset %d)", message, offset);
  }

  public MalformedValueException(String message, long offset) {
    this(appendOffsetToMessage(message, offset));
  }

  public MalformedValueException(String message, long offset,
      Throwable parent) {
    this(appendOffsetToMessage(message, offset), parent);
  }
}
