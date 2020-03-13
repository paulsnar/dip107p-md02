package lv.paulsnar.edu.dip107p.md02;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

final public class BookDatabase implements AutoCloseable {
  private static final byte[] HEADER_MAGIC = {
      (byte) 0xF0, (byte) 0x9F, (byte) 0x93, (byte) 0x96,
      'd', 'a', 't', 'a',
  };

  private RandomAccessFile file;
  private FileLock lock;

  private Map<String, Book> books;

  BookDatabase(File path) throws IOException {
    file = new RandomAccessFile(path, "rw");
    lock = file.getChannel().tryLock();
    if (lock == null) {
      file.close();
      throw new DatabaseAlreadyOpenException();
    }

    byte[] header = new byte[8];
    int read = file.read(header);
    if (read == -1) {
      initFile();
      return;
    }
    if ( ! Arrays.equals(HEADER_MAGIC, header)) {
      throw new MalformedDatabaseException("Header magic mismatch");
    }

    int entryCount = file.readInt();
    books = new HashMap<>(entryCount);

    for (int i = 0; i < entryCount; i += 1) {
      Book book = Book.readFrom(file);
      books.put(book.id, book);
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
      iterator.next().writeTo(file);
    }
    file.setLength(file.getFilePointer());
  }

  public List<Book> getAll() {
    return List.copyOf(books.values());
  }
}
