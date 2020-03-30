package lv.paulsnar.edu.dip107p.md02;

import java.io.File;

import lv.paulsnar.edu.dip107p.md02.db.Database;
import lv.paulsnar.edu.dip107p.md02.db.DatabaseAlreadyOpenException;
import lv.paulsnar.edu.dip107p.md02.ui.UserDialogue;

public final class Main {
  private Main() { }

  public static final String VERSION = "1.0.0";

  private static int main() {
    try (Database db = new Database(new File("database.dat"))) {
      try (UserDialogue dialogue = new UserDialogue(db)) {
        dialogue.run();
      }
      db.save();
      return 0;
    } catch (DatabaseAlreadyOpenException exc) {
      System.err.println("--- Kļūda: Datubāze jau ir atvērta. ---");
      System.err.println(
          "Lūdzu, aizveriet citas programmas instances, lai turpinātu.");
      return 1;
    } catch (Exception exc) {
      System.err.println(
          "--- Atvainojiet, programmas izpildes laikā notika kļūda. ---");
      System.err.println("---- 8< ----");
      exc.printStackTrace(System.err);
      System.err.println("---- 8< ----");
      return 1;
    }
  }

  public static void main(String[] args) {
    System.exit(main());
  }
}
