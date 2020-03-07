package lv.paulsnar.edu.dip107p.md02.db;

import java.io.EOFException;
import java.io.IOException;
import java.lang.ref.WeakReference;

class QPageInterval implements ValueReaderInterface, ValueWriterInterface {
  static final int QPAGE_SIZE = Page.PAGE_SIZE / 4;

  long offset;
  long size = 1;
  private WeakReference<ValueReadWriter> file;
  private long position = 0;

  QPageInterval(long offset, ValueReadWriter file) {
    this.offset = offset;
    this.file = new WeakReference<>(file);
  }

  private ValueReadWriter getFile() throws IOException {
    ValueReadWriter file = this.file.get();
    if (file == null) {
      throw new RuntimeException("QPageInterval outlived its file");
    }
    setPosition(file);
    return file;
  }

  private void setPosition(ValueReadWriter file) throws IOException {
    long offset = file.position();
    long expectedOffset = offset * QPAGE_SIZE + position;
    if (expectedOffset != offset) {
      file.seek(expectedOffset);
    }
  }

  @Override
  public long position() {
    return offset * QPAGE_SIZE + position;
  }

  public long offset() {
    return position;
  }

  public long size() {
    return QPAGE_SIZE * size;
  }

  @Override
  public void rewind() throws IOException {
    position = 0;
    getFile(); // seeks implicitly
  }

  @Override
  public void seek(long position) throws IOException {
    // TODO Auto-generated method stub

  }
  @Override
  public void skip(long amount) throws IOException {
    // TODO Auto-generated method stub

  }
  @Override
  public void write(int value) throws IOException {
    // TODO Auto-generated method stub

  }
  @Override
  public void write(byte[] buffer) throws IOException {
    // TODO Auto-generated method stub

  }
  @Override
  public void write(byte[] buffer, int offset, int length) throws IOException {
    // TODO Auto-generated method stub

  }
  @Override
  public void writeU15(int value) throws ValueOutOfBoundsException, IOException {
    // TODO Auto-generated method stub

  }
  @Override
  public void writeU31(int value) throws ValueOutOfBoundsException, IOException {
    // TODO Auto-generated method stub

  }
  @Override
  public long length() {
    // TODO Auto-generated method stub
    return 0;
  }
  @Override
  public int read() throws EOFException, IOException {
    // TODO Auto-generated method stub
    return 0;
  }
  @Override
  public int read(byte[] buffer) throws EOFException, IOException {
    // TODO Auto-generated method stub
    return 0;
  }
  @Override
  public int read(byte[] buffer, int offset, int length) throws EOFException, IOException {
    // TODO Auto-generated method stub
    return 0;
  }
  @Override
  public void readExactly(byte[] buffer) throws EOFException, IOException {
    // TODO Auto-generated method stub

  }
  @Override
  public void readExactly(byte[] buffer, int offset, int length) throws EOFException, IOException {
    // TODO Auto-generated method stub

  }
  @Override
  public int readU15() throws MalformedValueException, EOFException, IOException {
    // TODO Auto-generated method stub
    return 0;
  }
  @Override
  public int readU31() throws MalformedValueException, EOFException, IOException {
    // TODO Auto-generated method stub
    return 0;
  }
}
