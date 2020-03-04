package lv.paulsnar.edu.dip107p.md02.db;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import lv.paulsnar.edu.dip107p.md02.util.ByteFormats;
import lv.paulsnar.edu.dip107p.md02.util.ParseException;

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
  public static ValueType readFrom(RandomAccessFile file) throws IOException, ParseException {
    int type = file.read();
    if (type == -1) {
      throw new EOFException();
    }
    if (type == Kind.NULL.value) {
      return ValueType.NULL;
    } else if (type == Kind.FALSE.value) {
      return ValueType.FALSE;
    } else if (type == Kind.TRUE.value) {
      return ValueType.TRUE;
    } else if (type == Kind.VARINT.value) {
      return new ValueType(ByteFormats.readVarint(file));
    } else if (type == Kind.STRING.value) {
      return new ValueType(ByteFormats.readString(file));
    } else {
      throw new ParseException("Unknown ValueType identifier: " + type);
    }
  }

  public static ValueType[] readFrom(ValueType[] values, RandomAccessFile file)
      throws IOException, ParseException {
    for (int i = 0; i < values.length; i += 1) {
      values[i] = readFrom(file);
    }
    return values;
  }
  
  public static int sizeOf(ValueType type) {
    return type.binarySize;
  }
  
  public static int sizeOf(String string) {
    return 1 + ByteFormats.stringBinarySize(string);
  }
  
  public static int sizeOf(boolean bool) {
    return 1;
  }
  
  public static int sizeOf(long number) {
    return 1 + ByteFormats.varintBinarySize(number);
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
    kind = Kind.VARINT;
    binarySize = ByteFormats.varintBinarySize(number) + 1;
  }
  
  private ValueType(String string) {
    stringValue = string;
    kind = Kind.STRING;
    this.binarySize = 1 + ByteFormats.stringBinarySize(string);
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
  
  public void writeTo(RandomAccessFile file) throws IOException, ParseException {
    file.write(kind.value);
    if (kind == Kind.NULL || kind == Kind.FALSE || kind == Kind.TRUE) {
      // noop
    } else if (kind == Kind.STRING) {
      ByteFormats.writeString(stringValue, file);
    } else if (kind == Kind.VARINT) {
      ByteFormats.writeVarint(longValue, file);
    } else {
      throw new RuntimeException(
          "Don't know how to write to file a value of kind " + kind.toString());
    }
  }
}
