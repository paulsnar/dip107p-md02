package lv.paulsnar.edu.dip107p.md02.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Scanner;

import lv.paulsnar.edu.dip107p.md02.Book;

final class SearchState implements StateExecutor {
  static enum Criterion { AUTHOR, TITLE }

  private Criterion criterion = null;
  private String value = null;
  public List<Book> results = null;

  public SearchState() {
  }
  public SearchState(Criterion criterion, String value) {
    this.criterion = criterion;
    this.value = value;
  }

  @Override
  public void run(State state, Scanner sc) {
    while (criterion == null) {
      System.out.print("Meklēšanas kritērijs: [a] autors  [n] nosaukums >> ");
      if ( ! sc.hasNextLine()) {
        System.out.println();
        return;
      }
      String criterionType = sc.nextLine();
      if (criterionType.length() == 0) {
        return;
      }

      char crit = criterionType.charAt(0);
      if (crit == 'a') {
        criterion = Criterion.AUTHOR;
      } else if (crit == 'n') {
        criterion = Criterion.TITLE;
      } else {
        System.out.printf("-- Kritērijs %c nav atpazīts.%n", crit);
      }
    }

    while (value == null) {
      System.out.print("Meklētā vērtība: ");
      if ( ! sc.hasNextLine()) {
        System.out.println();
        return;
      }

      value = sc.nextLine();
    }
    value = value.toLowerCase();

    List<Book> books = new LinkedList<>();
    Iterator<Book> iterator = state.db.getAll().iterator();
    while (iterator.hasNext()) {
      Book book = iterator.next();
      if (matches(book)) {
        books.add(book);
      }
    }

    results = new ArrayList<>(books);
    int count = results.size();
    System.out.printf("-- Atrast%c %d rezultāt%1$c:%n",
      count % 10 == 1 && count % 100 != 11 ? 's' : 'i', count);
  }

  private boolean matches(Book book) {
    switch (criterion) {
    case AUTHOR:
      return book.author.toString().toLowerCase().indexOf(value) != -1;
    case TITLE:
      return book.title.toLowerCase().indexOf(value) != -1;
    default:
      throw new RuntimeException("Cannot match null criterion");
    }
  }
}
