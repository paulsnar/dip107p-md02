package lv.paulsnar.edu.dip107p.md02.ui;

import java.util.HashMap;
import java.util.Map;

class HelpMessages {
  private HelpMessages() { }

  private static final String
    PRINT_TITLE = "Apskatīšana",
    PRINT_SUMMARY = "p[ain][+-]   atlasīt grāmatas",
    PRINT_HANDBOOK =
    "Lai apskatītu visu grāmatu sarakstu, izvēlnē ievadiet komandu [p].\n"
  + "Ja vēlaties kārtot sarakstu, pēc p burta bez atstarpes norādiet\n"
  + "kārtošanas kritēriju:\n"
  + "  [i] kārtot pēc grāmatas numura (ID)\n"
  + "  [a] kārtot pēc autora (uzvārda, vārda)\n"
  + "  [n] kārtot pēc grāmatas nosaukuma\n"
  + "Ja norādat kārtošanas kritēriju, pēc kritērija varat norādīt [+] vai [-]\n"
  + "kārtošanai, attiecīgi, augošā vai dilstošā secībā. Ja kārtošanas secība\n"
  + "nav norādīta, kārtošana tiek realizēta augošā virzienā; ja nav norādīts\n"
  + "kārtošanas kritērijs, kārtošana tiek veikta pēc grāmatas numura.\n"
  + "Saraksta apskatīšanas laikā var ievadīt jebkuru no pārējām komandām. Lai\n"
  + "atvērtu nākamo rezultātu lapu, ievadiet komandu [l] vai nospiediet Enter.\n"
  + "\nPiemēri:\n"
  + "  p            apskatīt grāmatas, kārtotas pēc to numura augoši\n"
  + "  pa           apskatīt grāmatas, kārtotas pēc autora augoši (A-Z)\n"
  + "  pn-          apskatīt grāmatas, kārtotas pēc nosaukuma dilstoži (Z-A)\n",

    ADD_TITLE = "Pievienošana",
    ADD_SUMMARY = "a [numurs]   pievienot grāmatu",
    ADD_HANDBOOK =
    "Lai pievienotu jaunas grāmatas datubāzei, izvēlnē ievadiet komandu [a].\n"
  + "Ja vēlaties ievadīt tikai vienu grāmatu, varat ievadīt tās numuru pēc\n"
  + "komandas, atdalītu ar atstarpi.\n"
  + "\nPiemēri:\n"
  + "  a            atvērt pievienošanas režīmu\n"
  + "  a LK-1       pievienot jaunu grāmatu ar numuru LK-1\n",

    EDIT_TITLE = "Rediģēšana",
    EDIT_SUMMARY = "e [numurs]   rediģēt grāmatu",
    EDIT_HANDBOOK =
    "Lai rediģētu vienu vai vairākas grāmatas (to autorus vai nosaukumus),\n"
  + "izvēlnē ievadiet komandu [e]. Ja vēlaties rediģēt tikai vienu grāmatu,\n"
  + "varat norādīt tās numuru pēc komandas, atdalītu ar atstarpi.\n"
  + "\nPiemēri:\n"
  + "  e            atvērt rediģēšanas režīmu\n"
  + "  e LK-1       rediģēt grāmatu ar numuru LK-1\n",

    DELETE_TITLE = "Dzēšana",
    DELETE_SUMMARY = "d [num...]   dzēst grāmatas",
    DELETE_HANDBOOK =
    "Lai dzēstu vienu vai vairākas grāmatas, izvēlnē ievadiet komandu [d].\n"
  + "Varat arī norādīt grāmatu numurus pēc komandas, atdalītus ar atstarpi.\n"
  + "\nPiemēri:\n"
  + "  d            atvērt dzēšanas režīmu\n"
  + "  d LK-1       dzēst grāmatu ar numuru LK-1\n"
  + "  d L1 L2 L3   dzēst grāmatas ar numuriem L1, L2 un L3\n",

    CHECKMOD_TITLE = "Izņemšana un atgriešana",
    CHECKMOD_SUMMARY =
      "ci [num...]           atgriezt grāmatas\n"
    + "co [las dat num...]   izņemt grāmatas",
    CHECKMOD_HANDBOOK =
    "Lai atzīmētu vienu vai vairākas grāmatas kā izņemtas vai atgrieztas,\n"
  + "izvēlnē ievadiet komandu [c]. Šī komanda atbalsta paplašinātu saīšņu\n"
  + "sintaksi:\n"
  + "- Pēc komandas bez atstarpes var sekot viens burts, kas norāda režīmu:\n"
  + "  [i] Atzīmēt grāmatas kā atgrieztas plauktā\n"
  + "  [o] Atzīmēt grāmatas kā izņemtas\n"
  + "- Ja grāmatas tiek atgrieztas, pēc komandas varat norādīt grāmatu\n"
  + "  numurus, kurus atgriež, atdalītus ar atstarpi.\n"
  + "- Ja grāmatas tiek izņemtas, pēc komandas norāda lasītāja kartes numuru,\n"
  + "  tad atgriešanas datumu ISO 8601 formātā (GGGG-MM-DD), tad grāmatu\n"
  + "  numurus, atdalītus ar atstarpi.\n"
  + "\nPiemēri:\n"
  + "  c            atvērt grāmatu izņemšanas/atgriešanas režīmu\n"
  + "  ci L1 L2     atzīmēt grāmatas L1 un L2 kā atgrieztas plauktā\n"
  + "  co K42 2020-04-01 L1 L3 L5\n"
  + "               atzīmēt, ka grāmatas L1, L3 un L5 izņēmis lasītājs\n"
  + "               K42 līdz 2020. gada 1. aprīlim\n",

    SEARCH_TITLE = "Meklēšana",
    SEARCH_SUMMARY = "s[an] termins   meklēt",
    SEARCH_HANDBOOK =
    "Lai meklētu grāmatas, izvēlnē ievadiet komandu [s]. Uzreiz pēc komandas\n"
  + "iespējams arī norādīt, pēc kura kritērija vēlaties veikt meklēšanu, kā\n"
  + "arī meklēto terminu.\n"
  + "- Pēc komandas bez atstarpes var sekot burts, kas norāda kritēriju:\n"
  + "  [a] Meklēt pēc autora (uzvārda vai vārda)\n"
  + "  [n] Meklēt pēc grāmatas nosaukuma\n"
  + "- Pēc atstarpes seko mekētais termins (attiecīgi, autors vai nosaukums).\n"
  + "Meklēšana tiek realizēta bez reģistrjutības.\n"
  + "Meklēšanas laikā var ievadīt jebkuru citu komandu. Lai atvērtu nākamo\n"
  + "rezultātu lapu, ievadiet komandu [l] vai nospiediet Enter.\n"
  + "\nPiemēri:\n"
  + "  s            atvērt meklēšanas režīmu\n"
  + "  sa Skalbe    meklēt grāmatas, kuru autors satur 'Skalbe'\n"
  + "  sn ābece     meklēt grāmatas, kuru nosaukums satur 'ābece'\n",

    REPORT_TITLE = "Atskaite",
    REPORT_SUMMARY = "r   rādīt atskaiti",
    REPORT_HANDBOOK =
    "Lai attēlotu atskaiti par izsniegto un plauktā esošo grāmatu skaitu un\n"
  + "grāmatām, kurām pārsniegts atgriešanas termiņš, izvēlnē ievadiet\n"
  + "komandu [r].\n"
  + "\nPiemēri:\n"
  + "  r            attēlot atskaiti\n",

    HELP_TITLE = "Palīdzība",
    HELP_SUMMARY =
      "h[k]   parādīt rokasgrāmatu\n"
    + "?[k]   parādīt komandas kopsavilkumu",
    HELP_HANDBOOK =
    "Lai parādītu programmas lietošanas pamācību, izvēlnē ievadiet \n"
  + "komandu [h]. Lai uzzinātu par konkrētas komandas pielietojumu, pēc\n"
  + "komandas [h] bez atstarpes norādiet aplūkojamās komandas burtu.\n"
  + "Lai apskatītu īsu kopsavilkumu par komandas sintaksi, [h] komandas vietā\n"
  + "izmantojiet komandu [?]. Lai apskatītu sarakstu ar visām pieejamajām\n"
  + "komandām, pēc komandas [?] nenorādiet papildu burtu.\n"
  + "\nPiemēri:\n"
  + "  h            aplūkot rokasgrāmatu\n"
  + "  ha           aplūkot palīdzību komandai a\n"
  + "  ?            aplūkot komandu sarakstu\n"
  + "  ?a           aplūkot sintakses kopsavilkumu komandai a\n",

    COMMAND_HOLISTICUM =
      "[p] apskatīt grāmatas   [s] meklēt grāmatas   [h] palīdzība\n"
    + "[a] pievienot   [e] rediģēt   [d] dzēst   [c] izņemt/atgriezt\n"
    + "[r] atskaite   [?] īspalīdzība   [q] iziet",

    HANDBOOK_PROLOGUE =
    "--- Rokasgrāmata ---\n"
  + "Lai izpildītu izvēlnē piedāvātu darbību, ievadiet simbolu, kurš norādīts\n"
  + "kvadrātiekavās pirms attiecīgās darbības.\n\n"
  + "Atsevišķas darbības piedāvā saīsnes, kuras ļauj ātrāk paveikt darbību,\n"
  + "uzreiz izvēlnē norādot papildu argumentus pie komandas.\n";

  private static class CommandHelpData {
    public String title;
    public String summary;
    public String handbook;
    public CommandHelpData(String title, String summary, String handbook) {
      this.title = title;
      this.summary = summary;
      this.handbook = handbook;
    }
  }
  private static final Map<Character, CommandHelpData> helpData;
  static {
    helpData = new HashMap<>(8);

    helpData.put('p',
      new CommandHelpData(PRINT_TITLE, PRINT_SUMMARY, PRINT_HANDBOOK));
    helpData.put('a',
      new CommandHelpData(ADD_TITLE, ADD_SUMMARY, ADD_HANDBOOK));
    helpData.put('e',
      new CommandHelpData(EDIT_TITLE, EDIT_SUMMARY, EDIT_HANDBOOK));
    helpData.put('d',
      new CommandHelpData(DELETE_TITLE, DELETE_SUMMARY, DELETE_HANDBOOK));
    helpData.put('c', new CommandHelpData(
      CHECKMOD_TITLE, CHECKMOD_SUMMARY, CHECKMOD_HANDBOOK));
    helpData.put('s',
      new CommandHelpData(SEARCH_TITLE, SEARCH_SUMMARY, SEARCH_HANDBOOK));
    helpData.put('r',
      new CommandHelpData(REPORT_TITLE, REPORT_SUMMARY, REPORT_HANDBOOK));

    CommandHelpData helpHelpData = new CommandHelpData(
      HELP_TITLE, HELP_SUMMARY, HELP_HANDBOOK);
    helpData.put('h', helpHelpData);
    helpData.put('?', helpHelpData);
  }

  private static final char[] commandOrder =
    {'p', 'a', 'e', 'd', 'c', 's', 'r', 'h'};

  public static void printHelp() {
    System.out.println(HANDBOOK_PROLOGUE);
    for (char command : commandOrder) {
      CommandHelpData helpData = HelpMessages.helpData.get(command);
      System.out.printf("[%s]\n", helpData.title);
      System.out.println(helpData.handbook);
    }
  }

  public static void printHelp(char command) {
    CommandHelpData helpData = HelpMessages.helpData.get(command);
    if (helpData == null) {
      System.out.println("-- Nezināma komanda.");
    } else {
      System.out.printf("[%s]\n", helpData.title);
      System.out.println(helpData.handbook);
    }
  }

  public static void printSummary(char command) {
    CommandHelpData helpData = HelpMessages.helpData.get(command);
    if (helpData == null) {
      System.out.println("-- Nezināma komanda.");
    } else {
      System.out.println(helpData.summary);
    }
  }

  public static void printHolisticum() {
    System.out.println(COMMAND_HOLISTICUM);
  }
}
