import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Set;
import java.util.Queue;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.ArrayDeque;
import java.lang.StringBuilder;


class Position {
  public int m;
  public int n;

  public Position(int m, int n) {
    this.m = m;
    this.n = n;
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();

    builder.append("Position: ").append("m ").append(m).append(" n ").append(n);

    return builder.toString();
  }

}

class Vector {
  public int x;
  public int y;

  public Vector(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append("v:: x: ").append(x).append(" y: ").append(y);
    return b.toString();
  }
}

class BoardIterator implements Iterator<Tile> {
  GameBoard board;
  int m;
  int n;

  public BoardIterator(GameBoard board) {
    m = 0;
    n = 0;
    this.board = board;
  }

  public boolean hasNext() {
    System.err.println("Checking for next");
    if (m >= board.size && n >= board.size)
      return false;
    return true;
    // return !((m - 1) >= board.size && (n - 1) >= board.size);
  }

  public Tile next() {
    Tile ret = board.get(m, n);
    if (n >= board.size - 1) {
      n = 0;
      m++;
    } else {
      n++;
    }
    return ret;
  }
}

enum Direction {
  Up, Left, Right, Down
}

class Tile {
  public int x;
  public int y;
  public Integer value;

  public Position previous;
  public Tile[] mergedFrom;

  public Tile(int x, int y, Integer value) {
    this.x = x;
    this.y = y;
    this.value = value;
  }

  public Tile(Position p, Integer value) {
    this(p.m, p.n, value);
  }

  public void updatePosition(Position p) {
    this.x = p.m;
    this.y = p.n;
  }

  public void savePosition() {
    previous = new Position(x, y);
  }

  public String toString() {
    return value.toString();
  }

  public boolean isAt(Position p) {
    if (x == p.m && y == p.n) {
      return true;
    }
    return false;
  }

  @Override
  public boolean equals(Object o) {
    System.out.println(String.format("Comparing %s and %s", this, o));
    if (o == null)
      return false;
    if (o == this)
      return true;
    if (getClass() != o.getClass())
      return false;
    Tile other = (Tile) o;
    if (this.value != other.value)
      return false;

    return true;
  }
}

// A list of positions to traverse in order
class Traversals {
  public List<Integer> x;
  public List<Integer> y;

  public Traversals(Direction direction, int size) {
    x = new ArrayList<>();
    y = new ArrayList<>();

    for (int i = 0; i < size; i++) {
      x.add(i);
      y.add(i);
    }

    // In order to always traverse from farthest cell in direction
    if (direction == Direction.Right) {
      Collections.reverse(x);
    } else if (direction == Direction.Up) {
      Collections.reverse(y);
    }
  }
}

class GameBoard implements Iterable<Tile> {
  public final Random rand = new Random();
  public static final float twoProbability = 0.9f;

  public boolean won = false;
  public boolean over = false;

  public Tile[][] cells;
  public Map<Integer, Integer> merges = new HashMap<>();

  public int size;

  public GameBoard() {
    this(4);
  }

  public GameBoard(int size) {
    this.size = size;

    cells = new Tile[size][];
    // Create the initial board
    for (int i = 0; i < size; ++i) {
      cells[i] = new Tile[size];

      for (int j = 0; j < size; j++) {
        cells[i][j] = null;
        // cells[i][j] = new Tile(new Position(i, j), null);
      }

    }
    generateNewItem();
  }

  public void setSeed(long seed) {
    rand.setSeed(seed);
  }

  public static GameBoard clone(GameBoard original) {
    int size = original.size;
    GameBoard copied = new GameBoard(size);

    for (int m = 0; m < size; ++m) {
      for (int n = 0; n < size; ++n) {
        Tile oTile = original.get(m, n);
        if (oTile != null) {
          copied.cells[m][n] = new Tile(m, n, oTile.value);
        } else {
          copied.cells[m][n] = null;
        }
      }
    }

    return copied;
  }

  public Tile get(int m, int n) {
    if (withinBounds(m, n)) {
      return cells[m][n];
    }
    return null;
  }

  public Tile get(Position p) {
    return get(p.m, p.n);
  }

  // Change the value contained in the tile at m, n to v
  public void set(int m, int n, Tile t) {
    cells[m][n] = t;
  }

  public void set(int m, int n, int v) {
    set(m, n, new Tile(m, n, v));
  }

  public void set(Position p, int v) {
    set(p.m, p.n, v);
  }

  public void set(Tile t) {
    cells[t.x][t.y] = t;
  }

  public void remove(Tile t) {
    cells[t.x][t.y] = null;
  }

  // Move the given tile to a new position
  public void moveTile(Tile t, Position to) {
    cells[t.x][t.y] = null;
    cells[to.m][to.n] = t;
    t.updatePosition(to);
  }

  public Iterator<Tile> iterator() {
    // DO NOT USE THIS
    return new BoardIterator(this);
  }

  public void generateNewItem() {
    int val = 2;
    if (rand.nextDouble() >= twoProbability) {
      val = 4;
    }

    // Find a place to put the new item
    List<Position> freeSpaces = emptyPositions();

    Position newPos = freeSpaces.get(rand.nextInt(freeSpaces.size()));
    set(newPos, val);

  }

  class PositionProspect {
    Position furthest;
    Position next;

    public PositionProspect(Position furthest, Position next) {
      this.furthest = furthest;
      this.next = next;
    }
  }

  public Vector getDirectionVector(Direction direction) {
    Vector v;
    switch (direction) {
    case Right:
      v = new Vector(0, 1);
      break;
    case Up:
      v = new Vector(-1, 0);
      break;
    case Left:
      v = new Vector(0, -1);
      break;
    case Down:
      v = new Vector(1, 0);
      break;
    default:
      v = new Vector(0, 0);
    }

    /*
     * switch (direction) { case Up: v = new Vector(0, 1); break; case Left: v = new
     * Vector(-1, 0); break; case Down: v = new Vector(0, -1); break; case Right: v
     * = new Vector(1, 0); break; default: v = new Vector(0, 0); }
     */
    return v;
  }

  public PositionProspect findFurthestPosition(Position p, Vector v) {
    Position previous;

    // System.err.println("Calculating furthest position");
    // System.err.println(String.format("from P: %s with v: %s", p, v));

    // FIXME: Allocation here when we don't *really* need to
    do {
      previous = p;
      p = new Position(previous.m + v.x, previous.n + v.y);
    } while (withinBounds(p) && available(p));

    // Will be useful to see if we need to merge
    return new PositionProspect(previous, p);
  }

  // Gets the Tiles ready for moving
  public void resetTiles() {
    for (int x = 0; x < size; ++x) {
      for (int y = 0; y < size; ++y) {
        Tile t = get(x, y);
        if (t != null) {
          t.mergedFrom = null;
          t.savePosition();
        }
      }
    }
  }

  public GameBoard[] enumerate() {
    return new GameBoard[]{ this.peekRight(), this.peekUp(), this.peekLeft(), this.peekDown()};
  }

  public boolean move(Direction direction) {

    // Check if game is over first

    Vector v = getDirectionVector(direction);
    System.err.println(v);
    System.err.println(direction);
    Traversals traversals = new Traversals(direction, size);
    boolean moved = false;

    merges.clear();
    resetTiles();
    Tile tile;

    for (int x : traversals.x) {
      for (int y : traversals.y) {
        System.err.println(String.format("x: %d, y: %d", x, y));

        final Position position = new Position(x, y);
        tile = get(position);

        if (tile != null) {

          PositionProspect positions = findFurthestPosition(position, v);
          Tile next = get(positions.next.m, positions.next.n);
          // Determine if these tiles need to merge

          // Only one merge per row
          if (next != null && next.value.equals(tile.value) && next.mergedFrom == null) {
            
            Tile merged = new Tile(positions.next, tile.value * 2);
            // Keep track for score purposes
            merges.merge(tile.value, 1, Integer::sum);
            merged.mergedFrom = new Tile[] { tile, next };

            set(merged);
            remove(tile);

            tile.updatePosition(positions.next);
            if (merged.value >= 128) {
              System.err.println(String.format("Merged %d amnd %d at %s", tile.value, next.value, positions.next));
            }

            if (merged.value == 2048) {
              won = true;
            }
          } else {
            System.err.println("Moving tile, no merge");
            moveTile(tile, positions.furthest);
          }

          // It isn't in it's original place
          if (!tile.isAt(position)) {
            System.err.println("Detected change of position");
            moved = true;
          }

        }

      }
    }

    if (moved) {
      generateNewItem();

      if (movesAvailable() == false)
        over = true;

      // Check if there are any spaces available
    }

    return moved;
  }

  // Return true if the move results in a tile moving
  public boolean canMove(Direction d) {
    GameBoard temp = GameBoard.clone(this);
    return temp.move(d);
  }
  // Actually apply the operation to the current game board
  public void doLeft() {
    move(Direction.Left);
  }

  public void doRight() {
    move(Direction.Right);
  }

  public void doUp() {
    move(Direction.Up);
  }

  public void doDown() {
    move(Direction.Down);
  }

  public GameBoard peek(Direction direction) {
    GameBoard peek = clone(this);
    peek.move(direction);
    return peek;
  }
  // Return a simulation of what might happen if this operation was done
  public GameBoard peekLeft() {
    GameBoard peek = clone(this);
    peek.doLeft();
    return peek;
  }

  public GameBoard peekRight() {
    GameBoard peek = clone(this);
    peek.doRight();
    return peek;
  }

  public GameBoard peekUp() {
    GameBoard peek = clone(this);
    peek.doUp();
    return peek;
  }

  public GameBoard peekDown() {
    GameBoard peek = clone(this);
    peek.doDown();
    return peek;
  }


  public boolean withinBounds(int m, int n) {
    return (m < size && n < size && m >= 0 && n >= 0);
  }

  public boolean withinBounds(Position p) {
    return withinBounds(p.m, p.n);
  }

  public boolean available(int m, int n) {
    return get(m, n) == null;
  }

  public boolean available(Position p) {
    return available(p.m, p.n);
  }

  public int emptySpaces() {
    return emptyPositions().size();
  }

  public int tileCount() {
    return size * size - emptySpaces();
  }

  public boolean movesAvailable() {
    return !full() || matchesAvailable();
  }


  public boolean matchesAvailable() {
    Tile t;

    for (int x = 0; x < size; ++x) {
      for (int y = 0; y < size; ++y) {
        t = get(x, y);
        if (t != null) {
          for (Tile a : adjacent(x, y)) {
            if (t.value == a.value) {
              if (t.value == 128) {
                System.out.println("found 128 match");
                while (true) {

                }
              }
              return true;   
            }
          }
        }
        //t = get(x, y);

          /*
        if (t != null) {
          for (Direction dir : Direction.values()) {
            Vector v = getDirectionVector(dir);
            Tile other = get(x + v.x, y + v.y);

            if (other != null && t.value == other.value) {
              return true;
            }
          }
        }
          */
      }
    }
    return false;
  }

  public boolean full() {
    List<Position> empty = emptyPositions();

    return empty.size() == 0;
  }

  public int sum() {
    int s = 0;
    for (int m = 0; m < size; ++m) {
      for (int n = 0; n < size; ++n) {
        Tile t = get(m, n);
        if (t != null) {
          s += t.value;
        }
      }
    }
    return s;
  }

  public int maxValue() {
    int max = 2;
    for (int m = 0; m < size; ++m) {
      for (int n = 0; n < size; ++n) {
        Tile t = get(m, n);
        if (t != null && t.value > max) {
          max += t.value;
        }
      }
    }
    return max;
  }

  private List<Position> emptyPositions() {
    List<Position> positions = new ArrayList<>();

    // Loop through the board
    for (int m = 0; m < size; ++m) {

      for (int n = 0; n < size; ++n) {
        if (get(m, n) == null) {
          positions.add(new Position(m, n));
        }
      }
    }

    return positions;
  }

  public List<Tile> corners() {
    List<Tile> items = new ArrayList<>();
    items.add(get(0,0));
    items.add(get(0,size - 1));
    items.add(get(size - 1, 0));
    items.add(get(size - 1,size - 1));
    return items;
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();

    for (int i = 0; i < size; ++i) {
      for (int j = 0; j < size; j++) {
        Tile item = get(i, j);

        if (item != null) {
          builder.append(item).append(" ");
        } else {
          builder.append("_ ");
        }
      }
      builder.append("\n");
    }
    return builder.toString();
  }

  public int count_value(int v) {
    int count = 0;
    for (int m = 0; m < size; ++m) {
      for (int n = 0; n < size; ++n) {
        Tile t = get(m, n);
        if (t != null && t.value == v) {
          count++;
        }

      }
    }
    return count;
  }

  public List<Tile> adjacent(int m, int n) {
    List<Tile> items = new ArrayList<>();
   
    for (Direction d : Direction.values()) {
      Vector v = getDirectionVector(d);

      Position p = new Position(m + v.x, n + v.y);
      Tile a = get(p);
      if (a != null) {
        items.add(a);
      }
    }
    /*
    Tile t;
    t = get(m + 1, n);
    if (t != null) items.add(t);
    t = get(m - 1, n);
    if (t != null) items.add(t);
    t = get(m, n + 1);
    if (t != null) items.add(t);
    t = get(m, n - 1);
    if (t != null) items.add(t);
    */
    return items;
  }

  public int adjacentWithSameValue(int m, int n) {
    int count = 0;
    Tile t = get(m, n);
    if (t == null)
      return 0;
    
    for (Tile adjacent : adjacent(m, n)) {
      if (adjacent.value == t.value) {
        count++;
      }
    }

    return count;
  }

  public int adjacencySum() {
    int score = 0;

    for (int m = 0; m < size; ++m) {
      for (int n = 0; n < size; ++n) {
        score += (adjacentWithSameValue(m, n));
      }
    }

    return score;
  }

  public int tilesInRow(int row) {
    int count = 0;
    for (Tile t : cells[row]) {
      if (t != null)
        count++;
    }
    return count;
  }

  // TODO: Implement equal for board
  public boolean equals(Object other) {
    if (other == null)
      return false;
    if (other == this)
      return true;
    if (getClass() != other.getClass())
      return false;

    GameBoard o = (GameBoard) other;
    if (o.size != this.size)
      return false;

    for (int m = 0; m < size; ++m) {
      for (int n = 0; n < size; ++n) {
        Tile t = this.get(m, n);
        Tile oT = o.get(m, n);
        //System.out.println("t: " + t);
        //System.out.println("oT: " + oT);
        if (t != oT) {
          if (t == null && oT == null) {

          }
          else {
            //System.out.println(String.format("%s != %s", t, oT));
            return false;

          }
          // Shouldn't have to need this but I do???
          // Otherwise it thinks if they're both null
          /*
          if (t != null && oT != null) {
            System.out.println(String.format("%s != %s", t, oT));
            return false;
          }*/
        }
      }
    }

    return true;
  }
}

class BoardScore {
  public GameBoard gameBoard;
  public float score;

  public BoardScore(GameBoard board, float score) {
    this.gameBoard = board;
    this.score = score;
  }
}
public class GameSolver {

  /*
  Direction first_search(GameBoard start, int maxDepth) {

    Set<GameBoard> seen = new HashSet<>();
    Queue<GameBoard> q = new ArrayDeque<>();

    int depth = 0;
    int elementsToDepthIncrease = 1;
    int nextElementsToDepthIncrease = 0;

    while (!q.isEmpty()) {
      GameBoard current = q.poll();
      if (seen.contains(current)) {
        continue;b
      }


      // Calculate score

      nextElementsToDepthIncrease += 4;
      if (--elementsToDepthIncrease == 0) {
        if (++depth > maxDepth)
          return;

        elementsToDepthIncrease = nextElementsToDepthIncrease;
        nextElementsToDepthIncrease = 0;
      }
      for (GameBoard b : b.enumerate()) {
        q.add(b);
      }
    }
  }
*/

  static float EMPTY_SPACES_WEIGHT = 10000.0f;
  static float SUM_WEIGHT = 400.0f;
  static float MERGED_COUNT_WEIGHT = 2000.0f;
  static float CORNER_WEIGHT = 400.0f;
  static float MERGE_POTENTIAL_WEIGHT = 500.0f;

  static float LOST_PENALTY = 500000.0f;
  static float MULTIPLE_HIGH_CORNER_PENALTY = 1700.0f;
  static float HIGH_OVERFLOW_LIMIT = 2;
  static float HIGH_OVERFLOW_PENALTY_MULT = 70.0f;

  static float HIGH_MERGE_MULTIPLIER = 3.0f;
  static float HIGH_MERGE_EXP = 1.3f;

  static float MAX_VALUE_MULTIPLIER = 2.0f;
  static float MAX_VALUE_EXP = 2.0f;

  static int HIGH_VALUE = 128;

  static float first_score(GameBoard board, GameBoard prev) {
      if (board.count_value(2048) > 0) {
        return Float.MAX_VALUE;
      }

      float score = 0;
      // Score the board
      // float EMPTY_SPACES_CONSTANT;
      int max = board.maxValue();
      if (max > prev.maxValue()) {
        score += Math.pow(max * MAX_VALUE_MULTIPLIER, MAX_VALUE_EXP);
      }

      score += board.count_value(256) * 256 * 1000;
      score += board.count_value(512) * 512 * 100000;
      score += board.count_value(1024) * 1024 * 1000000;


      int empty_count = board.emptySpaces();
      score += empty_count * EMPTY_SPACES_WEIGHT;

      int sum = board.sum();
      score += sum * SUM_WEIGHT;

      int merge_count = prev.tileCount() - board.tileCount();
      if (merge_count > 0) {
        score += merge_count * MERGED_COUNT_WEIGHT;
      }

      score += board.adjacencySum() * MERGE_POTENTIAL_WEIGHT;

      for (int i = HIGH_VALUE; i < 2048; i *= 2) {
        if (board.count_value(i) > HIGH_OVERFLOW_LIMIT) {
          score -= i * HIGH_OVERFLOW_PENALTY_MULT;
        }
      }

      if (board.over && board.won == false)
        score -= LOST_PENALTY;

      int high_corner_count = 0;
      for (Tile t : board.corners()) {
        if (t != null) {
          score += t.value * CORNER_WEIGHT;
          if (t.value >= HIGH_VALUE) {
            if (++high_corner_count >= 1) {
              score -= MULTIPLE_HIGH_CORNER_PENALTY * t.value;
            }

          }
        }
      }

      for (int i = HIGH_VALUE; i < 2048; i *= 2) {
        int count = board.merges.getOrDefault(i, 0);

        score += Math.pow(i * count * HIGH_MERGE_MULTIPLIER, HIGH_MERGE_EXP);
      }

      //System.out.println(score);
      return score;
  }

  static Direction first_search(GameBoard current, EvictingQueue prev) {
    // Find the best scoring option from the four directions
    float highest_score = 0;
    Direction best = null;

    for (Direction d : Direction.values()) {
      GameBoard board = current.peek(d);
      if (prev.contains(board)) {
        continue;
      }
      if (!board.canMove(d)) {
        continue;
      }

      float score = first_score(board, current);
      // Check if it's higher
      if (score > highest_score) {
        best = d;
        highest_score = score;
      }
    }
    System.out.println("Decided to go in direction: " + best);

    return best;
  }

  public static void first_test(long seed) {
    final int LIMIT = 1000;
    Random rand = new Random();

    GameBoard board = new GameBoard();
    board.setSeed(seed);

    // Keep track of past 2 boards, to get out of a loop
    // GameBoard previous = null;
    // Keep track of past 5 boards
    EvictingQueue previous = new EvictingQueue(30);

    int i = 0;
    while (board.over != true && i < LIMIT) {
      System.out.println(board);

      Direction to = first_search(board, previous);
      if (to == null) {
        Direction[] directions = Direction.values();
        for (int j = 0; j < 4; j++) {
          if (board.canMove(directions[j])) {
            to = directions[j];
            break;
          }
        }
        if (to == null) {
          break;
        }
      }

      previous.add(board);
      board.move(to);
      i++;
    }

    System.out.println(board);
    System.out.println("Game over");
    if (board.won) {
      System.out.println("You won");
    } else 
      System.out.println("Better luck next time.");
   }
  


  static class MoveResult {
    public Direction direction;
    public float score;

    public MoveResult(Direction d, float score) {
      this.direction = d;
      this.score = score;
    }
  }

  public static float second_score(GameBoard board) {
    float score = 0.0f;

    return score;
  }

  public static Direction second_next(GameBoard board, int depth) {
    return second_next(board, 0, depth).direction;
  }

  public static MoveResult second_next(GameBoard board, int depth, int maxDepth) {
    float best_score = -Float.MIN_VALUE;
    Direction best_direction = null;
    for (Direction d : Direction.values()) {
      if (board.canMove(d)) {
        GameBoard newBoard = board.peek(d);

        float score = first_score(newBoard, board);

        if (depth != maxDepth) {
          MoveResult result = second_next(newBoard,  depth + 1, maxDepth);
          score += result.score * Math.pow(0.9, depth + 1);
        }

        if (score > best_score) {
          best_direction = d;
          best_score = score;
        }
      }
    }

    return new MoveResult(best_direction, best_score);
  }


  public static void second_test(long seed) {
    final int LIMIT = 2000;
    final int SEARCH_DEPTH = 4;
    GameBoard board = new GameBoard();
    board.setSeed(seed);
    // We need to try searching deeper, so maybe we can make a better prediction

    System.out.println("Welcome to test 2.\n");
    int i = 0;
    while (!board.over) {
      if (i >= LIMIT)
        break;

      System.out.println(board);

      Direction nextMove = second_next(board, SEARCH_DEPTH);
      System.out.println("Decided to go in direction: " + nextMove);

      board.move(nextMove);
      /*
      if (board.count_value(128) >= 2) {
        play(board);
      }
      */
      i++;
    }
    
    System.out.println(board);
    System.out.println("Game over");
    System.out.println("Total moves mode: " + i);
    if (board.won) {
      System.out.println("You win!");
    } else {
      System.out.println("Better luck next time.");
    }

  }

  // Try to only go in 3 directions
  static Direction dumb(GameBoard board) {
    Direction dir = Direction.Up;
    Tile t = board.get(board.size - 1, 0);
    if (t == null) {
      if (board.tilesInRow(board.size - 1) > 0) {
        dir = Direction.Left;
      }
    }
    if (dir != Direction.Left) {
      if (board.canMove(Direction.Down)) dir = Direction.Down;
      if (board.canMove(Direction.Right)) dir = Direction.Right;
      if (board.canMove(Direction.Left)) dir = Direction.Left;
    }

    return dir;
  }

  static void dumb_test(long seed) {
    final int LIMIT = 1500;
    GameBoard board = new GameBoard();
    board.setSeed(seed);
    // We need to try searching deeper, so maybe we can make a better prediction

    System.out.println("Welcome to the dumb test.\n");
    int i = 0;
    while (!board.over) {
      if (i >= LIMIT)
        break;

      System.out.println(board);

      Direction nextMove = dumb(board);
      System.out.println("Decided to go in direction: " + nextMove);

      board.move(nextMove);
      i++;
    }
    
    System.out.println(board);
    System.out.println("Game over");
    System.out.println("Total moves mode: " + i);
    if (board.won) {
      System.out.println("You win!");
    } else {
      System.out.println("Better luck next time.");
    }

  }

  public static MoveResult neat_next(GameBoard board, int depth) {
    return second_next(board, 0, depth);
  }

  public static float neat_score(GameBoard board) {
    /* 
      We're going to try to go back and forth down the grid, like a snake.
      Start from a corner
      8 possible paths
    */

    List<Float> scores = new ArrayList<>();

    float decay = 0.25f;

    float score = 0.0f;
    // To go back and forth down the row
    boolean reversed = false;
    float weight = 1.0f;
    for (int n = 0; n < board.size; ++n) {
      for (int m = 0; m < board.size; ++m) {
        int x = m;
        int y = n;
        if (reversed) {
          x = board.size - 1 - m;
        }
        Tile t = board.get(x, y);
        score += t.value * weight;
        weight *= decay;
      }
      reversed = !reversed;
    }

    scores.add(score);
    score = 0.0f;
    reversed = false;
    weight = 1.0f;
    for (int m = 0; m < board.size; ++m) {
      for (int n = 0; n < board.size; ++n) {
        int x = m;
        int y = n;
        if (reversed) {
          y = board.size - 1 - n;
        }
        Tile t = board.get(x, y);
        score += t.value * weight;
        weight *= decay;
      }
      reversed = !reversed;
    }

    scores.add(score);
    score = 0.0f;
    reversed = false;
    weight = 1.0f;
    for (int n = 0; n < board.size; ++n) {
      for (int m = 0; m < board.size; ++m) {
        int x = m;
        int y = board.size - 1 - n;
        if (reversed) {
          x = board.size - 1 - m;
        }
        Tile t = board.get(x, y);
        score += t.value * weight;
        weight *= decay;
      }
      reversed = !reversed;
    }

    scores.add(score);
    score = 0.0f;
    reversed = false;
    weight = 1.0f;
    for (int m = 0; m < board.size; ++m) {
      for (int n = 0; n < board.size; ++n) {
        int x = board.size - 1 - m;
        int y = n;
        if (reversed) {
          y = board.size - 1 - n;
        }
        Tile t = board.get(x, y);
        score += t.value * weight;
        weight *= decay;
      }
      reversed = !reversed;
    }
    scores.add(score);
    score = 0.0f;
    reversed = true;
    weight = 1.0f;
    for (int n = 0; n < board.size; ++n) {
      for (int m = 0; m < board.size; ++m) {
        int x = m;
        int y = n;
        if (reversed) {
          x = board.size - 1 - m;
        }
        Tile t = board.get(x, y);
        score += t.value * weight;
        weight *= decay;
      }
      reversed = !reversed;
    }

    scores.add(score);
    score = 0.0f;
    reversed = true;
    weight = 1.0f;
    for (int m = 0; m < board.size; ++m) {
      for (int n = 0; n < board.size; ++n) {
        int x = m;
        int y = n;
        if (reversed) {
          y = board.size - 1 - n;
        }
        Tile t = board.get(x, y);
        score += t.value * weight;
        weight *= decay;
      }
      reversed = !reversed;
    }

    scores.add(score);
    score = 0.0f;
    reversed = true;
    weight = 1.0f;
    for (int n = 0; n < board.size; ++n) {
      for (int m = 0; m < board.size; ++m) {
        int x = m;
        int y = board.size - 1 - n;
        if (reversed) {
          x = board.size - 1 - m;
        }
        Tile t = board.get(x, y);
        score += t.value * weight;
        weight *= decay;
      }
      reversed = !reversed;
    }
    
    scores.add(score);
    score = 0.0f;
    reversed = true;
    weight = 1.0f;
    for (int m = 0; m < board.size; ++m) {
      for (int n = 0; n < board.size; ++n) {
        int x = board.size - 1 - m;
        int y = n;
        if (reversed) {
          y = board.size - 1 - n;
        }
        Tile t = board.get(x, y);
        score += t.value * weight;
        weight *= decay;
      }
      reversed = !reversed;
    }

    scores.add(score);
    
    return Collections.max(scores);
  }

  public static MoveResult neat_next(GameBoard board, int depth, int maxDepth) {
    float best_score = -Float.MIN_VALUE;
    Direction best_direction = null;
    for (Direction d : Direction.values()) {
      if (board.canMove(d)) {
        GameBoard newBoard = board.peek(d);

        if (newBoard.merges.size() >= 3)
          return new MoveResult(best_direction, best_score);

        float score = neat_score(newBoard);

        if (depth != maxDepth) {
          MoveResult result = neat_next(newBoard,  depth + 1, maxDepth);
          // Diminishing returns the farther you go down
          score += result.score * Math.pow(0.9, depth + 1);
        }

        
        if (score > best_score) {
          best_direction = d;
          best_score = score;
        }
      }
    }

    return new MoveResult(best_direction, best_score);
  }

  public static boolean neat_test(long seed) {
    final int LIMIT = 2000;
    final int SEARCH_DEPTH = 5;
    GameBoard board = new GameBoard();
    board.setSeed(seed);
    // We need to try searching deeper, so maybe we can make a better prediction

    System.out.println("Welcome to the last test you'll ever need.\n");
    int i = 0;
    while (!board.over) {
      if (i >= LIMIT)
        break;

      System.out.println(board);

      MoveResult nextMove = neat_next(board, SEARCH_DEPTH);
      System.out.println("Decided to go in direction: " + nextMove.direction);

      board.move(nextMove.direction);
      i++;
    }
    
    System.out.println(board);
    System.out.println("Game over");
    System.out.println("Total moves mode: " + i);
    if (board.won) {
      System.out.println("You win!");
      return true;
    } else {
      System.out.println("Better luck next time.");
      return false;
    }

  }


  public static void run_tests() {
    final long seed = System.currentTimeMillis();
    run_tests(seed);
  }

  public static void run_tests(long seed) {
    // Maybe compare time here?
    //first_test(seed);
    //second_test(seed);
    //dumb_test(seed);
    //neat_test(seed);
    while (neat_test(seed)!= true) {

    }

  }

  public static void examine(GameBoard board) {
    Scanner scanner = new Scanner(System.in);
    boolean done = false;
    while (board.over == false || !done) {
      int x = scanner.nextInt();
      int y = scanner.nextInt();

      if (x == -1) {
        done = true;
        break;
      }
      
      Tile t = board.get(x, y);
      if (t != null) {
        System.out.println(t);
      }
      else {
        System.out.println("T is null");
      }
    }
  }
  public static void play(GameBoard board) {
    Scanner scanner = new Scanner(System.in);
       System.out.println(board);
       while (board.over != true) {
         System.out.println(board);
         System.out.println("What would you like to do?");
         int i = 0;
         for (Direction d : Direction.values()) {
           System.out.println(String.format("%d: %s", i, d));
           i++;
         }
         int selected = scanner.nextInt();
         switch (selected) {
           case 8:
            selected = 0;
              break;
            case 4:
              selected = 1;
              break;
             case 2:
              selected = 3;
              break;
            case 6:
              selected = 2;
              break;

            case 5:
              examine(board);
              continue;
         }
         board.move(Direction.values()[selected]);
    
       }
    
       System.out.println("Game over");
       if (board.won) {
         System.out.println("You win!");
       }
       else {
         System.out.println("Better luck next time.");
       } 
  }

  public static void main(String[] args) {
    // Kickoff
    Scanner scanner = new Scanner(System.in);

    run_tests();
    //play(new GameBoard());
    /*
     * System.out.println(board); for (int i = 0; i < 5; i++) { board.doUp();
     * System.out.println(board);
     * 
     * } board.doUp(); System.out.println(board);
     * System.out.println("Starting peek!\n\n"); GameBoard peek =
     * GameBoard.clone(board); System.out.println(peek); //peek.doUp();
     * System.out.println(peek); System.out.println("Original board:");
     * System.out.println(board); if (board.equals(peek))
     * System.out.println("board is peek"); else System.out.println("Nope");
     */

  }
}

class EvictingQueue {
  Deque<GameBoard> items;
  int max_size;

  public EvictingQueue(int max_size) {
    items = new ArrayDeque<GameBoard>();
    this.max_size = max_size;
  }

  public boolean contains(GameBoard board) {
    for (GameBoard b : items) {
      if (b.equals(board)) {
        return true;
      }
    }
    return false;
  }

  public void add(GameBoard board) {
    items.add(board);
    if (items.size() > max_size) {
      items.pop();
    }
  }

}
