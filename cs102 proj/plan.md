# Splendor — Java Implementation Plan

## 1. Project Overview

Implement a console-based Splendor card game in Java supporting 2–4 human players. The implementation follows standard Splendor rules with a configurable winning threshold. All configuration parameters are externalised to `config.properties`; card and noble data are loaded from CSV files.

---

## 2. Project Directory Structure

```
cs102 project/
├── compile.sh                   # Compiles all .java files into classes/
├── run.sh                       # Runs the game
├── config.properties            # External configuration (points, gem counts, file paths)
├── src/
│   └── splendor/
│       ├── SplendorGame.java    # Entry point (main)
│       ├── config/
│       │   └── GameConfig.java
│       ├── model/
│       │   ├── GemType.java
│       │   ├── Card.java
│       │   ├── Noble.java
│       │   ├── Deck.java
│       │   ├── Player.java
│       │   └── Board.java
│       ├── engine/
│       │   ├── GameEngine.java
│       │   └── WinChecker.java
│       ├── ui/
│       │   └── ConsoleUI.java
│       └── data/
│           ├── CardLoader.java
│           └── NobleLoader.java
├── classes/                     # Empty at submission; populated by compile.sh
├── data/
│   ├── cards_level1.csv
│   ├── cards_level2.csv
│   ├── cards_level3.csv
│   └── nobles.csv
├── lib/                         # Empty (no external libraries planned)
└── docs/
    ├── splendor.md
    └── plan.md
```

### compile.sh
```bash
#!/bin/bash
find src -name "*.java" | xargs javac -d classes
```

### run.sh
```bash
#!/bin/bash
java -cp classes splendor.SplendorGame
```

---

## 3. External Configuration — `config.properties`

```properties
# Winning threshold (prestige points)
winning.points=15

# Initial gem counts per colour by player count
gems.per.color.2players=4
gems.per.color.3players=5
gems.per.color.4players=7

# Gold gems (always fixed)
gold.gems=5

# Paths to card data files
data.cards.level1=data/cards_level1.csv
data.cards.level2=data/cards_level2.csv
data.cards.level3=data/cards_level3.csv

# Path to noble data file
data.nobles=data/nobles.csv
```

---

## 4. Data File Formats

### Card CSV (`cards_level1.csv`, `cards_level2.csv`, `cards_level3.csv`)
```
# level, bonus_gem, prestige_points, ruby, emerald, sapphire, diamond, onyx
1,RUBY,0,0,3,0,0,0
1,EMERALD,0,0,0,2,1,0
...
```

### Noble CSV (`nobles.csv`)
```
# prestige_points, ruby, emerald, sapphire, diamond, onyx
3,0,4,4,0,0
3,3,0,0,3,3
...
```

---

## 5. Packages & Classes

### 5.1 `splendor` (root)

#### `SplendorGame.java`
Entry point. Reads `config.properties`, initialises players, starts `GameEngine`.

```
main(String[] args)
  - Load GameConfig from "config.properties"
  - Ask number of players and player names via ConsoleUI
  - Load cards (CardLoader) and nobles (NobleLoader)
  - Build Board
  - Create Player list
  - new GameEngine(...).start()
```

---

### 5.2 `splendor.config`

#### `GameConfig.java`
Loads and exposes all values from `config.properties`.

| Method | Returns | Notes |
|--------|---------|-------|
| `load(String path)` | `void` | Reads Properties file |
| `getWinningPoints()` | `int` | Default 15 |
| `getGemCount(int numPlayers)` | `int` | Returns per-colour gem count |
| `getGoldCount()` | `int` | Always 5 |
| `getCardDataPath(int level)` | `String` | Level 1–3 |
| `getNobleDataPath()` | `String` | — |

---

### 5.3 `splendor.model`

#### `GemType.java` (enum)
```java
public enum GemType {
    RUBY, EMERALD, SAPPHIRE, DIAMOND, ONYX, GOLD
}
```

#### `Card.java`
Immutable data class representing a development card.

| Field | Type | Description |
|-------|------|-------------|
| `level` | `int` | 1, 2, or 3 |
| `bonusGem` | `GemType` | Permanent discount colour |
| `prestigePoints` | `int` | 0–5 |
| `cost` | `Map<GemType, Integer>` | Gem cost (non-zero entries only) |

| Method | Returns |
|--------|---------|
| `getLevel()` | `int` |
| `getBonusGem()` | `GemType` |
| `getPrestigePoints()` | `int` |
| `getCost()` | `Map<GemType, Integer>` |

#### `Noble.java`
Immutable data class for a noble tile.

| Field | Type | Description |
|-------|------|-------------|
| `prestigePoints` | `int` | Always 3 |
| `requirements` | `Map<GemType, Integer>` | Required bonus gems |

| Method | Returns |
|--------|---------|
| `getPrestigePoints()` | `int` |
| `getRequirements()` | `Map<GemType, Integer>` |
| `canVisit(Player p)` | `boolean` | True if player's bonus gems meet requirements |

#### `Deck.java`
Wraps a `List<Card>` with deck operations.

| Method | Returns | Notes |
|--------|---------|-------|
| `Deck(List<Card> cards)` | — | Constructor |
| `shuffle()` | `void` | Randomises order |
| `deal()` | `Card` | Removes and returns top card |
| `peek()` | `Card` | Top card without removing (for display) |
| `isEmpty()` | `boolean` | — |
| `size()` | `int` | — |

#### `Player.java`
Holds all player state.

| Field | Type | Description |
|-------|------|-------------|
| `name` | `String` | Player name |
| `gems` | `Map<GemType, Integer>` | Tokens held |
| `purchasedCards` | `List<Card>` | Bought cards |
| `reservedCards` | `List<Card>` | Reserved cards (max 3) |
| `nobles` | `List<Noble>` | Nobles acquired |

| Method | Returns | Notes |
|--------|---------|-------|
| `getName()` | `String` | — |
| `getGems()` | `Map<GemType, Integer>` | — |
| `getTotalGems()` | `int` | Sum of all tokens held |
| `getBonusGems()` | `Map<GemType, Integer>` | Count of bonus gems from purchased cards |
| `getPrestigePoints()` | `int` | Sum: cards + nobles |
| `getPurchasedCardCount()` | `int` | For tie-breaking |
| `canAfford(Card c)` | `boolean` | Checks gems + bonuses + gold |
| `addGem(GemType, int)` | `void` | Add tokens |
| `removeGem(GemType, int)` | `void` | Remove tokens |
| `reserveCard(Card)` | `void` | Add to reservedCards |
| `buyCard(Card, Board)` | `void` | Pay gems, move card to purchased |
| `addNoble(Noble)` | `void` | Acquire noble |

#### `Board.java`
Holds the shared game state visible to all players.

| Field | Type | Description |
|-------|------|-------------|
| `gemBank` | `Map<GemType, Integer>` | Available gems |
| `visibleCards` | `Card[][]` | [level 0–2][slot 0–3] |
| `decks` | `Deck[]` | [level 0–2] |
| `nobles` | `List<Noble>` | Active nobles |

| Method | Returns | Notes |
|--------|---------|-------|
| `Board(GameConfig, List<Card>, List<Noble>, int numPlayers)` | — | Constructor, initialises everything |
| `getGemBank()` | `Map<GemType, Integer>` | — |
| `getVisibleCards()` | `Card[][]` | — |
| `getNobles()` | `List<Noble>` | — |
| `takeGems(Map<GemType,Integer>)` | `void` | Remove from bank; validates availability |
| `returnGems(Map<GemType,Integer>)` | `void` | Add back to bank |
| `takeVisibleCard(int level, int slot)` | `Card` | Remove card; trigger refill |
| `takeReserveCard(int level)` | `Card` | Take top of face-down deck |
| `refillCards()` | `void` | Fill empty slots from decks |
| `removeNoble(Noble)` | `void` | After noble visits a player |

---

### 5.4 `splendor.engine`

#### `GameEngine.java`
Orchestrates the game loop.

| Field | Type |
|-------|------|
| `players` | `List<Player>` |
| `board` | `Board` |
| `ui` | `ConsoleUI` |
| `config` | `GameConfig` |
| `winChecker` | `WinChecker` |

| Method | Returns | Notes |
|--------|---------|-------|
| `GameEngine(List<Player>, Board, ConsoleUI, GameConfig)` | — | Constructor |
| `start()` | `void` | Main game loop |
| `playRound()` | `void` | Each player takes one turn |
| `playTurn(Player)` | `void` | Single player turn: action + noble check |
| `checkNobles(Player)` | `void` | Check all nobles; trigger visit if qualified |
| `isGameTriggered()` | `boolean` | Any player at ≥ winning points |
| `finishRound(int triggerIndex)` | `void` | Complete round from trigger point |

**Game Loop (pseudocode):**
```
start():
  ui.displayBoard(board)
  triggered = false
  triggerPlayerIndex = -1

  while not triggered:
    for each player in players:
      ui.displayTurnHeader(player)
      player.chooseAction(board, ui)
      checkNobles(player)
      ui.displayPlayerStatus(player)
      if player.getPrestigePoints() >= config.getWinningPoints():
        triggered = true
        triggerPlayerIndex = index
        break

  // Finish the round
  finishRound(triggerPlayerIndex)

  // Determine winner
  List<Player> winners = winChecker.getWinners(players)
  ui.displayWinner(winners)
```

#### `WinChecker.java`
Stateless utility for determining winners.

| Method | Returns | Notes |
|--------|---------|-------|
| `hasTriggered(List<Player>, int threshold)` | `boolean` | Any player ≥ threshold |
| `getWinners(List<Player>)` | `List<Player>` | Applies tie-break rules |

**Tie-break logic:**
1. Find max prestige points.
2. Filter players with max points.
3. Among those, find min purchased card count.
4. Return all players matching both (shared victory if > 1).

---

### 5.5 `splendor.data`

#### `CardLoader.java`
Parses CSV files into `Card` objects.

| Method | Returns | Notes |
|--------|---------|-------|
| `loadCards(String path, int level)` | `List<Card>` | Reads CSV, skips comment lines |

#### `NobleLoader.java`
Parses noble CSV into `Noble` objects.

| Method | Returns | Notes |
|--------|---------|-------|
| `loadNobles(String path)` | `List<Noble>` | Reads CSV, skips comment lines |

---

### 5.6 `splendor.ui`

#### `ConsoleUI.java`
All console input/output — no game logic here.

| Method | Returns | Notes |
|--------|---------|-------|
| `displayBoard(Board)` | `void` | Shows bank, visible cards, nobles |
| `displayPlayerStatus(Player)` | `void` | Gems, bonuses, points, reserved |
| `displayTurnHeader(Player)` | `void` | "=== Alice's Turn ===" |
| `displayWinner(List<Player>)` | `void` | Announce result |
| `promptPlayerCount()` | `int` | 2–4 |
| `promptPlayerName(int index)` | `String` | — |
| `promptAction(Player, Board)` | `int` | Returns 1–4 (action choice) |
| `promptTake3Gems(Board)` | `Map<GemType,Integer>` | Validates input |
| `promptTake2Gems(Board)` | `GemType` | Validates ≥ 4 in bank |
| `promptReserveCard(Board)` | `int[]` | [level, slot] or [level, -1] for deck |
| `promptBuyCard(Player, Board)` | `Card` | Shows affordable cards |
| `promptDiscardGems(Player, int excess)` | `Map<GemType,Integer>` | Returns gems to discard |
| `promptNobleChoice(List<Noble>)` | `Noble` | If multiple nobles qualify |
| `readInt(String prompt, int min, int max)` | `int` | Validates range |

---

## 6. ASCII Class Diagram

```
+-------------------+
|   SplendorGame    |
|   (main entry)    |
+---+---------------+
    |
    |  uses
    v
+-------------------+     reads     +-------------------+
|    GameConfig     |<-----------   |  config.properties|
+-------------------+               +-------------------+
    |
    |  creates / passes to
    v
+-------------------+
|    GameEngine     |
|  - players        |
|  - board          |
|  - ui             |
|  - winChecker     |
+---+---------------+
    |           |
    |           |  uses
    v           v
+--------+  +----------+
| Board  |  |ConsoleUI |
+---+----+  +----------+
    |
    +---------------------------+-------------------+
    |                           |                   |
+---v----+  +----------+  +-----v----+  +-----------v-+
|  Deck  |  |   Card   |  |  Noble   |  |GemType(enum)|
+--------+  +----------+  +----------+  +-------------+

+-------------------+          +------------------+
|     Player        |          |   WinChecker     |
+-------------------+          +------------------+

+-------------------+  +-------------------+
|   CardLoader      |  |   NobleLoader     |
+-------------------+  +-------------------+

Legend:
  ---->  uses / depends on
  +---+  class / interface
```

---

## 7. Move Validation Summary

All validation is performed before executing any action. Illegal moves prompt the user to choose again.

| Action | Validation Rules |
|--------|-----------------|
| Take 3 different | Each selected colour has ≥ 1 in bank; all 3 colours are different; no gold |
| Take 2 same | Selected colour has ≥ 4 in bank; no gold |
| Reserve card | Player has < 3 reserved cards; card/deck exists |
| Buy card | Card is face-up on board or in player's reserved; player can afford (gems + bonuses + gold) |
| Discard gems | Total discarded = amount over 10; gems discarded are in player's possession |

---

## 8. Game Flow Diagram

```
main()
  └── load config
  └── prompt player setup
  └── load cards + nobles
  └── build Board
  └── GameEngine.start()
        └── loop turns until triggered:
              └── player chooses action via ConsoleUI prompts
              └── validate + execute action on Board
              └── checkNobles(player)
              └── check win trigger
        └── finish remaining turns in round
        └── WinChecker.getWinners()
        └── ConsoleUI.displayWinner()
```

---

## 9. Implementation Order (Suggested)

| Phase | Files | Notes |
|-------|-------|-------|
| 1 | `GemType`, `Card`, `Noble`, `Deck` | Pure data classes — no dependencies |
| 2 | `GameConfig`, `CardLoader`, `NobleLoader` | File I/O; test with real CSV data |
| 3 | `Player`, `Board` | Core state; test gem/card operations |
| 4 | `ConsoleUI` | Build display + input prompts |
| 5 | `GameEngine`, `WinChecker` | Wire up game loop |
| 6 | `SplendorGame` (main) | Integration entry point |
| 7 | `compile.sh`, `run.sh` | Final packaging |

---

## 10. Assumptions

- All players are human; no AI player is implemented.
- A player with no legal actions (edge case: 0 gems in bank, 3 reserved cards, no affordable card) may pass their turn (not required by rules but defensively handled).
- Noble visit is triggered only after buying a card (not after taking gems or reserving).
- Gold gems taken via reserving go directly to the player's token pool and can be discarded if over 10.
- Shared victory (tied prestige + tied card count) is displayed but the game ends normally.
