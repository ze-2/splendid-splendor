package splendor.ui;

import java.util.*;
import splendor.engine.*;
import splendor.model.*;

public class ConsoleUI {
    private final Scanner sc;

    public ConsoleUI() {
        sc = new Scanner(System.in);
    }

    /**
     * Displays the current board.
     * @param board the board
     */
    public void displayBoard(Board board) {
        System.out.println();
        System.out.println("========================================");
        System.out.println("              GAME BOARD                ");
        System.out.println("========================================");

        // Prints the Gem bank
        System.out.println("\n--- Gem Bank ---");
        Map<GemType, Integer> bank = board.getGemBank();
        for (GemType gem : GemType.values()) {
            System.out.printf("  %-10s: %d%n", gem, bank.getOrDefault(gem, 0));
        }

        // Prints the Visible cards (highest level first)
        for (int level = 2; level >= 0; level--) {
            System.out.printf("%n--- Level %d Cards ---%n", level + 1);
            Card[] row = board.getVisibleCards()[level];
            for (int slot = 0; slot < 4; slot++) {
                if (row[slot] != null) {
                    System.out.printf("  [%d] %s%n", slot + 1, row[slot]);
                } else {
                    System.out.printf("  [%d] (empty)%n", slot + 1);
                }
            }
            System.out.printf("  Deck: %d cards remaining%n", board.getDecks()[level].size());
        }

        // Prints the nobles
        System.out.println("\n--- Nobles ---");
        List<Noble> nobles = board.getNobles();
        for (int i = 0; i < nobles.size(); i++) {
            System.out.printf("  [%d] %s%n", i + 1, nobles.get(i));
        }
        System.out.println();
    }
    /**
     * Displays a given player's status
     * @param player the player
     * @param isCurrPlayer whether the player being displayed is the current player. 
     * Affects whether hidden cards are shown
     */
    public void displayPlayerStatus(Player player, boolean isCurrPlayer) {
        System.out.printf("%n--- " + player.getName() + "'s Status%s ---%n", isCurrPlayer ? "(You)" : " ");
        System.out.println("  Prestige Points: " + player.getPrestigePoints());

        //Gems printing
        System.out.print("  Gems: ");
        StringJoiner gemJoiner = new StringJoiner(", ");
        for (GemType gem : GemType.values()) {
            int count = player.getGems().getOrDefault(gem, 0);
            if (count > 0) {
                gemJoiner.add(gem + " = " + count);
            }
        }
        System.out.println(gemJoiner.length() > 0 ? gemJoiner : "(none)");

        //Print bonus gems from cards
        System.out.print("  Bonuses: ");
        StringJoiner bonusJoiner = new StringJoiner(", ");
        Map<GemType, Integer> bonuses = player.getBonusGems();
        for (Map.Entry<GemType, Integer> entry : bonuses.entrySet()) {
            if (entry.getValue() > 0) {
                bonusJoiner.add(entry.getKey() + "=" + entry.getValue());
            }
        }
        System.out.println(bonusJoiner.length() > 0 ? bonusJoiner : "(none)");

        //Print cards avail currently
        System.out.println("  Purchased Cards: " + player.getPurchasedCardCount());

        if (!player.getReservedCards().isEmpty()) {
            System.out.println("  Reserved Cards:");
            List<Card> reserved = player.getReservedCards();
            for (int i = 0; i < reserved.size(); i++) {
                if (!isCurrPlayer && reserved.get(i).isHidden()) {
                    System.out.printf("    [%d] %s%n", i + 1, "Card Face Down");
                } else {
                    System.out.printf("    [%d] %s%n", i + 1, reserved.get(i));
                }
            }
        }

        if (!player.getNobles().isEmpty()) {
            System.out.println("  Nobles: " + player.getNobles().size());
        }
        System.out.println();
    }

    /**
     * Displays turn header
     * @param player the player
     */
    public void displayTurnHeader(Player player) {
        System.out.println("=== " + player.getName() + "'s Turn ===");
    }

    /**
     * Displays the winners
     * @param List<player> If equal prestige, lowest development card count wins. 
     * If lowest development card count is equal, multiple winners.
     */
    public void displayWinner(List<Player> winners) {
        System.out.println("\n========================================");
        System.out.println("              GAME OVER!                ");
        System.out.println("========================================");
        if (winners.size() == 1) {
            Player winner = winners.get(0);
            System.out.printf("%s wins with %d prestige points and %d purchased cards!%n",
                    winner.getName(), winner.getPrestigePoints(), winner.getPurchasedCardCount());
        } else {
            System.out.println("Shared victory!");
            System.out.printf("  %d prestige points, %d purchased cards%n  Winners: %n",
                        winners.get(0).getPrestigePoints(), winners.get(0).getPurchasedCardCount());
            for (Player w : winners) {
                System.out.printf("  %s %n",
                        w.getName());
            }
        }
    }

    // ── Setup prompts ────────────────────────────────────────────────

    public int promptPlayerCount() {
        return readInt("Enter number of players (2-4): ", 2, 4);
    }

    public String promptPlayerName(int index) {
        System.out.printf("Enter name for Player %d: ", index);
        String name = sc.nextLine().trim();
        while (name.isEmpty()) {
            System.out.print("Name cannot be empty. Enter name: ");
            name = sc.nextLine().trim();
        }
        return name;
    }

    public String promptPlayerType(int index) {
        System.out.printf("Is Player %d human or AI? (human/ai): ", index);
        while (true) {
            String type = sc.nextLine().trim().toLowerCase();
            if (type.equals("human") || type.equals("ai")) {
                return type;
            }
            System.out.print("Invalid input. Enter 'human' or 'ai': ");
        }
    }

    // ── Action prompts ───────────────────────────────────────────────

    public ActionType promptAction(Player player, Board board, ActionValidator validator) {
        List<ActionType> available = validator.getAvailableActions(player, board);
        if (available.isEmpty()) {
            System.out.println("No actions available. Passing turn.");
            return null;
        }

        System.out.println("Available actions:");
        for (int i = 0; i < available.size(); i++) {
            System.out.printf("  [%d] %s%n", i + 1, describeAction(available.get(i)));
        }

        int choice = readInt("Choose action: ", 1, available.size());
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
        System.out.println("Available gems:");
        for (int i = 0; i < availableGems.size(); i++) {
            System.out.printf("  [%d] %s (%d available)%n",
                    i + 1, availableGems.get(i), board.getGemBank().get(availableGems.get(i)));
        }

        Map<GemType, Integer> selected = new EnumMap<>(GemType.class);
        for (int i = 0; i < toTake; i++) {
            while (true) {
                int choice = readInt(
                        String.format("Choose gem %d of %d: ", i + 1, toTake),
                        1, availableGems.size());
                GemType gem = availableGems.get(choice - 1);
                if (selected.containsKey(gem)) {
                    System.out.println("Already selected that gem. Choose a different one.");
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

        System.out.println("Gems with 4+ available:");
        for (int i = 0; i < eligible.size(); i++) {
            System.out.printf("  [%d] %s (%d available)%n",
                    i + 1, eligible.get(i), board.getGemBank().get(eligible.get(i)));
        }

        int choice = readInt("Choose gem colour: ", 1, eligible.size());
        return eligible.get(choice - 1);
    }

    public int[] promptReserveCard(Player player, Board board, ActionValidator validator) {
        System.out.println("Reserve a card:");
        List<int[]> options = new ArrayList<>();
        int idx = 1;

        for (int level = 2; level >= 0; level--) {
            Card[] row = board.getVisibleCards()[level];
            for (int slot = 0; slot < 4; slot++) {
                if (row[slot] != null) {
                    System.out.printf("  [%d] Level %d, Slot %d: %s%n",
                            idx, level + 1, slot + 1, row[slot]);
                    options.add(new int[] { level, slot });
                    idx++;
                }
            }
            if (!board.getDecks()[level].isEmpty()) {
                System.out.printf("  [%d] Level %d deck (face-down, %d remaining)%n",
                        idx, level + 1, board.getDecks()[level].size());
                options.add(new int[] { level, -1 });
                idx++;
            }
        }

        int choice = readInt("Choose card to reserve: ", 1, options.size());
        return options.get(choice - 1);
    }

    public Card promptBuyCard(Player player, Board board, ActionValidator validator) {
        System.out.println("Affordable cards:");
        List<Card> options = new ArrayList<>();
        int idx = 1;

        Card[][] visible = board.getVisibleCards();
        for (int level = 0; level < 3; level++) {
            for (int slot = 0; slot < 4; slot++) {
                if (visible[level][slot] != null && validator.canBuy(player, visible[level][slot])) {
                    System.out.printf("  [%d] Level %d, Slot %d: %s%n",
                            idx, level + 1, slot + 1, visible[level][slot]);
                    options.add(visible[level][slot]);
                    idx++;
                }
            }
        }

        for (int i = 0; i < player.getReservedCards().size(); i++) {
            Card card = player.getReservedCards().get(i);
            if (validator.canBuy(player, card)) {
                System.out.printf("  [%d] Reserved: %s%n", idx, card);
                options.add(card);
                idx++;
            }
        }

        int choice = readInt("Choose card to buy: ", 1, options.size());
        return options.get(choice - 1);
    }

    public Map<GemType, Integer> promptDiscardGems(Player player, int excess) {
        System.out.printf("You have %d gems, which is %d over the limit of 10.%n",
                player.getTotalGems(), excess);
        System.out.println("Your gems:");

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
            System.out.printf("Select a gem to discard (%d more to discard):%n", remaining);
            List<GemType> discardable = new ArrayList<>();
            for (GemType gem : heldTypes) {
                if (tempGems.getOrDefault(gem, 0) > 0) {
                    discardable.add(gem);
                }
            }
            for (int i = 0; i < discardable.size(); i++) {
                System.out.printf("  [%d] %s (%d held)%n",
                        i + 1, discardable.get(i), tempGems.get(discardable.get(i)));
            }

            int choice = readInt("Choose gem: ", 1, discardable.size());
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
        System.out.println("Multiple nobles can visit you! Choose one:");
        for (int i = 0; i < eligible.size(); i++) {
            System.out.printf("  [%d] %s%n", i + 1, eligible.get(i));
        }

        int choice = readInt("Choose noble: ", 1, eligible.size());
        return eligible.get(choice - 1);
    }

    // ── Utility ──────────────────────────────────────────────────────

    public void clearScreen() {
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
    }

    public void waitForEnter() {
        System.out.print("Press Enter to continue...");
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
                System.out.printf("Please enter a number between %d and %d.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.printf("Invalid input. Please enter a number between %d and %d.%n", min, max);
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
