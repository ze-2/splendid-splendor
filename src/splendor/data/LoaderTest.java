package splendor.data;

import java.io.*;
import java.util.*;
import splendor.model.Card;
import splendor.model.Noble;

// compile with
// javac -d classes -cp "src" src/splendor/data/LoaderTest.java

// run with
// java -cp "classes" splendor.data.LoaderTest

public class LoaderTest {

    public static void main(String[] args) throws FileNotFoundException {

        // Load card data
        List<Card> level1 = CardLoader.loadCards("data/cards_level1.csv", 1);
        List<Card> level2 = CardLoader.loadCards("data/cards_level2.csv", 2);
        List<Card> level3 = CardLoader.loadCards("data/cards_level3.csv", 3);

        // Load noble data
        List<Noble> nobles = NobleLoader.loadNobles("data/nobles.csv");

        // print out all card data
        System.out.println("Total Level 1 Cards: " + level1.size());
        System.out.println("Total Level 2 Cards: " + level2.size());
        System.out.println("Total Level 3 Cards: " + level3.size());
        System.out.println("Total Noble Cards: " + nobles.size());
    }
}
