package lv.paulsnar.edu.dip107p.md02;

import java.io.File;

import lv.paulsnar.edu.dip107p.md02.db.Record;
import lv.paulsnar.edu.dip107p.md02.db.RecordPageFile;

public class Main {
  public static void main(String[] args) {
    File f = new File("database.dat");
    try (RecordPageFile file = new RecordPageFile(f)) {
      Record record = new Record("Skalbe", "Kārlis", "Kaķīša dzirnaviņas");
      record.persist(file);
    } catch (Exception exc) {
      exc.printStackTrace();
    }
  }
}
