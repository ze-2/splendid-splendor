package splendor.model;

import java.util.*;

import splendor.engine.*;

public abstract class Player {

    private final String name;
    private final Map<GemType, Integer> gems;
    private final List<Card> purchasedCards;
    private final List<Card> reservedCards;
    private final List<Noble> nobles;

    public Player(String name) {
        this.name = name;
        this.gems = new EnumMap<>(GemType.class);
        this.purchasedCards = new ArrayList<>();
        this.reservedCards = new ArrayList<>();
        this.nobles = new ArrayList<>();

        // Pre-populate every colour with 0 so get() never returns null
        for (GemType type : GemType.values()) {
            gems.put(type, 0);
        }
    }

    // Abstract methods, to be implemented by HumanPlayer and AIPlayer
    public abstract ActionType chooseAction(Board board, ActionValidator actionValidator);
    public abstract Map<GemType,Integer> chooseTake3Gems(Board board, ActionValidator actionValidator);
    public abstract GemType chooseTake2Gems(Board board, ActionValidator actionValidator);
    public abstract int[] chooseReserveCard(Board board, ActionValidator actionValidator);
    public abstract Card chooseBuyCard(Board board, ActionValidator actionValidator);
    public abstract Map<GemType, Integer> chooseDiscard(int excess);
    public abstract Noble chooseNoble(List<Noble> nobles);

    public String getName() {
        return name;
    }

    // assumes the canBuy validator is ran before (invoked by gameEngine)
    public Map<GemType, Integer> buyCard(Card card) {

        Map<GemType, Integer> bonuses = getBonusGems();
        Map<GemType, Integer> spent = new HashMap<>();

        int goldNeeded = 0;

        for (Map.Entry<GemType, Integer> entry : card.getCost().entrySet()) {
            GemType color = entry.getKey();
            int cost = entry.getValue();

            // get bonuses
            int bonus = bonuses.getOrDefault(color, 0);
            int effectiveCost = Math.max(0, cost - bonus);

            // calculate if any shortfall, then add to goldneeded
            if (effectiveCost != 0) {
                int available = gems.getOrDefault(color, 0);
                // how much player can pay
                int pay = Math.min(available, effectiveCost);
                int shortfall = effectiveCost - pay;

                if (pay > 0) {
                    spent.put(color, pay);
                    gems.put(color, available - pay);
                }

                goldNeeded += shortfall;
            }
        }

        if (goldNeeded > 0) {
            spent.put(GemType.GOLD, goldNeeded);
            gems.put(GemType.GOLD, gems.getOrDefault(GemType.GOLD, 0) - goldNeeded);
        }

        // move card from reserved to purchased (if it was reserved)
        reservedCards.remove(card);
        purchasedCards.add(card);

        return spent;
    }

    public Map<GemType, Integer> getGems() {
        return Collections.unmodifiableMap(gems);
    }

    // Sum of all gem tokens held across every colour
    public int getTotalGems() {
        int total = 0;

        for (int count : gems.values()) {
            total += count;
        }

        return total;
    }

    public int getPurchasedCardCount() {
        return purchasedCards.size();
    }

    // Count of bonus gems of each type from purchased cards
    public Map<GemType, Integer> getBonusGems() {

        Map<GemType, Integer> bonuses = new HashMap<>();

        for (Card card : purchasedCards) {
            GemType bonus = card.getBonusGem();
            bonuses.put(bonus, bonuses.getOrDefault(bonus, 0) + 1);
        }

        return Collections.unmodifiableMap(bonuses);
    }

    // total prestige points = sum of all purchased cards + all acquired nobles
    public int getPrestigePoints() {
        int total = 0;
        for (Card card : purchasedCards) {
            total += card.getPrestigePoints();
        }
        for (Noble noble : nobles) {
            total += noble.getPrestigePoints();
        }
        return total;
    }

    public void addGem(GemType type, int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Cannot add a negative gem amount: " + amount);
        }
        gems.put(type, gems.get(type) + amount);
    }

    public void removeGem(GemType type, int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Cannot remove a negative gem amount: " + amount);
        }
        int current = gems.get(type);
        if (amount > current) {
            throw new IllegalArgumentException("Cannot remove " + amount + " " + type + " gem(s); only " + current + " held.");
        }
        gems.put(type, current - amount);
    }

    public List<Card> getReservedCards() {
        return Collections.unmodifiableList(reservedCards);
    }

    // reserves card - doesn't change any gold info
    public void reserveCard(Card card) {
        if (reservedCards.size() >= 3) {
            throw new IllegalStateException(
                name + " already has the maximum of 3 reserved cards.");
        }
        reservedCards.add(card);
    }

    public List<Noble> getNobles() {
        return Collections.unmodifiableList(nobles);
    }

    public void addNoble(Noble noble) {
        nobles.add(noble);
    }

    public List<Card> getPurchasedCards() {
        return Collections.unmodifiableList(purchasedCards);
    }

    @Override
    public String toString() {
        return "Player{" +
            "name='" + name + '\'' +
            ", prestige=" + getPrestigePoints() +
            ", gems=" + gems +
            ", bonusGems=" + getBonusGems() +
            ", purchased=" + purchasedCards.size()+
            ", reserved=" + reservedCards.size() +
            ", nobles=" + nobles.size() + '}';
    }
}
