package splendor.engine;

import java.io.*;
import java.util.*;
import splendor.data.*;
import splendor.model.*;
import splendor.config.GameConfig;
import splendor.ui.ConsoleUI;

// compile with: find src -name "*.java" | xargs javac -d classes
// run with:     java -cp classes splendor.engine.GameEngineTest

public class GameEngineTest {

    public static void main(String[] args) throws FileNotFoundException {
        System.out.println("=== GameEngine Test ===\n");

        // ── 1. Load data ─────────────────────────────────────────────
        CardLoader cardLoader = new CardLoader();
        NobleLoader nobleLoader = new NobleLoader();

        System.out.println(
            "[OK] CardLoader: L1=" +
                cardLoader.getDeckSize(1) +
                " L2=" +
                cardLoader.getDeckSize(2) +
                " L3=" +
                cardLoader.getDeckSize(3)
        );
        System.out.println(
            "[OK] NobleLoader: " +
                nobleLoader.getAllNobles().size() +
                " nobles loaded"
        );

        // ── 2. Build board manually (Board has no constructor yet) ───
        GameConfig config = new GameConfig();
        config.load("config.properties");

        List<Card> cards = new ArrayList<>();
        cards.addAll(cardLoader.getAllCards(1));
        cards.addAll(cardLoader.getAllCards(2));
        cards.addAll(cardLoader.getAllCards(3));

        List<Noble> nobles = nobleLoader.getAllNobles();
        
        Board board = new Board(config, cards, nobles, 2);

        System.out.println(
            "[OK] Board built: gem bank, 12 visible cards, 3 nobles"
        );

        // Print gem bank
        System.out.println("\nGem Bank:");
        for (GemType gem : GemType.values()) {
            System.out.println("  " + gem + ": " + board.getGemBank().get(gem));
        }

        // Print visible cards
        for (int level = 0; level < 3; level++) {
            System.out.println("\nLevel " + (level + 1) + " visible:");
            for (int slot = 0; slot < 4; slot++) {
                Card c = board.getVisibleCards()[level][slot];
                System.out.println(
                    "  [" + slot + "] " + (c != null ? c : "(empty)")
                );
            }
            System.out.println(
                "  Deck remaining: " + board.getDecks()[level].size()
            );
        }

        // Print nobles
        System.out.println("\nNobles:");
        for (Noble n : board.getNobles()) {
            System.out.println("  " + n);
        }

        // ── 3. Create players ────────────────────────────────────────
        ConsoleUI ui = new ConsoleUI();
        Player p1 = new HumanPlayer("Alice", ui);
        Player p2 = new HumanPlayer("Bob", ui);

        List<Player> players = new ArrayList<>();
        players.add(p1);
        players.add(p2);
        System.out.println(
            "\n[OK] Players created: " + p1.getName() + ", " + p2.getName()
        );

        // ── 4. Test gem operations ───────────────────────────────────
        System.out.println("\n=== Gem Operations ===");

        // Take 3 different gems from bank
        Map<GemType, Integer> take3 = new EnumMap<>(GemType.class);
        take3.put(GemType.RUBY, 1);
        take3.put(GemType.EMERALD, 1);
        take3.put(GemType.SAPPHIRE, 1);
        board.takeGems(take3);
        for (Map.Entry<GemType, Integer> e : take3.entrySet()) {
            p1.addGem(e.getKey(), e.getValue());
        }
        System.out.println("[OK] Alice took 3 gems: RUBY, EMERALD, SAPPHIRE");
        System.out.println("  Alice gems: " + p1.getGems());
        System.out.println(
            "  Bank RUBY: " + board.getGemBank().get(GemType.RUBY)
        );

        // Take 2 same gems
        Map<GemType, Integer> take2 = new EnumMap<>(GemType.class);
        take2.put(GemType.DIAMOND, 2);
        board.takeGems(take2);
        p2.addGem(GemType.DIAMOND, 2);
        System.out.println("[OK] Bob took 2 DIAMOND gems");
        System.out.println("  Bob gems: " + p2.getGems());
        System.out.println(
            "  Bank DIAMOND: " + board.getGemBank().get(GemType.DIAMOND)
        );

        // ── 5. Test reserve card ─────────────────────────────────────
        System.out.println("\n=== Reserve Card ===");

        Card visCard = board.getVisibleCards()[0][0];
        System.out.println("Card to reserve: " + visCard);
        Card taken = board.takeVisibleCard(0, 0);
        p1.reserveCard(taken);

        // Grant gold
        int goldBefore = board.getGemBank().get(GemType.GOLD);
        Map<GemType, Integer> goldTake = new EnumMap<>(GemType.class);
        goldTake.put(GemType.GOLD, 1);
        board.takeGems(goldTake);
        p1.addGem(GemType.GOLD, 1);

        System.out.println("[OK] Alice reserved card + got 1 gold");
        System.out.println("  Reserved cards: " + p1.getReservedCards().size());
        System.out.println("  Gold held: " + p1.getGems().get(GemType.GOLD));
        System.out.println(
            "  Bank gold: " +
                board.getGemBank().get(GemType.GOLD) +
                " (was " +
                goldBefore +
                ")"
        );
        System.out.println(
            "  Slot refilled: " +
                (board.getVisibleCards()[0][0] != null ? "YES" : "NO")
        );

        // ── 6. Test buy card (give player enough gems first) ─────────
        System.out.println("\n=== Buy Card ===");

        // Give Bob plenty of gems to afford a card
        for (GemType gem : new GemType[] {
            GemType.RUBY,
            GemType.EMERALD,
            GemType.SAPPHIRE,
            GemType.DIAMOND,
            GemType.ONYX,
        }) {
            p2.addGem(gem, 5);
        }
        System.out.println("Gave Bob 5 of each gem for testing");

        Card toBuy = board.getVisibleCards()[0][1];
        System.out.println("Card to buy: " + toBuy);
        System.out.println("  Cost: " + toBuy.getCost());

        Card boughtCard = board.takeVisibleCard(0, 1);
        Map<GemType, Integer> spent = p2.buyCard(boughtCard);
        board.returnGems(spent);

        System.out.println("[OK] Bob bought card");
        System.out.println("  Gems spent: " + spent);
        System.out.println("  Bob prestige: " + p2.getPrestigePoints());
        System.out.println(
            "  Bob purchased cards: " + p2.getPurchasedCardCount()
        );
        System.out.println("  Bob bonus gems: " + p2.getBonusGems());

        // ── 7. Test noble visit ──────────────────────────────────────
        System.out.println("\n=== Noble Visit Check ===");

        System.out.println("Nobles on board: " + board.getNobles().size());
        Noble firstNoble = board.getNobles().get(0);
        System.out.println(
            "Noble requirements: " + firstNoble.getRequirements()
        );
        System.out.println("Bob bonus gems: " + p2.getBonusGems());
        System.out.println("Can visit? " + firstNoble.canVisit(p2));

        // ── 8. Test WinChecker ───────────────────────────────────────
        System.out.println("\n=== WinChecker ===");

        WinChecker winChecker = new WinChecker();
        System.out.println("Alice prestige: " + p1.getPrestigePoints());
        System.out.println("Bob prestige: " + p2.getPrestigePoints());
        System.out.println(
            "Triggered (threshold 15)? " + winChecker.hasTriggered(players, 15)
        );

        List<Player> winners = winChecker.getWinners(players);
        System.out.println("Current winner(s): ");
        for (Player w : winners) {
            System.out.println(
                "  " +
                    w.getName() +
                    " (" +
                    w.getPrestigePoints() +
                    " pts, " +
                    w.getPurchasedCardCount() +
                    " cards)"
            );
        }

        // ── 9. Test discard logic ────────────────────────────────────
        System.out.println("\n=== Discard Check ===");

        System.out.println("Bob total gems: " + p2.getTotalGems());
        int excess = p2.getTotalGems() - 10;
        System.out.println("Excess over 10: " + Math.max(0, excess));

        // ── 10. Summary ─────────────────────────────────────────────
        System.out.println("\n=== All Tests Complete ===");
        System.out.println("Alice: " + p1);
        System.out.println("Bob: " + p2);
    }

    /**
     * Builds a Board manually since Board has no constructor yet.
     * Uses CardLoader to draw visible cards and NobleLoader to draw nobles.
     * Depricated after board constructor fix
     */
    // private static Board buildBoard(
    //     CardLoader cardLoader,
    //     NobleLoader nobleLoader,
    //     int numPlayers
    // ) throws FileNotFoundException {
    //     //Board board = new Board();

    //     // Use reflection-like approach: Board fields are package-private-ish
    //     // Since Board is a stub we control, set fields via a helper
    //     // Actually Board fields are private — we need to init them through the class
    //     // Let's add a temp init method or use the fact that fields default to null
    //     // For now, create a proper board setup using a subclass or direct field init

    //     // We'll work around this by creating a proper board via init
    //     // return initBoard(board, cardLoader, nobleLoader, numPlayers);
    // }

    /* Depricated after board constructor fix */
    private static Board initBoard(
        Board board,
        CardLoader cardLoader,
        NobleLoader nobleLoader,
        int numPlayers
    ) {
        // Board fields are private — need to use a workaround
        // Since Board is our stub, let's just create one with reflection
        try {
            java.lang.reflect.Field gemBankField = Board.class.getDeclaredField(
                "gemBank"
            );
            gemBankField.setAccessible(true);
            Map<GemType, Integer> gemBank = new EnumMap<>(GemType.class);
            int gemsPerColour = (numPlayers == 2)
                ? 4
                : (numPlayers == 3)
                    ? 5
                    : 7;
            for (GemType gem : GemType.values()) {
                if (gem == GemType.GOLD) {
                    gemBank.put(gem, 5);
                } else {
                    gemBank.put(gem, gemsPerColour);
                }
            }
            gemBankField.set(board, gemBank);

            java.lang.reflect.Field decksField = Board.class.getDeclaredField(
                "decks"
            );
            decksField.setAccessible(true);
            Deck[] decks = new Deck[3];
            for (int i = 0; i < 3; i++) {
                int level = i + 1;
                // Draw remaining cards into deck
                decks[i] = new Deck(
                    new ArrayList<>(cardLoader.getAvailCards(level))
                );
            }
            decksField.set(board, decks);

            java.lang.reflect.Field visibleField = Board.class.getDeclaredField(
                "visibleCards"
            );
            visibleField.setAccessible(true);
            Card[][] visibleCards = new Card[3][4];
            for (int level = 0; level < 3; level++) {
                for (int slot = 0; slot < 4; slot++) {
                    Card drawn = cardLoader.drawCard(level + 1);
                    visibleCards[level][slot] = drawn;
                }
            }
            visibleField.set(board, visibleCards);

            java.lang.reflect.Field noblesField = Board.class.getDeclaredField(
                "nobles"
            );
            noblesField.setAccessible(true);
            List<Noble> nobles = nobleLoader.drawNobles(numPlayers);
            noblesField.set(board, new ArrayList<>(nobles));
        } catch (Exception e) {
            throw new RuntimeException(
                "Failed to init board for testing: " + e.getMessage(),
                e
            );
        }

        return board;
    }
}
