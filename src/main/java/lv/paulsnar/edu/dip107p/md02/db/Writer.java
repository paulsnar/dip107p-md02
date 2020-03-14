package lv.paulsnar.edu.dip107p.md02.db;

import java.io.IOException;

/**
 * An union of {@link java.io.Writer} and {@link java.io.DataOutput}, plus some
 * convenience methods.
 */
interface Writer {
  void write(int b) throws IOException;
  void write(byte[] buf) throws IOException;
  void write(byte[] buf, int offset, int length) throws IOException;

  void writeBoolean(boolean v) throws IOException;
  void writeByte(int v) throws IOException;
  void writeBytes(String s) throws IOException;
  void writeChar(int v) throws IOException;
  void writeChars(String s) throws IOException;
  void writeDouble(double v) throws IOException;
  void writeFloat(float v) throws IOException;
  void writeInt(int v) throws IOException;
  void writeLong(long v) throws IOException;
  void writeShort(int v) throws IOException;
  void writeUTF(String s) throws IOException;

  long position() throws IOException;
  void seek(long position) throws IOException;
  int skip(int n) throws IOException;
  long length() throws IOException;
  void length(long newLength) throws IOException;
}
