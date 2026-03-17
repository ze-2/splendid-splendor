package splendor.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public abstract class Player {
    private final String name;
    private final Map<GemType, Integer> gems;
    private final List<Card> purchasedCards;
    private final List<Card> reservedCards;
    private final List<Noble> nobles;

    public Player(String name) {
        this.name = name;
        this.gems = new EnumMap<>(GemType.class);
        for (GemType g : GemType.values()) {
            gems.put(g, 0);
        }
        this.purchasedCards = new ArrayList<>();
        this.reservedCards = new ArrayList<>();
        this.nobles = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public Map<GemType, Integer> getGems() {
        return gems;
    }

    public int getTotalGems() {
        int total = 0;
        for (int v : gems.values()) {
            total += v;
        }
        return total;
    }

    public Map<GemType, Integer> getBonusGems() {
        Map<GemType, Integer> bonuses = new EnumMap<>(GemType.class);
        for (Card card : purchasedCards) {
            bonuses.merge(card.getBonusGem(), 1, Integer::sum);
        }
        return bonuses;
    }

    public int getPrestigePoints() {
        int points = 0;
        for (Card card : purchasedCards) {
            points += card.getPrestigePoints();
        }
        for (Noble noble : nobles) {
            points += noble.getPrestigePoints();
        }
        return points;
    }

    public int getPurchasedCardCount() {
        return purchasedCards.size();
    }

    public List<Card> getPurchasedCards() {
        return purchasedCards;
    }

    public List<Card> getReservedCards() {
        return reservedCards;
    }

    public List<Noble> getNobles() {
        return nobles;
    }

    public void addGem(GemType type, int count) {
        gems.merge(type, count, Integer::sum);
    }

    public void removeGem(GemType type, int count) {
        gems.merge(type, -count, Integer::sum);
    }

    public void reserveCard(Card card) {
        reservedCards.add(card);
    }

    /**
     * Pays for and purchases the given card.
     * Moves the card from reserved (if present) to purchased.
     * Returns the map of gem tokens actually spent (to be returned to the bank).
     */
    public Map<GemType, Integer> buyCard(Card card) {
        Map<GemType, Integer> bonuses = getBonusGems();
        Map<GemType, Integer> spent = new EnumMap<>(GemType.class);
        int goldNeeded = 0;

        for (Map.Entry<GemType, Integer> entry : card.getCost().entrySet()) {
            GemType gem = entry.getKey();
            int required = entry.getValue();
            int bonus = bonuses.getOrDefault(gem, 0);
            int remaining = Math.max(0, required - bonus);
            int fromTokens = Math.min(remaining, gems.getOrDefault(gem, 0));
            if (fromTokens > 0) {
                spent.put(gem, fromTokens);
            }
            goldNeeded += remaining - fromTokens;
        }

        if (goldNeeded > 0) {
            spent.put(GemType.GOLD, goldNeeded);
        }

        // Remove spent gems from player
        for (Map.Entry<GemType, Integer> entry : spent.entrySet()) {
            removeGem(entry.getKey(), entry.getValue());
        }

        // Move card to purchased; remove from reserved if applicable
        reservedCards.remove(card);
        purchasedCards.add(card);

        return spent;
    }

    public void addNoble(Noble noble) {
        nobles.add(noble);
    }

    public abstract boolean isHuman();

    @Override
    public String toString() {
        return String.format("%s [%dpt, %d gems, %d cards, %d reserved, %d nobles]",
                name, getPrestigePoints(), getTotalGems(),
                purchasedCards.size(), reservedCards.size(), nobles.size());
    }
}
