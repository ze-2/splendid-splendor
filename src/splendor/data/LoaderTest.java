package splendor.data;

import java.io.*;
import java.util.*;
import splendor.model.Card;
import splendor.model.Noble;

// compile with
// javac -d classes src/splendor/model/GemType.java src/splendor/model/Card.java src/splendor/model/Noble.java src/splendor/data/CardLoader.java src/splendor/data/NobleLoader.java src/splendor/data/LoaderTest.java

// run with
// java -cp classes splendor.data.LoaderTest

public class LoaderTest {

    public static void main(String[] args) throws FileNotFoundException {

        // CardLoader test
        CardLoader cardLoader = new CardLoader();
        System.out.println("=== Card Deck Sizes ===");
        System.out.println("Level 1 deck: " + cardLoader.getDeckSize(1));
        System.out.println("Level 2 deck: " + cardLoader.getDeckSize(2));
        System.out.println("Level 3 deck: " + cardLoader.getDeckSize(3));

        System.out.println("\n=== Drawing Cards ===");
        Card drawn1 = cardLoader.drawCard(1);
        Card drawn2 = cardLoader.drawCard(2);
        System.out.println("Drew L1 card: " + drawn1);
        System.out.println("Drew L2 card: " + drawn2);
        System.out.println("Level 1 deck after 1 draws: " + cardLoader.getDeckSize(1));
        System.out.println("Level 2 deck after 1 draws: " + cardLoader.getDeckSize(2));

        // NobleLoader test
        NobleLoader nobleLoader = new NobleLoader();
        System.out.println("\n=== Noble Data ===");
        System.out.println("Total nobles loaded: " + nobleLoader.getAllNobles().size());

        List<Noble> drawnNobles = nobleLoader.drawNobles(2);
        System.out.println("Drew Nobles for 2 players: " + drawnNobles);
        System.out.println("Available nobles (2 players): " + nobleLoader.getAvailNobles().size());

        for (Noble noble : nobleLoader.getAvailNobles()) {
            System.out.println("  " + noble);
        }
    }
}
