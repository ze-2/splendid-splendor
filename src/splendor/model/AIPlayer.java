import java.util.*;

import splendor.ActionType;

public class AIPlayer extends Player {

    /* Current state of face up cards, updated every time chooseAction is called */
    Card[][] boardVisibleCards;

    /*
     * When gem bonuses exceed 7, consider as late game.
     * (Gem bonus tends to drive the game more than prestige)
     */
    boolean isLateGame = false;

    public AIPlayer(String name) {
        super(name);
    }

    @Override
    public boolean isHuman() {
        return false;
    }

    /**
     * @param board     The current game board.
     * @param validator The action validator to check legal moves.
     * @return The chosen ActionType to execute, or null if no actions are
     *         available.
     */
    @Override
    public ActionType chooseAction(Board board, ActionValidator validator) {

        boardVisibleCards = board.getVisibleCards();
        if (!isLateGame) {
            int count = 0;
            for (int v : getBonusGems().values()) {
                count += v;
            }
            isLateGame = count >= 7;
        }
        List<ActionType> available = validator.getAvailableActions(this, board);
        if (available.isEmpty()) {
            return null;
        }

        // 1. If we have a good card we can afford, buying is almost always the best
        // engine move.
        if (available.contains(ActionType.BUY)) {
            if(chooseBuyCard(board, validator) != null) return ActionType.BUY;
        }

        // 2. Smart Reserve: If we are near the gem limit (8+ gems) and can't buy,
        // reserving a high-value/threat target prevents wasted turns discarding.
        
        int totalGems = 0;
        for(int v: getGems().values()){
            totalGems += v;   
        }
        if (totalGems >= 8 && available.contains(ActionType.RESERVE) && getReservedCards().size() < 3) {
            return ActionType.RESERVE;
        }

        // 3. Take gems to work towards our single highest-scoring target card.
        if (available.contains(ActionType.TAKE_THREE)) {
            return ActionType.TAKE_THREE;
        }
        if (available.contains(ActionType.TAKE_TWO)) {
            return ActionType.TAKE_TWO;
        }

        // Fallback
        if (available.contains(ActionType.RESERVE)) {
            return ActionType.RESERVE;
        }

        return null;
    }

    /**
     * Find all available cards, and choose the best option.
     *
     * @param board     The current game board.
     * @param validator The action validator to check legal moves.
     * @return The best Card to buy, or null if none are affordable.
     */
    @Override
    public Card chooseBuyCard(Board board, ActionValidator validator) {
        List<Card> affordable = new ArrayList<>();

        for (int level = 0; level < 3; level++) {
            for (int slot = 0; slot < 4; slot++) {
                if (boardVisibleCards[level][slot] != null && validator.canBuy(this, boardVisibleCards[level][slot])) {
                    affordable.add(boardVisibleCards[level][slot]);
                }
            }
        }
        for (Card card : getReservedCards()) {
            if (validator.canBuy(this, card)) {
                affordable.add(card);
            }
        }

        if (affordable.isEmpty())
            return null;

        affordable.sort((a, b) -> Double.compare(evaluateCard(b, board), evaluateCard(a, board)));
        return affordable.get(0);
    }

    /**
     * Identifies the best overall card (affordable or not) and takes up to 3
     * different gems for it.
     *
     * @param board     The current game board.
     * @param validator The action validator to check legal moves.
     * @return A map of the selected GemTypes and their quantities (always 1 each,
     *         max 3 total).
     */
    @Override
    public Map<GemType, Integer> chooseTake3Gems(Board board, ActionValidator validator) {
        Map<GemType, Integer> needed = getTargetGems(board);
        Map<GemType, Integer> selected = new EnumMap<>(GemType.class);

        List<GemType> sorted = new ArrayList<>(needed.keySet());
        sorted.sort((a, b) -> needed.get(b) - needed.get(a));
        
        int count = 0;
        for (GemType gem : sorted) {
            if (count >= 3)
                break;
            if (board.getGemBank().get(gem) > 0) {
                selected.put(gem, 1);
                count++;
            }
        }

        if (count < 3) {
            for (GemType gem : GemType.values()) {
                if(gem == GemType.GOLD) continue;
                if (count >= 3) break;
                if (!needed.containsKey(gem) && board.getGemBank().get(gem) > 0) {
                    selected.put(gem, 1);
                    count++;
                }
            }
        }
        return selected;
    }

    /**
     * Takes 2 gems of the same colour, prioritizing the gem most needed for our
     * target card.
     *
     * @param board     The current game board.
     * @param validator The action validator to check legal moves.
     * @return The selected GemType to take 2 of, or null if no valid option exists.
     */
    @Override
    public GemType chooseTake2Gems(Board board, ActionValidator validator) {
        Map<GemType, Integer> needed = getTargetGems(board);
        GemType best = null;
        int bestNeed = -1;

        for (GemType gem : GemType.values()) {
            if (gem == GemType.GOLD)
                continue;
            if (board.getGemBank().get(gem) >= 4) {
                if(best == null) best = gem;
                if(needed.containsKey(gem)){
                    if(needed.get(gem) > bestNeed){
                    bestNeed = needed.get(gem);
                    best = gem;
                }
                }
            }
        }
        return best;
    }

    /**
     * Reserves a card based on a combination of its value to us and its danger in
     * the hands of opponents.
     *
     * @param board     The current game board.
     * @param validator The action validator to check legal moves.
     * @return An int array where index 0 is the level and index 1 is the slot
     *         (0-3). Slot -1 indicates drawing from the deck.
     */
    @Override
    public int[] chooseReserveCard(Board board, ActionValidator validator) {
        int bestLevel = -1;
        int bestSlot = -1;
        double bestCombinedScore = Double.NEGATIVE_INFINITY;

        for (int level = 2; level >= 0; level--) {
            for (int slot = 0; slot < 4; slot++) {
                Card card = boardVisibleCards[level][slot];
                if (card != null) {
                    double ourScore = evaluateCard(card, board);

                    double threatScore = evaluateOpponentThreat(card, board);

                    double combinedScore = ourScore + threatScore;

                    if (combinedScore > bestCombinedScore) {
                        bestCombinedScore = combinedScore;
                        bestLevel = level;
                        bestSlot = slot;
                    }
                }
            }
        }

        if (bestLevel >= 0)
            return new int[] { bestLevel, bestSlot };

        // Fallback: Take face down card
        for (int level = 2; level >= 0; level--) {
            if (!board.getDecks()[level].isEmpty()) {
                return new int[] { level, -1 };
            }
        }
        return new int[0];
    }

    /**
     * Chooses which gems to discard when over the 10-token limit.
     * Discards gems held in the highest quantities first to maintain color
     * flexibility.
     *
     * @param excess The number of gems that must be discarded.
     * @return A map containing the GemTypes and quantities to discard.
     */
    @Override
    public Map<GemType, Integer> chooseDiscardGems(int excess) {
        Map<GemType, Integer> toDiscard = new EnumMap<>(GemType.class);
        List<GemType> gemsByCount = new ArrayList<>();

        for (GemType gem : GemType.values()) {
            if (gem != GemType.GOLD && getGems().getOrDefault(gem, 0) > 0) {
                gemsByCount.add(gem);
            }
        }
        gemsByCount.sort((a, b) -> getGems().getOrDefault(b, 0) - getGems().getOrDefault(a, 0));

        int remaining = excess;
        for (GemType gem : gemsByCount) {
            if (remaining <= 0)
                break;
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
     *
     * @param eligible A list of Nobles the player has fulfilled requirements for.
     * @return The chosen Noble, or null if the list is empty.
     */
    @Override
    public Noble chooseNoble(List<Noble> eligible) {
        return eligible.isEmpty() ? null : eligible.get(0);
    }

    // ── Private Helpers ──────────────────────────────────────────────

    /**
     * Calculates a threat score based on how close opponents are to buying a
     * high-value card.
     *
     * @param card  The card being evaluated.
     * @param board The current game board.
     * @return A double representing the threat level of the card to our opponents.
     */
    private double evaluateOpponentThreat(Card card, Board board) {
        double maxThreat = 0;

        // Only bother blocking cards that yield points or are high tier
        if (card.getPrestigePoints() < 2 && totalCost(card) < 5) {
            return 0;
        }

        for (Player opponent : board.getPlayers()) {
            if (opponent == this)
                continue;

            int deficit = calculateDeficitForPlayer(card, opponent);

            // If the opponent is 0-2 gems away from a valuable card, it's a threat
            if (deficit <= 2) {
                double threat = (card.getPrestigePoints() * 5.0) + ((3 - deficit) * 3.0);
                if (threat > maxThreat) {
                    maxThreat = threat;
                }
            }
        }
        return maxThreat;
    }

    /**
     * Finds the best card heuristically, and returns the remaining gems needed to buy.
     *
     * @param board The current game board.
     * @return A map representing the exact GemTypes and quantities needed to buy
     *         the target card.
     */
    private Map<GemType, Integer> getTargetGems(Board board) {
        Map<GemType, Integer> needed = new EnumMap<>(GemType.class);
        Card target = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (int level = 0; level < 3; level++) {
            for (int slot = 0; slot < 4; slot++) {
                Card card = boardVisibleCards[level][slot];
                if (card != null) {
                    double score = evaluateCard(card, board);
                    if (score > bestScore) {
                        bestScore = score;
                        target = card;
                    }
                }
            }
        }

        if (target != null) {
            Map<GemType, Integer> bonuses = getBonusGems();
            Map<GemType, Integer> gems = getGems();
            for (Map.Entry<GemType, Integer> e : target.getCost().entrySet()) {
                int required = e.getValue();
                int have = bonuses.getOrDefault(e.getKey(), 0) + gems.getOrDefault(e.getKey(), 0);
                if (required > have) {
                    needed.put(e.getKey(), required - have);
                }
            }
        }
        return needed;
    }

    /**
     * Scores how good a card is to purchase base on the game phase, the player's
     * gems
     *
     * @param card  The card to evaluate.
     * @param board The current game board.
     * @return A double representing the strategic value of the card.
     */
    private double evaluateCard(Card card, Board board) {

        double score = 0;

        score += card.getPrestigePoints() * (isLateGame ? 15.0 : 5.0);

        Map<GemType, Integer> bonuses = getBonusGems();
        for (Noble noble : board.getNobles()) {
            int req = noble.getRequirements().getOrDefault(card.getBonusGem(), 0);
            int have = bonuses.getOrDefault(card.getBonusGem(), 0);
            if (req > have) {
                score += 8.0;
            }
            if(req == have + 1){
                score += 30;
            }
        }
        score -= totalCost(card) * 1.5;

        int deficit = calculateDeficitForPlayer(card, this);
        score -= deficit * 3.0;

        return score;
    }

    /**
     * Calculates how many gems a specific player is short to buy a card.
     *
     * @param card   The card to check.
     * @param player The player to check against (can be the AI or an opponent).
     * @return The number of missing gems (accounting for gold).
     */
    private int calculateDeficitForPlayer(Card card, Player p) {
        int deficit = 0;
        Map<GemType, Integer> bonuses = p.getBonusGems();
        Map<GemType, Integer> gems = p.getGems();
        int gold = gems.getOrDefault(GemType.GOLD, 0);

        for (GemType type : card.getCost().keySet()) {
            int cost = card.getCost().get(type);
            int have = bonuses.getOrDefault(type, 0) + gems.getOrDefault(type, 0);
            deficit += Math.max(0, cost - have);
        }
        return Math.max(0, deficit - gold);
    }

    /**
     * Calculates the total gem cost of a card.
     *
     * @param card The card to evaluate.
     * @return The integer sum of all gems required to buy the card.
     */
    private int totalCost(Card card) {
        int total = 0;
        for(int v: card.getCost().values()) {
            total += v;
        }
        return total;
    }
}
