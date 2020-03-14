package lv.paulsnar.edu.dip107p.md02.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
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
      printCommandList();
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

  private void printCommandList() {
    System.out.print(
      "[p] apskatīt grāmatas  [s] meklēt grāmatas  [h] palīdzība \n"
    + "[a] pievienot  [e] rediģēt  [d] dzēst  [c] izņemt/atgriezt \n"
    + (state.currentListing != null ? "[l] lappuse  " : "")
    + "[r] atskaite  [?] komandu saraksts  [q] iziet\n");
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
      printHelp();
    } else if (action == '?') {
      printCommandList();
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
          checkoutState = new CheckoutState(holderId, returnDate, books);
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
    while (iterator.hasNext()) {
      Book book = iterator.next();
      if (book.checkoutInfo == null) {
        inShelf += 1;
      } else {
        checkedOut += 1;
      }
    }
    System.out.printf("-- Grāmatas plauktā: %d, izņemtas: %s%n",
      inShelf, checkedOut);
  }

  private void printHelp() {
    System.out.print("--- Palīdzība ---\n"
  + "Lai izpildītu izvēlnē piedāvātu darbību, ievadiet simbolu, kurš norādīts\n"
  + "kvadrātiekavās pirms attiecīgās darbības.\n"
  + "\n"
  + "Atsevišķas darbības piedāvā saīsnes, kuras ļauj ātrāk paveikt darbību,\n"
  + "uzreiz izvēlnē norādot papildu argumentus pie komandas.\n"
  + "\n"
  + "[Apskatīšana]\n"
  + "Lai apskatītu visu grāmatu sarakstu, izvēlnē ievadiet komandu [p].\n"
  + "Ja vēlaties kārtot sarakstu, pēc p burta bez atstarpes norādiet\n"
  + "kārtošanas kritēriju:\n"
  + "  [i] kārtot pēc grāmatas numura\n"
  + "  [a] kārtot pēc autora (uzvārda, vārda)\n"
  + "  [n] kārtot pēc nosaukuma\n"
  + "Ja norādat kārtošanas kritēriju, pēc kritērija varat norādīt [+] vai [-]\n"
  + "kārtošanai, attiecīgi, augošā vai dilstošā secībā. Ja kārtošanas secība\n"
  + "nav norādīta, kārtošana tiek realizēta augošā virzienā; ja kārtošanas"
  + "kritērijs nav norādīts, kārtošana tiek realizēta pēc grāmatas numura.\n"
  + "Apskatīšanas laikā var ievadīt jebkuru no pārējām šeit redzamajām \n"
  + "komandām. Lai atvērtu nākamo rezultātu lapu, ievadiet komandu [l]."
  + "Piemēri:\n"
  + "  p            apskatīt grāmatas, kārtotas pēc to numura augoši\n"
  + "  pa           apskatīt grāmatas, kārtotas pēc autora augoši (A-Z)\n"
  + "  pn-          apskatīt grāmatas, kārtotas pēc nosaukuma dilstoši (Z-A)\n"
  + "\n"
  + "[Pievienošana]\n"
  + "Lai pievienotu jaunas grāmatas datubāzei, izvēlnē ievadiet komandu [a].\n"
  + "Ja vēlaties ievadīt tikai vienu grāmatu, varat ievadīt tās numuru pēc \n"
  + "komandas ar atstarpi.\n"
  + "Piemēri:\n"
  + "  a            atvērt pievienošanas režīmu\n"
  + "  a 6          pievienot jaunu grāmatu ar numuru 6\n"
  + "\n"
  + "[Rediģēšana]\n"
  + "Lai rediģētu vienu vai vairākas grāmatas (to autorus vai nosaukumus), \n"
  + "izvēlnē ievadiet komandu [e]. Ja vēlaties rediģēt tikai vienu grāmatu, \n"
  + "varat norādīt tās numuru pēc komandas ar atstarpi.\n"
  + "Piemēri:\n"
  + "  e            atvērt rediģēšanas režīmu\n"
  + "  e 1          rediģēt grāmatu ar numuru 1\n"
  + "\n"
  + "[Dzēšana]\n"
  + "Lai dzēstu vienu vai vairākas grāmatas, izvēlnē ievadiet komandu [d]. \n"
  + "Varat arī norādīt grāmatu numurus pēc komandas, atdalītus ar atstarpi.\n"
  + "Piemēri:\n"
  + "  d            atvērt dzēšanas režīmu\n"
  + "  d 1          dzēst grāmatu ar numuru 1\n"
  + "  d 1 2 3      dzēst grāmatas ar numuriem 1, 2 un 3\n"
  + "\n"
  + "[Izņemšana un atgriešana]\n"
  + "Lai atzīmētu vienu vai vairākas grāmatas kā izņemtas vai atgrieztas, \n"
  + "izvēlnē ievadiet komandu [c]. Šī komanda atbalsta nedaudz sarežģītāku\n"
  + "saīšņu sintaksi:\n"
  + "- Pēc komandas bez atstarpes var sekot viens burts, kas norāda režīmu:\n"
  + "  [i] Atzīmēt grāmatas kā atgrieztas plauktā\n"
  + "  [o] Atzīmēt grāmatas kā izņemtas\n"
  + "- Ja grāmatas tiek atgrieztas, pēc komandas uzskaita grāmatu numurus,\n"
  + "  atdalītus ar atstarpi\n"
  + "- Ja grāmatas tiek izņemtas, pēc komandas norāda lasītāja kartes numuru,\n"
  + "  tad atgriešanas datumu ISO 8601 formātā, tad grāmatu numurus, \n"
  + "  atdalītus ar atstarpi\n"
  + "Piemēri:\n"
  + "  c            atvērt grāmatu izņemšanas un atgriešanas režīmu\n"
  + "  ci 2 4       atzīmēt grāmatas 2 un 4 kā atgrieztas plauktā\n"
  + "  co 12345 2020-04-01 1 3 5\n"
  + "               atzīmēt, ka grāmatas 1, 3 un 5 ir izņēmis lasītājs \n"
  + "               nr. 12345 līdz 2020. gada 1. aprīlim\n"
  + "\n"
  + "[Meklēšana]\n"
  + "Lai meklētu grāmatas, izvēlnē norādiet komandu [s]. Uzreiz pēc komandas\n"
  + "iespējams arī norādīt, pēc kā vēlaties veikt meklēšanu, kā arī meklēto\n"
  + "terminu.\n"
  + "- Pēc komandas bez atstarpes var sekot burts, kas norāda kritēriju:\n"
  + "  [a] Meklēt pēc autora\n"
  + "  [n] Meklēt pēc grāmatas nosaukuma\n"
  + "- Pēc atstarpes seko meklētais termins (autors vai nosaukums)\n"
  + "Meklēšana tiek realizēta bez reģistrjutības.\n"
  + "Meklēšanas laikā var ievadīt jebkuru no pārējām šeit redzamajām\n"
  + "komandām. Lai atvērtu nākamo rezultātu lapu, ievadiet komandu [l].\n"
  + "Piemēri:\n"
  + "  s            Atvērt meklēšanas režīmu\n"
  + "  sa Skalbe    Meklēt grāmatas, kuru autors satur 'Skalbe'\n"
  + "  sn ābece     Meklēt grāmatas, kuru nosaukums satur 'ābece'\n");
  }

  private void doQuit() {
    System.out.println("Visu labu!");
    state.isDone = true;
  }

}
