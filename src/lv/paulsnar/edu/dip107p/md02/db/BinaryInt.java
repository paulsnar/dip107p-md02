package lv.paulsnar.edu.dip107p.md02.db;

final class BinaryInt {
  private BinaryInt() { }

  public static final int U15_SIZE = 2;
  public static final int U31_SIZE = 4;

  public static int getU15(byte[] buf, int offset)
      throws MalformedValueException {
    int a = (buf[offset] + 256) % 256;
    int b = (buf[offset + 1] + 256) % 256;
    if (a >= 128) {
      throw new MalformedValueException("Invalid u15: top bit set");
    }
    return (a << 8) | b;
  }

  public static int getU31(byte[] buf, int offset)
      throws MalformedValueException {
    int a = (buf[offset] + 256) % 256;
    int b = (buf[offset + 1] + 256) % 256;
    int c = (buf[offset + 2] + 256) % 256;
    int d = (buf[offset + 3] + 256) % 256;
    if (a >= 128) {
      throw new MalformedValueException("Invalid u31: top bit set");
    }
    return (a << 24) | (b << 16) | (c << 8) | d;
  }
}
