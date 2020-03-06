package lv.paulsnar.edu.dip107p.md02.db;

import java.io.IOException;

interface SeekableInterface {
  long position();
  void rewind() throws IOException;
  void seek(long position) throws IOException;
  void skip(long amount) throws IOException;
}
