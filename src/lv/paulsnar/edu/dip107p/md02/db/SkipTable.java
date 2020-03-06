package lv.paulsnar.edu.dip107p.md02.db;

import java.io.IOException;
import java.io.RandomAccessFile;

abstract class SkipTable {
  static final int QPAGE_SIZE = Page.PAGE_SIZE / 4;
  private static final int ENTRY_SIZE = 8;

  protected int qpageNumber;
  protected int[] pages;

  SkipTable(int qpageNumber) {
    this.qpageNumber = qpageNumber;
  }

  abstract protected void processEntry(byte[] entry, int page);
  public void readFrom(RandomAccessFile file) throws IOException {
    // byte[] buf = new byte[ENTRY_SIZE];

    // file.seek(QPAGE_SIZE * qpageNumber);
    // file.read(buf, 0, 2);
    // int length = ByteFormats.getU15(buf, 0);

    // int remaining = QPAGE_SIZE * length - 2;
    // while (remaining > 0) {
    //   file.read(buf);
    //   int page = ByteFormats.getU31(buf, 4);
    //   processEntry(buf, page);
    //   remaining -= 8;
    // }
  }
}
