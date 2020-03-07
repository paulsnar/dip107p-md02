package lv.paulsnar.edu.dip107p.md02.db;

import java.io.EOFException;
import java.io.IOException;

class MemoryBuffer implements ValueReaderInterface, ValueWriterInterface {
  private int alignment;
  private int maxSize = 0;
  private byte[] data = null;

  private int position = 0;

  public MemoryBuffer() {
    this(1);
  }
  public MemoryBuffer(int alignment) {
    this(alignment, 0);
  }
  public MemoryBuffer(int alignment, int maxSize) {
    this.alignment = alignment;
    this.maxSize = maxSize;
  }

  static int alignTo(int size, int alignment) {
    int pages = size / alignment;
    int remainder = size % (pages * alignment);
    if (remainder > 0) {
      pages += 1;
    }
    return pages * alignment;
  }

  private int align(int size) {
    return alignTo(size, alignment);
  }

  private void ensurePosition(int position)
      throws PageBoundaryExceededException {
    if (position > maxSize) {
      throw new PageBoundaryExceededException();
    }

    if (data == null) {
      data = new byte[align(position)];
    } else if (position > data.length) {
      byte[] newData = new byte[align(position)];
      for (int i = 0; i < this.position; i += 1) {
        newData[i] = data[i];
      }
      data = newData;
    }
  }

  private void ensureSize(int size) throws PageBoundaryExceededException {
    ensurePosition(position + size);
  }

  @Override
  public long position() {
    return position;
  }

  @Override
  public void rewind() throws IOException {
    position = 0;
  }

  @Override
  public void seek(long position)
      throws PageBoundaryExceededException, IOException {
    ensurePosition((int) position);
    this.position = (int) position;
  }

  @Override
  public void skip(long amount)
      throws PageBoundaryExceededException, IOException {
    ensureSize((int) amount);
    position += amount;
  }

  @Override
  public void write(int value) throws ValueOutOfBoundsException, IOException {
    if (value < -128 || value >= 256) {
      throw new ValueOutOfBoundsException(
        "Cannot fit " + value + " into a byte");
    }
    ensureSize(1);
    data[position] = (byte) value;
    position += 1;
  }

  @Override
  public void write(byte[] buffer) throws IOException {
    ensureSize(buffer.length);
    for (int i = 0; i < buffer.length; i += 1) {
      data[position + i] = buffer[i];
    }
    position += buffer.length;
  }

  @Override
  public void write(byte[] buffer, int offset, int length) throws IOException {
    ensureSize(length);
    for (int i = 0; i < length; i += 1) {
      data[position + i] = buffer[offset + i];
    }
    position += length;
  }

  @Override
  public void writeU15(int value) throws ValueOutOfBoundsException, IOException {
    if (value < 0 || value > 0x7FFF) {
      throw new ValueOutOfBoundsException("Cannot fit " + value + " into u15");
    }
    ensureSize(BinaryInt.U15_SIZE);
    data[position] = (byte) ((value & 0x7F00) >> 8);
    data[position + 1] = (byte) (value & 0xFF);
    position += 2;
  }

  @Override
  public void writeU31(int value) throws ValueOutOfBoundsException, IOException {
    if (value < 0) {
      throw new ValueOutOfBoundsException("Cannot fit " + value + " into u31");
    }
    ensureSize(BinaryInt.U31_SIZE);
    data[position] = (byte) ((value & 0x7F000000) >> 24);
    data[position + 1] = (byte) ((value & 0xFF0000) >> 16);
    data[position + 2] = (byte) ((value & 0xFF00) >> 8);
    data[position + 3] = (byte) (value & 0xFF);
    position += 4;
  }

  @Override
  public long length() {
    return data == null ? alignment : data.length;
  }

  @Override
  public int read() throws EOFException, IOException {
    try {
      ensureSize(1);
    } catch (PageBoundaryExceededException exc) {
      throw new EOFException();
    }
    int value = data[position];
    if (value < 0) {
      value += 256;
    }
    position += 1;
    return value;
  }

  @Override
  public int read(byte[] buffer) throws EOFException, IOException {
    try {
      ensureSize(buffer.length);
    } catch (PageBoundaryExceededException exc) {
      throw new EOFException();
    }
    for (int i = 0; i < buffer.length; i += 1) {
      buffer[i] = data[position + i];
    }
    position += buffer.length;
    return buffer.length;
  }

  @Override
  public int read(byte[] buffer, int offset, int length)
      throws EOFException, IOException {
    try {
      ensureSize(length);
    } catch (PageBoundaryExceededException exc) {
      throw new EOFException();
    }
    for (int i = 0; i < length; i += 1) {
      buffer[offset + i] = data[position + i];
    }
    position += length;
    return length;
  }

  @Override
  public void readExactly(byte[] buffer) throws EOFException, IOException {
    read(buffer);
  }

  @Override
  public void readExactly(byte[] buffer, int offset, int length) throws EOFException, IOException {
    read(buffer, offset, length);
  }

  @Override
  public int readU15()
      throws MalformedValueException, EOFException, IOException {
    try {
      ensureSize(BinaryInt.U15_SIZE);
    } catch (PageBoundaryExceededException exc) {
      throw new EOFException();
    }
    int a = data[position];
    int b = (data[position + 1] + 256) % 256;
    if (a < 0) {
      throw new MalformedValueException("Invalid u15: top bit set");
    }
    position += 2;
    return (a << 8) | b;
  }

  @Override
  public int readU31() throws MalformedValueException, EOFException, IOException {
    try {
      ensureSize(BinaryInt.U31_SIZE);
    } catch (PageBoundaryExceededException exc) {
      throw new EOFException();
    }

    int a = data[position];
    int b = (data[position + 1] + 256) % 256;
    int c = (data[position + 2] + 256) % 256;
    int d = (data[position + 3] + 256) % 256;
    if (a < 0) {
      throw new MalformedValueException("Invalid u31: top bit set");
    }
    position += 4;
    return (a << 24) | (b << 16) | (c << 8) | d;
  }

  public void writeTo(ValueWriterInterface target)
      throws IOException {
    target.write(data);
  }
}
