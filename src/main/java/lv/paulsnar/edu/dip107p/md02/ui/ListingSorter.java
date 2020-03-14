package lv.paulsnar.edu.dip107p.md02.ui;

import java.util.Comparator;
import java.util.List;

import lv.paulsnar.edu.dip107p.md02.Book;

final class ListingSorter {
  static enum SortCriterion { ID, AUTHOR, TITLE }
  static enum SortDirection {
    ASC(1), DESC(-1);
    private int value;
    private SortDirection(int value) {
      this.value = value;
    }
  }

  private ListingSorter() { }

  private static class BookComparator implements Comparator<Book> {
    private SortCriterion criterion;
    private SortDirection direction;

    private BookComparator(SortCriterion criterion, SortDirection direction) {
      this.criterion = criterion;
      this.direction = direction;
    }

    @Override
    public int compare(Book o1, Book o2) {
      switch (criterion) {
      case ID:
        return o1.id.compareTo(o2.id) * direction.value;
      case TITLE:
        return o1.title.compareTo(o2.title) * direction.value;
      case AUTHOR: {
        int result = o1.author.surname.compareTo(o2.author.surname);
        if (result == 0) {
          result = o1.author.name.compareTo(o2.author.name);
        }
        if (result == 0) {
          result = o1.title.compareTo(o2.title);
        }
        return result * direction.value;
      }
      default:
        throw new RuntimeException("Unknown comparison criterion");
      }
    }
  }

  static void sortByDefault(List<Book> books) {
    sortBy(books, SortCriterion.ID, SortDirection.ASC);
  }

  static void sortBy(List<Book> books, SortCriterion criterion) {
    sortBy(books, criterion, SortDirection.ASC);
  }

  static void sortBy(List<Book> books, SortCriterion criterion,
      SortDirection direction) {
    books.sort(new BookComparator(criterion, direction));
  }
}
