package lv.paulsnar.edu.dip107p.md02;

import java.io.IOException;

import lv.paulsnar.edu.dip107p.md02.db.Database;
import lv.paulsnar.edu.dip107p.md02.db.Tuple;

public class Main {
  public static void main(String[] args) {
    try (Database db = new Database(".")) {
      System.out.println("Opened database!");
      System.in.read();
      
      Tuple t = new Tuple(0, "Skalbe", "Kārlis", "Kaķīša dzirnaviņas");
      db.appendEntry(t);
      System.out.println("Appended entry!");
      System.in.read();
    } catch (IOException exc) {
      System.err.println("Something went wrong :/");
      exc.printStackTrace(System.err);
    }
  }
}
