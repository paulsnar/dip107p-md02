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

  private RandomAccessFile file = null;
  private FileLock lock;

  int rowCount = 0;

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
      writeHeader();
    }
  }

  public void close() throws IOException {
    if (file != null) {
      file.close();
    }
  }

  private boolean readHeader() throws IOException {
    byte[] header = new byte[16];
    file.seek(0);
    int read = file.read(header);
    if (read == -1) {
      return false;
    }

    if ( ! Arrays.equals(header, 0, 8, HEADER_MAGIC, 0, 8)) {
      throw new IOException("Malformed database WAL header");
    }

    try {
      rowCount = ByteFormats.getU31(header, 8);
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
      ByteFormats.putU31(rowCount, header, 8);
    } catch (ParseException exc) {
      throw new RuntimeException("Row count out of bounds", exc);
    }

    file.seek(0);
    file.write(header);
  }
}
