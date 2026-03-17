package splendor.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class GameConfig {
    private final Properties props = new Properties();

    public void load(String path) {
        try (FileInputStream fis = new FileInputStream(path)) {
            props.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config: " + path, e);
        }
    }

    public int getWinningPoints() {
        return Integer.parseInt(props.getProperty("winning.points", "15"));
    }

    public int getGemCount(int numPlayers) {
        String key = "gems.per.color." + numPlayers + "players";
        return Integer.parseInt(props.getProperty(key, "7"));
    }

    public int getGoldCount() {
        return Integer.parseInt(props.getProperty("gold.gems", "5"));
    }

    public String getCardDataPath(int level) {
        return props.getProperty("data.cards.level" + level);
    }

    public String getNobleDataPath() {
        return props.getProperty("data.nobles");
    }
}
