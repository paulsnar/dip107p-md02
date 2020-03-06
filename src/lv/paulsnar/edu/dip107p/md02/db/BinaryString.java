package lv.paulsnar.edu.dip107p.md02.db;

import java.io.IOException;
import java.util.Iterator;
import java.util.WeakHashMap;

/**
 *  @see https://www.fileformat.info/unicode/utf8.htm
 */
final class BinaryString {
  private BinaryString() { }

  private static WeakHashMap<String, Integer> stringLengthCache =
    new WeakHashMap<>();

  public static int measureStringLength(String string) {
    if (stringLengthCache.containsKey(string)) {
      return stringLengthCache.get(string).intValue();
    }

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
        throw new ValueOutOfBoundsException(
          "Code point within string out of bounds: " + sym);
      }
    }
    stringLengthCache.put(string, size);
    return size;
  }

  public static int byteSize(String string) {
    int size = measureStringLength(string);
    return size + Varint.byteSize(size);
  }

  public static String readFrom(ValueReaderInterface source)
      throws MalformedDatabaseException, IOException {
    int size = (int) Varint.readFrom(source);
    StringBuilder str = new StringBuilder(size);

    int pos = 0;
    while (pos < size) {
      int p1 = source.read();
      if ((p1 & 0x80) == 0) {
        pos += 1;
        str.append((char) p1);
      } else if ((p1 & 0xE0) == 0xC0) {
        int p2 = source.read();
        if ((p2 & 0xC0) != 0x80) {
          throw new MalformedDatabaseException(
            "Invalid UTF-8 sequence", source.position());
        }

        pos += 2;
        int sym = (p1 & 0x1F) << 6;
        sym |= p2 & 0x3F;
        str.append((char) sym);
      } else if ((p1 & 0xF0) == 0xE0) {
        int p2 = source.read();
        int p3 = source.read();
        if ((p2 & 0xC0) != 0x80 || (p3 & 0xC0) != 0x80) {
          throw new MalformedDatabaseException(
            "Invalid UTF-8 sequence", source.position());
        }

        pos += 3;
        int sym = (p1 & 0x0F) << 12;
        sym |= (p2 & 0x3F) << 6;
        sym |= p3 & 0x3F;
        if (sym >= 0xD800) {
          throw new MalformedDatabaseException(
            "Invalid UTF-8 sequence - unexpected surrogate codepoint",
            source.position());
        }
        str.append((char) sym);
      } else if ((p1 & 0xF8) == 0xF0) {
        int p2 = source.read();
        int p3 = source.read();
        int p4 = source.read();
        if ((p2 & 0xC0) != 0x80 ||
            (p3 & 0xC0) != 0x80 ||
            (p4 & 0xC0) != 0x80) {
          throw new MalformedDatabaseException(
            "Invalid UTF-8 sequence", source.position());
        }

        pos += 4;
        int sym = (p1 & 0x07) << 18;
        sym |= (p2 & 0x3F) << 12;
        sym |= (p3 & 0x3F) << 6;
        sym |= p4 & 0x3F;

        // treat surrogate pairs
        if (0xD800 <= sym && sym < 0x10000) {
          throw new MalformedDatabaseException(
            "Invalid UTF-8 sequence - unexpected surrogate codepoint",
            source.position());
        }
        if (sym >= 0x10000) {
          sym -= 0x10000;
          char surr1 = (char) (0xD800 | sym >> 10);
          char surr2 = (char) (0xDC00 | (sym & 0x3FF));
          str.append(surr1);
          str.append(surr2);
        } else {
          str.append((char) sym);
        }
      } else {
        throw new MalformedDatabaseException(
          "Invalid UTF-8 sequence", source.position());
      }
    }

    String string = str.toString();
    stringLengthCache.put(string, size);
    return string;
  }

  public static void writeTo(String string, ValueWriterInterface target)
      throws ValueOutOfBoundsException, IOException {
    int size = measureStringLength(string);
    Varint.writeTo(size, target);

    byte[] buf = new byte[4];
    Iterator<Integer> codePoints = string.codePoints().iterator();
    while (codePoints.hasNext()) {
      int sym = codePoints.next();
      if (sym == 0) {
        // not sure why but we still prefer overlong encoding
        buf[0] = (byte) 0xC0;
        buf[1] = (byte) 0x80;
        target.write(buf, 0, 2);
      } else if (sym < 0x80) {
        target.write(sym);
      } else if (sym < 0x800) {
        buf[0] = (byte) (0xC0 | (sym & 0x7C0) >> 6);
        buf[1] = (byte) (0x80 | (sym & 0x3F));
        target.write(buf, 0, 2);
      } else if (sym < 0x800) {
        buf[0] = (byte) (0xE0 | (sym & 0xF000) >> 12);
        buf[1] = (byte) (0x80 | (sym & 0xFC0) >> 6);
        buf[2] = (byte) (0x80 | (sym & 0x3F));
        target.write(buf, 0, 3);
      } else if (sym < 0x110000) {
        buf[0] = (byte) (0xF0 | (sym & 0x1C0000) >> 18);
        buf[1] = (byte) (0x80 | (sym & 0x3F000) >> 12);
        buf[2] = (byte) (0x80 | (sym & 0xFC0) >> 6);
        buf[3] = (byte) (0x80 | (sym & 0x3F));
        target.write(buf, 0, 4);
      } else {
        throw new ValueOutOfBoundsException("Code point out of bounds: " + sym);
      }
    }
  }
}
