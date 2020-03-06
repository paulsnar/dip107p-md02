package lv.paulsnar.edu.dip107p.md02.db;

class ValueOutOfBoundsException extends RuntimeException
{
  public ValueOutOfBoundsException() {
    super();
  }

  public ValueOutOfBoundsException(String message) {
    super(message);
  }

  public ValueOutOfBoundsException(String message, Throwable previous) {
    super(message, previous);
  }
}
