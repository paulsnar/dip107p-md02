package lv.paulsnar.edu.dip107p.md02.db;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.Arrays;

public class RecordPageFile implements AutoCloseable {
  private static final byte[] HEADER_MAGIC = {
      (byte) 0xF0, (byte) 0x9F, (byte) 0x93, (byte) 0x96,
      'd', 'a', 't', 'a',
  };

  private ValueReadWriter file;
  private FileLock lock;
  private int sequence = 0;
  private RecordPage head, tail;

  public RecordPageFile(File filename)
      throws MalformedDatabaseException, IOException {
    RandomAccessFile file = new RandomAccessFile(filename, "rw");
    lock = file.getChannel().tryLock();
    if (lock == null) {
      file.close();
      throw new AlreadyOpenException();
    }
    this.file = new ValueReadWriter(file);

    readHeader();
    head = getPage(1);
  }

  @Override
  public void close() throws IOException {
    lock.close();
    file.close();
  }

  private void readHeader() throws MalformedDatabaseException, IOException {
    byte[] header = new byte[HEADER_MAGIC.length];
    file.seek(0);
    try {
      file.readExactly(header);
    } catch (EOFException exc) {
      writeHeader();
      return;
    }
    if ( ! Arrays.equals(HEADER_MAGIC, header)) {
      throw new MalformedDatabaseException("Bad header magic");
    }

    sequence = file.readU31();
    file.skip(8 * 64); // TODO implement skiplist
  }

  void writeHeader() throws IOException {
    Page zeroPage = Page.openAndWipe(0, file);
    zeroPage.write(HEADER_MAGIC);
    zeroPage.writeU31(sequence);
    zeroPage.skip(8 * 64);
  }

  public RecordPage getPage(int page) throws IOException {
    Page rawPage = new Page(page, file);
    try {
      rawPage.read();
      rawPage.skip(-1);
    } catch (EOFException exc) {
      exc.printStackTrace();
      rawPage = Page.append(file);
    }
    return new RecordPage(rawPage);
  }

  private RecordPage getTailPage() throws IOException {
    if (tail == null) {
      tail = head;
      while (tail.header.nextPage != 0) {
        tail = getPage(tail.header.nextPage);
      }
    }
    return tail;
  }

  public void appendRecord(Record record) throws IOException {
    record.id = sequence += 1;
    RecordPage tail = getTailPage();
    tail.appendRecord(record);
    writeHeader();
  }
}
