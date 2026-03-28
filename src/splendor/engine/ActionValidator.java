package splendor.engine;

import splendor.model.*;
import java.util.*;


public class ActionValidator {

    public boolean canTake3(Board board, Map<GemType, Integer> selection) {
        if (selection == null || selection.isEmpty()) {
            return false;
        }

        // get non-empty and non-gold colors
        Map<GemType, Integer> bank = board.getGemBank();
        int availableColors = 0;
        for (GemType g : GemType.values()) {
            if (g != GemType.GOLD && bank.getOrDefault(g, 0) >= 1) {
                availableColors++;
            }
        }

        // rule: player cannot request more than available colors
        // note: can take <3 colors if there are <3 colors to take
        int requiredCount = Math.min(availableColors, 3);
        if (selection.size() != requiredCount) {
            return false;
        }

        for (Map.Entry<GemType, Integer> entry : selection.entrySet()) {
            GemType gem = entry.getKey();
            int qty = entry.getValue();

            // rule: cannot select gold out of reserve conditions
            if (gem == GemType.GOLD) {
                return false;
            }

            // rule: must select 1 gem (take 3 gems of 3 different colors)
            if (qty != 1) {
                return false;
            }

            // check that there is enough gems in the bank to take from
            if (bank.getOrDefault(gem, 0) < 1) {
                return false;
            }
        }

        return true;
    }

    public boolean canTake2(Board board, GemType color) {
        if (color == null || color == GemType.GOLD) {
            return false;
        }

        // rule: can take 2 of same color if num of gems is >= 4
        int available = board.getGemBank().getOrDefault(color, 0);
        return available >= 4;
    }

    public boolean canReserve(Player player, Board board, int level, int slot) {
        // rule: player cannot hold >= 3 reserved cards
        if (player.getReservedCards().size() >= 3) {
            return false;
        }

        // rule: take from levels 1-3
        if (level < 0 || level > 2) {
            return false;
        }

        // rule: check that card exists
        if (slot >= 0) {
            // take visible card
            Card[][] visible = board.getVisibleCards();

            // rule: check that cards for specified level exist by length
            if (slot >= visible[level].length) {
                return false;
            }

            // rule: check that there is actually a card at specified slot
            return visible[level][slot] != null;

        } else {
            // take face-down card

            // rule: can take as long as there's a card there
            return !board.getDecks()[level].isEmpty();
        }
    }

    public boolean canBuy(Player player, Card card) {
        if (card == null) {
            return false;
        }

        Map<GemType, Integer> cost = card.getCost();
        Map<GemType, Integer> playerGems = player.getGems();
        Map<GemType, Integer> bonuses = player.getBonusGems();

        int goldNeeded = 0;

        // calculates shortfall for each gem type
        for (Map.Entry<GemType, Integer> entry : cost.entrySet()) {
            GemType colour = entry.getKey();
            int required = entry.getValue();

            int bonus = bonuses.getOrDefault(colour, 0);
            int afterBonus = Math.max(0, required - bonus);

            int haveTokens = playerGems.getOrDefault(colour, 0);
            int shortfall = Math.max(0, afterBonus - haveTokens);
            goldNeeded += shortfall;
        }

        // if can purchase shortfall with gold
        int goldHeld = playerGems.getOrDefault(GemType.GOLD, 0);

        // rule: if not enough gems/gold, cannot buy card
        return goldNeeded <= goldHeld;
    }


    // returns num of gems over 10
    public int mustDiscard(Player player) {
        return Math.max(0, player.getTotalGems() - 10);
    }

    public List<ActionType> getAvailableActions(Player player, Board board) {
        List<ActionType> available = new ArrayList<>();

        // checks if can buy
        // loops through all visible cards, checks if any of them can be bought
        boolean canBuyAny = false;
        Card[][] visible = board.getVisibleCards();

        for (Card[] level : visible) {
            if (canBuyAny) {
                break;
            }

            for (Card card : level) {
                if (card != null && canBuy(player, card)) {
                    canBuyAny = true;
                    break;
                }
            }
        }

        // check reserved cards if none can be bought
        if (!canBuyAny) {
            for (Card reserved : player.getReservedCards()) {
                if (canBuy(player, reserved)) {
                    canBuyAny = true;
                    break;
                }
            }
        }

        if (canBuyAny) {
            available.add(ActionType.BUY);
        }

        // checks if can take 3 gems

        // rule: if there is at least 1 gem then player can take 3 gems
        // eg in case late stage not enough gem
        int nonEmptyColours = 0;
        for (GemType g : GemType.values()) {
            if (g != GemType.GOLD && board.getGemBank().getOrDefault(g, 0) >= 1) {
                nonEmptyColours++;
            }
        }

        if (nonEmptyColours >= 1) {
            available.add(ActionType.TAKE_THREE);
        }

        // checks if can take 2
        boolean canTake2Any = false;
        for (GemType g : GemType.values()) {
            if (g != GemType.GOLD && canTake2(board, g)) {
                canTake2Any = true;
                break;
            }
        }

        if (canTake2Any) {
            available.add(ActionType.TAKE_TWO);
        }

        // checks if can reserve card
        boolean canReserveAny = false;
        for (int lvl = 0; lvl < 3; lvl++) {
            // check each visible slot
            if (canReserveAny) {
                break;
            }

            if (!canReserveAny) {
                for (int slot = 0; slot < 4; slot++) {
                    if (canReserve(player, board, lvl, slot)) {
                        canReserveAny = true;
                        break;
                    }
                }
            }

            // check face-down deck
            if (!canReserveAny && canReserve(player, board, lvl, -1)) {
                canReserveAny = true;
            }
        }

        if (canReserveAny) {
            available.add(ActionType.RESERVE);
        }

        return available;
    }
}
