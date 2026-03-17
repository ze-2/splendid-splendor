package splendor.model;

import splendor.config.GameConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Board {
    private final Map<GemType, Integer> gemBank;
    private final Card[][] visibleCards; // [level 0-2][slot 0-3]
    private final Deck[] decks;          // [level 0-2]
    private final List<Noble> nobles;

    public Board(GameConfig config, List<Card> allCards, List<Noble> allNobles, int numPlayers) {
        // Initialise gem bank
        gemBank = new EnumMap<>(GemType.class);
        int gemsPerColor = config.getGemCount(numPlayers);
        for (GemType gem : GemType.values()) {
            if (gem == GemType.GOLD) {
                gemBank.put(gem, config.getGoldCount());
            } else {
                gemBank.put(gem, gemsPerColor);
            }
        }

        // Sort cards into three lists by level
        List<List<Card>> cardsByLevel = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            cardsByLevel.add(new ArrayList<>());
        }
        for (Card card : allCards) {
            cardsByLevel.get(card.getLevel() - 1).add(card);
        }

        // Create and shuffle decks
        decks = new Deck[3];
        for (int i = 0; i < 3; i++) {
            decks[i] = new Deck(cardsByLevel.get(i));
            decks[i].shuffle();
        }

        // Deal 4 visible cards per level
        visibleCards = new Card[3][4];
        for (int level = 0; level < 3; level++) {
            for (int slot = 0; slot < 4; slot++) {
                visibleCards[level][slot] = decks[level].deal();
            }
        }

        // Shuffle nobles, reveal numPlayers + 1, discard the rest
        List<Noble> shuffled = new ArrayList<>(allNobles);
        Collections.shuffle(shuffled);
        nobles = new ArrayList<>();
        int nobleCount = Math.min(numPlayers + 1, shuffled.size());
        for (int i = 0; i < nobleCount; i++) {
            nobles.add(shuffled.get(i));
        }
    }

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
            gemBank.merge(entry.getKey(), -entry.getValue(), Integer::sum);
        }
    }

    public void returnGems(Map<GemType, Integer> gems) {
        for (Map.Entry<GemType, Integer> entry : gems.entrySet()) {
            gemBank.merge(entry.getKey(), entry.getValue(), Integer::sum);
        }
    }

    public Card takeVisibleCard(int level, int slot) {
        Card card = visibleCards[level][slot];
        visibleCards[level][slot] = decks[level].deal(); // refill; null if deck empty
        return card;
    }

    public Card takeReserveCard(int level) {
        return decks[level].deal();
    }

    public void refillCards() {
        for (int level = 0; level < 3; level++) {
            for (int slot = 0; slot < 4; slot++) {
                if (visibleCards[level][slot] == null) {
                    visibleCards[level][slot] = decks[level].deal();
                }
            }
        }
    }

    public void removeNoble(Noble noble) {
        nobles.remove(noble);
    }
}
