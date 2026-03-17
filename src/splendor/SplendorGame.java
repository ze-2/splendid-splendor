package splendor;

import splendor.config.GameConfig;
import splendor.data.CardLoader;
import splendor.data.NobleLoader;
import splendor.engine.GameEngine;
import splendor.model.AIPlayer;
import splendor.model.Board;
import splendor.model.Card;
import splendor.model.HumanPlayer;
import splendor.model.Noble;
import splendor.model.Player;
import splendor.ui.ConsoleUI;

import java.util.ArrayList;
import java.util.List;

public class SplendorGame {

    public static void main(String[] args) {
        GameConfig config = new GameConfig();
        config.load("config.properties");

        ConsoleUI ui = new ConsoleUI();

        // Player setup
        int numPlayers = ui.promptPlayerCount();
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < numPlayers; i++) {
            String name = ui.promptPlayerName(i);
            String type = ui.promptPlayerType(i);
            if (type.equals("human")) {
                players.add(new HumanPlayer(name));
            } else {
                players.add(new AIPlayer(name));
            }
        }

        // Load cards from all three levels
        List<Card> allCards = new ArrayList<>();
        for (int level = 1; level <= 3; level++) {
            allCards.addAll(CardLoader.loadCards(config.getCardDataPath(level), level));
        }

        // Load nobles
        List<Noble> allNobles = NobleLoader.loadNobles(config.getNobleDataPath());

        // Build board and start the game
        Board board = new Board(config, allCards, allNobles, numPlayers);
        GameEngine engine = new GameEngine(players, board, ui, config);
        engine.start();
    }
}
