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

public interface PlayerLogic {
    ActionType chooseAction(Player player, Board board, ActionValidator validator);
    Map<GemType, Integer> chooseTake3Gems(Player player, Board board, ActionValidator validator);
    GemType chooseTake2Gems(Player player, Board board, ActionValidator validator);
    int[] chooseReserveCard(Player player, Board board, ActionValidator validator);
    Card chooseBuyCard(Player player, Board board, ActionValidator validator);
    Map<GemType, Integer> chooseDiscard(Player player, int excess);
    Noble chooseNoble(Player player, List<Noble> nobles);
}
