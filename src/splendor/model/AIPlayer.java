package splendor.model;

import splendor.engine.ActionValidator;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class AIPlayer extends Player {

    public AIPlayer(String name) {
        super(name);
    }

    @Override
    public boolean isHuman() {
        return false;
    }

    /**
     * Selects the best available action using a simple heuristic:
     * 1. Buy a high-value card (>= 3 prestige).
     * 2. Buy a card whose bonus helps toward a noble.
     * 3. Reserve a high-value card if < 3 reserved.
     * 4. Buy any affordable card.
     * 5. Take 3 different gems.
     * 6. Take 2 same-colour gems.
     * 7. Reserve as fallback.
     */
    public ActionType chooseAction(Board board, ActionValidator validator) {
        List<ActionType> available = validator.getAvailableActions(this, board);
        if (available.isEmpty()) {
            return null;
        }

        // Priority 1: Buy a high-value card
        if (available.contains(ActionType.BUY)) {
            Card best = chooseBuyCard(board, validator);
            if (best != null && best.getPrestigePoints() >= 3) {
                return ActionType.BUY;
            }
        }

        // Priority 2: Buy a card that helps toward a noble
        if (available.contains(ActionType.BUY)) {
            Card best = chooseBuyCard(board, validator);
            if (best != null) {
                Map<GemType, Integer> bonuses = getBonusGems();
                for (Noble noble : board.getNobles()) {
                    int req = noble.getRequirements().getOrDefault(best.getBonusGem(), 0);
                    int have = bonuses.getOrDefault(best.getBonusGem(), 0);
                    if (req > have) {
                        return ActionType.BUY;
                    }
                }
                if (best.getPrestigePoints() > 0) {
                    return ActionType.BUY;
                }
            }
        }

        // Priority 3: Reserve a high-value visible card we can't yet afford
        if (available.contains(ActionType.RESERVE) && getReservedCards().size() < 3) {
            Card[][] visible = board.getVisibleCards();
            for (int level = 2; level >= 0; level--) {
                for (int slot = 0; slot < 4; slot++) {
                    Card card = visible[level][slot];
                    if (card != null && card.getPrestigePoints() >= 3 && !validator.canBuy(this, card)) {
                        return ActionType.RESERVE;
                    }
                }
            }
        }

        // Priority 4: Buy any affordable card
        if (available.contains(ActionType.BUY)) {
            return ActionType.BUY;
        }

        // Priority 5: Take 3 different gems
        if (available.contains(ActionType.TAKE_THREE)) {
            return ActionType.TAKE_THREE;
        }

        // Priority 6: Take 2 same-colour gems
        if (available.contains(ActionType.TAKE_TWO)) {
            return ActionType.TAKE_TWO;
        }

        // Fallback: reserve
        if (available.contains(ActionType.RESERVE)) {
            return ActionType.RESERVE;
        }

        return null;
    }

    /**
     * Picks up to 3 different gems, prioritising colours most needed for visible cards.
     */
    public Map<GemType, Integer> chooseTake3Gems(Board board, ActionValidator validator) {
        Map<GemType, Integer> needed = getNeededGems(board);
        Map<GemType, Integer> selected = new EnumMap<>(GemType.class);
        int count = 0;

        // Sort gems by need (descending)
        List<GemType> sorted = new ArrayList<>(needed.keySet());
        sorted.sort((a, b) -> needed.getOrDefault(b, 0) - needed.getOrDefault(a, 0));

        for (GemType gem : sorted) {
            if (count >= 3) break;
            if (gem == GemType.GOLD) continue;
            if (board.getGemBank().getOrDefault(gem, 0) > 0) {
                selected.put(gem, 1);
                count++;
            }
        }

        // Fill remaining slots with any available colour
        if (count < 3) {
            for (GemType gem : GemType.values()) {
                if (count >= 3) break;
                if (gem == GemType.GOLD) continue;
                if (selected.containsKey(gem)) continue;
                if (board.getGemBank().getOrDefault(gem, 0) > 0) {
                    selected.put(gem, 1);
                    count++;
                }
            }
        }

        return selected;
    }

    /**
     * Picks the colour with >= 4 in the bank that we need most.
     */
    public GemType chooseTake2Gems(Board board, ActionValidator validator) {
        Map<GemType, Integer> needed = getNeededGems(board);
        GemType best = null;
        int bestNeed = -1;

        for (GemType gem : GemType.values()) {
            if (gem == GemType.GOLD) continue;
            if (board.getGemBank().getOrDefault(gem, 0) >= 4) {
                int need = needed.getOrDefault(gem, 0);
                if (need > bestNeed) {
                    bestNeed = need;
                    best = gem;
                }
            }
        }

        // Fallback: any colour with >= 4
        if (best == null) {
            for (GemType gem : GemType.values()) {
                if (gem == GemType.GOLD) continue;
                if (board.getGemBank().getOrDefault(gem, 0) >= 4) {
                    return gem;
                }
            }
        }
        return best;
    }

    /**
     * Chooses a card to reserve.
     * Returns int[]{level, slot} (0-indexed). Slot -1 means from the face-down deck.
     */
    public int[] chooseReserveCard(Board board, ActionValidator validator) {
        Card[][] visible = board.getVisibleCards();
        int bestLevel = -1;
        int bestSlot = -1;
        int bestPoints = -1;

        for (int level = 2; level >= 0; level--) {
            for (int slot = 0; slot < 4; slot++) {
                if (visible[level][slot] != null) {
                    int pts = visible[level][slot].getPrestigePoints();
                    if (pts > bestPoints) {
                        bestPoints = pts;
                        bestLevel = level;
                        bestSlot = slot;
                    }
                }
            }
        }

        if (bestLevel >= 0) {
            return new int[]{bestLevel, bestSlot};
        }

        // Fallback: take from the highest non-empty deck
        for (int level = 2; level >= 0; level--) {
            if (!board.getDecks()[level].isEmpty()) {
                return new int[]{level, -1};
            }
        }

        return new int[]{0, 0}; // should not be reached
    }

    /**
     * Picks the best affordable card: highest prestige first, then lowest total cost.
     */
    public Card chooseBuyCard(Board board, ActionValidator validator) {
        List<Card> affordable = new ArrayList<>();

        Card[][] visible = board.getVisibleCards();
        for (int level = 0; level < 3; level++) {
            for (int slot = 0; slot < 4; slot++) {
                if (visible[level][slot] != null && validator.canBuy(this, visible[level][slot])) {
                    affordable.add(visible[level][slot]);
                }
            }
        }

        for (Card card : getReservedCards()) {
            if (validator.canBuy(this, card)) {
                affordable.add(card);
            }
        }

        if (affordable.isEmpty()) {
            return null;
        }

        affordable.sort((a, b) -> {
            if (b.getPrestigePoints() != a.getPrestigePoints()) {
                return b.getPrestigePoints() - a.getPrestigePoints();
            }
            return totalCost(a) - totalCost(b);
        });

        return affordable.get(0);
    }

    /**
     * Chooses which gems to discard when over the 10-token limit.
     * Discards gems held in the highest quantities first.
     */
    public Map<GemType, Integer> chooseDiscardGems(int excess) {
        Map<GemType, Integer> toDiscard = new EnumMap<>(GemType.class);

        List<GemType> gemsByCount = new ArrayList<>();
        for (GemType gem : GemType.values()) {
            if (getGems().getOrDefault(gem, 0) > 0) {
                gemsByCount.add(gem);
            }
        }
        gemsByCount.sort((a, b) -> getGems().getOrDefault(b, 0) - getGems().getOrDefault(a, 0));

        int remaining = excess;
        for (GemType gem : gemsByCount) {
            if (remaining <= 0) break;
            int have = getGems().getOrDefault(gem, 0);
            int discard = Math.min(have, remaining);
            if (discard > 0) {
                toDiscard.put(gem, discard);
                remaining -= discard;
            }
        }

        return toDiscard;
    }

    /**
     * Picks a noble when multiple are eligible. Simply takes the first.
     */
    public Noble chooseNoble(List<Noble> eligible) {
        if (eligible.isEmpty()) {
            return null;
        }
        return eligible.get(0);
    }

    // ── Private helpers ──────────────────────────────────────────────

    private Map<GemType, Integer> getNeededGems(Board board) {
        Map<GemType, Integer> needed = new EnumMap<>(GemType.class);
        Map<GemType, Integer> bonuses = getBonusGems();

        Card[][] visible = board.getVisibleCards();
        for (int level = 0; level < 3; level++) {
            for (int slot = 0; slot < 4; slot++) {
                Card card = visible[level][slot];
                if (card == null) continue;
                for (Map.Entry<GemType, Integer> e : card.getCost().entrySet()) {
                    int required = e.getValue();
                    int bonus = bonuses.getOrDefault(e.getKey(), 0);
                    int have = getGems().getOrDefault(e.getKey(), 0);
                    int deficit = Math.max(0, required - bonus - have);
                    if (deficit > 0) {
                        needed.merge(e.getKey(), deficit, Integer::sum);
                    }
                }
            }
        }
        return needed;
    }

    private int totalCost(Card card) {
        int total = 0;
        for (int v : card.getCost().values()) {
            total += v;
        }
        return total;
    }
}
