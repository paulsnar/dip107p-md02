package lv.paulsnar.edu.dip107p.md02.db;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;

class ValueReadWriter
    implements AutoCloseable, ValueReaderInterface, ValueWriterInterface {

  static final int U15_SIZE = 2;
  static final int U31_SIZE = 4;

  private RandomAccessFile file;
  private long cursor = 0;
  private byte[] scratch = new byte[8];

  public ValueReadWriter(RandomAccessFile file) throws IOException {
    this.file = file;
    cursor = file.getFilePointer();
  }

  public void close() throws IOException {
    file.close();
  }

  public void seek(long offset) throws IOException {
    if (offset != cursor) {
      file.seek(offset);
      cursor = offset;
    }
  }

  public void rewind() throws IOException {
    seek(0);
  }

  public void skip(long amount) throws IOException {
    seek(cursor + amount);
  }

  public long position() {
    return cursor;
  }

  public long length() {
    try {
      return file.length();
    } catch (IOException exc) {
      throw new RuntimeException("Cannot obtain file size", exc);
    }
  }

  public int read() throws EOFException, IOException {
    int value = file.read();
    if (value == -1) {
      throw new EOFException();
    }
    cursor += 1;
    return value;
  }

  public int read(byte[] buffer) throws EOFException, IOException {
    int read = file.read(buffer);
    if (read == -1) {
      throw new EOFException();
    }
    cursor += read;
    return read;
  }

  public int read(byte[] buffer, int offset, int length)
      throws EOFException, IOException {
    int read = file.read(buffer, offset, length);
    if (read == -1) {
      throw new EOFException();
    }
    cursor += read;
    return read;
  }

  public void readExactly(byte[] buffer) throws EOFException, IOException {
    if (read(buffer) < buffer.length) {
      throw new EOFException();
    }
  }

  public void readExactly(byte[] buffer, int offset, int length)
      throws EOFException, IOException {
    if (read(buffer, offset, length) < length) {
      throw new EOFException();
    }
  }

  public int readU15()
      throws MalformedValueException, EOFException, IOException {
    readExactly(scratch, 0, 2);
    if (scratch[0] < 0) {
      throw new MalformedValueException("Invalid u15: top bit set");
    }
    int a = (int) scratch[0];
    int b = (int) scratch[1];
    if (b < 0) {
      b += 256;
    }
    return (a << 8) | b;
  }

  public int readU31()
      throws MalformedValueException, EOFException, IOException {
    readExactly(scratch, 0, 4);
    if (scratch[0] < 0) {
      throw new MalformedValueException("Invalid u31: top bit set");
    }
    int a = scratch[0];
    int b = (scratch[1] + 256) % 256;
    int c = (scratch[2] + 256) % 256;
    int d = (scratch[3] + 256) % 256;
    return (a << 24) | (b << 16) | (c << 8) | d;
  }

  public void write(int value) throws IOException {
    file.write(value);
    cursor += 1;
  }

  public void write(byte[] buffer) throws IOException {
    file.write(buffer);
    cursor += buffer.length;
  }

  public void write(byte[] buffer, int offset, int length) throws IOException {
    file.write(buffer, offset, length);
    cursor += length;
  }

  public void writeU15(int value)
      throws ValueOutOfBoundsException, IOException {
    if (value < 0 || value > 0x7FFF) {
      throw new ValueOutOfBoundsException("Cannot fit " + value + " into u15");
    }
    scratch[0] = (byte) ((value & 0x7F00) >> 8);
    scratch[1] = (byte) (value & 0xFF);
    file.write(scratch, 0, 2);
  }

  public void writeU31(int value)
      throws ValueOutOfBoundsException, IOException {
    if (value < 0) { // other bound impossible due to Java int specifics
      throw new ValueOutOfBoundsException("Cannot fit " + value + " into u31");
    }

    scratch[0] = (byte) ((value & 0x7F000000) >> 24);
    scratch[1] = (byte) ((value & 0xFF0000) >> 16);
    scratch[2] = (byte) ((value & 0xFF00) >> 8);
    scratch[3] = (byte) (value & 0xFF);
    file.write(scratch, 0, 4);
  }
}
