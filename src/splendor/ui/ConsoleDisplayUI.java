package splendor.ui;

import splendor.model.Board;
import splendor.model.Card;
import splendor.model.GemType;
import splendor.model.Noble;
import splendor.model.Player;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class ConsoleDisplayUI {
    private final ConsoleTerminal terminal;
    private final ConsoleFormatter formatter;

    public ConsoleDisplayUI(ConsoleTerminal terminal, ConsoleFormatter formatter) {
        this.terminal = terminal;
        this.formatter = formatter;
    }

    public void clearScreen() {
        terminal.clearScreen();
    }

    public void waitForEnter() {
        terminal.waitForEnter();
    }

    /**
     * Displays the current board.
     * @param board the board
     */
    public void displayBoard(Board board) {
        System.out.println();
        int boxWidth = 50;
        formatter.printBoxHeader("G A M E   B O A R D", boxWidth);

        // Gem bank
        formatter.printSectionDivider("Gem Bank");
        Map<GemType, Integer> bank = board.getGemBank();
        StringBuilder gemLine = new StringBuilder("  ");
        for (GemType gem : GemType.values()) {
            int count = bank.getOrDefault(gem, 0);
            gemLine.append(formatter.colourGemShort(gem, count)).append("  ");
        }
        System.out.println(gemLine);

        // Visible cards (highest level first)
        for (int level = 2; level >= 0; level--) {
            String levelLabel = "Level " + (level + 1);
            String stars = "";
            for (int s = 0; s <= level; s++) stars += "\u2605";
            formatter.printSectionDivider(levelLabel + " " + ConsoleFormatter.DIM + stars + ConsoleFormatter.RESET);

            Card[] row = board.getVisibleCards()[level];
            for (int slot = 0; slot < 4; slot++) {
                if (row[slot] != null) {
                    System.out.println(formatter.formatCardOption(slot + 1, row[slot]));
                } else {
                    System.out.printf("    %s[%d]%s %s(empty)%s%n",
                            ConsoleFormatter.DIM, slot + 1, ConsoleFormatter.RESET,
                            ConsoleFormatter.DIM, ConsoleFormatter.RESET);
                }
            }
            int deckSize = board.getDecks()[level].size();
            System.out.printf("    %sDeck: %d remaining%s%n",
                    ConsoleFormatter.DIM, deckSize, ConsoleFormatter.RESET);
        }

        // Nobles
        formatter.printSectionDivider("Nobles");
        List<Noble> nobles = board.getNobles();
        for (int i = 0; i < nobles.size(); i++) {
            System.out.println(formatter.formatNobleOption(i + 1, nobles.get(i)));
        }

        System.out.println();
        formatter.printBoxFooter(boxWidth);
    }

    /**
     * Displays a given player's status
     * @param player the player
     * @param isCurrPlayer whether the player being displayed is the current player.
     * Affects whether hidden cards are shown
     */
    public void displayPlayerStatus(Player player, boolean isCurrPlayer) {
        String tag = isCurrPlayer ? ConsoleFormatter.BOLD + " (You)" + ConsoleFormatter.RESET : "";
        System.out.printf("%n  %s%s%s%s%n", ConsoleFormatter.BOLD, player.getName(), ConsoleFormatter.RESET, tag);

        // Prestige points
        System.out.printf("    %sPrestige:%s %s%d%s%n",
                ConsoleFormatter.DIM, ConsoleFormatter.RESET,
                ConsoleFormatter.BOLD, player.getPrestigePoints(), ConsoleFormatter.RESET);

        // Gems
        System.out.print("    " + ConsoleFormatter.DIM + "Gems:    " + ConsoleFormatter.RESET);
        StringJoiner gemJoiner = new StringJoiner("  ");
        for (GemType gem : GemType.values()) {
            int count = player.getGems().getOrDefault(gem, 0);
            if (count > 0) {
                gemJoiner.add(formatter.colourGemShort(gem, count));
            }
        }
        System.out.println(gemJoiner.length() > 0 ? gemJoiner : ConsoleFormatter.DIM + "(none)" + ConsoleFormatter.RESET);

        // Bonuses
        System.out.print("    " + ConsoleFormatter.DIM + "Bonuses: " + ConsoleFormatter.RESET);
        StringJoiner bonusJoiner = new StringJoiner("  ");
        Map<GemType, Integer> bonuses = player.getBonusGems();
        for (Map.Entry<GemType, Integer> entry : bonuses.entrySet()) {
            if (entry.getValue() > 0) {
                bonusJoiner.add(formatter.colourGemShort(entry.getKey(), entry.getValue()));
            }
        }
        System.out.println(bonusJoiner.length() > 0 ? bonusJoiner : ConsoleFormatter.DIM + "(none)" + ConsoleFormatter.RESET);

        // Total
        System.out.print("    " + ConsoleFormatter.DIM + "Total:   " + ConsoleFormatter.RESET);
        StringJoiner totalJoiner = new StringJoiner("  ");
        for (GemType gem: GemType.values()) {
            int sum = player.getGems().getOrDefault(gem, 0) + bonuses.getOrDefault(gem, 0);
            if (sum > 0) {
                totalJoiner.add(formatter.colourGemShort(gem, sum));
            }
        }
        System.out.println(totalJoiner.length() > 0 ? totalJoiner : ConsoleFormatter.DIM + "(none)" + ConsoleFormatter.RESET);
        // Cards
        System.out.printf("    %sCards:   %s%d purchased%n",
                ConsoleFormatter.DIM, ConsoleFormatter.RESET, player.getPurchasedCardCount());

        // Reserved cards
        if (!player.getReservedCards().isEmpty()) {
            System.out.println("    " + ConsoleFormatter.DIM + "Reserved:" + ConsoleFormatter.RESET);
            List<Card> reserved = player.getReservedCards();
            for (int i = 0; i < reserved.size(); i++) {
                if (!isCurrPlayer && reserved.get(i).isHidden()) {
                    System.out.printf("        %s[%d]%s %s\u2587\u2587\u2587 Face Down%s%n",
                            ConsoleFormatter.DIM, i + 1, ConsoleFormatter.RESET,
                            ConsoleFormatter.DIM, ConsoleFormatter.RESET);
                } else {
                    System.out.println("    " + formatter.formatCardOption(i + 1, reserved.get(i)));
                }
            }
        }

        // Nobles
        if (!player.getNobles().isEmpty()) {
            System.out.printf("    %sNobles:  %s%d%n",
                    ConsoleFormatter.DIM, ConsoleFormatter.RESET, player.getNobles().size());
        }
    }

    /**
     * Displays turn header
     * @param player the player
     */
    public void displayTurnHeader(Player player) {
        System.out.println();
        String header = " " + player.getName() + "'s Turn ";
        int totalWidth = 44;
        int pad = Math.max(0, totalWidth - header.length());
        int left = pad / 2;
        int right = pad - left;
        System.out.println(ConsoleFormatter.BOLD + formatter.horizontalLine(left)
                + header + formatter.horizontalLine(right) + ConsoleFormatter.RESET);
    }

    /**
     * Displays the winners
     * @param List<player> If equal prestige, lowest development card count wins.
     * If lowest development card count is equal, multiple winners.
     */
    public void displayWinner(List<Player> winners) {
        System.out.println();
        int boxWidth = 50;
        formatter.printBoxHeader("\u2605  G A M E   O V E R  \u2605", boxWidth);
        System.out.println();

        if (winners.size() == 1) {
            Player winner = winners.get(0);
            System.out.printf("    %s\u2605 %s wins! \u2605%s%n",
                    ConsoleFormatter.BOLD, winner.getName(), ConsoleFormatter.RESET);
            System.out.printf("    %sPrestige: %d  |  Cards: %d%s%n",
                    ConsoleFormatter.DIM, winner.getPrestigePoints(),
                    winner.getPurchasedCardCount(), ConsoleFormatter.RESET);
        } else {
            System.out.printf("    %s\u2605 Shared Victory! \u2605%s%n",
                    ConsoleFormatter.BOLD, ConsoleFormatter.RESET);
            System.out.printf("    %sPrestige: %d  |  Cards: %d%s%n",
                    ConsoleFormatter.DIM, winners.get(0).getPrestigePoints(),
                    winners.get(0).getPurchasedCardCount(), ConsoleFormatter.RESET);
            for (Player w : winners) {
                System.out.printf("      %s\u2605%s %s%n",
                        ConsoleFormatter.YELLOW, ConsoleFormatter.RESET, w.getName());
            }
        }

        System.out.println();
        formatter.printBoxFooter(boxWidth);
    }
}
