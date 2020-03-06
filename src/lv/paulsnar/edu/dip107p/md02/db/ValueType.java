package lv.paulsnar.edu.dip107p.md02.db;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

class ValueType {
  enum Kind {
    NULL (1),
    FALSE (2),
    TRUE (3),
    STRING (4),
    VARINT (5);

    public int value;
    private Kind(int value) {
      this.value = value;
    }
  }
  public static ValueType readFrom(Page page)
      throws MalformedValueException, PageBoundaryExceededException,
      IOException {
    int type = page.read();
    if (type == Kind.NULL.value) {
      return ValueType.NULL;
    } else if (type == Kind.FALSE.value) {
      return ValueType.FALSE;
    } else if (type == Kind.TRUE.value) {
      return ValueType.TRUE;
    } else if (type == Kind.VARINT.value) {
      long value = Varint.readFrom(page);
      return new ValueType(Kind.VARINT, value);
    } else if (type == Kind.STRING.value) {
      String string = BinaryString.readFrom(page);
      return new ValueType(string);
    } else {
      throw new MalformedValueException(
        "Unknown ValueType identifier: " + type, page.offset());
    }
  }

  public static int sizeOf(ValueType type) {
    switch (type.kind) {
      case NULL: // fallthrough
      case FALSE: // fallthrough
      case TRUE: return 1;
      case VARINT: return sizeOf(type.longValue);
      case STRING: return sizeOf(type.stringValue);
      default: return -1;
    }
  }

  public static int sizeOf(String string) {
    return 1 + BinaryString.byteSize(string);
  }

  public static int sizeOf(boolean bool) {
    return 1;
  }

  public static int sizeOf(long number) {
    return 1 + Varint.byteSize(number);
  }

  public static void writeTo(ValueType type, ValueWriterInterface target)
      throws ValueOutOfBoundsException, IOException {
  }

  public static void writeNullTo(ValueWriterInterface target)
      throws IOException {
    target.write(Kind.NULL.value);
  }

  public static void writeTo(boolean bool, ValueWriterInterface target)
      throws IOException {
    target.write((bool ? Kind.TRUE : Kind.FALSE).value);
  }

  public static void writeTo(long varint, ValueWriterInterface target)
      throws ValueOutOfBoundsException, IOException {
    target.write(Kind.VARINT.value);
    Varint.writeTo(varint, target);
  }

  public static void writeTo(String string, ValueWriterInterface target)
      throws ValueOutOfBoundsException, IOException {
    target.write(Kind.STRING.value);
    BinaryString.writeTo(string, target);
  }

  public final static ValueType
    NULL = new ValueType(Kind.NULL),
    FALSE = new ValueType(Kind.FALSE),
    TRUE = new ValueType(Kind.TRUE);

  private Kind kind;
  private long longValue;
  private String stringValue;

  private ValueType(Kind kind) {
    this.kind = kind;
  }

  private ValueType(Kind kind, long number) {
    this.kind = kind;
    longValue = number;
  }

  private ValueType(String string) {
    stringValue = string;
    kind = Kind.STRING;
  }

  public Kind getKind() {
    return kind;
  }

  public String stringValue() {
    if (kind != Kind.STRING) {
      throw new BadValueException("Value type is not a string");
    }
    return stringValue;
  }

  public int intValue() {
    if (kind != Kind.VARINT) {
      throw new BadValueException("Value type is not an integer");
    }
    int value = (int) longValue;
    if (value != longValue) {
      throw new BadValueException("Integer doesn't fit into an int");
    }
    return value;
  }

  public long longValue() {
    if (kind != Kind.VARINT) {
      throw new BadValueException("Value type is not an integer");
    }
    return longValue;
  }

  public boolean booleanValue() {
    if (kind == Kind.TRUE) {
      return true;
    } else if (kind == Kind.FALSE) {
      return false;
    } else {
      throw new BadValueException("Value type is not a boolean");
    }
  }

  public boolean isNull() {
    return kind == Kind.NULL;
  }

  public void writeTo(Page page)
      throws ValueOutOfBoundsException, IOException {
    switch (kind) {
      case NULL:
        writeNullTo(page);
        break;
      case TRUE: // fallthrough
        writeTo(true, page);
        break;
      case FALSE:
        writeTo(false, page);
        break;
      case VARINT:
        writeTo(longValue, page);
        break;
      case STRING:
        writeTo(stringValue, page);
        break;
    }
  }
}
