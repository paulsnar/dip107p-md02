package lv.paulsnar.edu.dip107p.md02.db;

import java.io.IOException;
import java.util.Calendar;
import lv.paulsnar.edu.dip107p.md02.Book;

final class BookPersister implements Persister<Book> {
  @Override
  public Book readFrom(Reader source) throws IOException {
    Book book = new Book();
    book.id = source.readUTF();
    book.author.surname = source.readUTF();
    book.author.name = source.readUTF();
    book.title = source.readUTF();
    boolean hasCheckoutInfo = source.readBoolean();
    if (hasCheckoutInfo) {
      book.checkoutInfo = new Book.CheckoutInfo();
      book.checkoutInfo.holderId = source.readUTF();

      Calendar returnDate = Calendar.getInstance();
      returnDate.set(Calendar.YEAR, source.readShort());
      returnDate.set(Calendar.DAY_OF_YEAR, source.readShort());
      book.checkoutInfo.returnDate = returnDate;
    } else {
      book.checkoutInfo = null;
    }
    return book;
  }

  @Override
  public void writeTo(Writer target, Book book) throws IOException {
    target.writeUTF(book.id);
    target.writeUTF(book.author.surname);
    target.writeUTF(book.author.name);
    target.writeUTF(book.title);
    if (book.checkoutInfo == null) {
      target.writeBoolean(false);
    } else {
      target.writeBoolean(true);
      target.writeUTF(book.checkoutInfo.holderId);
      target.writeShort(book.checkoutInfo.returnDate.get(Calendar.YEAR));
      target.writeShort(book.checkoutInfo.returnDate.get(Calendar.DAY_OF_YEAR));
    }
  }
}
