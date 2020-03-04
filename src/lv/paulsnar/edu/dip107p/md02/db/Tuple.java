package lv.paulsnar.edu.dip107p.md02.db;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;

import lv.paulsnar.edu.dip107p.md02.util.ByteFormats;
import lv.paulsnar.edu.dip107p.md02.util.ParseException;

public class Tuple {
  public static Tuple readFrom(RandomAccessFile file) throws IOException, ParseException {
    try {
      int size = (int) ByteFormats.readVarint(file);
      int id = (int) ByteFormats.readVarint(file);
      ValueType[] values = new ValueType[8];
      ValueType.readFrom(values, file);
      int trailer = file.read();
      if (trailer != 0) {
        throw new IOException("Malformed database file: expected null byte, got: " + trailer);
      }
      return new Tuple(id, values);
    } catch (EOFException exc) {
      return null;
    }
  }
  
  public int id;
  public String authorSurname = null, authorName = null, bookTitle = null;
  public boolean isInShelf = false;
  public String holderId = null;
  public int returnYear = 0, returnMonth = 0, returnDay = 0;
  
  private Tuple(int id, ValueType[] tuple) {
    this.id = id;
    try {
      authorSurname = tuple[0].stringValue();
      authorName = tuple[1].stringValue();
      bookTitle = tuple[2].stringValue();
      isInShelf = tuple[3].booleanValue();
      if ( ! isInShelf) {
        holderId = tuple[4].stringValue();
        returnYear = (int) tuple[5].longValue();
        returnMonth = (int) tuple[6].longValue();
        returnDay = (int) tuple[7].longValue();
      } else {
        assert tuple[4].isNull();
        assert tuple[5].isNull();
        assert tuple[6].isNull();
        assert tuple[7].isNull();
        holderId = null;
        returnYear = returnMonth = returnDay = 0;
      }
    } catch (BadValueException exc) {
      throw new RuntimeException("Malformed tuple", exc);
    }
  }
  
  public static final Tuple SCRUBBED = new Tuple(-1, null, null, null);
  
  public Tuple(int id) {
    this.id = id;
  }

  public Tuple(int id, String authorSurname, String authorName, String bookTitle) {
    this.id = id;
    this.authorSurname = authorSurname;
    this.authorName = authorName;
    this.bookTitle = bookTitle;
    isInShelf = true;
  }
  
  public Tuple(int id, String authorSurname, String authorName, String bookTitle, String holderId,
        int returnYear, int returnMonth, int returnDay) {
    this(id, authorSurname, authorName, bookTitle);
    isInShelf = false;
    this.holderId = holderId;
    this.returnYear = returnYear;
    this.returnMonth = returnMonth;
    this.returnDay = returnDay;
  }

  int size() {
    int size = ByteFormats.varintBinarySize((long) id) +
        ValueType.sizeOf(authorSurname) +
        ValueType.sizeOf(authorName) +
        ValueType.sizeOf(bookTitle) +
        ValueType.sizeOf(isInShelf);
    if (isInShelf) {
      size += 4 * ValueType.sizeOf(ValueType.NULL); 
    } else {
      size += ValueType.sizeOf(holderId) +
          ValueType.sizeOf((long) returnYear) +
          ValueType.sizeOf((long) returnMonth) +
          ValueType.sizeOf((long) returnDay);
    }
    return size;
  }
  
  int length() {
    int size = size();
    return size + ValueType.sizeOf((long) size);
  }
  
  public void writeTo(RandomAccessFile file) throws IOException, ParseException {
    ByteFormats.writeVarint((long) size(), file);
    ByteFormats.writeVarint(id, file);
    ValueType.writeTo(authorSurname, file);
    ValueType.writeTo(authorName, file);
    ValueType.writeTo(bookTitle, file);
    ValueType.writeTo(isInShelf, file);
    if (isInShelf) {
      for (int i = 0; i < 4; i += 1) {
        ValueType.NULL.writeTo(file);
      }
    } else {
      ValueType.writeTo(holderId, file);
      ValueType.writeTo(returnYear, file);
      ValueType.writeTo(returnMonth, file);
      ValueType.writeTo(returnDay, file);
    }
    file.write(0);
  }
}
