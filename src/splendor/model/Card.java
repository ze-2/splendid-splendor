package splendor.model;

import java.util.*;

public class Card {
    private int level;
    private GemType bonusGem; // permanent discount
    private int prestigePoints;
    private HashMap <GemType, Integer> cost; // what the player pays


    public Card (int level, GemType bonusGem, int prestigePoints, HashMap <GemType, Integer> cost) {
        this.level = level;
        this.bonusGem = bonusGem;
        this.prestigePoints = prestigePoints;
        this.cost = cost;
    }

    public int getLevel() {
        return level;
    }

    public GemType getBonusGem () {
        return bonusGem;
    }

    public int getPrestigePoints() {
        return prestigePoints;
    }

    public HashMap <GemType, Integer> getCost () {
        return cost;
    }

    public String toString () {
        return String.format("[Level: " + this.level +
                            ", Gem Cost: " + this.cost +
                            ", Bonus Gem Colour: " + this.bonusGem +
                            ", Prestige Points: " + this.prestigePoints + "]");
    }

}