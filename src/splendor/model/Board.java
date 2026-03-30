package splendor.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import splendor.config.GameConfig;

public class Board {
    private Map<GemType, Integer> gemBank;
    private Card[][] visibleCards; // 3x4 array of face up cards
    private Deck[] decks; // have 3 levels 0, 1, 2
    private List<Noble> nobles;
    
    public Board(GameConfig gameConfig, List<Card> allCards, List<Noble> nobles, int numPlayers) {
        // initialise gem bank
        gemBank = new HashMap<>();
        for (GemType gemType : GemType.values()) {
            if (gemType == GemType.GOLD) {
                gemBank.put(gemType, gameConfig.getGoldCount());
            } else {
                gemBank.put(gemType, gameConfig.getGemCount(numPlayers));
            }
        }

        // card sorting
        List<Card> level1Cards = new ArrayList<>();
        List<Card> level2Cards = new ArrayList<>();
        List<Card> level3Cards = new ArrayList<>();

        for (Card card : allCards) {
            int level = card.getLevel();
            switch (level) {
                case 1:
                    level1Cards.add(card);
                    break;
                case 2:
                    level2Cards.add(card);
                    break;
                case 3:
                    level3Cards.add(card);
                    break;
                default:
                    break;
            }
        }
        
        decks = new Deck[]{new Deck(level1Cards), new Deck(level2Cards), new Deck(level3Cards)};

        // shuffle deck
        for (int i = 0; i < 3; i++) {
            decks[i].shuffle();
        }

        // Deal Cards
        visibleCards = new Card[3][4];
        for (int i = 0; i < visibleCards.length; i++) {
            for (int j = 0; j < visibleCards.length; j++) {
                visibleCards[i][j] = decks[i].deal();
            }
        }

        // Reveal Noble cards
        List<Noble> shuffled = nobles;
        Collections.shuffle(shuffled);
        this.nobles = shuffled;
    }
    
    //Getters-----------------------------------------------------------------
    
    public Deck[] getDecks() {
        return decks;
    }

    public Map<GemType, Integer> getGemBank() {
        return gemBank;
    }

    public Card[][] getVisibleCards() {
        return visibleCards;
    }

    public List<Noble> getNobles() {
        return nobles;
    }

    public void takeGems(Map<GemType, Integer> mp) {
        for (Map.Entry<GemType, Integer> entry : mp.entrySet()) {
            GemType type = entry.getKey();
            int count = entry.getValue();
            if (count != 0) {
                gemBank.put(type, gemBank.get(type) - count);
            }
        }
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

    public void returnGems(Map<GemType, Integer> mp) {
        for (Map.Entry<GemType, Integer> e : mp.entrySet()) {
            // assumption that the gemBank will contain all the gem (keys)
            GemType g = e.getKey();
            Integer i = e.getValue();
            gemBank.put(g, i + gemBank.get(g));
        }
    }

    public Card takeVisibleCard(int level, int slot) {
        Card cardTaken = visibleCards[level][slot];
        visibleCards[level][slot] = decks[level].deal();
        return cardTaken;
    }

    public Card takeReserveCard (int level) {
        Card card = decks[level].deal();
        card.setHidden();
        return card;
    }

    public void removeNoble(Noble noble) {
        nobles.remove(noble);
    }
}
