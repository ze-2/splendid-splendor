package splendor.model;

import java.util.*;

/**
 * Stub — P4 replaces with full implementation.
 * Provides the API that GameEngine, ConsoleUI, and ActionValidator depend on.
 */
public class Board {

    private Map<GemType, Integer> gemBank;
    private Card[][] visibleCards;   // [level 0-2][slot 0-3]
    private Deck[] decks;            // [level 0-2]
    private List<Noble> nobles;

    public Map<GemType, Integer> getGemBank() {
        return gemBank;
    }

    public Card[][] getVisibleCards() {
        return visibleCards;
    }

    public Deck[] getDecks() {
        return decks;
    }

    public List<Noble> getNobles() {
        return nobles;
    }

    public void takeGems(Map<GemType, Integer> gems) {
        for (Map.Entry<GemType, Integer> entry : gems.entrySet()) {
            gemBank.put(entry.getKey(), gemBank.get(entry.getKey()) - entry.getValue());
        }
    }

    public void returnGems(Map<GemType, Integer> gems) {
        for (Map.Entry<GemType, Integer> entry : gems.entrySet()) {
            gemBank.put(entry.getKey(), gemBank.getOrDefault(entry.getKey(), 0) + entry.getValue());
        }
    }

    public Card takeVisibleCard(int level, int slot) {
        Card card = visibleCards[level][slot];
        visibleCards[level][slot] = null;
        refillCards();
        return card;
    }

    public Card takeReserveCard(int level) {
        if (decks[level].isEmpty()) {
            return null;
        }
        return decks[level].deal();
    }

    public void refillCards() {
        for (int level = 0; level < 3; level++) {
            for (int slot = 0; slot < 4; slot++) {
                if (visibleCards[level][slot] == null && !decks[level].isEmpty()) {
                    visibleCards[level][slot] = decks[level].deal();
                }
            }
        }
    }

    public void removeNoble(Noble noble) {
        nobles.remove(noble);
    }
}
