package splendor.ui;

import splendor.engine.ActionValidator;
import splendor.model.ActionType;
import splendor.model.Board;
import splendor.model.Card;
import splendor.model.GemType;
import splendor.model.Noble;
import splendor.model.Player;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ConsoleActionUI {
    private final ConsoleTerminal terminal;
    private final ConsoleFormatter formatter;

    public ConsoleActionUI(ConsoleTerminal terminal, ConsoleFormatter formatter) {
        this.terminal = terminal;
        this.formatter = formatter;
    }

    public ActionType promptUserAction(Player player, Board board, ActionValidator validator) {
        List<ActionType> available = validator.getAvailableActions(player, board);
        if (available.isEmpty()) {
            System.out.println("  " + ConsoleFormatter.DIM + "No actions available. Passing turn." + ConsoleFormatter.RESET);
            return null;
        }

        System.out.println();
        System.out.println("  " + ConsoleFormatter.BOLD + "Available Actions" + ConsoleFormatter.RESET);
        for (int i = 0; i < available.size(); i++) {
            System.out.printf("    %s[%d]%s %s%n",
                    ConsoleFormatter.DIM, i + 1, ConsoleFormatter.RESET, describeAction(available.get(i)));
        }
        System.out.println();

        int choice = terminal.readInt("  Choose action: ", 1, available.size());
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
        System.out.println("  " + ConsoleFormatter.BOLD + "Take 3 Different Gems" + ConsoleFormatter.RESET);
        for (int i = 0; i < availableGems.size(); i++) {
            GemType gem = availableGems.get(i);
            int count = board.getGemBank().get(gem);
            System.out.printf("    %s[%d]%s %s  %s(%d available)%s%n",
                    ConsoleFormatter.DIM, i + 1, ConsoleFormatter.RESET,
                    formatter.colourGem(gem), ConsoleFormatter.DIM, count, ConsoleFormatter.RESET);
        }
        System.out.printf("    %s[0]%s Go back%n", ConsoleFormatter.DIM, ConsoleFormatter.RESET);
        System.out.println();

        Map<GemType, Integer> selected = new EnumMap<>(GemType.class);
        for (int i = 0; i < toTake; i++) {
            while (true) {
                int choice = terminal.readInt(
                        String.format("  Choose gem %d of %d (0 to go back): ", i + 1, toTake),
                        0, availableGems.size());
                if (choice == 0) return null;
                GemType gem = availableGems.get(choice - 1);
                if (selected.containsKey(gem)) {
                    System.out.println("  " + ConsoleFormatter.RED + "Already selected. Choose a different gem." + ConsoleFormatter.RESET);
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
        System.out.println("  " + ConsoleFormatter.BOLD + "Take 2 Same-Colour Gems" + ConsoleFormatter.RESET);
        for (int i = 0; i < eligible.size(); i++) {
            GemType gem = eligible.get(i);
            int count = board.getGemBank().get(gem);
            System.out.printf("    %s[%d]%s %s  %s(%d available)%s%n",
                    ConsoleFormatter.DIM, i + 1, ConsoleFormatter.RESET,
                    formatter.colourGem(gem), ConsoleFormatter.DIM, count, ConsoleFormatter.RESET);
        }
        System.out.printf("    %s[0]%s Go back%n", ConsoleFormatter.DIM, ConsoleFormatter.RESET);
        System.out.println();

        int choice = terminal.readInt("  Choose gem colour (0 to go back): ", 0, eligible.size());
        if (choice == 0) return null;
        return eligible.get(choice - 1);
    }

    public int[] promptReserveCard(Player player, Board board, ActionValidator validator) {
        System.out.println();
        System.out.println("  " + ConsoleFormatter.BOLD + "Reserve a Card" + ConsoleFormatter.RESET);
        List<int[]> options = new ArrayList<>();
        int idx = 1;

        for (int level = 2; level >= 0; level--) {
            String stars = "";
            for (int s = 0; s <= level; s++) stars += "\u2605";
            System.out.printf("    %sLevel %d %s%s%n",
                    ConsoleFormatter.DIM, level + 1, stars, ConsoleFormatter.RESET);

            Card[] row = board.getVisibleCards()[level];
            for (int slot = 0; slot < 4; slot++) {
                if (row[slot] != null) {
                    System.out.println(formatter.formatCardOption(idx, row[slot]));
                    options.add(new int[] { level, slot });
                    idx++;
                }
            }
            if (!board.getDecks()[level].isEmpty()) {
                System.out.printf("    %s[%d]%s %sDeck (face-down, %d remaining)%s%n",
                        ConsoleFormatter.DIM, idx, ConsoleFormatter.RESET,
                        ConsoleFormatter.DIM, board.getDecks()[level].size(), ConsoleFormatter.RESET);
                options.add(new int[] { level, -1 });
                idx++;
            }
        }
        System.out.printf("    %s[0]%s Go back%n", ConsoleFormatter.DIM, ConsoleFormatter.RESET);
        System.out.println();

        int choice = terminal.readInt("  Choose card to reserve (0 to go back): ", 0, options.size());
        if (choice == 0) return null;
        return options.get(choice - 1);
    }

    public Card promptBuyCard(Player player, Board board, ActionValidator validator) {
        System.out.println();
        System.out.println("  " + ConsoleFormatter.BOLD + "Buy a Card" + ConsoleFormatter.RESET);
        List<Card> options = new ArrayList<>();
        int idx = 1;

        Card[][] visible = board.getVisibleCards();
        for (int level = 0; level < 3; level++) {
            for (int slot = 0; slot < 4; slot++) {
                if (visible[level][slot] != null && validator.canBuy(player, visible[level][slot])) {
                    System.out.println(formatter.formatCardOption(idx, visible[level][slot]));
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
                        System.out.println("    " + ConsoleFormatter.DIM + "Reserved:" + ConsoleFormatter.RESET);
                        hasAffordable = true;
                    }
                    System.out.println(formatter.formatCardOption(idx, card));
                    options.add(card);
                    idx++;
                }
            }
        }
        System.out.printf("    %s[0]%s Go back%n", ConsoleFormatter.DIM, ConsoleFormatter.RESET);
        System.out.println();

        int choice = terminal.readInt("  Choose card to buy (0 to go back): ", 0, options.size());
        if (choice == 0) return null;
        return options.get(choice - 1);
    }

    public Map<GemType, Integer> promptDiscardGems(Player player, int excess) {
        System.out.println();
        System.out.printf("  %s\u26A0 Gem Limit Exceeded%s%n",
                ConsoleFormatter.YELLOW + ConsoleFormatter.BOLD, ConsoleFormatter.RESET);
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
                        ConsoleFormatter.DIM, i + 1, ConsoleFormatter.RESET,
                        formatter.colourGem(gem), ConsoleFormatter.DIM,
                        tempGems.get(gem), ConsoleFormatter.RESET);
            }

            int choice = terminal.readInt("  Choose gem to discard: ", 1, discardable.size());
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
        System.out.println("  " + ConsoleFormatter.BOLD + "A Noble Visits!" + ConsoleFormatter.RESET);
        System.out.println("  Multiple nobles can visit you. Choose one:");
        for (int i = 0; i < eligible.size(); i++) {
            System.out.println(formatter.formatNobleOption(i + 1, eligible.get(i)));
        }
        System.out.println();

        int choice = terminal.readInt("  Choose noble: ", 1, eligible.size());
        return eligible.get(choice - 1);
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
