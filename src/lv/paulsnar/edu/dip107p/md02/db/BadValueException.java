package lv.paulsnar.edu.dip107p.md02.db;

class BadValueException extends RuntimeException {
  private static final long serialVersionUID = 2352219295988141342L;

  BadValueException(String message) {
    super(message);
  }
}
