package splendor.data;

import splendor.model.*;
import splendor.config.*;

import java.io.*;
import java.util.*;

public class CardLoader {

    // Per-level tracking (index 0 = level 1, index 1 = level 2, index 2 = level 3)
    private Map<Integer, List<Card>> availCards = new HashMap<Integer, List<Card>>();     // face-down deck cards still available to draw
    private Map<Integer, List<Card>> drawnCards = new HashMap<Integer, List<Card>>();     // all cards that have been drawn

    /**
     * Loads all development cards from CSV files and prepares shuffled decks.
     * CSV format: level,bonus_gem,prestige_points,ruby,emerald,sapphire,diamond,onyx
     * @throws FileNotFoundException if any CSV file cannot be found
     */
    public CardLoader(GameConfig config) throws FileNotFoundException {
        String[] paths = {
             config.getCardDataPath(1),
             config.getCardDataPath(2),
             config.getCardDataPath(3)
        };

        for (int i = 0; i < 3; i++) {
            int level = i + 1;
            List<Card> loaded = loadCards(paths[i]);
            availCards.put(level, loaded);
            drawnCards.put(level, new ArrayList<>());
        }

        shuffle();
    }

    /**
     * Shuffles the available cards for all 3 levels
     */
    public void shuffle() {
        for (int level = 1; level <= 3; level++) {
            Collections.shuffle(availCards.get(level));         // shuffling algo may be improved
        }
    }

    /**
     * Draws the top card from a level's face-down deck.
     * returns the drawn Card, or null if the deck is empty
     */
    public Card drawCard(int level) {
        List<Card> deck = availCards.get(level);
        if (deck.isEmpty()) {
            return null;
        }
        Card card = deck.remove(0);
        drawnCards.get(level).add(card);
        return card;
    }

    /**
     * Draws multiple cards from a level's face-down deck.
     * returns list of drawn Cards (may be smaller than count if deck runs out)
     */
    public List<Card> drawCard(int level, int count) {
        List<Card> drawn = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Card card = drawCard(level);
            if (card == null) {
                break;
            }
            drawn.add(card);
        }
        return drawn;
    }

    // Returns the drawn cards for a level
    public List<Card> getAvailCards(int level) {
        return availCards.get(level);
    }

    public List<Card> getDrawnCards(int level) {
        return drawnCards.get(level);
    }

    public int getDeckSize(int level) {
        return availCards.get(level).size();
    }

    public boolean isDeckEmpty(int level) {
        return availCards.get(level).isEmpty();
    }

    // Loads and Parses CSV file into a list of Card objects
    private List<Card> loadCards(String csvPath) throws FileNotFoundException {
        List<Card> cards = new ArrayList<>();

        File file = new File(csvPath);
        Scanner fr = new Scanner(file);

        fr.nextLine(); // skip header

        while (fr.hasNext()) {
            String line = fr.nextLine().trim();

            // skip comments if any
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            // split based on headers
            // level,bonus_gem,prestige_points,ruby,emerald,sapphire,diamond,onyx
            String[] tokens = line.split(",");
            int cardLevel = Integer.parseInt(tokens[0].trim());
            GemType bonusGem = GemType.valueOf(tokens[1].trim());
            int prestigePoints = Integer.parseInt(tokens[2].trim());

            Map<GemType, Integer> cost = new HashMap<>();
            cost.put(GemType.RUBY, Integer.parseInt(tokens[3].trim()));
            cost.put(GemType.EMERALD, Integer.parseInt(tokens[4].trim()));
            cost.put(GemType.SAPPHIRE, Integer.parseInt(tokens[5].trim()));
            cost.put(GemType.DIAMOND, Integer.parseInt(tokens[6].trim()));
            cost.put(GemType.ONYX, Integer.parseInt(tokens[7].trim()));

            cards.add(new Card(cardLevel, bonusGem, prestigePoints, cost));
        }

        fr.close();
        return cards;
    }
}
