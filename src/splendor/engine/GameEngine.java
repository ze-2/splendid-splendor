package splendor.engine;

import splendor.config.GameConfig;
import splendor.model.AIPlayer;
import splendor.model.ActionType;
import splendor.model.Board;
import splendor.model.Card;
import splendor.model.GemType;
import splendor.model.Noble;
import splendor.model.Player;
import splendor.ui.ConsoleUI;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class GameEngine {
    private final List<Player> players;
    private final Board board;
    private final ConsoleUI ui;
    private final GameConfig config;
    private final WinChecker winChecker;
    private final ActionValidator validator;

    public GameEngine(List<Player> players, Board board, ConsoleUI ui, GameConfig config) {
        this.players = players;
        this.board = board;
        this.ui = ui;
        this.config = config;
        this.winChecker = new WinChecker();
        this.validator = new ActionValidator();
    }

    /**
     * Main game loop. Runs full rounds until at least one player reaches the
     * winning threshold, then finishes that round and determines the winner.
     */
    public void start() {
        boolean triggered = false;

        while (true) {
            for (int i = 0; i < players.size(); i++) {
                Player player = players.get(i);

                ui.displayBoard(board);
                ui.displayTurnHeader(player);
                ui.displayPlayerStatus(player);

                // Get action choice
                ActionType action;
                if (player.isHuman()) {
                    action = ui.promptAction(player, board, validator);
                } else {
                    AIPlayer ai = (AIPlayer) player;
                    action = ai.chooseAction(board, validator);
                    if (action != null) {
                        System.out.println(player.getName() + " chooses: " + describeAction(action));
                    } else {
                        System.out.println(player.getName() + " passes.");
                    }
                }

                if (action != null) {
                    executeAction(player, action);
                }

                handleDiscard(player);
                checkNobles(player);
                ui.displayPlayerStatus(player);

                if (player.getPrestigePoints() >= config.getWinningPoints()) {
                    triggered = true;
                }
            }

            if (triggered) {
                break;
            }
        }

        List<Player> winners = winChecker.getWinners(players);
        ui.displayWinner(winners);
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

    private void executeTake3(Player player) {
        Map<GemType, Integer> gems;
        if (player.isHuman()) {
            gems = ui.promptTake3Gems(board, validator);
        } else {
            gems = ((AIPlayer) player).chooseTake3Gems(board, validator);
            System.out.println(player.getName() + " takes gems: " + formatGems(gems));
        }
        board.takeGems(gems);
        for (Map.Entry<GemType, Integer> entry : gems.entrySet()) {
            player.addGem(entry.getKey(), entry.getValue());
        }
    }

    private void executeTake2(Player player) {
        GemType gem;
        if (player.isHuman()) {
            gem = ui.promptTake2Gems(board, validator);
        } else {
            gem = ((AIPlayer) player).chooseTake2Gems(board, validator);
            System.out.println(player.getName() + " takes 2 " + gem);
        }
        Map<GemType, Integer> gems = new EnumMap<>(GemType.class);
        gems.put(gem, 2);
        board.takeGems(gems);
        player.addGem(gem, 2);
    }

    private void executeReserve(Player player) {
        int[] choice;
        if (player.isHuman()) {
            choice = ui.promptReserveCard(player, board, validator);
        } else {
            choice = ((AIPlayer) player).chooseReserveCard(board, validator);
        }

        int level = choice[0]; // 0-indexed
        int slot = choice[1];  // -1 = from deck
        Card card;

        if (slot >= 0) {
            card = board.takeVisibleCard(level, slot);
            if (!player.isHuman()) {
                System.out.println(player.getName() + " reserves: " + card);
            }
        } else {
            card = board.takeReserveCard(level);
            if (player.isHuman()) {
                System.out.println("You reserved from deck: " + card);
            } else {
                System.out.println(player.getName() + " reserves a card from Level " + (level + 1) + " deck.");
            }
        }

        if (card != null) {
            player.reserveCard(card);
            if (board.getGemBank().getOrDefault(GemType.GOLD, 0) > 0) {
                Map<GemType, Integer> goldGem = new EnumMap<>(GemType.class);
                goldGem.put(GemType.GOLD, 1);
                board.takeGems(goldGem);
                player.addGem(GemType.GOLD, 1);
                System.out.println(player.getName() + " receives a gold gem.");
            }
        }

        // Hotseat privacy: hide face-down card after showing to the current player
        if (slot < 0 && player.isHuman()) {
            ui.waitForEnter();
            ui.clearScreen();
        }
    }

    private void executeBuy(Player player) {
        Card card;
        if (player.isHuman()) {
            card = ui.promptBuyCard(player, board, validator);
        } else {
            card = ((AIPlayer) player).chooseBuyCard(board, validator);
            System.out.println(player.getName() + " buys: " + card);
        }

        if (card != null) {
            // Remove from board if it is a visible card
            boolean fromVisible = false;
            Card[][] visible = board.getVisibleCards();
            for (int level = 0; level < 3 && !fromVisible; level++) {
                for (int slot = 0; slot < 4; slot++) {
                    if (visible[level][slot] == card) {
                        board.takeVisibleCard(level, slot);
                        fromVisible = true;
                        break;
                    }
                }
            }
            // If not visible, it is a reserved card — handled inside buyCard()

            Map<GemType, Integer> spent = player.buyCard(card);
            board.returnGems(spent);
        }
    }

    // ── Post-action phases ───────────────────────────────────────────

    private void handleDiscard(Player player) {
        int excess = validator.mustDiscard(player);
        if (excess <= 0) return;

        Map<GemType, Integer> toDiscard;
        if (player.isHuman()) {
            toDiscard = ui.promptDiscardGems(player, excess);
        } else {
            toDiscard = ((AIPlayer) player).chooseDiscardGems(excess);
            System.out.println(player.getName() + " discards: " + formatGems(toDiscard));
        }

        for (Map.Entry<GemType, Integer> entry : toDiscard.entrySet()) {
            player.removeGem(entry.getKey(), entry.getValue());
        }
        board.returnGems(toDiscard);
    }

    private void checkNobles(Player player) {
        List<Noble> eligible = new ArrayList<>();
        for (Noble noble : board.getNobles()) {
            if (noble.canVisit(player)) {
                eligible.add(noble);
            }
        }
        if (eligible.isEmpty()) return;

        Noble chosen;
        if (eligible.size() == 1) {
            chosen = eligible.get(0);
        } else if (player.isHuman()) {
            chosen = ui.promptNobleChoice(eligible);
        } else {
            chosen = ((AIPlayer) player).chooseNoble(eligible);
        }

        player.addNoble(chosen);
        board.removeNoble(chosen);
        System.out.println("Noble visits " + player.getName()
                + "! +" + chosen.getPrestigePoints() + " prestige points.");
    }

    // ── Formatting helpers ───────────────────────────────────────────

    private String describeAction(ActionType action) {
        switch (action) {
            case TAKE_THREE: return "Take 3 different gems";
            case TAKE_TWO:   return "Take 2 same-colour gems";
            case RESERVE:    return "Reserve a card";
            case BUY:        return "Buy a card";
            default:         return action.name();
        }
    }

    private String formatGems(Map<GemType, Integer> gems) {
        StringJoiner sj = new StringJoiner(", ");
        for (Map.Entry<GemType, Integer> entry : gems.entrySet()) {
            if (entry.getValue() > 0) {
                sj.add(entry.getKey() + "=" + entry.getValue());
            }
        }
        return sj.toString();
    }
}
