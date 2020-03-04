package lv.paulsnar.edu.dip107p.md02.db;

class BadValueException extends RuntimeException {
  BadValueException(String message) {
    super(message);
  }
}
