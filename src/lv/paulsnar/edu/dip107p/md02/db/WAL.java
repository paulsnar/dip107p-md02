package lv.paulsnar.edu.dip107p.md02.db;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.Arrays;
import lv.paulsnar.edu.dip107p.md02.util.ByteFormats;
import lv.paulsnar.edu.dip107p.md02.util.ParseException;

class WAL implements AutoCloseable {
  private static final byte[] HEADER_MAGIC =
    {-16, -97, -109, -106, 32, 119, 97, 108};
  private static final int HEADER_SIZE = HEADER_MAGIC.length + 8;

  interface ReplayHandler {
    void handleInsert(int sequence, Tuple tuple);
    void handleEdit(int sequence, Tuple tuple);
    void handleDelete(int sequence, int id);
  }

  private RandomAccessFile file = null;
  private FileLock lock;
  private int sequence = 0;

  public WAL(String directory) throws IOException {
    File path = new File(directory, "database.wal");
    file = new RandomAccessFile(path, "rw");
    try {
      lock = file.getChannel().tryLock();
      if (lock == null) {
        throw new AlreadyOpenException();
      }
    } catch (IOException exc) {
      file.close();
      file = null;
      throw exc;
    }

    boolean hadHeader = readHeader();
    if ( ! hadHeader) {
      sequence = 1;
      writeHeader();
    }
  }

  public void close() throws IOException {
    if (file != null) {
      lock.close();
      file.close();
      lock = null;
      file = null;
    }
  }

  private boolean readHeader() throws IOException {
    byte[] header = new byte[HEADER_SIZE];
    file.seek(0);
    int read = file.read(header);
    if (read == -1) {
      return false;
    }

    if ( ! Arrays.equals(header, 0, 8, HEADER_MAGIC, 0, 8)) {
      throw new IOException("Malformed database WAL header");
    }

    try {
      sequence = ByteFormats.getU31(header, 8);
    } catch (ParseException exc) {
      throw new IOException("Malformed database WAL header", exc);
    }
    return true;
  }

  private void writeHeader() throws IOException {
    byte[] header = new byte[16];
    for (int i = 0; i < 8; i++) {
      header[i] = HEADER_MAGIC[i];
    }
    try {
      ByteFormats.putU31(sequence, header, 8);
    } catch (ParseException exc) {
      throw new RuntimeException("Row count out of bounds", exc);
    }

    file.seek(0);
    file.write(header);
  }

  void replay(ReplayHandler handler) throws IOException {
    file.seek(HEADER_SIZE);

    WALRow row;
    while ((row = readRow()) != null) {
      switch (row.op) {
        case INSERT: handler.handleInsert(row.sequence, row.data); break;
        case EDIT: handler.handleEdit(row.sequence, row.data); break;
        case DELETE: handler.handleDelete(row.sequence, row.id); break;
      }
    }
  }

  void truncate() throws IOException {
    file.setLength(HEADER_SIZE);
    sequence = 0;
    file.seek(0);
    writeHeader();
    file.seek(HEADER_SIZE);
  }

  private WALRow readRow() throws IOException {
    return WALRow.readFrom(file);
  }

  void append(WALRow.Op operation, Tuple tuple) throws IOException {
    WALRow row;
    if (operation == WALRow.Op.DELETE) {
      row = WALRow.delete(sequence, tuple.id);
    } else {
      row = new WALRow(operation, sequence, tuple);
    }
    sequence += 1;
    try {
      row.writeTo(file);
    } catch (ParseException exc) {
      throw new RuntimeException("WAL append failed due to unforeseen circumstances", exc);
    }
  }
}
