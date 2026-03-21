package splendor.data;

import splendor.model.Card;
import splendor.model.GemType;

import java.io.*;
import java.util.*;

public class CardLoader {

    /**
     * Loads development cards from a CSV file.
     * CSV format: level,bonus_gem,prestige_points,ruby,emerald,sapphire,diamond,onyx
     *
     * @param csvPath path to the CSV file
     * @param level   expected card level (1, 2, or 3)
     * @return list of Card objects parsed from the file
     * @throws FileNotFoundException if the file cannot be found
     */
    public static List<Card> loadCards(String csvPath, int level) throws FileNotFoundException {
        List<Card> cards = new ArrayList<>();

        File file = new File(csvPath);
        Scanner fr = new Scanner(file);

        fr.nextLine(); // skip header

        while (fr.hasNext()) {
            String line = fr.nextLine().trim();

            // skip comments if any
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }


            // split based on headers
            // level,bonus_gem,prestige_points,ruby,emerald,sapphire,diamond,onyx
            String[] tokens = line.split(",");
            int cardLevel = Integer.parseInt(tokens[0].trim());
            GemType bonusGem = GemType.valueOf(tokens[1].trim());
            int prestigePoints = Integer.parseInt(tokens[2].trim());

            Map<GemType, Integer> cost = new HashMap<>();
            cost.put(GemType.RUBY, Integer.parseInt(tokens[3].trim()));
            cost.put(GemType.EMERALD, Integer.parseInt(tokens[4].trim()));
            cost.put(GemType.SAPPHIRE, Integer.parseInt(tokens[5].trim()));
            cost.put(GemType.DIAMOND, Integer.parseInt(tokens[6].trim()));
            cost.put(GemType.ONYX, Integer.parseInt(tokens[7].trim()));

            cards.add(new Card(cardLevel, bonusGem, prestigePoints, cost));
        }

        fr.close();
        return cards;
    }
}
