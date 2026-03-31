package splendor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import splendor.config.GameConfig;
import splendor.data.CardLoader;
import splendor.data.NobleLoader;
import splendor.engine.ActionValidator;
import splendor.engine.GameEngine;
import splendor.engine.WinChecker;
import splendor.logic.AIPlayerLogic;
import splendor.logic.HumanPlayerLogic;
import splendor.model.Board;
import splendor.model.Card;
import splendor.model.Noble;
import splendor.model.Player;
import splendor.ui.ConsoleUI;

public class SplendorGame {
    public static void main(String[] args) throws IOException {
        // main menu
        ConsoleUI ui = new ConsoleUI();
        
        int startStatus = ui.displayMainMenu();
        if (startStatus == 2) {
            return;
        }
        ui.clearScreen();
        //load game config 
        GameConfig config = new GameConfig();
        config.load("config.properties");
        
        // asking number of players 
        int numOfPlayers = ui.promptPlayerCount();

        List<Player> players = new ArrayList<>();

        // for each player, ask name and type 
        String name;
        String type;
        for (int i = 0; i < numOfPlayers; i++) {
            name = ui.promptPlayerName(i);
            type = ui.promptPlayerType(i).trim().toLowerCase(); 

            Player p = new Player(name);

            if (type.equals("human")) {
                p.setLogic(new HumanPlayerLogic(ui));
            } else if (type.equals("ai")) {
                p.setLogic(new AIPlayerLogic(players));
            }
            // create player list
            players.add(p);
        }

        // load cards and nobles 
        CardLoader cardLoader = new CardLoader(config);
        NobleLoader nobleLoader = new NobleLoader(config);

        List<Card> cards = new ArrayList<>();
        cards.addAll(cardLoader.getAllCards(1));
        cards.addAll(cardLoader.getAllCards(2));
        cards.addAll(cardLoader.getAllCards(3));

        List<Noble> nobles = nobleLoader.getAllNobles();

        // build board 
        Board board = new Board(config, cards, nobles, numOfPlayers);
        
        WinChecker winChecker = new WinChecker();
        ActionValidator actionValidator = new ActionValidator();

        GameEngine engine = new GameEngine(players, board, ui, config, winChecker, actionValidator);

        engine.start();

    }

   
    
}
