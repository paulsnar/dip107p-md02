package lv.paulsnar.edu.dip107p.md02.db;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import lv.paulsnar.edu.dip107p.md02.util.ByteFormats;
import lv.paulsnar.edu.dip107p.md02.util.ParseException;

public class Database implements AutoCloseable {
  private static final byte[] HEADER_MAGIC = {-16, -97, -109, -106};
  private static final int HEADER_SIZE = HEADER_MAGIC.length + 12;

  private WAL wal = null;
  private File filePath;
  private RandomAccessFile file = null;
  private int indicesLength, recordLength, topSequence, topPersisted;
  private HashMap<Integer, WeakReference<Tuple>> cachedTuples = new HashMap<>();
  private HashMap<Integer, Tuple> changedTuples = new HashMap<>();

  public Database(String directory) throws IOException {
    wal = new WAL(directory);

    try {
      filePath = new File(directory, "database.dat");
      file = new RandomAccessFile(filePath, "rw");
    } catch (IOException exc) {
      wal.close();
      wal = null;
      throw exc;
    }

    boolean haveHeader = readHeader();
    if ( ! haveHeader) {
      indicesLength = 0;
      recordLength = 0;
      topSequence = 0;
      topPersisted = 0;
      writeHeader();
    }
  }

  public void close() throws IOException {
//    replayWal();
    rewrite();
    wal.truncate();
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

    if ( ! Arrays.equals(header, 0, HEADER_MAGIC.length, HEADER_MAGIC, 0, HEADER_MAGIC.length)) {
      throw new IOException("Malformed database header (bad magic)");
    }
    
    try {
      indicesLength = ByteFormats.getU31(header, 4);
      recordLength = ByteFormats.getU31(header, 8);
      topSequence = ByteFormats.getU31(header, 12);
    } catch (ParseException exc) {
      throw new IOException("Malformed database header", exc);
    }
    return true;
  }
  
  private void writeHeader() throws IOException {
    writeHeader(file);
  }
  private void writeHeader(RandomAccessFile file) throws IOException {
    byte[] header = new byte[HEADER_SIZE];
    for (int i = 0; i < HEADER_MAGIC.length; i++) {
      header[i] = HEADER_MAGIC[i];
    }
    
    try {
      ByteFormats.putU31(indicesLength, header, 4);
      ByteFormats.putU31(recordLength, header, 8);
      ByteFormats.putU31(topSequence, header, 12);
    } catch (ParseException exc) {
      throw new RuntimeException("Sizes out of bounds", exc);
    }
    
    file.seek(0);
    file.write(header);
  }
  
  private void rewrite() throws IOException {
    File tmpfile = new File(filePath + "-swp");
    RandomAccessFile target = new RandomAccessFile(tmpfile, "rw");
    FileLock targetLock = target.getChannel().tryLock();
    if (targetLock == null) {
      target.close();
      throw new IOException("Could not open swap file for WAL replay");
    }
    writeHeader(target);

    // TODO: write indices placeholder
    
    HashMap<Integer, Tuple> inserts = new HashMap<>();    
    HashMap<Integer, Tuple> edits = new HashMap<>();
    HashMap<Integer, Object> deletes = new HashMap<>();

    wal.replay(new WAL.ReplayHandler() {
      @Override
      public void handleInsert(int sequence, Tuple tuple) {
        inserts.put(tuple.id, tuple);
      }
      
      @Override
      public void handleEdit(int sequence, Tuple tuple) {
        edits.put(tuple.id, tuple);
      }
      
      @Override
      public void handleDelete(int sequence, int id) {
        deletes.put(id, null);
      }
    });
    
    file.seek(HEADER_SIZE + indicesLength);
    
    Tuple row;
    while ((row = readEntry()) != null) {
      if (edits.containsKey(row.id)) {
        row = edits.get(row.id);
      } else if (deletes.containsKey(row.id)) {
        continue;
      }
      try {
        row.writeTo(target);
      } catch (ParseException exc) {
        throw new IOException("Row couldn't be serialized", exc);
      }
    }
    
    Set<Integer> newIdSet = inserts.keySet();
    Integer[] newIds = new Integer[newIdSet.size()];
    newIds = newIdSet.toArray(newIds);
    Arrays.sort(newIds);
    for (Integer id : newIds) {
      try {
        inserts.get(id).writeTo(target);
      } catch (ParseException exc) {
        throw new IOException("Row couldn't be serialized", exc);
      }
    }
    
    targetLock.close();
    tmpfile.renameTo(filePath);
    filePath = tmpfile;
    file.close();
    file = target;
  }
  
  Tuple readEntry() throws IOException {
    try {
      return Tuple.readFrom(file);
    } catch (ParseException exc) {
      throw new IOException("Malformed database file", exc);
    }
  }
  
  public void appendEntry(Tuple tuple) throws IOException {
    topSequence += 1;
    tuple.id = topSequence;
    wal.append(WALRow.Op.INSERT, tuple);
    changedTuples.put(tuple.id, tuple);
    recordLength += tuple.length();
  }
}
