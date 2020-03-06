package lv.paulsnar.edu.dip107p.md02.db;

import java.io.EOFException;
import java.io.IOException;

interface ValueReaderInterface extends SeekableInterface {
  long length();
  int read() throws EOFException, IOException;
  int read(byte[] buffer) throws EOFException, IOException;
  int read(byte[] buffer, int offset, int length)
      throws EOFException, IOException;
  void readExactly(byte[] buffer) throws EOFException, IOException;
  void readExactly(byte[] buffer, int offset, int length)
      throws EOFException, IOException;
  int readU15() throws MalformedValueException, EOFException, IOException;
  int readU31() throws MalformedValueException, EOFException, IOException;
}
