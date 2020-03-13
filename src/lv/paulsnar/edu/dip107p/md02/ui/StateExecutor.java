package lv.paulsnar.edu.dip107p.md02.ui;

import java.util.Scanner;

interface StateExecutor {
  public void run(State state, Scanner sc);
}
