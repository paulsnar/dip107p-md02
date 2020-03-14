package lv.paulsnar.edu.dip107p.md02;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

final public class Book {
  public static class Author {
    public String surname, name;
    public Author() {
      this.surname = null;
      this.name = null;
    }
    public Author(String surname, String name) {
      this.surname = surname;
      this.name = name;
    }
    @Override
    public String toString() {
      return String.format("%s, %s", surname, name);
    }
  }
  public static class CheckoutInfo {
    static final DateFormat ISO8601 = new SimpleDateFormat("yyyy-LL-dd");
    public String holderId;
    public Calendar returnDate;
    public CheckoutInfo() {
      holderId = null;
      returnDate = null;
    }
    public CheckoutInfo(String holderId, Calendar returnDate) {
      this.holderId = holderId;
      this.returnDate = returnDate;
    }
    public String returnDateIso8601() {
      return ISO8601.format(returnDate.getTime());
    }
  }
  public String id;
  public Author author;
  public String title;
  public CheckoutInfo checkoutInfo;

  public Book() {
    this.author = new Author();
    this.title = null;
    this.checkoutInfo = null;
  }
  public Book(String id, String authorSurname, String authorName, String title) {
    this.id = id;
    this.author = new Author(authorSurname, authorName);
    this.title = title;
    this.checkoutInfo = null;
  }

  @Override
  public boolean equals(Object obj) {
    if ( ! (obj instanceof Book)) {
      return false;
    }
    return id.equals(((Book)obj).id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
