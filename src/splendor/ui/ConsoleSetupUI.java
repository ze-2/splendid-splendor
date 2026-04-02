package splendor.ui;

public class ConsoleSetupUI {
    private final ConsoleTerminal terminal;
    private final ConsoleFormatter formatter;

    public ConsoleSetupUI(ConsoleTerminal terminal, ConsoleFormatter formatter) {
        this.terminal = terminal;
        this.formatter = formatter;
    }

    public int displayMainMenu() {
        terminal.clearScreen();
        System.out.println("  ____  ____  _      _____ _   _ ____  _ ____  ");
        System.out.println(" / ___||  _ \\| |    | ____| \\ | |  _ \\(_)  _ \\ ");
        System.out.println(" \\___ \\| |_) | |    |  _| |  \\| | | | | | | | |");
        System.out.println("  ___) |  __/| |___ | |___| |\\  | |_| | | |_| |");
        System.out.println(" |____/|_|   |_____||_____|_| \\_|____/|_|____/ ");
        System.out.println("  ____  ____  _      _____ _   _ ____   ___  ____ ");
        System.out.println(" / ___||  _ \\| |    | ____| \\ | |  _ \\ / _ \\|  _ \\");
        System.out.println(" \\___ \\| |_) | |    |  _| |  \\| | | | | | | | |_) |");
        System.out.println("  ___) |  __/| |___ | |___| |\\  | |_| | |_| |  _ < ");
        System.out.println(" |____/|_|   |_____||_____|_| \\_|____/ \\___/|_| \\_\\");
        System.out.println("");
        System.out.println("                    .---.");
        System.out.println("                    |[#]|");
        System.out.println("                    '---'");
        System.out.println("");
        System.out.println("               [ 1 ]  S T A R T");
        System.out.println("               [ 2 ]  E X I T");

        return terminal.readInt("  Menu select: ", 1, 2);
    }

    // â”€â”€ Setup prompts â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public int promptPlayerCount() {
        System.out.println();
        formatter.printSectionDivider("Game Setup");
        return terminal.readInt("  Enter number of players (2-4): ", 2, 4);
    }

    public String promptPlayerName(int index) {
        System.out.printf("  Enter name for Player %d: ", index + 1);
        String name = terminal.readLine().trim();
        while (name.isEmpty()) {
            System.out.print("  Name cannot be empty. Enter name: ");
            name = terminal.readLine().trim();
        }
        return name;
    }

    public String promptPlayerType(int index) {
        System.out.printf("  Is Player %d human or AI? (%shuman%s/%sai%s): ",
                index + 1, ConsoleFormatter.BOLD, ConsoleFormatter.RESET,
                ConsoleFormatter.DIM, ConsoleFormatter.RESET);
        while (true) {
            String type = terminal.readLine().trim().toLowerCase();
            if (type.equals("human") || type.equals("ai")) {
                return type;
            }
            System.out.print("  Invalid input. Enter 'human' or 'ai': ");
        }
    }
}
