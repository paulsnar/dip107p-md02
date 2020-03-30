package lv.paulsnar.edu.dip107p.md02.db;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lv.paulsnar.edu.dip107p.md02.Book;

final public class Database implements AutoCloseable {
  private static final byte[] HEADER_MAGIC = {
      (byte) 0xF0, (byte) 0x9F, (byte) 0x93, (byte) 0x96,
      'd', 'a', 't', 'a',
  };

  private DataFile file;
  private FileLock lock;

  private BookPersister bookPersister = new BookPersister();
  private Map<String, Book> books;

  public Database(File path) throws IOException {
    {
      RandomAccessFile file = new RandomAccessFile(path, "rw");
      lock = file.getChannel().tryLock();
      if (lock == null) {
        file.close();
        throw new DatabaseAlreadyOpenException();
      }

      this.file = new DataFile(file);
    }

    byte[] header = new byte[8];
    try {
      file.readFully(header);
      if ( ! Arrays.equals(HEADER_MAGIC, header)) {
        throw new MalformedDatabaseException("Header magic mismatch");
      }
    } catch (EOFException exc) {
      initFile();
      return;
    }

    int entryCount = file.readInt();
    books = new HashMap<>(entryCount);

    try {
      for (int i = 0; i < entryCount; i += 1) {
        Book book = bookPersister.readFrom(file);
        books.put(book.id, book);
      }
    } catch (IOException exc) {
      throw new MalformedDatabaseException("Format error");
    }
  }

  private void initFile() throws IOException {
    file.write(HEADER_MAGIC);
    file.writeInt(0);
    books = new HashMap<>();
  }

  @Override
  public void close() throws IOException {
    lock.close();
    file.close();
  }

  public void add(Book book) {
    books.put(book.id, book);
  }

  public void remove(String id) {
    books.remove(id);
  }

  public Book get(String id) {
    return books.get(id);
  }

  public int entryCount() {
    return books.size();
  }

  public void save() throws IOException {
    file.seek(0);
    file.write(HEADER_MAGIC);
    file.writeInt(books.size());
    Iterator<Book> iterator = books.values().iterator();
    while (iterator.hasNext()) {
      bookPersister.writeTo(file, iterator.next());
    }
    file.length(file.position());
  }

  public List<Book> getAll() {
    return new ArrayList<>(books.values());
  }
}
