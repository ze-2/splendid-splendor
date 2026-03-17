package splendor.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.StringJoiner;

public class Card {
    private final int level;
    private final GemType bonusGem;
    private final int prestigePoints;
    private final Map<GemType, Integer> cost;

    public Card(int level, GemType bonusGem, int prestigePoints, Map<GemType, Integer> cost) {
        this.level = level;
        this.bonusGem = bonusGem;
        this.prestigePoints = prestigePoints;
        this.cost = Collections.unmodifiableMap(new EnumMap<>(cost));
    }

    public int getLevel() {
        return level;
    }

    public GemType getBonusGem() {
        return bonusGem;
    }

    public int getPrestigePoints() {
        return prestigePoints;
    }

    public Map<GemType, Integer> getCost() {
        return cost;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(", ");
        for (Map.Entry<GemType, Integer> e : cost.entrySet()) {
            if (e.getValue() > 0) {
                sj.add(e.getKey() + "=" + e.getValue());
            }
        }
        return String.format("[L%d %s %dpt | Cost: %s]", level, bonusGem, prestigePoints, sj);
    }
}
