package lv.paulsnar.edu.dip107p.md02.ui;

import lv.paulsnar.edu.dip107p.md02.Book;
import lv.paulsnar.edu.dip107p.md02.BookDatabase;

class State {
  public boolean isInitialized = false, isDone = false;
  public ListingPrinter currentListing = null;
  public BookDatabase db;
}
