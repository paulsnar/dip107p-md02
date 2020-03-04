package lv.paulsnar.edu.dip107p.md02;

import java.io.IOException;

import lv.paulsnar.edu.dip107p.md02.db.Database;

public class Main {
  public static void main(String[] args) {
    try (Database db = new Database(".")) {
      System.out.println("Opened database!");
      System.in.read();
    } catch (IOException exc) {
      System.err.println("Something went wrong :/");
      exc.printStackTrace(System.err);
    }
  }
}
