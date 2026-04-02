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
import splendor.ui.ConsoleActionUI;
import splendor.ui.ConsoleDisplayUI;
import splendor.ui.ConsoleFormatter;
import splendor.ui.ConsoleSetupUI;
import splendor.ui.ConsoleTerminal;

public class SplendorGame {
    public static void main(String[] args) throws IOException {
        ConsoleTerminal terminal = new ConsoleTerminal();
        ConsoleFormatter formatter = new ConsoleFormatter();
        ConsoleDisplayUI displayUI = new ConsoleDisplayUI(terminal, formatter);
        ConsoleSetupUI setupUI = new ConsoleSetupUI(terminal, formatter);
        ConsoleActionUI actionUI = new ConsoleActionUI(terminal, formatter);

        // main menu
        
        int startStatus = setupUI.displayMainMenu();
        if (startStatus == 2) {
            return;
        }
        displayUI.clearScreen();
        //load game config 
        GameConfig config = new GameConfig();
        config.load("config.properties");
        
        // asking number of players 
        int numOfPlayers = setupUI.promptPlayerCount();

        List<Player> players = new ArrayList<>();

        // for each player, ask name and type 
        for (int i = 0; i < numOfPlayers; i++) {
            String name = setupUI.promptPlayerName(i);
            String type = setupUI.promptPlayerType(i).trim().toLowerCase(); 

            Player p = new Player(name);

            if (type.equals("human")) {
                p.setLogic(new HumanPlayerLogic(actionUI));
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
        cards.addAll(cardLoader.getAvailCards(1));
        cards.addAll(cardLoader.getAvailCards(2));
        cards.addAll(cardLoader.getAvailCards(3));

        List<Noble> nobles = nobleLoader.getAllNobles();

        // build board 
        Board board = new Board(config, cards, nobles, numOfPlayers);
        
        WinChecker winChecker = new WinChecker();
        ActionValidator actionValidator = new ActionValidator();

        GameEngine engine = new GameEngine(players, board, displayUI, config, winChecker, actionValidator);

        engine.start();

    }

   
    
}
