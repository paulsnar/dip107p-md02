package lv.paulsnar.edu.dip107p.md02.db;

import java.io.IOException;

interface ValueWriterInterface extends SeekableInterface {
  void write(int value) throws IOException;
  void write(byte[] buffer) throws IOException;
  void write(byte[] buffer, int offset, int length) throws IOException;
  void writeU15(int value) throws ValueOutOfBoundsException, IOException;
  void writeU31(int value) throws ValueOutOfBoundsException, IOException;
}
