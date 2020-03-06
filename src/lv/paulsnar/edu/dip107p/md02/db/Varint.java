package lv.paulsnar.edu.dip107p.md02.db;

import java.io.IOException;

final class Varint {
  private Varint() { }

  static int byteSize(long value) {
    if (-64L < value && value < 64L && value != -1) { // 6 bits
      return 1;
    } else if (-8192L < value && value < 8192) { // 7 + 6 bits
      return 2;
    } else if (-1048576L < value && value < 1048576L) { // 2*7 + 6 bits
      return 3;
    } else if (-134217728L < value && value < 134217728L) { // 3*7 + 6
      return 4;
    } else if (-17179869184L < value && value < 17179869184L) { // 4*7 + 6
      return 5;
    } else if (-2199023255552L < value && value < 2199023255552L) { // etc...
      return 6;
    } else if (-281474976710656L < value && value < 281474976710656L) {
      return 7;
    } else if (-36028797018963968L < value && value < 36028797018963968L) {
      return 8;
    } else if (-4611686018427387904L < value && value < 4611686018427387904L) {
      return 9;
    } else { // out of bounds
      return -1;
    }
  }

  public static long readFrom(ValueReaderInterface source)
      throws MalformedValueException, IOException {
    long value = 0L;
    for (int pieces = 0; pieces < 9; pieces += 1) {
      int piece = source.read();
      if ((piece & 0x80) == 0) {
        value |= (piece & 0x7E) >> 1;
        int sign = piece & 1;
        if (sign == 1) {
          value *= -1;
        }
        return value;
      } else {
        value |= (piece & 0x7F);
        value <<= 7;
      }
    }
    throw new MalformedValueException(
      "Varint longer than 9 bytes", source.position());
  }

  public static void writeTo(long value, ValueWriterInterface target)
      throws IOException, ValueOutOfBoundsException {
    if (value == 0L) {
      target.write(1);
      return;
    }

    int size = byteSize(value);
    if (size == -1) {
      throw new ValueOutOfBoundsException(
        "Varint value " + value + " out of bounds");
    }

    byte[] varint = new byte[size];
    if (value < 0) {
      varint[size - 1] = 1;
      value *= -1;
    }
    varint[size - 1] |= (byte) ((value & 0x3F) << 1);

    for (int i = 1; i < size; i += 1) {
      int shift = 7 * i - 1;
      varint[size - i - 1] = (byte)
        ((1 << 7) | ((value & (0x7F << shift)) >> shift));
    }

    target.write(varint);
  }
}
