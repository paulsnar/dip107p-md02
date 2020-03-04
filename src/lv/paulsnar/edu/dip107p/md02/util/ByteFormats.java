package lv.paulsnar.edu.dip107p.md02.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.WeakHashMap;

public final class ByteFormats {
  private ByteFormats() { }

  private static int getSignlessByte(byte[] array, int offset) throws ParseException {
    int value = (int) array[offset];
    if (value < 0) {
      throw new ParseException("Top bit was set unexpectedly");
    }
    return value;
  }
  private static int getUnsignedByte(byte[] array, int offset) {
    int value = (int) array[offset];
    if (value < 0) {
      value += 256;
    }
    return value;
  }
  
  public static int getU15(byte[] array, int offset) throws ParseException {
    int value;
    try {
      value = getSignlessByte(array, offset) << 8;
    } catch (ParseException exc) {
      throw new ParseException("Malformed U31: top bit was set");
    }
    value |= getUnsignedByte(array, offset + 1);
    return value;
  }
  
  public static void putU15(int value, byte[] array, int offset) throws ParseException {
    if (value < 0 || value > 0x7FFF) {
      throw new ParseException("Value out of bounds for U15: " + value);
    }
    
    array[offset] = (byte) ((value & 0x7F00) >> 8);
    array[offset + 1] = (byte) (value & 0xFF);
  }

  public static int readU15(RandomAccessFile file) throws IOException, ParseException {
    int b1 = file.read(),
        b2 = file.read();
    if (b1 == -1 || b2 == -1) {
      throw new EOFException();
    }
    if (b1 > 128) {
      throw new ParseException("Malformed U15: top bit was set");
    }
    return (b1 << 8) | b2;
  }
  
  public static void writeU15(int value, RandomAccessFile file) throws IOException, ParseException {
    if (value < 0 || value > 0x7FFF) {
      throw new ParseException("Value out of bounds for U15: " + value);
    }
    
    file.write(value >> 8);
    file.write(value & 0x7F);
  }
  
  public static int getU31(byte[] array, int offset) throws ParseException {
    int value;
    try {
      value = getSignlessByte(array, offset) << 24;
    } catch (ParseException exc) {
      throw new ParseException("Malformed U31: top bit was set");
    }
    value |= getUnsignedByte(array, offset + 1) << 16;
    value |= getUnsignedByte(array, offset + 2) << 8;
    value |= getUnsignedByte(array, offset + 3);
    return value;
  }

  public static void putU31(int value, byte[] array, int offset) throws ParseException {
    if (value < 0) {
      throw new ParseException("Value out of bounds for U31: " + value);
    }

    array[offset] = (byte) ((value & 0x7F000000) >> 24);
    array[offset + 1] = (byte) ((value & 0xFF0000) >> 16);
    array[offset + 2] = (byte) ((value & 0xFF00) >> 8);
    array[offset + 3] = (byte) (value & 0xFF);
  }
  
  public static int varintBinarySize(long value) {
    if (-64L < value && value < 64L && value != -1) { // 6 bits
      return 1;
    } else if (-8192L < value && value < 8192L) { // 7 + 6 bits
      return 2;
    } else if (-1048576L < value && value < 1048576L) { // 2*7 + 6 bits
      return 3;
    } else if (-134217728L < value && value < 134217728L) { // 3*7 + 6
      return 4;
    } else if (-17179869184L < value && value < 17179869184L) { // 4*7 + 6
      return 5;
    } else if (-2199023255552L < value && value < 2199023255552L) { // 5*7 + 6
      return 6;
    } else if (-281474976710656L < value && value < 281474976710656L) { // 6*7 + 6
      return 7;
    } else if (-36028797018963968L < value && value < 36028797018963968L) { // 7*7 + 6
      return 8;
    } else if (-4611686018427387904L < value && value < 4611686018427387904L) { // 8*7 + 6
      return 9;
    } else { // out of bounds
      return -1;
    }
  }
  
  public static long readVarint(RandomAccessFile file) throws IOException, ParseException {
    long value = 0L;
    for (int pieces = 0; pieces < 9; pieces += 1) {
      int piece = file.read();
      if (piece == -1) {
        throw new EOFException();
      }
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
    throw new ParseException("Overlong varint");
  }
  
  public static void writeVarint(long value, RandomAccessFile file)
      throws IOException, ParseException {
    if (value == 0) {
      file.write(1);
      return;
    }
    int size = varintBinarySize(value);
    if (size == -1) {
      throw new ParseException("Varint value out of bounds");
    }
    
    byte[] varint = new byte[size];
    if (value < 0) {
      varint[size - 1] = 1;
      value *= -1;
    }
    varint[size - 1] |= (byte) ((value & 0x3F) << 1);
    for (int i = 1; i < size; i += 1) {
      int shift = 7 * i - 1;
      varint[size - i - 1] = (byte) ((1 << 7) | ((value & (0x7F << shift)) >> shift));
    }
    file.write(varint);
  }
  
  public static int stringBinarySize(String string) {
    int size = measureStringLength(string);
    size += varintBinarySize((long) size);
    return size;
  }

  private static WeakHashMap<String, Integer> stringLengthCache = new WeakHashMap<>();
  public static int measureStringLength(String string) {
    if (stringLengthCache.containsKey(string)) {
      return stringLengthCache.get(string).intValue();
    }

    /** @see https://www.fileformat.info/info/unicode/utf8.htm */
    int size = 0;
    Iterator<Integer> codePoints = string.codePoints().iterator();
    while (codePoints.hasNext()) {
      int sym = codePoints.next();
      if (sym == 0) {
        size += 2;
      } else if (sym < 0x80) {
        size += 1;
      } else if (sym < 0x800) {
        size += 2;
      } else if (sym < 0x10000) {
        size += 3;
      } else if (sym < 0x110000) {
        size += 4;
      } else {
        throw new RuntimeException("Code point out of bounds: " + sym);
      }
    }
    stringLengthCache.put(string, size);
    return size;
  }
  
  public static String readString(RandomAccessFile file) throws IOException, ParseException {
    int size = (int) readVarint(file);
    StringBuilder str = new StringBuilder(size);
    
    int pos = 0;
    /** @see https://www.fileformat.info/info/unicode/utf8.htm */
    while (pos < size) {
      int piece = file.read();
      if (piece == -1) {
        throw new EOFException();
      }
      if ((piece & 0x80) == 0) {
        pos += 1;
        str.append((char) piece);
        continue;
      }
      if ((piece & 0xE0) == 0xC0) {
        int p2 = file.read();
        if ((p2 & 0xC0) != 0x80) {
          throw new ParseException("Malformed UTF-8 sequence");
        }
        pos += 2;

        int sym = (piece & 0x1F) << 6;
        sym |= p2 & 0x3F;
        str.append((char) sym);
        continue;
      }
      if ((piece & 0xF0) == 0xE0) {
        int p2 = file.read(),
            p3 = file.read();
        if ((p2 & 0xC0) != 0x80 || (p3 & 0xC0) != 0x80) {
          throw new ParseException("Malformed UTF-8 sequence");
        }
        pos += 3;

        int sym = (piece & 0x0F) << 12;
        sym |= (p2 & 0x3F) << 6;
        sym |= p3 & 0x3F;
        if (sym >= 0xD800) {
          throw new ParseException("Unexpected surrogate codepoint in UTF-8");
        }
        str.append((char) sym);
        continue;
      }
      if ((piece & 0xF8) == 0xF0) {
        int p2 = file.read(),
            p3 = file.read(),
            p4 = file.read();
        if ((p2 & 0xC0) != 0x80 || (p3 & 0xC0) != 0x80 || (p4 & 0xC0) != 0x80) {
          throw new ParseException("Malformed UTF-8 sequence");
        }
        pos += 4;

        int sym = (piece & 0x07) << 18;
        sym |= (p2 & 0x3F) << 12;
        sym |= (p3 & 0x3F) << 6;
        sym |= p4 & 0x3F;
        // treat surrogates like Java doesn't
        if (sym >= 0x10000) {
          sym -= 0x10000;
          char surr1 = (char) (0xD800 | (sym >> 10)),
               surr2 = (char) (0xDC00 | (sym & 0x3FF));
          str.append(surr1);
          str.append(surr2);
        } else {
          str.append((char) sym);
        }
        continue;
      }
      throw new ParseException("Malformed UTF-8 initial byte");
    }
    
    String string = str.toString();
    stringLengthCache.put(string, size);
    return string;
  }
  
  public static void writeString(String string, RandomAccessFile file)
      throws IOException, ParseException {
    int size = measureStringLength(string);
    writeVarint((long) size, file);
    
    /** @see https://www.fileformat.info/info/unicode/utf8.htm */
    byte[] buf = new byte[4];
    Iterator<Integer> codePoints = string.codePoints().iterator();
    while (codePoints.hasNext()) {
      int sym = codePoints.next();
      if (sym == 0) {
        // overlong zero
        buf[0] = (byte) 0xC0;
        buf[1] = (byte) 0x80;
        file.write(buf, 0, 2);
      } else if (sym < 0x80) {
        file.write(sym);
      } else if (sym < 0x800){
        buf[0] = (byte) (0xC0 | (sym & 0x7C0) >> 6);
        buf[1] = (byte) (0x80 | (sym & 0x3F));
        file.write(buf, 0, 2);
      } else if (sym < 0x10000) {
        buf[0] = (byte) (0xE0 | (sym & 0xF000) >> 12);
        buf[1] = (byte) (0x80 | (sym & 0xFC0) >> 6);
        buf[2] = (byte) (0x80 | (sym & 0x3F));
        file.write(buf, 0, 3);
      } else if (sym < 0x110000) {
        buf[0] = (byte) (0xF0 | (sym & 0x1C0000) >> 18);
        buf[1] = (byte) (0x80 | (sym & 0x3F000) >> 12);
        buf[2] = (byte) (0x80 | (sym & 0xFC0) >> 6);
        buf[3] = (byte) (0x80 | (sym & 0x3F));
        file.write(buf, 0, 4);
      } else {
        // technically unreachable due to the same being tested for in stringBinarySize, but anyway:
        throw new RuntimeException("Code point uot of bounds: " + sym);
      }
    }
  }
}
