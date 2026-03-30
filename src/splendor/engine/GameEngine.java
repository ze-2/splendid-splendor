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

            ui.clearScreen();
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
        // Step 1: Choose and execute action (loop if player backs out)
        boolean actionDone = false;
        while (!actionDone) {
            ActionType action = chooseAction(player);
            if (action != null) {
                actionDone = executeAction(player, action);
                if (!actionDone) {
                    // Player backed out — redraw the screen cleanly
                    ui.clearScreen();
                    ui.displayBoard(board);
                    for (Player p : players) {
                        ui.displayPlayerStatus(p, p == player);
                    }
                    ui.displayTurnHeader(player);
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
        if (player instanceof HumanPlayer) {
            return ui.promptAction(player, board, validator);
        } else {
            // TODO: Replace with ((AIPlayer) player).chooseAction(board, validator) when P5 delivers
            return aiChooseAction(player);
        }
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
        Map<GemType, Integer> chosen;

        if (player instanceof HumanPlayer) {
            chosen = ui.promptTake3Gems(board, validator);
            if (chosen == null) return false;
        } else {
            // TODO: Replace with ((AIPlayer) player).chooseTake3Gems(board, validator)
            chosen = aiChooseTake3();
        }

        board.takeGems(chosen);
        for (Map.Entry<GemType, Integer> entry : chosen.entrySet()) {
            player.addGem(entry.getKey(), entry.getValue());
        }
        return true;
    }

    // ── Execute: Take 2 same colour ─────────────────────────────────

    private boolean executeTake2(Player player) {
        GemType colour;

        if (player instanceof HumanPlayer) {
            colour = ui.promptTake2Gems(board, validator);
            if (colour == null) return false;
        } else {
            // TODO: Replace with ((AIPlayer) player).chooseTake2Gems(board, validator)
            colour = aiChooseTake2();
        }

        Map<GemType, Integer> toTake = new EnumMap<>(GemType.class);
        toTake.put(colour, 2);
        board.takeGems(toTake);
        player.addGem(colour, 2);
        return true;
    }

    // ── Execute: Reserve a card ──────────────────────────────────────

    private boolean executeReserve(Player player) {
        int[] selection;

        if (player instanceof HumanPlayer) {
            selection = ui.promptReserveCard(player, board, validator);
            if (selection == null) return false;
        } else {
            // TODO: Replace with ((AIPlayer) player).chooseReserveCard(board, validator)
            selection = aiChooseReserve();
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
        Card card;

        if (player instanceof HumanPlayer) {
            card = ui.promptBuyCard(player, board, validator);
            if (card == null) return false;
        } else {
            // TODO: Replace with ((AIPlayer) player).chooseBuyCard(board, validator)
            card = aiChooseBuy(player);
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

        Map<GemType, Integer> toDiscard;

        if (player instanceof HumanPlayer) {
            toDiscard = ui.promptDiscardGems(player, excess);
        } else {
            // TODO: Replace with ((AIPlayer) player).chooseDiscard(excess)
            toDiscard = aiChooseDiscard(player, excess);
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
            if (canVisit(noble, player)) {
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
            // TODO: Replace with ((AIPlayer) player).chooseNoble(eligible)
            chosen = eligible.get(0);
        }

        player.addNoble(chosen);
        board.removeNoble(chosen);
        System.out.println(player.getName() + " is visited by a noble! (+3 prestige)");
    }

    /**
     * Checks if a noble's bonus gem requirements are met by the player.
     * Inline helper until Noble.canVisit(Player) is added to the model class.
     */
    private boolean canVisit(Noble noble, Player player) {
        Map<GemType, Integer> bonuses = player.getBonusGems();
        for (Map.Entry<GemType, Integer> entry : noble.getRequirements().entrySet()) {
            if (entry.getValue() > 0) {
                int has = bonuses.getOrDefault(entry.getKey(), 0);
                if (has < entry.getValue()) {
                    return false;
                }
            }
        }
        return true;
    }

    // ── AI stub methods ──────────────────────────────────────────────
    // TODO: Remove all stubs below when P5 delivers AIPlayer

    private ActionType aiChooseAction(Player player) {
        List<ActionType> available = validator.getAvailableActions(player, board);
        if (available.isEmpty()) {
            return null;
        }
        // Prefer BUY if available, else TAKE_THREE, else first available
        if (available.contains(ActionType.BUY)) return ActionType.BUY;
        if (available.contains(ActionType.TAKE_THREE)) return ActionType.TAKE_THREE;
        return available.get(0);
    }

    private Map<GemType, Integer> aiChooseTake3() {
        Map<GemType, Integer> chosen = new EnumMap<>(GemType.class);
        for (GemType gem : GemType.values()) {
            if (gem == GemType.GOLD) continue;
            if (board.getGemBank().getOrDefault(gem, 0) > 0) {
                chosen.put(gem, 1);
                if (chosen.size() == 3) break;
            }
        }
        return chosen;
    }

    private GemType aiChooseTake2() {
        for (GemType gem : GemType.values()) {
            if (gem == GemType.GOLD) continue;
            if (board.getGemBank().getOrDefault(gem, 0) >= 4) {
                return gem;
            }
        }
        return GemType.RUBY; // fallback
    }

    private int[] aiChooseReserve() {
        // Reserve first available visible card
        Card[][] visible = board.getVisibleCards();
        for (int level = 2; level >= 0; level--) {
            for (int slot = 0; slot < 4; slot++) {
                if (visible[level][slot] != null) {
                    return new int[]{level, slot};
                }
            }
        }
        return new int[]{0, -1}; // fallback: top of level 1 deck
    }

    private Card aiChooseBuy(Player player) {
        // Buy first affordable visible card, preferring higher prestige
        Card[][] visible = board.getVisibleCards();
        Card best = null;
        int bestLevel = -1;
        int bestSlot = -1;

        for (int level = 2; level >= 0; level--) {
            for (int slot = 0; slot < 4; slot++) {
                Card c = visible[level][slot];
                if (c != null && validator.canBuy(player, c)) {
                    if (best == null || c.getPrestigePoints() > best.getPrestigePoints()) {
                        best = c;
                        bestLevel = level;
                        bestSlot = slot;
                    }
                }
            }
        }

        // Also check reserved cards
        for (Card c : player.getReservedCards()) {
            if (validator.canBuy(player, c)) {
                if (best == null || c.getPrestigePoints() > best.getPrestigePoints()) {
                    best = c;
                }
            }
        }

        return best;
    }

    private Map<GemType, Integer> aiChooseDiscard(Player player, int excess) {
        Map<GemType, Integer> toDiscard = new EnumMap<>(GemType.class);
        int remaining = excess;

        // Discard gold first, then least useful gems
        for (GemType gem : new GemType[]{GemType.GOLD, GemType.RUBY, GemType.EMERALD,
                GemType.SAPPHIRE, GemType.DIAMOND, GemType.ONYX}) {
            int held = player.getGems().getOrDefault(gem, 0);
            if (held > 0 && remaining > 0) {
                int discard = Math.min(held, remaining);
                toDiscard.put(gem, discard);
                remaining -= discard;
            }
            if (remaining <= 0) break;
        }

        return toDiscard;
    }
}
