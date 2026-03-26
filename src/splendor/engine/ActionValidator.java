package splendor.engine;

import splendor.model.*;

import java.util.*;

/**
 * Stub — returns permissive defaults so GameEngine and ConsoleUI can compile.
 * P3 replaces this with real validation logic.
 */
public class ActionValidator {

    public List<ActionType> getAvailableActions(Player player, Board board) {
        return Arrays.asList(ActionType.values());
    }

    public boolean canTake3(Board board, Map<GemType, Integer> gems) {
        return true;
    }

    public boolean canTake2(Board board, GemType gem) {
        return true;
    }

    public boolean canReserve(Player player, Board board, int level, int slot) {
        return true;
    }

    public boolean canBuy(Player player, Card card) {
        return true;
    }

    public int mustDiscard(Player player) {
        int total = player.getTotalGems();
        return total > 10 ? total - 10 : 0;
    }
}
