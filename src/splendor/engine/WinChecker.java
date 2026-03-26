package splendor.engine;

import splendor.model.*;

import java.util.*;

/**
 * Stub — P1 replaces with full implementation.
 * Determines winners using tie-break logic: max prestige, then fewest purchased cards.
 */
public class WinChecker {

    public boolean hasTriggered(List<Player> players, int threshold) {
        for (Player p : players) {
            if (p.getPrestigePoints() >= threshold) {
                return true;
            }
        }
        return false;
    }

    public List<Player> getWinners(List<Player> players) {
        // Find max prestige
        int maxPrestige = 0;
        for (Player p : players) {
            if (p.getPrestigePoints() > maxPrestige) {
                maxPrestige = p.getPrestigePoints();
            }
        }

        // Filter players with max prestige
        List<Player> candidates = new ArrayList<>();
        for (Player p : players) {
            if (p.getPrestigePoints() == maxPrestige) {
                candidates.add(p);
            }
        }

        // Tie-break: fewest purchased cards
        int minCards = Integer.MAX_VALUE;
        for (Player p : candidates) {
            if (p.getPurchasedCardCount() < minCards) {
                minCards = p.getPurchasedCardCount();
            }
        }

        List<Player> winners = new ArrayList<>();
        for (Player p : candidates) {
            if (p.getPurchasedCardCount() == minCards) {
                winners.add(p);
            }
        }

        return winners;
    }
}
