package splendor.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.StringJoiner;

public class Noble {
    private final int prestigePoints;
    private final Map<GemType, Integer> requirements;

    public Noble(int prestigePoints, Map<GemType, Integer> requirements) {
        this.prestigePoints = prestigePoints;
        this.requirements = Collections.unmodifiableMap(new EnumMap<>(requirements));
    }

    public int getPrestigePoints() {
        return prestigePoints;
    }

    public Map<GemType, Integer> getRequirements() {
        return requirements;
    }

    public boolean canVisit(Player player) {
        Map<GemType, Integer> bonuses = player.getBonusGems();
        for (Map.Entry<GemType, Integer> entry : requirements.entrySet()) {
            if (entry.getValue() > 0 && bonuses.getOrDefault(entry.getKey(), 0) < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(", ");
        for (Map.Entry<GemType, Integer> e : requirements.entrySet()) {
            if (e.getValue() > 0) {
                sj.add(e.getKey() + "=" + e.getValue());
            }
        }
        return String.format("[Noble %dpt | Requires: %s]", prestigePoints, sj);
    }
}
