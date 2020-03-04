package lv.paulsnar.edu.dip107p.md02.db;

import java.io.IOException;
import java.io.RandomAccessFile;

import lv.paulsnar.edu.dip107p.md02.util.ByteFormats;
import lv.paulsnar.edu.dip107p.md02.util.ParseException;

class WALRow {
  public static WALRow readFrom(RandomAccessFile file) throws IOException, ParseException {
    byte[] header = new byte[16];
    file.read(header);

    int optype, recordLength, startOffset, sequence;
    try {
      optype = ByteFormats.getU31(header, 0);
      recordLength = ByteFormats.getU31(header, 4);
      startOffset = ByteFormats.getU31(header, 8);
      sequence = ByteFormats.getU31(header, 12);
    } catch (ParseException exc) {
      throw new IOException("Malformed WAL row", exc);
    }

    Tuple data = Tuple.readFrom(file);
    data.offset = startOffset;
    return new WALRow(optype, recordLength, startOffset, sequence, data);
  }

  int optype, recordLength, startOffset, sequence;
  Tuple data;

  WALRow(
    int optype,
    int recordLength,
    int startOffset,
    int sequence,
    Tuple data
  ) {
    this.optype = optype;
    this.recordLength = recordLength;
    this.startOffset = startOffset;
    this.sequence = sequence;
    this.data = data;
  }

  public void writeTo(RandomAccessFile file) throws IOException, ParseException {
    byte[] header = new byte[16];
    try {
      ByteFormats.putU31(optype, header, 0);
      ByteFormats.putU31(recordLength, header, 4);
      ByteFormats.putU31(startOffset, header, 8);
      ByteFormats.putU31(sequence, header, 12);
    } catch (ParseException exc) {
      throw new RuntimeException("Value out-of-bounds", exc);
    }
    file.write(header);
    data.writeTo(file);
  }
}
