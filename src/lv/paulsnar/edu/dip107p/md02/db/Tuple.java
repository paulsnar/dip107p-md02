package lv.paulsnar.edu.dip107p.md02.db;

import java.io.IOException;
import java.io.RandomAccessFile;

import lv.paulsnar.edu.dip107p.md02.util.ByteFormats;
import lv.paulsnar.edu.dip107p.md02.util.ParseException;

public class Tuple {
  public static Tuple readFrom(RandomAccessFile file) throws IOException, ParseException {
    long offset = file.getFilePointer();
    int size = ByteFormats.readU15(file);
    ValueType[] values = ValueType.readFrom(file, size);
    return new Tuple(offset, values);
  }
  
  long offset;
  public String authorSurname, authorName, bookTitle;
  public boolean isInShelf;
  public String holderId;
  public int returnYear, returnMonth, returnDay;
  
  private Tuple(long offset, ValueType[] tuple) {
    this.offset = offset;
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
  
  Tuple(long offset, String authorSurname, String authorName, String bookTitle) {
    this.offset = offset;
    this.authorSurname = authorSurname;
    this.authorName = authorName;
    this.bookTitle = bookTitle;
    isInShelf = true;
    holderId = null;
    returnYear = returnMonth = returnDay = 0;
  }
  
  Tuple(long offset, String authorSurname, String authorName, String bookTitle, String holderId,
        int returnYear, int returnMonth, int returnDay) {
    this(offset, authorSurname, authorName, bookTitle);
    isInShelf = false;
    this.holderId = holderId;
    this.returnYear = returnYear;
    this.returnMonth = returnMonth;
    this.returnDay = returnDay;
  }

  public void writeTo(RandomAccessFile file) throws IOException, ParseException {
    int size = ValueType.sizeOf(authorSurname) + ValueType.sizeOf(authorName) +
        ValueType.sizeOf(bookTitle) + ValueType.sizeOf(isInShelf);
    if (isInShelf) {
      size += 4 * ValueType.sizeOf(ValueType.NULL); 
    } else {
      size += ValueType.sizeOf(holderId) + ValueType.sizeOf((long) returnYear) +
          ValueType.sizeOf((long) returnMonth) + ValueType.sizeOf((long) returnDay);
    }

    ByteFormats.writeU15(size, file);
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
  }
}
