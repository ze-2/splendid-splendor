package splendor.model;

import java.util.Map;

public class Card {
    private int level;
    private GemType bonusGem;
    private int prestigePoints;
    private Map<GemType, Integer> cost;

    public Card(int level, GemType bonusGem, int prestigePoints, Map<GemType, Integer> cost) {
        this.level = level;
        this.bonusGem = bonusGem;
        this.prestigePoints = prestigePoints;
        this.cost = cost;
    }

    public int getLevel() { return level; }
    public GemType getBonusGem() { return bonusGem; }
    public int getPrestigePoints() { return prestigePoints; }
    public Map<GemType, Integer> getCost() { return cost; }

    @Override
    public String toString() {
        return "Card[L" + level + " " + bonusGem + " " + prestigePoints + "pts cost=" + cost + "]";
    }
}
