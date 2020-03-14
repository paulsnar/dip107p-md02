package lv.paulsnar.edu.dip107p.md02.db;

import java.io.IOException;

/**
 * An union of {@link java.io.Reader} and {@link java.io.DataInput}.
 */
interface Reader {
  int read() throws IOException;
  int read(byte[] buf) throws IOException;
  int read(byte[] buf, int offset, int length) throws IOException;
  void readFully(byte[] buf) throws IOException;
  void readFully(byte[] buf, int offset, int length) throws IOException;

  boolean readBoolean() throws IOException;
  byte readByte() throws IOException;
  char readChar() throws IOException;
  double readDouble() throws IOException;
  float readFloat() throws IOException;
  int readInt() throws IOException;
  String readLine() throws IOException;
  long readLong() throws IOException;
  short readShort() throws IOException;
  int readUnsignedByte() throws IOException;
  int readUnsignedShort() throws IOException;
  String readUTF() throws IOException;

  long position() throws IOException;
  void seek(long position) throws IOException;
  int skip(int n) throws IOException;
  long length() throws IOException;
}
