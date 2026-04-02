package splendor.ui;

import splendor.model.Card;
import splendor.model.GemType;
import splendor.model.Noble;

import java.util.Map;
import java.util.StringJoiner;

public class ConsoleFormatter {

    // ANSI colour codes
    static final String RESET  = "\u001B[0m";
    static final String BOLD   = "\u001B[1m";
    static final String DIM    = "\u001B[2m";

    static final String RED    = "\u001B[31m";
    static final String GREEN  = "\u001B[32m";
    static final String YELLOW = "\u001B[33m";
    static final String BLUE   = "\u001B[34m";
    static final String WHITE  = "\u001B[37m";
    static final String BLACK_BG = "\u001B[40m";
    static final String BRIGHT_WHITE = "\u001B[97m";

    // Box-drawing characters
    static final String H_LINE = "\u2500";
    static final String V_LINE = "\u2502";
    static final String TL     = "\u250C";
    static final String TR     = "\u2510";
    static final String BL     = "\u2514";
    static final String BR     = "\u2518";
    static final String T_DOWN = "\u252C";
    static final String T_UP   = "\u2534";
    static final String T_RIGHT = "\u251C";
    static final String T_LEFT  = "\u2524";

    // â”€â”€ Colour helpers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    String gemColour(GemType gem) {
        switch (gem) {
            case RUBY:    return RED;
            case EMERALD: return GREEN;
            case SAPPHIRE:return BLUE;
            case DIAMOND: return BRIGHT_WHITE;
            case ONYX:    return BLACK_BG + BRIGHT_WHITE;
            case GOLD:    return YELLOW;
            default:      return RESET;
        }
    }

    String gemSymbol(GemType gem) {
        switch (gem) {
            case RUBY:    return "\u25C6"; // diamond
            case EMERALD: return "\u25C6";
            case SAPPHIRE:return "\u25C6";
            case DIAMOND: return "\u25C6"; // filled diamond
            case ONYX:    return "\u25C7"; // hollow diamond
            case GOLD:    return "\u2605"; // star
            default:      return "\u25CF";
        }
    }

    String colourGem(GemType gem) {
        return gemColour(gem) + BOLD + gemSymbol(gem) + " " + gem.name() + RESET;
    }

    String colourGemShort(GemType gem, int count) {
        return gemColour(gem) + gemSymbol(gem) + " " + count + RESET;
    }

    String horizontalLine(int width) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < width; i++) sb.append(H_LINE);
        return sb.toString();
    }

    String centreText(String text, int width) {
        int pad = Math.max(0, width - text.length());
        int left = pad / 2;
        int right = pad - left;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < left; i++) sb.append(' ');
        sb.append(text);
        for (int i = 0; i < right; i++) sb.append(' ');
        return sb.toString();
    }

    void printBoxHeader(String title, int width) {
        System.out.println(DIM + TL + horizontalLine(width - 2) + TR + RESET);
        System.out.println(DIM + V_LINE + RESET + BOLD
                + centreText(title, width - 2)
                + RESET + DIM + V_LINE + RESET);
        System.out.println(DIM + T_RIGHT + horizontalLine(width - 2) + T_LEFT + RESET);
    }

    void printBoxFooter(int width) {
        System.out.println(DIM + BL + horizontalLine(width - 2) + BR + RESET);
    }

    void printSectionDivider(String label) {
        System.out.println();
        System.out.println(DIM + "  " + horizontalLine(3) + " " + RESET
                + BOLD + label + RESET
                + DIM + " " + horizontalLine(30) + RESET);
    }

    // â”€â”€ Card & Noble formatters â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    String formatCardOption(int index, Card card) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("    %s[%d]%s ", DIM, index, RESET));

        // Bonus gem colour
        sb.append(colourGem(card.getBonusGem()));

        // Prestige points
        if (card.getPrestigePoints() > 0) {
            sb.append(String.format("  %s%d pts%s", BOLD, card.getPrestigePoints(), RESET));
        } else {
            sb.append(String.format("  %s0 pts%s", DIM, RESET));
        }

        // Cost
        sb.append("  " + DIM + "Cost:" + RESET + " ");
        Map<GemType, Integer> cost = card.getCost();
        StringJoiner costJoiner = new StringJoiner(" ");
        for (Map.Entry<GemType, Integer> entry : cost.entrySet()) {
            if (entry.getValue() > 0) {
                costJoiner.add(colourGemShort(entry.getKey(), entry.getValue()));
            }
        }
        sb.append(costJoiner);

        return sb.toString();
    }

    String formatNobleOption(int index, Noble noble) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("    %s[%d]%s ", DIM, index, RESET));
        sb.append(String.format("%s%d pts%s  ", BOLD, noble.getPrestigePoints(), RESET));
        sb.append(noble.getName() + " ");
        sb.append(DIM + "Requires:" + RESET + " ");

        Map<GemType, Integer> reqs = noble.getRequirements();
        StringJoiner reqJoiner = new StringJoiner(" ");
        for (Map.Entry<GemType, Integer> entry : reqs.entrySet()) {
            if (entry.getValue() > 0) {
                reqJoiner.add(colourGemShort(entry.getKey(), entry.getValue()));
            }
        }
        sb.append(reqJoiner);

        return sb.toString();
    }
}
