package lv.paulsnar.edu.dip107p.md02.ui;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;

import lv.paulsnar.edu.dip107p.md02.Book;

final class DeleteState implements StateExecutor {
  private LinkedList<Book> deleteList = new LinkedList<>();
  private String[] ids = null;

  DeleteState() {
  }
  DeleteState(String[] ids) {
    this.ids = ids;
  }

  @Override
  public void run(State state, Scanner sc) {
    if (ids != null) {
      for (int i = 0; i < ids.length; i += 1) {
        String id = ids[i];
        Book book = state.db.get(id);
        if (book == null) {
          System.out.printf("-- Grāmata ar numuru %s nav atrasta.\n", id);
          continue;
        }
        deleteList.add(book);
      }
    } else {
      System.out.println("-- Norādiet grāmatu numurus, kurus vēlaties dzēst.");
      System.out.println("-- Kad numuru ievade ir pabeigta, nospiediet Enter, "
        + "kamēr lauks ir tukšs.");

      for (;;) {
        System.out.print("Numurs: ");

        if ( ! sc.hasNextLine()) {
          System.out.println();
          return;
        }

        String id = sc.nextLine();
        if (id.length() == 0) {
          break;
        }

        Book book = state.db.get(id);
        if (book == null) {
          System.out.printf("-- Grāmata ar numuru %s nav atrasta.\n", id);
          continue;
        }

        deleteList.add(book);
        System.out.printf(
          "-- Grāmata %s '%s' (%s, %s) pievienota dzēšanas rindai.\n",
          book.id, book.title, book.author.surname, book.author.name);
      }
    }

    int count = deleteList.size();
    if (count == 0) {
      return;
    }
    System.out.println("-- Uzmanību! Šī darbība ir neatgriezeniska.");
    for (;;) {
      System.out.printf("Vai tiešām vēlaties dzēst %d grāmat%s? [jā/nē] ",
        count, (count % 10 == 1 && count % 100 != 11 ? "u" : "as"));
      if ( ! sc.hasNextLine()) {
        System.out.println();
        return;
      }

      String confirmation = sc.nextLine().toLowerCase();
      if (confirmation.equals("jā")) {
        break;
      } else if (confirmation.equals("nē") || confirmation.equals("ne") ||
          confirmation.equals("n")) {
        return;
      }
      System.out.println("Lūdzu ievadiet 'jā' vai 'nē'.");
    }

    Iterator<Book> iterator = deleteList.iterator();
    while (iterator.hasNext()) {
      state.db.remove(iterator.next().id);
    }
    System.out.println("-- Grāmatas dzēstas.");
  }
}
