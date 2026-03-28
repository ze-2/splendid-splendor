package splendor;

import java.util.*;
import java.io.*;
import splendor.config.*;
import splendor.data.*;
import splendor.ui.*;
import splendor.model.*;
import splendor.engine.*;

public class SplendorGame {
    public static void main(String[] args) throws IOException {
        //load game config 
        GameConfig config = new GameConfig();
        config.load("config.properties");

        ConsoleUI ui = new ConsoleUI();
        // asking number of players 
        int numOfPlayers = ui.promptPlayerCount();

        List<Player> players = new ArrayList<>();

        // for each player, ask name and type 
        String name;
        String type;
        for (int i = 0; i < numOfPlayers; i++) {
            name = ui.promptPlayerName(i + 1);
            type = ui.promptPlayerType(i + 1); 

            Player p;

            if (type.equals("human")) {
                p = new HumanPlayer(name);
            }
            else if (type.equals("ai")) {
                p = new AIPlayer(name);
            }
            // create player list
            players.add(p);
        }

        // load cards and nobles 
        CardLoader cardLoader = new CardLoader();
        NobleLoader nobleLoader = new NobleLoader();

        List<Card> cards = new ArrayList<>();
        cards.addAll(cardLoader.getAllCards(1));
        cards.addAll(cardLoader.getAllCards(2));
        cards.addAll(cardLoader.getAllCards(3));

        List<Noble> nobles = nobleLoader.getAllNobles();

        // build board 
        Board board = new Board(config, cards, nobles, numOfPlayers);

        GameEngine engine = new GameEngine(players, board, ui, config);
        engine.start();

    }
}