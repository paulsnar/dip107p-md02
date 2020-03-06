package lv.paulsnar.edu.dip107p.md02.db;

import java.io.IOException;
import java.lang.ref.WeakReference;

class Page implements ValueReaderInterface, ValueWriterInterface {
  static final int PAGE_SIZE = 1024;
  private static final byte[] zeroes = new byte[PAGE_SIZE];

  long number;
  private WeakReference<ValueReadWriter> file;
  private long position = 0;

  Page(long number, ValueReadWriter file) {
    this.number = number;
    this.file = new WeakReference<>(file);
  }

  public static Page append(ValueReadWriter file) throws IOException {
    long currentSize = file.length();
    currentSize += PAGE_SIZE - (currentSize % PAGE_SIZE);
    file.seek(currentSize);
    file.write(zeroes);

    return new Page(currentSize / PAGE_SIZE, file);
  }

  private void setPosition(ValueReadWriter file) throws IOException {
    long offset = file.position();
    long expectedOffset = number * PAGE_SIZE + position;
    if (expectedOffset != offset) {
      file.seek(expectedOffset);
    }
  }
  private void guardPosition(int length) throws PageBoundaryExceededException {
    if ( ! ensureSufficientSpace(length)) {
      throw new PageBoundaryExceededException();
    }
  }

  public boolean ensureSufficientSpace(int length) {
    return position + length < PAGE_SIZE;
  }

  private ValueReadWriter getFile() throws IOException {
    ValueReadWriter file = this.file.get();
    if (file == null) {
      throw new RuntimeException("Page outlived its file");
    }
    setPosition(file);
    return file;
  }

  public long length() {
    return PAGE_SIZE;
  }

  public void rewind() throws IOException {
    position = 0;
    getFile(); // seeks implicitly
  }

  public void seek(long offset) throws IOException {
    if (offset < 0 || offset > PAGE_SIZE) {
      throw new RuntimeException("Cannot seek page outside its boundaries");
    }
    position = offset;
    getFile(); // seeks implicitly
  }

  public void skip(long amount) throws IOException {
    seek(position + amount);
  }

  public long position() {
    return number * PAGE_SIZE + position;
  }
  public long offset() {
    return position;
  }

  public int read() throws PageBoundaryExceededException, IOException {
    guardPosition(1);
    int value = getFile().read();
    position += 1;
    return value;
  }

  public int read(byte[] buffer) throws PageBoundaryExceededException, IOException {
    guardPosition(buffer.length);
    getFile().readExactly(buffer);
    position += buffer.length;
    return buffer.length;
  }

  public int read(byte[] buffer, int start, int length)
      throws PageBoundaryExceededException, IOException {
    guardPosition(length);
    getFile().readExactly(buffer, start, length);
    position += length;
    return length;
  }

  public void readExactly(byte[] buffer)
      throws PageBoundaryExceededException, IOException {
    read(buffer);
  }

  public void readExactly(byte[] buffer, int start, int length)
      throws PageBoundaryExceededException, IOException {
    read(buffer, start, length);
  }

  public int readU15() throws PageBoundaryExceededException, IOException {
    guardPosition(ValueReadWriter.U15_SIZE);
    int value = getFile().readU15();
    position += ValueReadWriter.U15_SIZE;
    return value;
  }

  public int readU31() throws PageBoundaryExceededException, IOException {
    guardPosition(ValueReadWriter.U31_SIZE);
    int value = getFile().readU31();
    position += ValueReadWriter.U31_SIZE;
    return value;
  }

  public void write(int value)
      throws PageBoundaryExceededException, IOException {
    guardPosition(1);
    getFile().write(value);
    position += 1;
  }

  public void writeU15(int value)
      throws PageBoundaryExceededException, IOException {
    guardPosition(ValueReadWriter.U15_SIZE);
    getFile().writeU15(value);
    position += ValueReadWriter.U15_SIZE;
  }

  public void writeU31(int value)
      throws PageBoundaryExceededException, IOException {
    guardPosition(ValueReadWriter.U31_SIZE);
    getFile().writeU31(value);
    position += ValueReadWriter.U31_SIZE;
  }

  public void write(byte[] data)
      throws PageBoundaryExceededException, IOException {
    guardPosition(data.length);
    getFile().write(data);
    position += data.length;
  }

  public void write(byte[] data, int offset, int length)
      throws PageBoundaryExceededException, IOException {
    guardPosition(length);
    getFile().write(data, offset, length);
    position += length;
  }
}
