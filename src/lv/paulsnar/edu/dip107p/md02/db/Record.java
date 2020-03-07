package lv.paulsnar.edu.dip107p.md02.db;

import java.io.IOException;
import java.lang.ref.WeakReference;

import lv.paulsnar.edu.dip107p.md02.db.RecordPage.BorrowInfo;
import lv.paulsnar.edu.dip107p.md02.db.RecordPage.StringStub;

public class Record {
  int id = -1;
  int offset = -1;
  private WeakReference<RecordPage> parentPage = null;
  private String authorSurname = null;
  private String authorName = null;
  private String bookTitle = null;

  StringStub authorSurnameStub = null;
  StringStub authorNameStub = null;
  StringStub bookTitleStub = null;
  BorrowInfo borrowInfo = null;

  Record(RecordPage parentPage, int id, StringStub authorSurnameStub,
      StringStub authorNameStub, StringStub bookTitleStub,
      BorrowInfo borrowInfo) {
    this.parentPage = new WeakReference<>(parentPage);
    this.id = id;
    this.authorSurnameStub = authorSurnameStub;
    this.authorNameStub = authorNameStub;
    this.bookTitleStub = bookTitleStub;
    this.borrowInfo = borrowInfo;
  }

  public Record(String authorSurname, String authorName, String bookTitle) {
    this.authorSurname = authorSurname;
    this.authorName = authorName;
    this.bookTitle = bookTitle;
    authorSurnameStub = new StringStub(authorSurname);
    authorNameStub = new StringStub(authorName);
    bookTitleStub = new StringStub(bookTitle);
  }

  public Record(String authorSurname, String authorName, String bookTitle,
      String borrowerId, int returnYear, int returnMonth, int returnDay) {
    this(authorSurname, authorName, bookTitle);
    borrowInfo = new BorrowInfo(true, new StringStub(borrowerId),
        returnYear, returnMonth, returnDay);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + id;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Record other = (Record) obj;
    if (id != other.id)
      return false;
    return true;
  }

  public void persist() throws IOException {
    RecordPage page = parentPage.get();
    if (page == null) {
      throw new RuntimeException(
          "Lost track of parent page, or none was specified.");
    }
    page.updateRecord(this);
  }
  public void persist(RecordPageFile file) throws IOException {
    try {
      persist();
    } catch (RuntimeException exc) {
      file.appendRecord(this);
    }
  }
  void persist(RecordPage page) throws IOException {
    page.appendRecord(this);
    if (parentPage == null) {
      parentPage = new WeakReference<>(page);
    }
  }

  public String authorSurname() throws IOException {
    if (authorSurname != null) {
      return authorSurname;
    }
    return authorSurname = authorSurnameStub.read();
  }
  public void authorSurname(String value) {
    authorSurname = value;
  }

  public String authorName() throws IOException {
    if (authorName != null) {
      return authorName;
    }
    return authorName = authorNameStub.read();
  }
  public void authorName(String value) {
    authorName = value;
  }

  public String bookTitle() throws IOException {
    if (bookTitle != null) {
      return bookTitle;
    }
    return bookTitle = bookTitleStub.read();
  }
  public void bookTitle(String value) {
    bookTitle = value;
  }

  public boolean isBorrowed() {
    if (borrowInfo == null) {
      return false;
    }
    return borrowInfo.isBorrowed;
  }
  public void isBorrowed(boolean value) {
    if (borrowInfo == null) {
      borrowInfo = new BorrowInfo();
    }
    borrowInfo.isBorrowed = value;
  }

  public String borrowerId() throws IOException {
    if (borrowInfo == null) {
      return null;
    }
    return borrowInfo.borrowerId.read();
  }
  public void borrowerId(String value) {
    if (borrowInfo == null) {
      borrowInfo = new BorrowInfo();
    }
    borrowInfo.borrowerId = new StringStub(value);
  }

  public int returnYear() {
    if (borrowInfo == null) {
      return -1;
    }
    return borrowInfo.returnYear;
  }
  public void returnYear(int value) {
    if (borrowInfo == null) {
      borrowInfo = new BorrowInfo();
    }
    borrowInfo.returnYear = value;
  }

  public int returnMonth() {
    if (borrowInfo == null) {
      return -1;
    }
    return borrowInfo.returnMonth;
  }
  public void returnMonth(int value) {
    if (borrowInfo == null) {
      borrowInfo = new BorrowInfo();
    }
    borrowInfo.returnMonth = value;
  }

  public int returnDay() {
    if (borrowInfo == null) {
      return -1;
    }
    return borrowInfo.returnDay;
  }
  public void returnDay(int value) {
    if (borrowInfo == null) {
      borrowInfo = new BorrowInfo();
    }
    borrowInfo.returnDay = value;
  }
}
