package lv.paulsnar.edu.dip107p.md02.db;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

class RecordPage {
  static class Header {
    private static final int OCCUPANCY_PATTERN = 0xAA;
    static final int SIZE = 8;

    Page page;
    boolean isOccupied;
    int recordCount;
    int constantPoolOffset;
    int firstId;
    int nextPage = 0;

    private Header(Page page) {
      this.page = page;
    }

    static Header readFrom(Page page) throws IOException {
      Header header = new Header(page);

      page.seek(0);
      int occupied = page.read();
      if (occupied != OCCUPANCY_PATTERN) {
        header.isOccupied = false;
        return header;
      }

      header.isOccupied = true;
      header.recordCount = page.read();
      header.constantPoolOffset = page.readU15();
      header.nextPage = page.readU31();
      header.firstId = page.readU31();
      page.skip(-BinaryInt.U31_SIZE);

      return header;
    }

    void write() throws IOException {
      page.seek(0);
      if ( ! isOccupied) {
        page.write(0);
      } else {
        page.write(OCCUPANCY_PATTERN);
        page.write(recordCount);
        page.writeU15(constantPoolOffset);
        page.writeU31(nextPage);
      }
    }
  }

  static class StringStub {
    Page page = null;
    int offset = -1;
    private String value = null;

    private StringStub(Page page, int offset) {
      this.page = page;
      this.offset = offset;
    }

    StringStub(String value) {
      this.value = value;
    }

    int length() {
      return BinaryString.byteSize(value);
    }

    void replaceValue(String newValue) {
      offset = -1;
      value = newValue;
    }

    String read() throws MalformedDatabaseException, IOException {
      if (value != null) {
        return value;
      }
      value = BinaryString.readFrom(page);
      return value;
    }

    void writeTo(Page page) throws IOException {
      if (offset == -1) {
        throw new RuntimeException(
            "Cannot write StringStub with unknown offset");
      }
      page.seek(offset);
      BinaryString.writeTo(value, page);
      this.page = page;
    }
  }

  static class BorrowInfo {
    boolean isBorrowed = false;
    StringStub borrowerId = null;
    int returnYear = -1, returnMonth = -1, returnDay = -1;

    BorrowInfo() { }
    BorrowInfo(boolean isBorrowed, StringStub borrowerId, int returnYear,
        int returnMonth, int returnDay) {
      this.isBorrowed = isBorrowed;
      this.borrowerId = borrowerId;
      this.returnYear = returnYear;
      this.returnMonth = returnMonth;
      this.returnDay = returnDay;
    }

    static BorrowInfo readFrom(RecordPage recordPage, Page page)
        throws IOException {
      byte[] data = new byte[4];
      page.readExactly(data);
      BorrowInfo info = new BorrowInfo();
      info.isBorrowed = data[0] < 0;
      int borrowerIdPtr = (data[0] & 0x7F) << 3 | (data[1] & 0xE0) >> 5;
      info.borrowerId = recordPage.getConstant(borrowerIdPtr);
      info.returnYear = (data[1] & 0x1F) << 7 | (data[2] & 0xFE) >> 1;
      info.returnMonth = (data[2] & 0x01) << 3 | (data[3] & 0xE0) >> 5;
      info.returnDay = data[3] & 0x1F;

      info.returnYear += 2000;
      info.returnMonth += 1;
      info.returnDay += 1;

      return info;
    }

    void writeTo(Page page) throws IOException {
      byte[] data = new byte[4];
      if (isBorrowed) {
        data[0] |= 0x80;
        int borrowerIdPtr;
        if (borrowerId == null) {
          borrowerIdPtr = 0;
        } else {
          borrowerIdPtr = borrowerId.offset;
        }
        data[0] |= (borrowerIdPtr & 0x3F8) >> 3;
        data[1] |= (borrowerIdPtr & 0x03) << 5;
        int year = returnYear - 2000;
        int month = returnMonth - 1;
        int day = returnDay - 1;
        data[1] |= (year & 0xF10) >> 8;
        data[2] |= (year & 0x7F) << 1;
        data[2] |= (month & 0x08) >> 1;
        data[3] |= (month & 0x07) << 5;
        data[3] |= (day & 0x1F);
      }
      page.write(data);
    }
  }

  private static final int RECORD_SIZE = 14;

  Header header;
  Set<Record> recordCache;
  private Map<Integer, StringStub> constants;

  RecordPage(Page page)
      throws MalformedDatabaseException, PageBoundaryExceededException,
      IOException {
    header = Header.readFrom(page);
    recordCache = Collections.newSetFromMap(new WeakHashMap<Record, Boolean>());
    constants = new HashMap<>();
  }

  private int occupiedRecordSpace() {
    return RECORD_SIZE * header.recordCount;
  }

  private int occupiedConstantSpace() {
    if (header.constantPoolOffset == 0) {
      return 0;
    }
    return Page.PAGE_SIZE - header.constantPoolOffset;
  }

  private int freeSpace() {
    int space = Page.PAGE_SIZE;
    space -= Header.SIZE;
    space -= occupiedRecordSpace();
    space -= occupiedConstantSpace();
    return space;
  }

  private StringStub getConstant(int offset) {
    if (constants.containsKey(offset)) {
      return constants.get(offset);
    }
    StringStub stub = new StringStub(header.page, offset);
    constants.put(offset, stub);
    return stub;
  }

  private void allocateConstant(StringStub stub)
      throws PageBoundaryExceededException, IOException {
    int length = stub.length();
    if (freeSpace() < length) {
      throw new PageBoundaryExceededException();
    }
    if (header.constantPoolOffset == 0) {
      header.constantPoolOffset = Page.PAGE_SIZE;
    }
    header.constantPoolOffset -= length;
    stub.offset = header.constantPoolOffset;
    stub.writeTo(header.page);
  }

  Record readRecord() throws IOException {
    Page page = header.page;
    long offset = page.offset();
    int id = page.readU31();
    if (id < 1) {
      page.skip(RECORD_SIZE - BinaryInt.U31_SIZE);
      return null;
    }
    StringStub surnameStub = getConstant(page.readU15());
    StringStub nameStub = getConstant(page.readU15());
    StringStub titleStub = getConstant(page.readU15());
    BorrowInfo borrowInfo = BorrowInfo.readFrom(this, page);
    Record record = new Record(
        this, id, surnameStub, nameStub, titleStub, borrowInfo);
    record.offset = (int) offset;
    return record;
  }

  private void writeRecord(Page page, Record record, int offset)
      throws PageBoundaryExceededException, IOException {
    if (record.authorSurnameStub.offset == -1) {
      allocateConstant(record.authorSurnameStub);
    }
    if (record.authorNameStub.offset == -1) {
      allocateConstant(record.authorNameStub);
    }
    if (record.bookTitleStub.offset == -1) {
      allocateConstant(record.bookTitleStub);
    }
    if (record.borrowInfo == null) {
      record.borrowInfo = new BorrowInfo();
    }
    if (record.borrowInfo.borrowerId != null &&
        record.borrowInfo.borrowerId.offset == -1) {
      allocateConstant(record.borrowInfo.borrowerId);
    }

    if (freeSpace() < RECORD_SIZE) {
      throw new PageBoundaryExceededException();
    }

    page.seek(offset);
    page.writeU31(record.id);
    page.writeU15(record.authorSurnameStub.offset);
    page.writeU15(record.authorNameStub.offset);
    page.writeU15(record.bookTitleStub.offset);
    record.borrowInfo.writeTo(page);
  }

  private Record findCachedRecord(int id) {
    Iterator<Record> iter = recordCache.iterator();
    while (iter.hasNext()) {
      Record record = iter.next();
      if (record.id == id) {
        return record;
      }
    }
    return null;
  }

  private Record loadRecord(int id) throws IOException {
    Page page = header.page;
    page.seek(Header.SIZE);
    for (int i = 0; i < header.recordCount; i += 1) {
      int tupleId = page.readU31();
      if (tupleId == id) {
        page.skip(-BinaryInt.U31_SIZE);
        Record record = readRecord();
        recordCache.add(record);
        return record;
      }
    }
    return null;
  }

  Record getRecord(int id) throws IOException {
    Record record = null;
    record = findCachedRecord(id);
    if (record == null) {
      record = loadRecord(id);
    }
    return record;
  }

  void appendRecord(Record record)
      throws PageBoundaryExceededException, IOException {
    Page page = header.page;
    int offset = Header.SIZE + RECORD_SIZE * header.recordCount;
    writeRecord(page, record, offset);
    header.isOccupied = true;
    header.recordCount += 1;
    record.offset = offset;
    header.write();
  }

  void updateRecord(Record record) throws IOException {
    if (record.offset == -1) {
      throw new RuntimeException("Record has no known location on page");
    }

    writeRecord(header.page, record, record.offset);
    header.write();
  }
}
