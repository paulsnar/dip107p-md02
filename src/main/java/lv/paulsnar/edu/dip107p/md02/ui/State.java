package lv.paulsnar.edu.dip107p.md02.ui;

import lv.paulsnar.edu.dip107p.md02.db.Database;

class State {
  public boolean isInitialized = false, isDone = false;
  public ListingPrinter currentListing = null;
  public Database db;
}
