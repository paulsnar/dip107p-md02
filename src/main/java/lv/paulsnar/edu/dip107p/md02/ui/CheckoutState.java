package lv.paulsnar.edu.dip107p.md02.ui;

import java.util.Calendar;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lv.paulsnar.edu.dip107p.md02.Book;
import lv.paulsnar.edu.dip107p.md02.DateFormatException;

final class CheckoutState implements StateExecutor {
  private static final Pattern ISO8601_PATTERN =
    Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})");
  private static enum Mode { IN, OUT }

  private Mode mode = null;
  private String holderId = null;
  private Calendar returnDate = null;
  private String[] ids = null;

  public CheckoutState() {
  }
  public CheckoutState(String[] checkingInBooks) {
    mode = Mode.IN;
    ids = checkingInBooks;
  }
  public CheckoutState(String holderId, String returnDate,
      String[] checkingOutBooks)
      throws DateFormatException, NumberFormatException{
    mode = Mode.OUT;
    ids = checkingOutBooks;
    this.holderId = holderId;
    this.returnDate = parseIso8601(returnDate);
  }

  private static Calendar parseIso8601(String date)
      throws DateFormatException, NumberFormatException {
    Matcher iso8601Matcher = ISO8601_PATTERN.matcher(date);
    if ( ! iso8601Matcher.matches()) {
      throw new DateFormatException(date);
    }
    String
      year = iso8601Matcher.group(1),
      month = iso8601Matcher.group(2),
      day = iso8601Matcher.group(3);

    Calendar calendar = Calendar.getInstance();
    calendar.set(Integer.parseInt(year), Integer.parseInt(month),
      Integer.parseInt(day));
    return calendar;
  }

  @Override
  public void run(State state, Scanner sc) {
    while (mode == null) {
      System.out.print("Norādiet veicamo operāciju: "
        + "\n  [i] atzīmēt grāmatas kā atgrieztas plauktā"
        + "\n  [o] atzīmēt grāmatas kā izņemtas"
        + "\n>> ");
      if ( ! sc.hasNextLine()) {
        return;
      }
      String line = sc.nextLine();
      if (line.length() == 0) {
        return;
      }
      char mode = line.charAt(0);
      if (mode == 'o') {
        this.mode = Mode.OUT;
      } else if (mode == 'i') {
        this.mode = Mode.IN;
      } else {
        System.out.println("-- Nederīgs režīms: " + mode);
      }
    }

    if (mode == Mode.OUT) {
      while (holderId == null) {
        System.out.print("Izņēmēja lasītāja numurs: ");
        if ( ! sc.hasNextLine()) {
          return;
        }
        String holder = sc.nextLine();
        if (holder.length() == 0) {
          System.out.println("-- Lūdzu, norādiet lasītāja numuru.");
          continue;
        }
        holderId = holder;
      }

      while (returnDate == null) {
        System.out.print("Atgriešanas datums (YYYY-MM-DD): ");
        if ( ! sc.hasNextLine()) {
          return;
        }
        String date = sc.nextLine();
        try {
          returnDate = parseIso8601(date);
        } catch (DateFormatException exc) {
          System.out.println("-- Lūdzu, norādiet derīgu atgriešanas datumu.");
          continue;
        }
      }
    }

    if (ids == null) {
      for (;;) {
        System.out.print("Grāmatas numurs: ");
        if ( ! sc.hasNextLine()) {
          return;
        }
        String id = sc.nextLine();
        if (id.length() == 0) {
          break;
        }
        Book book = state.db.get(id);
        if (book == null) {
          System.out.printf("-- Grāmata ar numuru %s nav atrasta.%n", id);
          continue;
        }

        switch (mode) {
          case IN: {
            book.checkoutInfo = null;
            break;
          }
          case OUT: {
            book.checkoutInfo = new Book.CheckoutInfo(holderId, returnDate);
            break;
          }
        }
        System.out.printf(
          "-- Grāmata %s '%s' (%s, %s) atzīmēta kā %s.%n",
          book.id, book.title, book.author.surname, book.author.name,
          mode == Mode.IN ? "atgriezta" : mode == Mode.OUT ? "izņemta" : "?");
      }
    } else {
      for (int i = 0; i < ids.length; i += 1) {
        String id = ids[i];
        Book book = state.db.get(id);
        if (book == null) {
          System.out.printf("-- Grāmata ar numuru %s nav atrasta.%n", id);
          continue;
        }

        switch (mode) {
          case IN: {
            book.checkoutInfo = null;
            break;
          }
          case OUT: {
            book.checkoutInfo = new Book.CheckoutInfo(holderId, returnDate);
            break;
          }
        }
        System.out.printf(
          "-- Grāmata %s '%s' (%s, %s) atzīmēta kā %s.%n",
          book.id, book.title, book.author.surname, book.author.name,
          mode == Mode.IN ? "atgriezta" : mode == Mode.OUT ? "izņemta" : "?");
      }
    }
  }
}
