package splendor.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class GameConfig {

    private int winningPoints;
    private int gems2Players;
    private int gems3Players;
    private int gems4Players;
    private int goldCount;
    private String cardsLevel1;
    private String cardsLevel2;
    private String cardsLevel3;
    private String noblesPath;

    public void load(String path) {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(path)) {
            props.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config: " + path, e);
        }

        winningPoints = Integer.parseInt(props.getProperty("winning.points", "15"));
        gems2Players  = Integer.parseInt(props.getProperty("gems.per.color.2players", "4"));
        gems3Players  = Integer.parseInt(props.getProperty("gems.per.color.3players", "5"));
        gems4Players  = Integer.parseInt(props.getProperty("gems.per.color.4players", "7"));
        goldCount     = Integer.parseInt(props.getProperty("gold.gems", "5"));
        cardsLevel1   = props.getProperty("data.cards.level1", "data/cards_level1.csv");
        cardsLevel2   = props.getProperty("data.cards.level2", "data/cards_level2.csv");
        cardsLevel3   = props.getProperty("data.cards.level3", "data/cards_level3.csv");
        noblesPath    = props.getProperty("data.nobles", "data/nobles.csv");
    }

    public int getWinningPoints() {
        return winningPoints;
    }

    public int getGemCount(int numPlayers) {
        switch (numPlayers) {
            case 2:
                return gems2Players;
            case 3:
                return gems3Players;
            case 4:
                return gems4Players;
            default:
                throw new IllegalArgumentException("Unsupported player count: " + numPlayers + " (must be 2-4)");
        }
    }

    public int getGoldCount() {
        return goldCount;
    }

    public String getCardDataPath(int level) {
        switch (level) {
            case 1:
                return cardsLevel1;
            case 2:
                return cardsLevel2;
            case 3:
                return cardsLevel3;
            default:
                throw new IllegalArgumentException("Invalid card level: " + level + " (must be 1-3)");
        }
    }

    public String getNobleDataPath() {
        return noblesPath;
    }

    @Override
    public String toString() {
        return "GameConfig{" +
                "winningPoints=" + winningPoints +
                ", gems(2p/3p/4p)=" + gems2Players + "/" + gems3Players + "/" + gems4Players +
                ", goldCount=" + goldCount +
                ", cards=[" + cardsLevel1 + ", " + cardsLevel2 + ", " + cardsLevel3 + "]" +
                ", nobles=" + noblesPath + '}';
    }

    // Test
    public static void main(String[] args) {
        GameConfig config = new GameConfig();
        config.load("../../config.properties");

        System.out.println(config);
    }
}