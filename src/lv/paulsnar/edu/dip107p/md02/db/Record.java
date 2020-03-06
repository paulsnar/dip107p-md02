package lv.paulsnar.edu.dip107p.md02.db;

import java.io.IOException;

public class Record {
  public static class Header {
    static final int FLAG_IS_SCRUBBED = 1 << 0;
    static final int BINARY_SIZE = 8;

    public int length;
    public boolean isScrubbed;
    public int id;

    public static Header readFrom(Page page)
        throws PageBoundaryExceededException, IOException {
      int length = page.readU15();
      int reservedAndFlags = page.readU15();
      int flags = reservedAndFlags & 0xFF;
      int id = page.readU31();

      return new Header(length, flags, id);
    }

    Header(int length, int flags, int id) {
      this.length = length;
      this.id = id;

      this.isScrubbed = (flags & FLAG_IS_SCRUBBED) != 0;
    }

    public void writeTo(Page page) throws IOException {
      page.writeU15(length);
      page.write(0);
      int flags = 0;
      if (isScrubbed) {
        flags |= FLAG_IS_SCRUBBED;
      }
      page.write(flags);
      page.writeU31(id);
    }
  }

  public static Record readFrom(Page page)
      throws PageBoundaryExceededException, IOException {
    Header header = Header.readFrom(page);
    return readFrom(page, header);
  }

  public static Record readFrom(Page page, Header header)
      throws PageBoundaryExceededException, IOException {
    if (header.isScrubbed) {
      page.skip(header.length);
      return null;
    }

    try {
      String authorSurname = ValueType.readFrom(page).stringValue();
      String authorName = ValueType.readFrom(page).stringValue();
      String bookTitle = ValueType.readFrom(page).stringValue();
      boolean isInShelf = ValueType.readFrom(page).booleanValue();
      Record record;
      if ( ! isInShelf) {
        String holderId = ValueType.readFrom(page).stringValue();
        int returnYear = ValueType.readFrom(page).intValue();
        int returnMonth = ValueType.readFrom(page).intValue();
        int returnDay = ValueType.readFrom(page).intValue();
        record = new Record(authorSurname, authorName, bookTitle,
          holderId, returnYear, returnMonth, returnDay);
      } else {
        record = new Record(authorSurname, authorName, bookTitle);
      }
      record.header = header;
      return record;
    } catch (BadValueException exc) {
      throw new MalformedDatabaseException(
        "Invalid record content type", page.position(), exc);
    }
  }

  public Header header = null;
  public String authorSurname = null, authorName = null, bookTitle = null;
  public boolean isInShelf = true;
  public String holderId = null;
  public int returnYear = -1, returnMonth = -1, returnDay = -1;

  public Record(String authorSurname, String authorName, String bookTitle) {
    this.authorSurname = authorSurname;
    this.authorName = authorName;
    this.bookTitle = bookTitle;
    isInShelf = true;
  }

  public Record(String authorSurname, String authorName, String bookTitle,
      String holderId, int returnYear, int returnMonth, int returnDay) {
    this(authorSurname, authorName, bookTitle);
    isInShelf = false;
    this.holderId = holderId;
    this.returnYear = returnYear;
    this.returnMonth = returnMonth;
    this.returnDay = returnDay;
  }

  public int binarySize() {
    int size = Header.BINARY_SIZE + ValueType.sizeOf(authorSurname) +
      ValueType.sizeOf(authorName) + ValueType.sizeOf(bookTitle) +
      ValueType.sizeOf(isInShelf);
    if ( ! isInShelf) {
      size += ValueType.sizeOf(holderId) +
        ValueType.sizeOf(returnYear) + ValueType.sizeOf(returnMonth) +
        ValueType.sizeOf(returnDay);
    }
    return size;
  }

  public void writeTo(Page page)
      throws ValueOutOfBoundsException, IOException {
    if (header == null) {
      throw new IllegalStateException("Cannot write out a headerless record, " +
        "please synthesize a header first");
    }

    header.length = this.binarySize();

    header.writeTo(page);
    ValueType.writeTo(authorSurname, page);
    ValueType.writeTo(authorName, page);
    ValueType.writeTo(bookTitle, page);
    ValueType.writeTo(isInShelf, page);
    if ( ! isInShelf) {
      ValueType.writeTo(holderId, page);
      ValueType.writeTo(returnYear, page);
      ValueType.writeTo(returnMonth, page);
      ValueType.writeTo(returnDay, page);
    }
  }
}
