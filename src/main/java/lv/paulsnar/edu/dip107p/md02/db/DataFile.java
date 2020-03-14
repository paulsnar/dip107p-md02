package lv.paulsnar.edu.dip107p.md02.db;

import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

/**
 * A class that wraps a {@link java.io.RandomAccessFile} and implements
 * {@link Reader} and {@link Writer}.
 *
 * After providing the file to this class, it will close the file when
 * requested. As such, no file operations should take place on it after passing
 * it into this class.
 */
final class DataFile implements AutoCloseable, Closeable, Reader, Writer {
  private RandomAccessFile file;
  public DataFile(RandomAccessFile file) {
    this.file = file;
  }

  @Override
  public void close() throws IOException {
    file.close();
  }

  public FileChannel getChannel() {
    return file.getChannel();
  }

  public long position() throws IOException {
    return file.getFilePointer();
  }

  public void seek(long position) throws IOException {
    file.seek(position);
  }

  public int skip(int n) throws IOException {
    return file.skipBytes(n);
  }

  public long length() throws IOException {
    return file.length();
  }

  public void length(long newLength) throws IOException {
    file.setLength(newLength);
  }

  public int read() throws IOException {
    return file.read();
  }

  public int read(byte[] buf) throws IOException {
    return file.read(buf);
  }

  public int read(byte[] buf, int offset, int length) throws IOException {
    return file.read(buf, offset, length);
  }

  public void readFully(byte[] buf) throws IOException {
    file.readFully(buf);
  }

  public void readFully(byte[] buf, int offset, int length) throws IOException {
    file.readFully(buf, offset, length);
  }

  public boolean readBoolean() throws IOException {
    return file.readBoolean();
  }

  public byte readByte() throws IOException {
    return file.readByte();
  }

  public char readChar() throws IOException {
    return file.readChar();
  }

  public double readDouble() throws IOException {
    return file.readDouble();
  }

  public float readFloat() throws IOException {
    return file.readFloat();
  }

  public int readInt() throws IOException {
    return file.readInt();
  }

  public String readLine() throws IOException {
    return file.readLine();
  }

  public long readLong() throws IOException {
    return file.readLong();
  }

  public short readShort() throws IOException {
    return file.readShort();
  }

  public int readUnsignedByte() throws IOException {
    return file.readUnsignedByte();
  }

  public int readUnsignedShort() throws IOException {
    return file.readUnsignedShort();
  }

  public String readUTF() throws IOException {
    return file.readUTF();
  }

  public void write(int b) throws IOException {
    file.write(b);
  }

  public void write(byte[] buf) throws IOException {
    file.write(buf);
  }

  public void write(byte[] buf, int offset, int length) throws IOException {
    file.write(buf, offset, length);
  }

  public void writeBoolean(boolean v) throws IOException {
    file.writeBoolean(v);
  }

  public void writeByte(int v) throws IOException {
    file.writeByte(v);
  }

  public void writeBytes(String s) throws IOException {
    file.writeBytes(s);
  }

  public void writeChar(int v) throws IOException {
    file.writeChar(v);
  }

  public void writeChars(String s) throws IOException {
    file.writeChars(s);
  }

  public void writeDouble(double v) throws IOException {
    file.writeDouble(v);
  }

  public void writeFloat(float v) throws IOException {
    file.writeFloat(v);
  }

  public void writeInt(int v) throws IOException {
    file.writeInt(v);
  }

  public void writeLong(long v) throws IOException {
    file.writeLong(v);
  }

  public void writeShort(int v) throws IOException {
    file.writeShort(v);
  }

  public void writeUTF(String s) throws IOException {
    file.writeUTF(s);
  }
}
