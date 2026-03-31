package splendor.logic;

import java.util.List;
import java.util.Map;

import splendor.engine.ActionValidator;
import splendor.model.ActionType;
import splendor.model.Board;
import splendor.model.Card;
import splendor.model.GemType;
import splendor.model.Noble;
import splendor.model.Player;
import splendor.ui.ConsoleUI;

public class HumanPlayerLogic implements PlayerLogic {

    private final ConsoleUI ui;

    public HumanPlayerLogic(ConsoleUI ui) {
        this.ui = ui;
    }

    @Override
    public ActionType chooseAction(Player player, Board board, ActionValidator validator) {
        return ui.promptAction(player, board, validator);
    }

    @Override
    public Map<GemType, Integer> chooseTake3Gems(Player player, Board board, ActionValidator validator) {
        return ui.promptTake3Gems(board, validator);
    }

    @Override
    public GemType chooseTake2Gems(Player player, Board board, ActionValidator validator) {
        return ui.promptTake2Gems(board, validator);
    }

    @Override
    public int[] chooseReserveCard(Player player, Board board, ActionValidator validator) {
        return ui.promptReserveCard(player, board, validator);
    }

    @Override
    public Card chooseBuyCard(Player player, Board board, ActionValidator validator) {
        return ui.promptBuyCard(player, board, validator);
    }

    @Override
    public Map<GemType, Integer> chooseDiscard(Player player, int excess) {
        return ui.promptDiscardGems(player, excess);
    }

    @Override
    public Noble chooseNoble(Player player, List<Noble> nobles) {
        return ui.promptNobleChoice(nobles);
    }
}
