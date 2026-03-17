package splendor.data;

import splendor.model.GemType;
import splendor.model.Noble;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class NobleLoader {

    public static List<Noble> loadNobles(String path) {
        List<Noble> nobles = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split(",");
                // prestige_points, ruby, emerald, sapphire, diamond, onyx
                int points = Integer.parseInt(parts[0].trim());

                Map<GemType, Integer> requirements = new EnumMap<>(GemType.class);
                requirements.put(GemType.RUBY,     Integer.parseInt(parts[1].trim()));
                requirements.put(GemType.EMERALD,  Integer.parseInt(parts[2].trim()));
                requirements.put(GemType.SAPPHIRE, Integer.parseInt(parts[3].trim()));
                requirements.put(GemType.DIAMOND,  Integer.parseInt(parts[4].trim()));
                requirements.put(GemType.ONYX,     Integer.parseInt(parts[5].trim()));

                nobles.add(new Noble(points, requirements));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load nobles from: " + path, e);
        }
        return nobles;
    }
}
