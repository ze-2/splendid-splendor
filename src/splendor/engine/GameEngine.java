package splendor.engine;

import splendor.model.*;
import splendor.ui.ConsoleUI;
import splendor.config.GameConfig;

import java.util.*;

public class GameEngine {

    private final List<Player> players;
    private final Board board;
    private final ConsoleUI ui;
    private final GameConfig config;
    private final WinChecker winChecker;
    private final ActionValidator validator;

    public GameEngine(List<Player> players, Board board, ConsoleUI ui,
                      GameConfig config, WinChecker winChecker, ActionValidator validator) {
        this.players = players;
        this.board = board;
        this.ui = ui;
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
        ui.displayWinner(winners);
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

            ui.displayBoard(board);

            // Show all players' statuses
            for (Player p : players) {
                ui.displayPlayerStatus(p, p == player);
            }

            ui.displayTurnHeader(player);
            playTurn(player);

            if (player.getPrestigePoints() >= config.getWinningPoints()) {
                hasWinner = true;
            }
        }

        return hasWinner;
    }

    // ── Single turn ──────────────────────────────────────────────────

    private void playTurn(Player player) {
        // Step 1: Choose and execute action
        ActionType action = chooseAction(player);
        if (action != null) {
            executeAction(player, action);
        }

        // Step 2: Handle discard if over 10 gems
        handleDiscard(player);

        // Step 3: Check noble visits
        checkNobles(player);
    }

    // ── Action selection (human/AI branching) ────────────────────────

    private ActionType chooseAction(Player player) {
        if (player instanceof HumanPlayer) {
            return ui.promptAction(player, board, validator);
        } else {
            return ((AIPlayer) player).chooseAction(board, validator);
        }
    }

    // ── Action dispatch ──────────────────────────────────────────────

    private void executeAction(Player player, ActionType action) {
        switch (action) {
            case TAKE_THREE:
                executeTake3(player);
                break;
            case TAKE_TWO:
                executeTake2(player);
                break;
            case RESERVE:
                executeReserve(player);
                break;
            case BUY:
                executeBuy(player);
                break;
        }
    }

    // ── Execute: Take 3 different gems ───────────────────────────────

    private void executeTake3(Player player) {
        Map<GemType, Integer> chosen;

        if (player instanceof HumanPlayer) {
            chosen = ui.promptTake3Gems(board, validator);
        } else {
            chosen = ((AIPlayer) player).chooseTake3Gems(board, validator);
        }

        board.takeGems(chosen);
        for (Map.Entry<GemType, Integer> entry : chosen.entrySet()) {
            player.addGem(entry.getKey(), entry.getValue());
        }
    }

    // ── Execute: Take 2 same colour ─────────────────────────────────

    private void executeTake2(Player player) {
        GemType colour;

        if (player instanceof HumanPlayer) {
            colour = ui.promptTake2Gems(board, validator);
        } else {
            colour = ((AIPlayer) player).chooseTake2Gems(board, validator);
        }

        Map<GemType, Integer> toTake = new EnumMap<>(GemType.class);
        toTake.put(colour, 2);
        board.takeGems(toTake);
        player.addGem(colour, 2);
    }

    // ── Execute: Reserve a card ──────────────────────────────────────

    private void executeReserve(Player player) {
        int[] selection;

        if (player instanceof HumanPlayer) {
            selection = ui.promptReserveCard(player, board, validator);
        } else {
            selection = ((AIPlayer) player).chooseReserveCard(board, validator);
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
    }

    // ── Execute: Buy a card ──────────────────────────────────────────

    private void executeBuy(Player player) {
        Card card;

        if (player instanceof HumanPlayer) {
            card = ui.promptBuyCard(player, board, validator);
        } else {
            card = ((AIPlayer) player).chooseBuyCard(board, validator);
        }

        // Remove card from board if it's a visible card
        removeCardFromBoard(card);

        // Player pays gems, adds card to purchased, removes from reserved if applicable
        Map<GemType, Integer> spent = player.buyCard(card);

        // Return spent gems to bank
        board.returnGems(spent);
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

        Map<GemType, Integer> toDiscard;

        if (player instanceof HumanPlayer) {
            toDiscard = ui.promptDiscardGems(player, excess);
        } else {
            toDiscard = ((AIPlayer) player).chooseDiscard(excess);
        }

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
        } else if (player instanceof HumanPlayer) {
            chosen = ui.promptNobleChoice(eligible);
        } else {
            chosen = ((AIPlayer) player).chooseNoble(eligible);
        }

        player.addNoble(chosen);
        board.removeNoble(chosen);
        System.out.println(player.getName() + " is visited by a noble! (+3 prestige)");
    }

}
