package lv.paulsnar.edu.dip107p.md02.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import lv.paulsnar.edu.dip107p.md02.Book;
import lv.paulsnar.edu.dip107p.md02.Main;
import lv.paulsnar.edu.dip107p.md02.db.Database;

final public class UserDialogue implements AutoCloseable {

  private Scanner scanner;
  private State state = new State();
//  private StateExecutor executor = null;

  public UserDialogue(Database db) {
    scanner = new Scanner(System.in);
    state.db = db;
  }

  @Override
  public void close() {
    scanner.close();
  }

  public void run() throws IOException {
    if ( ! state.isInitialized) {
      printHeader();
      printCommandList("");
      state.isInitialized = true;
    }
    while ( ! state.isDone) {
      if (state.currentListing != null) {
        System.out.print("+> ");
      } else {
        System.out.print("> ");
      }
      if ( ! scanner.hasNextLine()) {
        System.out.println("q");
        doQuit();
        break;
      }
      String input = scanner.nextLine();
      if (state.currentListing == null && input.length() == 0) {
        continue;
      }
      processCommandLine(input);
    }
  }

  private void printHeader() {
    int entryCount = state.db.entryCount();
    System.out.printf("Grāmatu datubāzes pārvaldības programma v%s\n"
        + "%d ierakst%s\n\n",
        Main.VERSION, entryCount,
        (entryCount % 10 == 1 && entryCount % 100 != 11 ? "s" : "i"));
  }

  private void printList() {
    System.out.println();
    state.currentListing.print();
    if ( ! state.currentListing.hasMore()) {
      state.currentListing = null;
    }
  }

  private void processCommandLine(String input) {
    if (state.currentListing != null &&
        (input.length() == 0 || input.charAt(0) == 'l')) {
      printList();
      return;
    }

    char action = input.charAt(0);
    if (action == 'p') {
      processMenuPrint(input);
    } else if (action == 'a') {
      processMenuAdd(input);
    } else if (action == 'e') {
      processMenuEdit(input);
    } else if (action == 'd') {
      processMenuDelete(input);
    } else if (action == 'c') {
      processMenuCheckout(input);
    } else if (action == 's') {
      processMenuSearch(input);
    } else if (action == 'r') {
      printReport();
    } else if (action == 'h') {
      printHelp(input);
    } else if (action == '?') {
      printCommandList(input);
    } else if (action == 'q') {
      doQuit();
    } else {
      System.out.println("Neatpazīta komanda: " + action);
    }
  }

  private void processMenuPrint(String input) {
    ListingSorter.SortCriterion sortType = null;
    ListingSorter.SortDirection sortDirection = null;

    if (input.length() > 2) {
      char direction = input.charAt(2);
      if (direction == '+') {
        sortDirection = ListingSorter.SortDirection.ASC;
      } else if (direction == '-') {
        sortDirection = ListingSorter.SortDirection.DESC;
      } else {
        System.out.println(
            "-- Kārtošanas virziens " + direction + " nav atpazīts.");
      }
    }
    if (input.length() > 1) {
      char criterion = input.charAt(1);
      if (criterion == 'a') {
        sortType = ListingSorter.SortCriterion.AUTHOR;
      } else if (criterion == 'n') {
        sortType = ListingSorter.SortCriterion.TITLE;
      } else if (criterion == 'i') {
        sortType = ListingSorter.SortCriterion.ID;
      } else {
        System.out.println(
            "-- Kārtošanas kritērijs " + criterion + " nav atpazīts.");
      }
    }

    if (sortType == null) {
      sortDirection = null;
    }

    List<Book> books = new ArrayList<>(state.db.getAll());
    if (sortDirection != null) {
      ListingSorter.sortBy(books, sortType, sortDirection);
    } else if (sortType != null) {
      ListingSorter.sortBy(books, sortType);
    } else {
      ListingSorter.sortByDefault(books);
    }

    state.currentListing = new ListingPrinter(books);
    printList();
  }
  private void processMenuAdd(String input) {
    String[] tokens = input.split("\\s+");
    AddState addState;
    if (tokens.length == 1) {
      addState = new AddState();
    } else {
      addState = new AddState(tokens[1]);
    }
    addState.run(state, scanner);
  }
  private void processMenuEdit(String input) {
    String[] tokens = input.split("\\s+");
    EditState editState;
    if (tokens.length > 1) {
      String[] books = new String[tokens.length - 1];
      for (int i = 1; i < tokens.length; i += 1) {
        books[i - 1] = tokens[i];
      }
      editState = new EditState(books);
    } else {
      editState = new EditState();
    }
    editState.run(state, scanner);
  }
  private void processMenuDelete(String input) {
    String[] tokens = input.split("\\s+");
    DeleteState deleteState;
    if (tokens.length > 1) {
      String[] books = new String[tokens.length - 1];
      for (int i = 1; i < tokens.length; i += 1) {
        books[i - 1] = tokens[i];
      }
      deleteState = new DeleteState(books);
    } else {
      deleteState = new DeleteState();
    }
    deleteState.run(state, scanner);
  }
  private void processMenuCheckout(String input) {
    String[] tokens = input.split("\\s+");
    CheckoutState checkoutState = null;
    if (tokens[0].length() > 1) {
      char type = tokens[0].charAt(1);
      if (type == 'i') {
        String[] books = new String[tokens.length - 1];
        for (int i = 1; i < tokens.length; i += 1) {
          books[i - 1] = tokens[i];
        }
        checkoutState = new CheckoutState(books);
      } else if (type == 'o') {
        if (tokens.length < 4) {
          System.out.println("-- Nepietiekami daudz argumentu izņemšanai.");
        } else {
          String holderId = tokens[1];
          String returnDate = tokens[2];
          String[] books = new String[tokens.length - 3];
          for (int i = 3; i < tokens.length; i += 1) {
            books[i - 3] = tokens[i];
          }
          try {
            checkoutState = new CheckoutState(holderId, returnDate, books);
          } catch (RuntimeException exc) {
            System.out.println("-- Nepareizi norādīti izņemšanas argumenti.");
            return;
          }
        }
      } else {
        System.out.printf(
          "-- Grāmatas atzīmes veids '%c' nav atpazīts.\n", type);
      }
    }
    if (checkoutState == null) {
      checkoutState = new CheckoutState();
    }
    checkoutState.run(state, scanner);
  }
  private void processMenuSearch(String input) {
    SearchState.Criterion criterion = null;
    String criterionValue = null;

    String[] tokens = input.split("\\s+");
    String command = tokens[0];
    if (command.length() > 1) {
      char criterionType = command.charAt(1);
      if (criterionType == 'a') {
        criterion = SearchState.Criterion.AUTHOR;
      } else if (criterionType == 'n') {
        criterion = SearchState.Criterion.TITLE;
      } else {
        System.out.printf(
          "-- Meklēšanas kritērijs %c nav atpazīts.%n", criterionType);
      }
    }
    if (tokens.length > 1) {
      String[] criterionValuePieces = new String[tokens.length - 1];
      for (int i = 1; i < tokens.length; i += 1) {
        criterionValuePieces[i - 1] = tokens[i];
      }
      criterionValue = String.join(" ", criterionValuePieces);
    }

    SearchState searchState;
    if (criterion != null) {
      searchState = new SearchState(criterion, criterionValue);
    } else {
      searchState = new SearchState();
    }

    searchState.run(state, scanner);

    List<Book> books = searchState.results;
    if (books == null) {
      return;
    }
    ListingSorter.sortByDefault(books);
    state.currentListing = new ListingPrinter(books);
    printList();
  }

  private void printReport() {
    int checkedOut = 0, inShelf = 0;
    List<Book> books = state.db.getAll();
    Iterator<Book> iterator = books.iterator();
    List<Book> overdueBooks = new LinkedList<>();
    Calendar now = Calendar.getInstance();

    while (iterator.hasNext()) {
      Book book = iterator.next();
      if (book.checkoutInfo == null) {
        inShelf += 1;
      } else {
        checkedOut += 1;
      }

      if (book.checkoutInfo != null &&
          now.after(book.checkoutInfo.returnDate)) {
        overdueBooks.add(book);
      }
    }

    System.out.printf("-- Grāmatas plauktā: %d, izņemtas: %s%n",
      inShelf, checkedOut);

    if (overdueBooks.size() > 0) {
      System.out.printf("-- Kavētas grāmatas: %d\n", overdueBooks.size());
      state.currentListing = new ListingPrinter(overdueBooks);
      printList();
    }
  }

  private void printCommandList(String input) {
    if (input.length() > 1) {
      char command = input.charAt(1);
      HelpMessages.printSummary(command);
    } else {
      HelpMessages.printHolisticum();
    }
  }

  private void printHelp(String input) {
    if (input.length() > 1) {
      char command = input.charAt(1);
      HelpMessages.printHelp(command);
    } else {
      HelpMessages.printHelp();
    }
  }

  private void doQuit() {
    System.out.println("Visu labu!");
    state.isDone = true;
  }
}
