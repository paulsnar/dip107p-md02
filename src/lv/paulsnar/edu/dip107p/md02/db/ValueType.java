package lv.paulsnar.edu.dip107p.md02.db;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import lv.paulsnar.edu.dip107p.md02.util.ByteFormats;
import lv.paulsnar.edu.dip107p.md02.util.ParseException;
import lv.paulsnar.edu.dip107p.md02.util.SizedString;

class ValueType {
  enum Kind {
    NULL (0),
    VARINT (1),
    FALSE (2),
    TRUE (3),
    STRING (4),
    SMALLINT (128);
    
    public int value;
    private Kind(int value) {
      this.value = value;
    }
  }
  public static ValueType readFrom(RandomAccessFile file) throws IOException, ParseException {
    int type = file.read();
    if (type == Kind.NULL.value) {
      return ValueType.NULL;
    } else if (type == Kind.FALSE.value) {
      return ValueType.FALSE;
    } else if (type == Kind.TRUE.value) {
      return ValueType.TRUE;
    } else if (type == Kind.VARINT.value) {
      return new ValueType(ByteFormats.readVarint(file));
    } else if (type == Kind.STRING.value) {
      SizedString string = ByteFormats.readString(file);
      return new ValueType(string.string, string.size);
    } else if ((type & Kind.SMALLINT.value) == Kind.SMALLINT.value) {
      int sign = type & 1;
      sign = (sign == 1) ? -1 : 1;
      int value = (type & 0x7E) >> 1;
      return new ValueType(sign * value);
    } else {
      throw new ParseException("Unknown ValueType identifier: " + type);
    }
  }

  public static ValueType[] readFrom(RandomAccessFile file, int size)
      throws IOException, ParseException {
    ArrayList<ValueType> list = new ArrayList<>();
    int i;
    for (i = 0; i < size; ) {
      ValueType type = readFrom(file);
      size += type.binarySize;
      list.add(type);
    }
    if (i > size) {
      throw new ParseException("Malformed value type list: size exceeded by " + (i - size));
    }
    return (ValueType[]) list.toArray();
  }
  
  public static int sizeOf(ValueType type) {
    return type.binarySize;
  }
  
  public static int sizeOf(String string) {
    return ByteFormats.stringBinarySize(string) + 1;
  }
  
  public static int sizeOf(boolean bool) {
    return 1;
  }
  
  public static int sizeOf(long number) {
    if (-64L <= number && number < 64L) {
      return 1;
    } else {
      return 1 + ByteFormats.varintBinarySize(number);
    }
  }
  
  public static void writeTo(ValueType type, RandomAccessFile file)
      throws IOException, ParseException {
    type.writeTo(file);
  }
  
  public static void writeTo(boolean bool, RandomAccessFile file)
      throws IOException, ParseException {
    (bool ? ValueType.TRUE : ValueType.FALSE).writeTo(file);
  }
  
  public static void writeTo(long varint, RandomAccessFile file)
      throws IOException, ParseException {
    new ValueType(varint).writeTo(file);
  }
  
  public static void writeTo(String string, RandomAccessFile file)
      throws IOException, ParseException {
    if (string == null) {
      throw new NullPointerException();
    }
    new ValueType(string).writeTo(file);
  }
  
  public final static ValueType
    NULL = new ValueType(Kind.NULL),
    FALSE = new ValueType(Kind.FALSE),
    TRUE = new ValueType(Kind.TRUE);
  
  private Kind kind;
  private long longValue;
  private String stringValue;
  private int binarySize;
  
  private ValueType(Kind kind) {
    this.kind = kind;
    binarySize = 1;
  }
  
  private ValueType(long number) { 
    longValue = number;
    if (-64L <= number && number < 64L) {
      kind = Kind.SMALLINT;
      binarySize = 1;
    } else {
      kind = Kind.VARINT;
      binarySize = ByteFormats.varintBinarySize(number) + 1;
    }
  }
  
  private ValueType(String string) {
    this(string, 0);
  }
  private ValueType(String string, int binarySize) {
    stringValue = string;
    kind = Kind.STRING;
    if (binarySize == 0) {
      this.binarySize = ByteFormats.stringBinarySize(string) + 1;
    } else {
      this.binarySize = binarySize;
    }
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
  
  public long longValue() {
    if (kind != Kind.VARINT && kind != Kind.SMALLINT) {
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
  
  public void writeTo(RandomAccessFile file) throws IOException, ParseException {
    if (kind == Kind.NULL || kind == Kind.FALSE || kind == Kind.TRUE) {
      file.write(kind.value);
    } else if (kind == Kind.SMALLINT) {
      int value = kind.value;
      value |= (int) (longValue & 0x3F) << 1;
      if (value < 0) {
        value |= 1;
      }
      file.write(value);
    } else if (kind == Kind.STRING) {
      file.write(kind.value);
      ByteFormats.writeString(stringValue, file);
    } else if (kind == Kind.VARINT) {
      file.write(kind.value);
      ByteFormats.writeVarint(longValue, file);
    } else {
      throw new RuntimeException(
          "Don't know how to write to file a value of kind " + kind.toString());
    }
  }
}
