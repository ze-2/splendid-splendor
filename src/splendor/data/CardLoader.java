package splendor.data;

import splendor.model.Card;
import splendor.model.GemType;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class CardLoader {

    public static List<Card> loadCards(String path, int level) {
        List<Card> cards = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split(",");
                // level, bonus_gem, prestige_points, ruby, emerald, sapphire, diamond, onyx
                GemType bonus = GemType.valueOf(parts[1].trim());
                int points = Integer.parseInt(parts[2].trim());

                Map<GemType, Integer> cost = new EnumMap<>(GemType.class);
                cost.put(GemType.RUBY,     Integer.parseInt(parts[3].trim()));
                cost.put(GemType.EMERALD,  Integer.parseInt(parts[4].trim()));
                cost.put(GemType.SAPPHIRE, Integer.parseInt(parts[5].trim()));
                cost.put(GemType.DIAMOND,  Integer.parseInt(parts[6].trim()));
                cost.put(GemType.ONYX,     Integer.parseInt(parts[7].trim()));

                cards.add(new Card(level, bonus, points, cost));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load cards from: " + path, e);
        }
        return cards;
    }
}
