package lv.paulsnar.edu.dip107p.md02.ui;

import java.util.Scanner;

import lv.paulsnar.edu.dip107p.md02.Book;

final class EditState implements StateExecutor {
  private static class InterruptedException extends RuntimeException { }

  private Book currentBook = null;
  private String[] ids = null;

  EditState() {
  }
  EditState(String[] ids) {
    this.ids = ids;
  }

  @Override
  public void run(State state, Scanner sc) {
    System.out.println("-- Atstājiet lauku tukšu, lai nemainītu tā vērtību.");
    if (ids == null) {
      System.out.println("-- Nenorādiet grāmatas numuru, lai pārtrauktu.");
    }

    for (int i = 0; ids == null || i < ids.length; i += 1) {
      System.out.println();

      try {
        String id;
        if (ids != null) {
          id = ids[i];
          System.out.printf("Grāmatas numurs: %s%n", id);
        } else {
          System.out.print("Grāmatas numurs: ");
          id = readLine(sc);
          if (id.length() == 0) {
            break;
          }
        }

        Book book = state.db.get(id);
        if (book == null) {
          System.out.println("-- Grāmata ar numuru " + id + " nav atrasta.");
          continue;
        }

        System.out.printf("Autora uzvārds [%s]: ", book.author.surname);
        String input = readLine(sc);
        if (input.length() > 0) {
          book.author.surname = input;
        }

        System.out.printf("Autora vārds: [%s]: ", book.author.name);
        input = readLine(sc);
        if (input.length() > 0) {
          book.author.name = input;
        }

        System.out.printf("Grāmatas nosaukums [%s]: ", book.title);
        input = readLine(sc);
        if (input.length() > 0) {
          book.title = input;
        }

        System.out.println("-- Izmaiņas saglabātas!");
      } catch (InterruptedException exc) {
        break;
      }
    }
  }

  private static String readLine(Scanner sc) throws InterruptedException {
    if ( ! sc.hasNextLine()) {
      System.out.println();
      throw new InterruptedException();
    }
    return sc.nextLine();
  }
}
