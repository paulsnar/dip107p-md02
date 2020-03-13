package lv.paulsnar.edu.dip107p.md02.ui;

import java.util.List;

import lv.paulsnar.edu.dip107p.md02.Book;

final class ListingPrinter {
  private static final int PAGE_SIZE = 20;
//  private static final int MAX_ENTRY_WIDTH = 80;

  private List<Book> items;
  private int offset = 0;

  public ListingPrinter(List<Book> items) {
    this.items = items;
  }

  public boolean hasMore() {
    return offset < this.items.size();
  }

  private static final String
    ROW_METAFORMAT = " %%-%ds | %%-%ds | %%-%ds | %%-%ds | %%-%ds ",
    SEPARATOR_METAFORMAT = "%%%ds+%%%ds+%%%ds+%%%ds+%%%ds",
    HEADER_ID = "Nr.",
    HEADER_AUTHOR = "Autors",
    HEADER_TITLE = "Nosaukums",
    HEADER_HOLDER_ID = "Turētāja nr.",
    HEADER_RETURN_DATE = "Atgriešanas datums",
    MESSAGE_EMPTY = "(nav ierakstu)";
  private static String repeat(String term, int amount) {
    StringBuilder result = new StringBuilder(term.length() * amount);
    while (amount > 0) {
      result.append(term);
      amount -= 1;
    }
    return result.toString();
  }

  public void print() {
    int[] columnWidths = {
        HEADER_ID.length(),
        HEADER_AUTHOR.length(),
        HEADER_TITLE.length(),
        HEADER_HOLDER_ID.length(),
        HEADER_RETURN_DATE.length(),
    };

    for (int i = offset; i < offset + PAGE_SIZE && i < items.size(); i += 1) {
      Book book = items.get(i);

      columnWidths[0] = Integer.max(columnWidths[0], book.id.length());
      columnWidths[1] = Integer.max(
          columnWidths[1], book.author.toString().length());
      columnWidths[2] = Integer.max(columnWidths[2], book.title.length());
      columnWidths[3] = Integer.max(columnWidths[3],
          book.checkoutInfo == null ? 1 : book.checkoutInfo.holderId.length());
      columnWidths[4] = Integer.max(columnWidths[4],
          book.checkoutInfo == null ? 1 :
            book.checkoutInfo.returnDateIso8601().length());
    }

    String rowFormat = String.format(ROW_METAFORMAT,columnWidths[0],
        columnWidths[1], columnWidths[2], columnWidths[3], columnWidths[4]);
    String header = String.format(rowFormat, HEADER_ID, HEADER_AUTHOR,
        HEADER_TITLE, HEADER_HOLDER_ID, HEADER_RETURN_DATE);
    String separator = String.format(
      String.format(SEPARATOR_METAFORMAT, columnWidths[0] + 2,
        columnWidths[1] + 2, columnWidths[2] + 2, columnWidths[3] + 2,
        columnWidths[4] + 2),
      repeat("-", columnWidths[0] + 2), repeat("-", columnWidths[1] + 2),
        repeat("-", columnWidths[2] + 2), repeat("-", columnWidths[3] + 2),
        repeat("-", columnWidths[4] + 2));

    System.out.println(header);
    System.out.println(separator);

    if (items.size() == 0) {
      int width = columnWidths[0] + 2 + columnWidths[1] + 2 +
          columnWidths[2] + 2 + columnWidths[3] + 2 + columnWidths[4] + 2 +
          columnWidths.length - 1;
      int offset = width / 2;
      offset += MESSAGE_EMPTY.length() / 2;
      System.out.printf("%" + offset + "s%n", MESSAGE_EMPTY);
    }

    for (int i = offset; i < offset + PAGE_SIZE && i < items.size(); i += 1) {
      Book book = items.get(i);
      System.out.println(
        String.format(rowFormat, book.id, book.author.toString(), book.title,
          book.checkoutInfo == null ? "-" : book.checkoutInfo.holderId,
          book.checkoutInfo == null ? "-" :
            book.checkoutInfo.returnDateIso8601()));
    }

    offset += PAGE_SIZE;
  }
}
