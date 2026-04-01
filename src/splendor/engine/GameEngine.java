package splendor.engine;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import splendor.config.GameConfig;
import splendor.model.ActionType;
import splendor.model.Board;
import splendor.model.Card;
import splendor.model.GemType;
import splendor.model.Noble;
import splendor.model.Player;
import splendor.ui.ConsoleDisplayUI;

public class GameEngine {

    private final List<Player> players;
    private final Board board;
    private final ConsoleDisplayUI displayUI;
    private final GameConfig config;
    private final WinChecker winChecker;
    private final ActionValidator validator;

    public GameEngine(List<Player> players, Board board, ConsoleDisplayUI displayUI,
                      GameConfig config, WinChecker winChecker, ActionValidator validator) {
        this.players = players;
        this.board = board;
        this.displayUI = displayUI;
        this.config = config;
        this.winChecker = winChecker;
        this.validator = validator;
    }

    // ── Main game loop ───────────────────────────────────────────────

    public void start() {
        boolean hasWinner = false;

        while (!hasWinner) {
            hasWinner = playRound();
        }

        List<Player> winners = winChecker.getWinners(players);
        displayUI.displayWinner(winners);
    }

    /**
     * Plays one full round (every player takes one turn).
     * Returns true if any player reached the winning threshold.
     * Does NOT break mid-round — all players finish the round.
     */
    private boolean playRound() {
        boolean hasWinner = false;

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);

            displayUI.clearScreen();
            displayUI.displayBoard(board);

            // Show all players' statuses
            for (Player p : players) {
                displayUI.displayPlayerStatus(p, p == player);
            }

            displayUI.displayTurnHeader(player);
            playTurn(player);

            if (player.getPrestigePoints() >= config.getWinningPoints()) {
                hasWinner = true;
            }
        }

        return hasWinner;
    }

    // ── Single turn ──────────────────────────────────────────────────

    private void playTurn(Player player) {
        // Step 1: Choose and execute action (loop if player backs out)
        boolean actionDone = false;
        while (!actionDone) {
            ActionType action = chooseAction(player);
            if (action != null) {
                actionDone = executeAction(player, action);
                if (!actionDone) {
                    // Player backed out — redraw the screen cleanly
                    displayUI.clearScreen();
                    displayUI.displayBoard(board);
                    for (Player p : players) {
                        displayUI.displayPlayerStatus(p, p == player);
                    }
                    displayUI.displayTurnHeader(player);
                }
            } else {
                actionDone = true; // no actions available, pass turn
            }
        }

        // Step 2: Handle discard if over 10 gems
        handleDiscard(player);

        // Step 3: Check noble visits
        checkNobles(player);
    }

    // ── Action selection (human/AI branching) ────────────────────────

    private ActionType chooseAction(Player player) {
        return player.getLogic().chooseAction(player, board, validator);
    }

    // ── Action dispatch ──────────────────────────────────────────────

    private boolean executeAction(Player player, ActionType action) {
        switch (action) {
            case TAKE_THREE:
                return executeTake3(player);
            case TAKE_TWO:
                return executeTake2(player);
            case RESERVE:
                return executeReserve(player);
            case BUY:
                return executeBuy(player);
            default:
                return true;
        }
    }

    // ── Execute: Take 3 different gems ───────────────────────────────

    private boolean executeTake3(Player player) {
        Map<GemType, Integer> chosen = player.getLogic().chooseTake3Gems(player, board, validator);
        if (chosen == null) {
            return false;
        }

        board.takeGems(chosen);
        for (Map.Entry<GemType, Integer> entry : chosen.entrySet()) {
            player.addGem(entry.getKey(), entry.getValue());
        }
        return true;
    }

    // ── Execute: Take 2 same colour ─────────────────────────────────

    private boolean executeTake2(Player player) {
        GemType colour = player.getLogic().chooseTake2Gems(player, board, validator);
        if (colour == null) {
            return false;
        }

        Map<GemType, Integer> toTake = new EnumMap<>(GemType.class);
        toTake.put(colour, 2);
        board.takeGems(toTake);
        player.addGem(colour, 2);
        return true;
    }

    // ── Execute: Reserve a card ──────────────────────────────────────

    private boolean executeReserve(Player player) {
        int[] selection = player.getLogic().chooseReserveCard(player, board, validator);
        if (selection == null) {
            return false;
        }

        int level = selection[0];
        int slot = selection[1];
        Card card;

        if (slot >= 0) {
            card = board.takeVisibleCard(level, slot);
        } else {
            card = board.takeReserveCard(level);
        }

        player.reserveCard(card);

        // Grant gold token if available
        int goldAvailable = board.getGemBank().getOrDefault(GemType.GOLD, 0);
        if (goldAvailable > 0) {
            Map<GemType, Integer> goldTake = new EnumMap<>(GemType.class);
            goldTake.put(GemType.GOLD, 1);
            board.takeGems(goldTake);
            player.addGem(GemType.GOLD, 1);
        }
        return true;
    }

    // ── Execute: Buy a card ──────────────────────────────────────────

    private boolean executeBuy(Player player) {
        Card card = player.getLogic().chooseBuyCard(player, board, validator);
        if (card == null) {
            return false;
        }

        // Remove card from board if it's a visible card
        removeCardFromBoard(card);

        // Player pays gems, adds card to purchased, removes from reserved if applicable
        Map<GemType, Integer> spent = player.buyCard(card);

        // Return spent gems to bank
        board.returnGems(spent);
        return true;
    }

    /**
     * Removes a card from visible board slots if found.
     * If the card is from the player's reserved list, buyCard() handles removal.
     */
    private void removeCardFromBoard(Card card) {
        Card[][] visible = board.getVisibleCards();
        for (int level = 0; level < 3; level++) {
            for (int slot = 0; slot < 4; slot++) {
                if (visible[level][slot] == card) {
                    board.takeVisibleCard(level, slot);
                    return;
                }
            }
        }
    }

    // ── Handle discard (over 10 gems) ────────────────────────────────

    private void handleDiscard(Player player) {
        int excess = player.getTotalGems() - 10;
        if (excess <= 0) {
            return;
        }

        Map<GemType, Integer> toDiscard = player.getLogic().chooseDiscard(player, excess);

        for (Map.Entry<GemType, Integer> entry : toDiscard.entrySet()) {
            player.removeGem(entry.getKey(), entry.getValue());
        }
        board.returnGems(toDiscard);
    }

    // ── Check noble visits ───────────────────────────────────────────

    private void checkNobles(Player player) {
        List<Noble> eligible = new ArrayList<>();

        for (Noble noble : board.getNobles()) {
            if (noble.canVisit(player)) {
                eligible.add(noble);
            }
        }

        if (eligible.isEmpty()) {
            return;
        }

        Noble chosen;
        if (eligible.size() == 1) {
            chosen = eligible.get(0);
        } else {
            chosen = player.getLogic().chooseNoble(player, eligible);
        }

        player.addNoble(chosen);
        board.removeNoble(chosen);
        System.out.println(player.getName() + " is visited by a noble! (+3 prestige)");
    }

}
