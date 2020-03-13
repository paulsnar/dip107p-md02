package lv.paulsnar.edu.dip107p.md02.ui;

import java.util.Scanner;

import lv.paulsnar.edu.dip107p.md02.Book;

final class AddState implements StateExecutor {
  private static class InterruptedException extends RuntimeException { }

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
    do {
      System.out.println();
      try {
        if (newBook.id == null) {
          System.out.print("Grāmatas numurs: ");
          newBook.id = readLine(sc);
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
