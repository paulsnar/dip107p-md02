package lv.paulsnar.edu.dip107p.md02.ui;

import java.util.Scanner;

import lv.paulsnar.edu.dip107p.md02.Book;

final class AddState implements StateExecutor {
  private static class InterruptedException extends RuntimeException {
    private static final long serialVersionUID = 6538650527376634342L;
  }

  private Book newBook = new Book();
  private boolean infinite = true;

  AddState() {
  }
  AddState(String bookId) {
    newBook.id = bookId;
    infinite = false;
  }

  @Override
  public void run(State state, Scanner sc) {
    System.out.println(
        "-- Atstājiet jebkuru lauku tukšu, lai atceltu/pārtrauktu.");

    Book idConflict = null;
    if (newBook.id != null && (idConflict = state.db.get(newBook.id)) != null) {
      System.out.printf(
            "-- Brīdinājums: Grāmata ar šādu numuru jau ir reģistrēta."
        + "%n   %s (%s, %s)"
        + "%n   Lai izmantotu šo pašu numuru (%s), otra grāmata papriekš ir"
        + "%n   jāizdzēš. Citādi lūdzam izmantot citu numuru šai grāmatai."
        + "%n",
        idConflict.title, idConflict.author.surname, idConflict.author.name,
        newBook.id);
      newBook.id = null;
    }

    do {
      System.out.println();
      try {
        while (newBook.id == null) {
          System.out.print("Grāmatas numurs: ");
          newBook.id = readLine(sc);
          if ((idConflict = state.db.get(newBook.id)) != null) {
            System.out.printf("-- Šāds numurs jau ir reģistrēts: %s (%s, %s)%n",
              idConflict.title, idConflict.author.surname,
              idConflict.author.name);
            newBook.id = null;
          }
        }
        System.out.print("Autora uzvārds: ");
        newBook.author.surname = readLine(sc);
        System.out.print("Autora vārds: ");
        newBook.author.name = readLine(sc);
        System.out.print("Grāmatas nosaukums: ");
        newBook.title = readLine(sc);
        state.db.add(newBook);
        System.out.println("-- Grāmata pievienota!");
        newBook = new Book();
      } catch (InterruptedException exc) {
        break;
      }
    } while (infinite);
  }

  private static String readLine(Scanner sc) throws InterruptedException {
    if ( ! sc.hasNextLine()) {
      System.out.println();
      throw new InterruptedException();
    }
    String line = sc.nextLine();
    if (line.length() == 0) {
      throw new InterruptedException();
    }
    return line;
  }
}
