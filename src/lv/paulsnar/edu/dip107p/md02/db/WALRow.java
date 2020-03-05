package lv.paulsnar.edu.dip107p.md02.db;

import java.io.IOException;
import java.io.RandomAccessFile;

import lv.paulsnar.edu.dip107p.md02.util.ByteFormats;
import lv.paulsnar.edu.dip107p.md02.util.ParseException;

class WALRow {
  public static WALRow readFrom(RandomAccessFile file) throws IOException {
    byte[] header = new byte[4];
    int read = file.read(header);
    if (read == -1) {
      return null;
    }

    int sequence;
    Op op;
    try {
      int optype = ByteFormats.getU15(header, 0);
      op = Op.byValue(optype);
      if (op == null) {
        throw new IOException("Bad op value: " + optype);
      }
      sequence = ByteFormats.getU15(header, 2);

      Tuple data = Tuple.readFrom(file);
      return new WALRow(op, sequence, data);
    } catch (ParseException exc) {
      throw new IOException("Malformed WAL row", exc);
    }
  }

  enum Op {
    INSERT (1),
    EDIT (2),
    DELETE (3);

    int value;
    private Op(int value) {
      this.value = value;
    }
    static Op byValue(int value) {
      switch (value) {
        case 1: return Op.INSERT;
        case 2: return Op.EDIT;
        case 3: return Op.DELETE;
        default: return null;
      }
    }
  }

  Op op;
  int sequence, id = -1;
  Tuple data = null;

  WALRow(Op op, int sequence, Tuple data) {
    this.op = op;
    this.sequence = sequence;
    this.data = data;
  }
  WALRow(Op op, int sequence, int id) {
    this.op = op;
    this.sequence = sequence;
    this.id = id;
  }

  public static WALRow insert(int sequence, Tuple data) {
    return new WALRow(Op.INSERT, sequence, data);
  }
  public static WALRow edit(int sequence, Tuple data) {
    return new WALRow(Op.EDIT, sequence, data);
  }
  public static WALRow delete(int sequence, int id) {
    return new WALRow(Op.DELETE, sequence, id);
  }

  public void writeTo(RandomAccessFile file) throws IOException, ParseException {
    try {
      ByteFormats.writeU15(op.value, file);
      ByteFormats.writeU15(sequence, file);
      if (op == Op.DELETE) {
        ByteFormats.writeVarint(data.id, file);
      } else {
        data.writeTo(file);
      }
    } catch (ParseException exc) {
      throw new RuntimeException("Value out-of-bounds", exc);
    }
  }
}
