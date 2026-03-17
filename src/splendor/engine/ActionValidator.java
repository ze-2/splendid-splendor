package splendor.engine;

import splendor.model.ActionType;
import splendor.model.Board;
import splendor.model.Card;
import splendor.model.GemType;
import splendor.model.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ActionValidator {

    /**
     * Validates a take-3-different-gems action.
     * Each selected colour must be different (value 1), present in bank, and not gold.
     * The player must take exactly min(3, availableNonGoldColors).
     */
    public boolean canTake3(Board board, Map<GemType, Integer> selected) {
        if (selected.isEmpty()) {
            return false;
        }

        int totalSelected = 0;
        for (Map.Entry<GemType, Integer> entry : selected.entrySet()) {
            if (entry.getKey() == GemType.GOLD) return false;
            if (entry.getValue() != 1) return false;
            if (board.getGemBank().getOrDefault(entry.getKey(), 0) < 1) return false;
            totalSelected++;
        }

        if (totalSelected > 3) {
            return false;
        }

        int availableColors = countAvailableColors(board);
        return totalSelected == Math.min(3, availableColors);
    }

    /**
     * Validates a take-2-same-colour action.
     * The chosen colour must have >= 4 tokens in the bank and must not be gold.
     */
    public boolean canTake2(Board board, GemType gem) {
        if (gem == GemType.GOLD) return false;
        return board.getGemBank().getOrDefault(gem, 0) >= 4;
    }

    /**
     * Validates a reserve action.
     * Player must have fewer than 3 reserved cards and the target card/deck must exist.
     * Level is 0-indexed. Slot -1 means from the face-down deck.
     */
    public boolean canReserve(Player player, Board board, int level, int slot) {
        if (player.getReservedCards().size() >= 3) return false;
        if (level < 0 || level > 2) return false;
        if (slot == -1) {
            return !board.getDecks()[level].isEmpty();
        }
        if (slot < 0 || slot > 3) return false;
        return board.getVisibleCards()[level][slot] != null;
    }

    /**
     * Validates whether a player can afford a card (gems + bonuses + gold).
     */
    public boolean canBuy(Player player, Card card) {
        if (card == null) return false;
        Map<GemType, Integer> bonuses = player.getBonusGems();
        int goldAvailable = player.getGems().getOrDefault(GemType.GOLD, 0);
        int goldNeeded = 0;

        for (Map.Entry<GemType, Integer> entry : card.getCost().entrySet()) {
            GemType gem = entry.getKey();
            int required = entry.getValue();
            int bonus = bonuses.getOrDefault(gem, 0);
            int remaining = Math.max(0, required - bonus);
            int fromTokens = player.getGems().getOrDefault(gem, 0);
            int deficit = Math.max(0, remaining - fromTokens);
            goldNeeded += deficit;
        }

        return goldNeeded <= goldAvailable;
    }

    /**
     * Returns the number of gems over the 10-token limit, or 0 if within limit.
     */
    public int mustDiscard(Player player) {
        return Math.max(0, player.getTotalGems() - 10);
    }

    /**
     * Returns which of the four action types are currently legal for the player.
     */
    public List<ActionType> getAvailableActions(Player player, Board board) {
        List<ActionType> actions = new ArrayList<>();

        // TAKE_THREE: at least one non-gold colour has >= 1 in bank
        if (countAvailableColors(board) > 0) {
            actions.add(ActionType.TAKE_THREE);
        }

        // TAKE_TWO: at least one non-gold colour has >= 4 in bank
        for (GemType gem : GemType.values()) {
            if (gem == GemType.GOLD) continue;
            if (board.getGemBank().getOrDefault(gem, 0) >= 4) {
                actions.add(ActionType.TAKE_TWO);
                break;
            }
        }

        // RESERVE: player has < 3 reserved and at least one card/deck exists
        if (player.getReservedCards().size() < 3) {
            if (hasAnyCardOrDeck(board)) {
                actions.add(ActionType.RESERVE);
            }
        }

        // BUY: player can afford at least one visible or reserved card
        if (canBuyAny(player, board)) {
            actions.add(ActionType.BUY);
        }

        return actions;
    }

    private int countAvailableColors(Board board) {
        int count = 0;
        for (GemType gem : GemType.values()) {
            if (gem == GemType.GOLD) continue;
            if (board.getGemBank().getOrDefault(gem, 0) > 0) {
                count++;
            }
        }
        return count;
    }

    private boolean hasAnyCardOrDeck(Board board) {
        for (int level = 0; level < 3; level++) {
            if (!board.getDecks()[level].isEmpty()) return true;
            for (int slot = 0; slot < 4; slot++) {
                if (board.getVisibleCards()[level][slot] != null) return true;
            }
        }
        return false;
    }

    private boolean canBuyAny(Player player, Board board) {
        Card[][] visible = board.getVisibleCards();
        for (int level = 0; level < 3; level++) {
            for (int slot = 0; slot < 4; slot++) {
                if (visible[level][slot] != null && canBuy(player, visible[level][slot])) {
                    return true;
                }
            }
        }
        for (Card card : player.getReservedCards()) {
            if (canBuy(player, card)) return true;
        }
        return false;
    }
}
