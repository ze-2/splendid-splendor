package splendor.ui;

import splendor.engine.*;
import splendor.model.*;

import java.util.*;

public class ConsoleUI {
    private final Scanner sc;

    // ANSI colour codes
    private static final String RESET  = "\u001B[0m";
    private static final String BOLD   = "\u001B[1m";
    private static final String DIM    = "\u001B[2m";

    private static final String RED    = "\u001B[31m";
    private static final String GREEN  = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE   = "\u001B[34m";
    private static final String WHITE  = "\u001B[37m";
    private static final String BLACK_BG = "\u001B[40m";
    private static final String BRIGHT_WHITE = "\u001B[97m";

    // Box-drawing characters
    private static final String H_LINE = "\u2500";
    private static final String V_LINE = "\u2502";
    private static final String TL     = "\u250C";
    private static final String TR     = "\u2510";
    private static final String BL     = "\u2514";
    private static final String BR     = "\u2518";
    private static final String T_DOWN = "\u252C";
    private static final String T_UP   = "\u2534";
    private static final String T_RIGHT = "\u251C";
    private static final String T_LEFT  = "\u2524";

    public ConsoleUI() {
        sc = new Scanner(System.in);
    }

    // ── Colour helpers ──────────────────────────────────────────────

    private String gemColour(GemType gem) {
        switch (gem) {
            case RUBY:    return RED;
            case EMERALD: return GREEN;
            case SAPPHIRE:return BLUE;
            case DIAMOND: return BRIGHT_WHITE;
            case ONYX:    return BLACK_BG + BRIGHT_WHITE;
            case GOLD:    return YELLOW;
            default:      return RESET;
        }
    }

    private String gemSymbol(GemType gem) {
        switch (gem) {
            case RUBY:    return "\u25C6"; // diamond
            case EMERALD: return "\u25C6";
            case SAPPHIRE:return "\u25C6";
            case DIAMOND: return "\u25C6"; // filled diamond
            case ONYX:    return "\u25C7"; // hollow diamond
            case GOLD:    return "\u2605"; // star
            default:      return "\u25CF";
        }
    }

    private String colourGem(GemType gem) {
        return gemColour(gem) + BOLD + gemSymbol(gem) + " " + gem.name() + RESET;
    }

    private String colourGemShort(GemType gem, int count) {
        return gemColour(gem) + gemSymbol(gem) + " " + count + RESET;
    }

    private String horizontalLine(int width) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < width; i++) sb.append(H_LINE);
        return sb.toString();
    }

    private String centreText(String text, int width) {
        int pad = Math.max(0, width - text.length());
        int left = pad / 2;
        int right = pad - left;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < left; i++) sb.append(' ');
        sb.append(text);
        for (int i = 0; i < right; i++) sb.append(' ');
        return sb.toString();
    }

    private void printBoxHeader(String title, int width) {
        System.out.println(DIM + TL + horizontalLine(width - 2) + TR + RESET);
        System.out.println(DIM + V_LINE + RESET + BOLD
                + centreText(title, width - 2)
                + RESET + DIM + V_LINE + RESET);
        System.out.println(DIM + T_RIGHT + horizontalLine(width - 2) + T_LEFT + RESET);
    }

    private void printBoxFooter(int width) {
        System.out.println(DIM + BL + horizontalLine(width - 2) + BR + RESET);
    }

    private void printBoxLine(String content, int width) {
        System.out.println(DIM + V_LINE + RESET + " " + content);
    }

    private void printSectionDivider(String label) {
        System.out.println();
        System.out.println(DIM + "  " + horizontalLine(3) + " " + RESET
                + BOLD + label + RESET
                + DIM + " " + horizontalLine(30) + RESET);
    }

    // ── Display methods ─────────────────────────────────────────────
    
    public int displayMainMenu() {
        clearScreen();
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
        
        return readInt("  Menu select: ", 1, 2);
    }
    
    /**
     * Displays the current board.
     * @param board the board
     */
    public void displayBoard(Board board) {
        System.out.println();
        int boxWidth = 50;
        printBoxHeader("G A M E   B O A R D", boxWidth);

        // Gem bank
        printSectionDivider("Gem Bank");
        Map<GemType, Integer> bank = board.getGemBank();
        StringBuilder gemLine = new StringBuilder("  ");
        for (GemType gem : GemType.values()) {
            int count = bank.getOrDefault(gem, 0);
            gemLine.append(colourGemShort(gem, count)).append("  ");
        }
        System.out.println(gemLine);

        // Visible cards (highest level first)
        for (int level = 2; level >= 0; level--) {
            String levelLabel = "Level " + (level + 1);
            String stars = "";
            for (int s = 0; s <= level; s++) stars += "\u2605";
            printSectionDivider(levelLabel + " " + DIM + stars + RESET);

            Card[] row = board.getVisibleCards()[level];
            for (int slot = 0; slot < 4; slot++) {
                if (row[slot] != null) {
                    System.out.println(formatCardOption(slot + 1, row[slot]));
                } else {
                    System.out.printf("    %s[%d]%s %s(empty)%s%n",
                            DIM, slot + 1, RESET, DIM, RESET);
                }
            }
            int deckSize = board.getDecks()[level].size();
            System.out.printf("    %sDeck: %d remaining%s%n", DIM, deckSize, RESET);
        }

        // Nobles
        printSectionDivider("Nobles");
        List<Noble> nobles = board.getNobles();
        for (int i = 0; i < nobles.size(); i++) {
            System.out.println(formatNobleOption(i + 1, nobles.get(i)));
        }

        System.out.println();
        printBoxFooter(boxWidth);
    }

    /**
     * Displays a given player's status
     * @param player the player
     * @param isCurrPlayer whether the player being displayed is the current player.
     * Affects whether hidden cards are shown
     */
    public void displayPlayerStatus(Player player, boolean isCurrPlayer) {
        String tag = isCurrPlayer ? BOLD + " (You)" + RESET : "";
        System.out.printf("%n  %s%s%s%s%n", BOLD, player.getName(), RESET, tag);

        // Prestige points
        System.out.printf("    %sPrestige:%s %s%d%s%n",
                DIM, RESET, BOLD, player.getPrestigePoints(), RESET);

        // Gems
        System.out.print("    " + DIM + "Gems:    " + RESET);
        StringJoiner gemJoiner = new StringJoiner("  ");
        for (GemType gem : GemType.values()) {
            int count = player.getGems().getOrDefault(gem, 0);
            if (count > 0) {
                gemJoiner.add(colourGemShort(gem, count));
            }
        }
        System.out.println(gemJoiner.length() > 0 ? gemJoiner : DIM + "(none)" + RESET);

        // Bonuses
        System.out.print("    " + DIM + "Bonuses: " + RESET);
        StringJoiner bonusJoiner = new StringJoiner("  ");
        Map<GemType, Integer> bonuses = player.getBonusGems();
        for (Map.Entry<GemType, Integer> entry : bonuses.entrySet()) {
            if (entry.getValue() > 0) {
                bonusJoiner.add(colourGemShort(entry.getKey(), entry.getValue()));
            }
        }
        System.out.println(bonusJoiner.length() > 0 ? bonusJoiner : DIM + "(none)" + RESET);

        // Total
        System.out.print("    " + DIM + "Total:   " + RESET);
        StringJoiner totalJoiner = new StringJoiner("  ");
        for (GemType gem: GemType.values()) {
            int sum = player.getGems().getOrDefault(gem, 0) + bonuses.getOrDefault(gem, 0);
            if (sum > 0) {
                totalJoiner.add(colourGemShort(gem, sum));
            }
        }
        System.out.println(totalJoiner.length() > 0 ? totalJoiner : DIM + "(none)" + RESET);
        // Cards
        System.out.printf("    %sCards:   %s%d purchased%n",
                DIM, RESET, player.getPurchasedCardCount());

        // Reserved cards
        if (!player.getReservedCards().isEmpty()) {
            System.out.println("    " + DIM + "Reserved:" + RESET);
            List<Card> reserved = player.getReservedCards();
            for (int i = 0; i < reserved.size(); i++) {
                if (!isCurrPlayer && reserved.get(i).isHidden()) {
                    System.out.printf("      %s[%d]%s %s\u2587\u2587\u2587 Face Down%s%n",
                            DIM, i + 1, RESET, DIM, RESET);
                } else {
                    System.out.println("    " + formatCardOption(i + 1, reserved.get(i)));
                }
            }
        }

        // Nobles
        if (!player.getNobles().isEmpty()) {
            System.out.printf("    %sNobles:  %s%d%n",
                    DIM, RESET, player.getNobles().size());
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
        System.out.println(BOLD + horizontalLine(left) + header + horizontalLine(right) + RESET);
    }

    /**
     * Displays the winners
     * @param List<player> If equal prestige, lowest development card count wins.
     * If lowest development card count is equal, multiple winners.
     */
    public void displayWinner(List<Player> winners) {
        System.out.println();
        int boxWidth = 50;
        printBoxHeader("\u2605  G A M E   O V E R  \u2605", boxWidth);
        System.out.println();

        if (winners.size() == 1) {
            Player winner = winners.get(0);
            System.out.printf("    %s\u2605 %s wins! \u2605%s%n", BOLD, winner.getName(), RESET);
            System.out.printf("    %sPrestige: %d  |  Cards: %d%s%n",
                    DIM, winner.getPrestigePoints(), winner.getPurchasedCardCount(), RESET);
        } else {
            System.out.printf("    %s\u2605 Shared Victory! \u2605%s%n", BOLD, RESET);
            System.out.printf("    %sPrestige: %d  |  Cards: %d%s%n",
                    DIM, winners.get(0).getPrestigePoints(),
                    winners.get(0).getPurchasedCardCount(), RESET);
            for (Player w : winners) {
                System.out.printf("      %s\u2605%s %s%n", YELLOW, RESET, w.getName());
            }
        }

        System.out.println();
        printBoxFooter(boxWidth);
    }

    // ── Card & Noble formatters ─────────────────────────────────────

    private String formatCardOption(int index, Card card) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("    %s[%d]%s ", DIM, index, RESET));

        // Bonus gem colour
        sb.append(colourGem(card.getBonusGem()));

        // Prestige points
        if (card.getPrestigePoints() > 0) {
            sb.append(String.format("  %s%d pts%s", BOLD, card.getPrestigePoints(), RESET));
        } else {
            sb.append(String.format("  %s0 pts%s", DIM, RESET));
        }

        // Cost
        sb.append("  " + DIM + "Cost:" + RESET + " ");
        Map<GemType, Integer> cost = card.getCost();
        StringJoiner costJoiner = new StringJoiner(" ");
        for (Map.Entry<GemType, Integer> entry : cost.entrySet()) {
            if (entry.getValue() > 0) {
                costJoiner.add(colourGemShort(entry.getKey(), entry.getValue()));
            }
        }
        sb.append(costJoiner);

        return sb.toString();
    }

    private String formatNobleOption(int index, Noble noble) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("    %s[%d]%s ", DIM, index, RESET));
        sb.append(String.format("%s%d pts%s  ", BOLD, noble.getPrestigePoints(), RESET));
        sb.append(noble.getName() + " ");
        sb.append(DIM + "Requires:" + RESET + " ");

        Map<GemType, Integer> reqs = noble.getRequirements();
        StringJoiner reqJoiner = new StringJoiner(" ");
        for (Map.Entry<GemType, Integer> entry : reqs.entrySet()) {
            if (entry.getValue() > 0) {
                reqJoiner.add(colourGemShort(entry.getKey(), entry.getValue()));
            }
        }
        sb.append(reqJoiner);

        return sb.toString();
    }

    // ── Setup prompts ────────────────────────────────────────────────

    public int promptPlayerCount() {
        System.out.println();
        printSectionDivider("Game Setup");
        return readInt("  Enter number of players (2-4): ", 2, 4);
    }

    public String promptPlayerName(int index) {
        System.out.printf("  Enter name for Player %d: ", index + 1);
        String name = sc.nextLine().trim();
        while (name.isEmpty()) {
            System.out.print("  Name cannot be empty. Enter name: ");
            name = sc.nextLine().trim();
        }
        return name;
    }

    public String promptPlayerType(int index) {
        System.out.printf("  Is Player %d human or AI? (%shuman%s/%sai%s): ",
                index + 1, BOLD, RESET, DIM, RESET);
        while (true) {
            String type = sc.nextLine().trim().toLowerCase();
            if (type.equals("human") || type.equals("ai")) {
                return type;
            }
            System.out.print("  Invalid input. Enter 'human' or 'ai': ");
        }
    }

    // ── Action prompts ───────────────────────────────────────────────

    public ActionType promptAction(Player player, Board board, ActionValidator validator) {
        List<ActionType> available = validator.getAvailableActions(player, board);
        if (available.isEmpty()) {
            System.out.println("  " + DIM + "No actions available. Passing turn." + RESET);
            return null;
        }

        System.out.println();
        System.out.println("  " + BOLD + "Available Actions" + RESET);
        for (int i = 0; i < available.size(); i++) {
            System.out.printf("    %s[%d]%s %s%n",
                    DIM, i + 1, RESET, describeAction(available.get(i)));
        }
        System.out.println();

        int choice = readInt("  Choose action: ", 1, available.size());
        return available.get(choice - 1);
    }

    public Map<GemType, Integer> promptTake3Gems(Board board, ActionValidator validator) {
        List<GemType> availableGems = new ArrayList<>();
        for (GemType gem : GemType.values()) {
            if (gem == GemType.GOLD)
                continue;
            if (board.getGemBank().getOrDefault(gem, 0) > 0) {
                availableGems.add(gem);
            }
        }

        int toTake = Math.min(3, availableGems.size());

        System.out.println();
        System.out.println("  " + BOLD + "Take 3 Different Gems" + RESET);
        for (int i = 0; i < availableGems.size(); i++) {
            GemType gem = availableGems.get(i);
            int count = board.getGemBank().get(gem);
            System.out.printf("    %s[%d]%s %s  %s(%d available)%s%n",
                    DIM, i + 1, RESET, colourGem(gem), DIM, count, RESET);
        }
        System.out.printf("    %s[0]%s Go back%n", DIM, RESET);
        System.out.println();

        Map<GemType, Integer> selected = new EnumMap<>(GemType.class);
        for (int i = 0; i < toTake; i++) {
            while (true) {
                int choice = readInt(
                        String.format("  Choose gem %d of %d (0 to go back): ", i + 1, toTake),
                        0, availableGems.size());
                if (choice == 0) return null;
                GemType gem = availableGems.get(choice - 1);
                if (selected.containsKey(gem)) {
                    System.out.println("  " + RED + "Already selected. Choose a different gem." + RESET);
                    continue;
                }
                selected.put(gem, 1);
                break;
            }
        }
        return selected;
    }

    public GemType promptTake2Gems(Board board, ActionValidator validator) {
        List<GemType> eligible = new ArrayList<>();
        for (GemType gem : GemType.values()) {
            if (gem == GemType.GOLD)
                continue;
            if (board.getGemBank().getOrDefault(gem, 0) >= 4) {
                eligible.add(gem);
            }
        }

        System.out.println();
        System.out.println("  " + BOLD + "Take 2 Same-Colour Gems" + RESET);
        for (int i = 0; i < eligible.size(); i++) {
            GemType gem = eligible.get(i);
            int count = board.getGemBank().get(gem);
            System.out.printf("    %s[%d]%s %s  %s(%d available)%s%n",
                    DIM, i + 1, RESET, colourGem(gem), DIM, count, RESET);
        }
        System.out.printf("    %s[0]%s Go back%n", DIM, RESET);
        System.out.println();

        int choice = readInt("  Choose gem colour (0 to go back): ", 0, eligible.size());
        if (choice == 0) return null;
        return eligible.get(choice - 1);
    }

    public int[] promptReserveCard(Player player, Board board, ActionValidator validator) {
        System.out.println();
        System.out.println("  " + BOLD + "Reserve a Card" + RESET);
        List<int[]> options = new ArrayList<>();
        int idx = 1;

        for (int level = 2; level >= 0; level--) {
            String stars = "";
            for (int s = 0; s <= level; s++) stars += "\u2605";
            System.out.printf("    %sLevel %d %s%s%n", DIM, level + 1, stars, RESET);

            Card[] row = board.getVisibleCards()[level];
            for (int slot = 0; slot < 4; slot++) {
                if (row[slot] != null) {
                    System.out.println(formatCardOption(idx, row[slot]));
                    options.add(new int[] { level, slot });
                    idx++;
                }
            }
            if (!board.getDecks()[level].isEmpty()) {
                System.out.printf("    %s[%d]%s %sDeck (face-down, %d remaining)%s%n",
                        DIM, idx, RESET, DIM, board.getDecks()[level].size(), RESET);
                options.add(new int[] { level, -1 });
                idx++;
            }
        }
        System.out.printf("    %s[0]%s Go back%n", DIM, RESET);
        System.out.println();

        int choice = readInt("  Choose card to reserve (0 to go back): ", 0, options.size());
        if (choice == 0) return null;
        return options.get(choice - 1);
    }

    public Card promptBuyCard(Player player, Board board, ActionValidator validator) {
        System.out.println();
        System.out.println("  " + BOLD + "Buy a Card" + RESET);
        List<Card> options = new ArrayList<>();
        int idx = 1;

        Card[][] visible = board.getVisibleCards();
        for (int level = 0; level < 3; level++) {
            for (int slot = 0; slot < 4; slot++) {
                if (visible[level][slot] != null && validator.canBuy(player, visible[level][slot])) {
                    System.out.println(formatCardOption(idx, visible[level][slot]));
                    options.add(visible[level][slot]);
                    idx++;
                }
            }
        }

        if (!player.getReservedCards().isEmpty()) {
            boolean hasAffordable = false;
            for (Card card : player.getReservedCards()) {
                if (validator.canBuy(player, card)) {
                    if (!hasAffordable) {
                        System.out.println("    " + DIM + "Reserved:" + RESET);
                        hasAffordable = true;
                    }
                    System.out.println(formatCardOption(idx, card));
                    options.add(card);
                    idx++;
                }
            }
        }
        System.out.printf("    %s[0]%s Go back%n", DIM, RESET);
        System.out.println();

        int choice = readInt("  Choose card to buy (0 to go back): ", 0, options.size());
        if (choice == 0) return null;
        return options.get(choice - 1);
    }

    public Map<GemType, Integer> promptDiscardGems(Player player, int excess) {
        System.out.println();
        System.out.printf("  %s\u26A0 Gem Limit Exceeded%s%n", YELLOW + BOLD, RESET);
        System.out.printf("  You have %d gems (%d over the limit of 10).%n",
                player.getTotalGems(), excess);

        List<GemType> heldTypes = new ArrayList<>();
        for (GemType gem : GemType.values()) {
            if (player.getGems().getOrDefault(gem, 0) > 0) {
                heldTypes.add(gem);
            }
        }

        Map<GemType, Integer> toDiscard = new EnumMap<>(GemType.class);
        Map<GemType, Integer> tempGems = new EnumMap<>(player.getGems());
        int remaining = excess;

        while (remaining > 0) {
            System.out.printf("%n  Discard %d more gem%s:%n", remaining, remaining > 1 ? "s" : "");
            List<GemType> discardable = new ArrayList<>();
            for (GemType gem : heldTypes) {
                if (tempGems.getOrDefault(gem, 0) > 0) {
                    discardable.add(gem);
                }
            }
            for (int i = 0; i < discardable.size(); i++) {
                GemType gem = discardable.get(i);
                System.out.printf("    %s[%d]%s %s  %s(%d held)%s%n",
                        DIM, i + 1, RESET, colourGem(gem),
                        DIM, tempGems.get(gem), RESET);
            }

            int choice = readInt("  Choose gem to discard: ", 1, discardable.size());
            GemType chosen = discardable.get(choice - 1);
            toDiscard.merge(chosen, 1, Integer::sum);
            tempGems.merge(chosen, -1, Integer::sum);
            remaining--;
        }
        return toDiscard;
    }

    public Noble promptNobleChoice(List<Noble> eligible) {
        if (eligible.size() == 1) {
            return eligible.get(0);
        }
        System.out.println();
        System.out.println("  " + BOLD + "A Noble Visits!" + RESET);
        System.out.println("  Multiple nobles can visit you. Choose one:");
        for (int i = 0; i < eligible.size(); i++) {
            System.out.println(formatNobleOption(i + 1, eligible.get(i)));
        }
        System.out.println();

        int choice = readInt("  Choose noble: ", 1, eligible.size());
        return eligible.get(choice - 1);
    }

    // ── Utility ──────────────────────────────────────────────────────

    public void clearScreen() {
        System.out.print("\033[H\033[2J\033[3J");
        System.out.flush();
    }

    public void waitForEnter() {
        System.out.print("  " + DIM + "Press Enter to continue..." + RESET);
        sc.nextLine();
    }

    public int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(sc.nextLine().trim());
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf("  %sPlease enter a number between %d and %d.%s%n",
                        RED, min, max, RESET);
            } catch (NumberFormatException e) {
                System.out.printf("  %sInvalid input. Enter a number between %d and %d.%s%n",
                        RED, min, max, RESET);
            }
        }
    }

    private String describeAction(ActionType action) {
        switch (action) {
            case TAKE_THREE:
                return "Take 3 different gems";
            case TAKE_TWO:
                return "Take 2 gems of same colour";
            case RESERVE:
                return "Reserve a card";
            case BUY:
                return "Buy a card";
            default:
                return action.name();
        }
    }
}
