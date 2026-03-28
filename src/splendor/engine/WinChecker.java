package splendor.engine;

import java.util.*;
import splendor.model.*;

public class WinChecker {
    
    public boolean hasTriggered(List<Player> players, int threshold) {
        for (Player p : players) {
            if (p.getPrestigePoints() >= threshold) {
                return true;
            }
        }

        return false;
    }

    public List<Player> getWinners(List<Player> players) {

        List<Player> temp = new ArrayList<>();
        int max = Integer.MIN_VALUE;
        for (Player p : players) {
            if (p.getPrestigePoints() > max) {
                if (temp.size() > 0) {
                    temp.clear();
                }
                temp.add(p);
                max = p.getPrestigePoints();
            }
            else if (p.getPrestigePoints() == max) {
                temp.add(p);
            }
            

        }

        List<Player> winners = new ArrayList<>();

        // tie breaker --> 2 or more have the same prestige points 
        int cmp = Integer.MAX_VALUE;
        for (Player t : temp) {
            if (t.getPurchasedCardCount() < cmp) {
                if (winners.size() > 0) {
                    winners.clear();
                }
                winners.add(t);
                cmp = t.getPurchasedCardCount();
            }
            else if (t.getPurchasedCardCount() == cmp) {
                winners.add(t);
            }
        }

        return winners;
    }
}

