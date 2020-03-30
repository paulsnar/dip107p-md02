package lv.paulsnar.edu.dip107p.md02.db;

import java.io.IOException;

public final class MalformedDatabaseException extends IOException {
  private static final long serialVersionUID = -7576988280222702910L;

  MalformedDatabaseException(String message) {
    super(message);
  }
}
