package lv.paulsnar.edu.dip107p.md02.db;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;

import lv.paulsnar.edu.dip107p.md02.util.ByteFormats;
import lv.paulsnar.edu.dip107p.md02.util.ParseException;

public class Database implements AutoCloseable {
  private static final byte[] HEADER_MAGIC =
    {-16, -97, -109, -106, 32, 100, 97, 116};
  private static final int HEADER_SIZE = HEADER_MAGIC.length + 8;

  private WAL wal = null;
  private RandomAccessFile file = null;
  private HashMap<Integer, WeakReference<Tuple>> cache = new HashMap<>();
  private long indicesStart = HEADER_SIZE, recordsStart;
  private int indicesLength, recordsLength;

  public Database(String directory) throws IOException {
    wal = new WAL(directory);

    try {
      File path = new File(directory, "database.dat");
      file = new RandomAccessFile(path, "rw");
    } catch (IOException exc) {
      wal.close();
      wal = null;
      throw exc;
    }

    boolean haveHeader = readHeader();
    if ( ! haveHeader) {
      indicesLength = 0;
      recordsLength = 0;
      writeHeader();
    } else {
      recordsStart = indicesStart = indicesLength;
    }
  }

  public void close() throws IOException {
    // TODO: sync WAL to a new database file
    wal.close();
    file.close();
  }

  private boolean readHeader() throws IOException {
    byte[] header = new byte[HEADER_SIZE];
    file.seek(0);
    int read = file.read(header);
    if (read == -1) {
      return false;
    }

    if ( ! Arrays.equals(header, 0, 8, HEADER_MAGIC, 0, 8)) {
      throw new IOException("Malformed database header (bad magic)");
    }
    
    try {
      indicesLength = ByteFormats.getU31(header, 8);
      recordsLength = ByteFormats.getU31(header, 12);
    } catch (ParseException exc) {
      throw new IOException("Malformed database header", exc);
    }
    return true;
  }
  
  private void writeHeader() throws IOException {
    byte[] header = new byte[HEADER_SIZE];
    for (int i = 0; i < 8; i++) {
      header[i] = HEADER_MAGIC[i];
    }
    
    try {
      ByteFormats.putU31(indicesLength, header, 8);
      ByteFormats.putU31(recordsLength, header, 12);
    } catch (ParseException exc) {
      throw new RuntimeException("Sizes out of bounds", exc);
    }
    
    file.seek(0);
    file.write(header);
  }
}
