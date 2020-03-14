package lv.paulsnar.edu.dip107p.md02.db;

import java.io.IOException;

interface Persister<T> {
  T readFrom(Reader source) throws IOException;
  void writeTo(Writer target, T item) throws IOException;
}
