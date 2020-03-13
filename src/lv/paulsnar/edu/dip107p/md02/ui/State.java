package lv.paulsnar.edu.dip107p.md02.ui;

import lv.paulsnar.edu.dip107p.md02.Book;
import lv.paulsnar.edu.dip107p.md02.BookDatabase;

class State {
  static enum Type { VOID, INIT, MENU, ADD, EDIT, DONE }

  public Type type = Type.VOID;
  public Book currentBook = null;
  public ListingPrinter currentListing = null;
  public BookDatabase db;

  public boolean isDone() {
    return type == Type.DONE;
  }
}
