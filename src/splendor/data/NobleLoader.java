package splendor.data;

import splendor.model.Noble;
import splendor.model.GemType;

import java.io.*;
import java.util.*;

public class NobleLoader {

    private List<Noble> allNobles = new ArrayList<>();        // all 10 nobles loaded from CSV
    private List<Noble> availNobles = new ArrayList<>();      // nobles currently avail to draw
    private List<Noble> drawnNobles = new ArrayList<>();       // nobles that have been drawn

    /**
     * Loads all noble tiles from CSV and prepares a shuffled list.
     * CSV format: prestige_points,ruby,emerald,sapphire,diamond,onyx
     *
     * @throws FileNotFoundException if the CSV file cannot be found
     */
    public NobleLoader() throws FileNotFoundException {

        File file = new File("data/nobles.csv");
        Scanner fr = new Scanner(file);

        fr.nextLine(); // skip header

        while (fr.hasNext()) {
            String line = fr.nextLine().trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            // read following headers
            // prestige_points,ruby,emerald,sapphire,diamond,onyx
            // CSV format: prestige_points,ruby,emerald,sapphire,diamond,onyx
            // Noble constructor hardcodes prestige to 3, so skip first column
            String[] tokens = line.split(",");

            Map<GemType, Integer> requirements = new HashMap<>();
            requirements.put(GemType.RUBY, Integer.parseInt(tokens[1].trim()));
            requirements.put(GemType.EMERALD, Integer.parseInt(tokens[2].trim()));
            requirements.put(GemType.SAPPHIRE, Integer.parseInt(tokens[3].trim()));
            requirements.put(GemType.DIAMOND, Integer.parseInt(tokens[4].trim()));
            requirements.put(GemType.ONYX, Integer.parseInt(tokens[5].trim()));
            String name = tokens[6].trim();
            allNobles.add(new Noble(requirements, name));
        }

        fr.close();

        // init avail nobles
        availNobles = allNobles;
        shuffle();
    }

    /**
     * Shuffles the available nobles list.
     */
    public void shuffle() {
        Collections.shuffle(availNobles);
    }

    /**
     * Selects numPlayers + 1 nobles for the game.
     * Must be called after constructor (which already shuffled).
     *
     * @param numPlayers number of players in the game
     */
    public List<Noble> drawNobles(int numPlayers) {
        int count = numPlayers + 1;
        List<Noble> drawn = new ArrayList<>();
        for (int i = 0; i < count && !availNobles.isEmpty(); i++) {
            Noble noble = availNobles.remove(0);
            drawnNobles.add(noble);
            drawn.add(noble);
        }
        return drawn;
    }

    public List<Noble> getAvailNobles() {
        return availNobles;
    }

    public List<Noble> getDrawnNobles() {
        return drawnNobles;
    }

    public List<Noble> getAllNobles() {
        return allNobles;
    }
}
