package splendor.data;

import splendor.model.Noble;
import splendor.model.GemType;

import java.io.*;
import java.util.*;

public class NobleLoader {

    /**
     * Loads noble tiles from a CSV file.
     * CSV format: prestige_points,ruby,emerald,sapphire,diamond,onyx
     *
     * @param csvPath path to the CSV file
     * @return list of Noble objects parsed from the file
     * @throws FileNotFoundException if the file cannot be found
     */
    public static List<Noble> loadNobles(String csvPath) throws FileNotFoundException {
        List<Noble> nobles = new ArrayList<>();

        File file = new File(csvPath);
        Scanner fr = new Scanner(file);

        fr.nextLine(); // skip header

        while (fr.hasNext()) {
            String line = fr.nextLine().trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            // read following headers
            // prestige_points,ruby,emerald,sapphire,diamond,onyx
            String[] tokens = line.split(",");
            int prestigePoints = Integer.parseInt(tokens[0].trim());

            Map<GemType, Integer> requirements = new HashMap<>();
            requirements.put(GemType.RUBY, Integer.parseInt(tokens[1].trim()));
            requirements.put(GemType.EMERALD, Integer.parseInt(tokens[2].trim()));
            requirements.put(GemType.SAPPHIRE, Integer.parseInt(tokens[3].trim()));
            requirements.put(GemType.DIAMOND, Integer.parseInt(tokens[4].trim()));
            requirements.put(GemType.ONYX, Integer.parseInt(tokens[5].trim()));

            nobles.add(new Noble(prestigePoints, requirements));
        }

        fr.close();
        return nobles;
    }
}
