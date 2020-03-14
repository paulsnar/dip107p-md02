package lv.paulsnar.edu.dip107p.md02;

import java.io.IOException;

final class MalformedDatabaseException extends IOException {
  MalformedDatabaseException(String message) {
    super(message);
  }
}
