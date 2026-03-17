package splendor.engine;

import splendor.model.Player;

import java.util.ArrayList;
import java.util.List;

public class WinChecker {

    public boolean hasTriggered(List<Player> players, int threshold) {
        for (Player player : players) {
            if (player.getPrestigePoints() >= threshold) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines the winner(s) using tie-break rules:
     * 1. Highest prestige points.
     * 2. Among tied players, fewest purchased cards.
     * 3. If still tied, shared victory.
     */
    public List<Player> getWinners(List<Player> players) {
        int maxPoints = 0;
        for (Player player : players) {
            maxPoints = Math.max(maxPoints, player.getPrestigePoints());
        }

        List<Player> top = new ArrayList<>();
        for (Player player : players) {
            if (player.getPrestigePoints() == maxPoints) {
                top.add(player);
            }
        }

        int minCards = Integer.MAX_VALUE;
        for (Player player : top) {
            minCards = Math.min(minCards, player.getPurchasedCardCount());
        }

        List<Player> winners = new ArrayList<>();
        for (Player player : top) {
            if (player.getPurchasedCardCount() == minCards) {
                winners.add(player);
            }
        }

        return winners;
    }
}
