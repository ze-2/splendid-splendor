package splendor.model;

import java.util.*;

public class Noble {
    private int prestigePoints; // each noble grants 3 prestige points
    private Map<GemType, Integer> requirements;

    public Noble(Map<GemType, Integer> requirements) {
        // each noble tile is worth 3 prestige points
        this.prestigePoints = 3;
        this.requirements = requirements;
    }

    public int getPrestigePoints() {
        return this.prestigePoints;
    }

    public Map<GemType, Integer> getRequirements() {
        return this.requirements;
    }

    public boolean canVisit (Player player) {
        for (GemType c : requirements.keySet()) {
            // loop through every required colour to see if there is enough for nobles to visit
            Integer playerGems = player.getBonusGems().get(c);
            if (playerGems == null) {
                playerGems = 0;
            }

            if (playerGems < requirements.get(c)) {
                return false;
                // if any requirements fail then immediately return false
            }
        }
        return true;
    }

    public String toString() {
        return String.format("Noble: " + prestigePoints + " points, requires" + requirements);
    }


}
