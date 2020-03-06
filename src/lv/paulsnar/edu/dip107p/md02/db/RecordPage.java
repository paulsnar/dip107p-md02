package lv.paulsnar.edu.dip107p.md02.db;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

class RecordPage {
  private Page page;
  private int recordCount;
  private ArrayList<Integer> offsets, indices;
  private HashMap<Integer, WeakReference<Record>> recordCache;

  RecordPage(Page page)
      throws MalformedDatabaseException, PageBoundaryExceededException,
      IOException {
    this.page = page;
    recordCount = page.readU15();

    offsets = new ArrayList<>(recordCount);
    indices = new ArrayList<>(recordCount);
    recordCache = new HashMap<>(recordCount);

    try {
      for (int i = 0; i < recordCount; i += 1) {
        offsets.add((int) page.offset());
        Record.Header header = Record.Header.readFrom(page);
        indices.add(header.isScrubbed ? -1 : header.id);
        page.skip(header.length);
      }
    } catch (PageBoundaryExceededException exc) {
      throw new MalformedDatabaseException(
        "Bad record page header: record count (" + recordCount + ") " +
        "overflows page", page.position(), exc);
    }
  }

  private int indexFor(int id) {
    for (int i = 0; i < recordCount; i += 1) {
      if (indices.get(i).intValue() == id) {
        return i;
      }
    }
    return -1;
  }

  public int firstId() {
    for (int i = 0; i < recordCount; i += 1) {
      int index = indices.get(i).intValue();
      if (index != -1) {
        return index;
      }
    }
    return -1;
  }

  public void appendRecord(Record record)
      throws PageBoundaryExceededException, IOException {
    int size = record.binarySize();
    page.seek(offsets.get(recordCount - 1).intValue());
    Record.Header header = Record.Header.readFrom(page);
    page.skip(header.length);

    int offset = (int) page.offset();
    if ( ! page.ensureSufficientSpace(size)) {
      throw new PageBoundaryExceededException();
    }
    recordCount += 1;
    record.writeTo(page);
    offsets.add(offset);
    indices.add(record.header.id);
  }

  public List<Record> loadAllRecords()
      throws MalformedDatabaseException, IOException {
    List<Record> records = new ArrayList<>(recordCount);

    for (int i = 0; i < recordCount; i += 1) {
      page.seek(offsets.get(i).intValue());
      Record record = Record.readFrom(page);
      if (record != null) {
        records.add(record);
        indices.set(i, record.header.id);
      } else {
        indices.set(i, -1);
      }
    }

    return records;
  }

  public Record loadRecord(int id)
      throws MalformedDatabaseException, IOException {
    if (recordCache.containsKey(id)) {
      Record record = recordCache.get(id).get();
      if (record == null) {
        recordCache.remove(id);
      } else {
        return record;
      }
    }

    int index = indexFor(id);
    if (index == -1) {
      return null;
    }

    page.seek(offsets.get(index));
    Record record = Record.readFrom(page);
    if (record != null) {
      recordCache.put(record.header.id, new WeakReference<>(record));
    } else {
      indices.set(index, -1);
    }
    return record;
  }

  public void scrubRecord(int id) throws IOException {
    int index = indexFor(id);
    if (index == -1) {
      return;
    }

    page.seek(offsets.get(index));
    Record.Header header = Record.Header.readFrom(page);
    header.isScrubbed = true;
    page.seek(offsets.get(index));
    header.writeTo(page);
    indices.set(index, -1);

    if (recordCache.containsKey(id)) {
      recordCache.remove(id);
    }
  }

  public void vacuum() throws ValueOutOfBoundsException, IOException {
    List<Record> records = loadAllRecords();

    page.rewind();
    page.writeU15(records.size());

    for (int i = 0, l = records.size(); i < l; i += 1) {
      records.get(i).writeTo(page);
    }
  }
}
