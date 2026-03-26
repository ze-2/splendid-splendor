package splendor.model;
import java.util.*;

public class Deck {
    private List<Card> cards;

    public Deck (List<Card> cards) {
        this.cards = cards;
    }

    public void shuffle () {
        Collections.shuffle(cards);
    }

    public Card deal() {
        return cards.remove(0);
    }

    public Card peek () {
        return cards.get(0);
    }

    public boolean isEmpty() {
        if (cards.size() == 0) {
            return true;
        }
        return false;
    }

    public int size() {
        return cards.size();
    }
}
