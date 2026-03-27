package splendor.model;

import java.util.List;
import java.util.Map;
import splendor.engine.*;
import splendor.ui.*;

public class HumanPlayer extends Player {

    private final ConsoleUI ui;

    public HumanPlayer(String name, ConsoleUI ui) {
        super(name);
        this.ui = ui;
    }

    @Override
    public ActionType chooseAction(Board board, ActionValidator validator) {
        return ui.promptAction(this, board, validator);
    }

    @Override
    public Map<GemType, Integer> chooseTake3Gems(Board board, ActionValidator validator) {
        return ui.promptTake3Gems(board, validator);
    }

    @Override
    public GemType chooseTake2Gems(Board board, ActionValidator validator) {
        return ui.promptTake2Gems(board, validator);
    }

    @Override
    public int[] chooseReserveCard(Board board, ActionValidator validator) {
        return ui.promptReserveCard(this, board, validator);
    }

    @Override
    public Card chooseBuyCard(Board board, ActionValidator validator) {
        return ui.promptBuyCard(this, board, validator);
    }

    @Override
    public Map<GemType, Integer> chooseDiscard(int excess) {
        return ui.promptDiscardGems(this, excess);
    }

    @Override
    public Noble chooseNoble(List<Noble> nobles) {
        return ui.promptNobleChoice(nobles);
    }
}
