package lv.paulsnar.edu.dip107p.md02.db;

import java.io.IOException;

public class MalformedDatabaseException extends IOException {
  public MalformedDatabaseException() {
    super();
  }

  public MalformedDatabaseException(String message) {
    super(message);
  }

  public MalformedDatabaseException(String message, Throwable parent) {
    super(message, parent);
  }

  private static String appendOffsetToMessage(String message, long offset) {
    return String.format("%s (at file offset %d)", message, offset);
  }

  public MalformedDatabaseException(String message, long offset) {
    this(appendOffsetToMessage(message, offset));
  }

  public MalformedDatabaseException(String message, long offset,
      Throwable parent) {
    this(appendOffsetToMessage(message, offset), parent);
  }
}
