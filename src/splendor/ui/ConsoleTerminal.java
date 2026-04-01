package splendor.ui;

import java.util.Scanner;

public class ConsoleTerminal {
    private final Scanner sc;

    public ConsoleTerminal() {
        sc = new Scanner(System.in);
    }

    void clearScreen() {
        System.out.print("\033[H\033[2J\033[3J");
        System.out.flush();
    }

    void waitForEnter() {
        System.out.print("  " + ConsoleFormatter.DIM + "Press Enter to continue..." + ConsoleFormatter.RESET);
        sc.nextLine();
    }

    String readLine() {
        return sc.nextLine();
    }

    int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(sc.nextLine().trim());
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf("  %sPlease enter a number between %d and %d.%s%n",
                        ConsoleFormatter.RED, min, max, ConsoleFormatter.RESET);
            } catch (NumberFormatException e) {
                System.out.printf("  %sInvalid input. Enter a number between %d and %d.%s%n",
                        ConsoleFormatter.RED, min, max, ConsoleFormatter.RESET);
            }
        }
    }
}
