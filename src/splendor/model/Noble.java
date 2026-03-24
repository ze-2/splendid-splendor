package splendor.model;

import java.util.Map;

public class Noble {
    private int prestigePoints;
    private Map<GemType, Integer> requirements;

    public Noble(int prestigePoints, Map<GemType, Integer> requirements) {
        this.prestigePoints = prestigePoints;
        this.requirements = requirements;
    }

    public int getPrestigePoints() { return prestigePoints; }
    public Map<GemType, Integer> getRequirements() { return requirements; }

    @Override
    public String toString() {
        return "Noble[" + prestigePoints + "pts req=" + requirements + "]";
    }
}
