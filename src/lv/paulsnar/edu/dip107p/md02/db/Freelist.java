package lv.paulsnar.edu.dip107p.md02.db;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import lv.paulsnar.edu.dip107p.md02.util.ByteFormats;

class Freelist {
  private static final int HEADER_SIZE = 12;

  private WeakReference<Page> page;
  private WeakReference<Freelist> previous, next;
  private int previousPage = 0, nextPage = 0;

  private ArrayList<Integer> freePages;

  public Freelist(Page page) throws MalformedDatabaseException, IOException {
    this(page, null, null);
  }

  public Freelist(Page page, Freelist previous, Freelist next)
      throws MalformedDatabaseException, IOException {
    this.page = new WeakReference<>(page);

    int count = page.readU15();
    previousPage = page.readU31();
    nextPage = page.readU31();

    this.previous = new WeakReference<>(previous);
    this.next = new WeakReference<>(next);

    freePages = new ArrayList<>(count);
    int i = 0;
    while (i < count) {
      int freePage;
      try {
        freePage = page.readU31();
      } catch (PageBoundaryExceededException exc) {
        throw new MalformedDatabaseException(
          "Incorrect freelist page count", exc);
      }
      if (freePage == 0) {
        continue;
      } else {
        freePages.add(freePage);
        i += 1;
      }
    }
  }

  public int claim() {
    int pages = freePages.size();
    if (pages == 0) {
      return -1;
    }
    Integer page = freePages.get(pages - 1);
    return page.intValue();
  }
  public void put(int page) {
    freePages.add(page);
  }

  private Page page() {
    Page page = this.page.get();
    if (page == null) {
      throw new RuntimeException("Freelist outlived its page");
    }
    return page;
  }

  void linkPrevious(Freelist previous) {
    linkPrevious(previous, false);
  }
  private void linkPrevious(Freelist previous, boolean preventRecursion) {
    previousPage = (int) previous.page().number;
    this.previous = new WeakReference<>(previous);
    if ( ! preventRecursion) {
      previous.linkNext(this, true);
    }
  }
  void linkNext(Freelist next) {
    linkNext(next, false);
  }
  private void linkNext(Freelist next, boolean preventRecursion) {
    nextPage = (int) next.page().number;
    this.next = new WeakReference<>(next);
    if ( ! preventRecursion) {
      next.linkPrevious(this, true);
    }
  }

  public void writeTo(Page page) throws IOException {
    page.rewind();

    page.writeU31(0);
    if (previous.get() != null) {
      page.writeU31((int) previous.get().page().number);
    } else {
      page.writeU31(previousPage);
    }
    if (next.get() != null) {
      page.writeU31((int) next.get().page().number);
    } else {
      page.writeU31(previousPage);
    }

    int nonzeroPages = 0, totalPages = freePages.size();
    for (int i = 0; i < totalPages; i += 1) {
      Integer freePageMaybe = freePages.get(i);
      if (freePageMaybe == null) {
        continue;
      }
      int freePage = freePageMaybe.intValue();
      if (freePage == 0) {
        continue;
      }
      nonzeroPages += 1;
      page.writeU31(freePage);
    }

    page.rewind();
    page.writeU15(nonzeroPages);
  }
}
