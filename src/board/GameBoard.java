package board;

import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.ArrayDeque;
import java.lang.StringBuilder;

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

public class GameBoard {
  public final Random rand = new Random();
  public static final float twoProbability = 0.9f;
  int WIN_VALUE = 2048;

  public boolean won = false;
  public boolean over = false;
  public boolean moved = false;

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

  // Set the win value to 2^newExp
  public void setWinValue(int newExp) {
    if (newExp < 31) {
      WIN_VALUE = (int) Math.pow(2, newExp);
    }
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
    return new GameBoard[] { this.peekRight(), this.peekUp(), this.peekLeft(), this.peekDown() };
  }

  public boolean move(Direction direction) {
    moved = false;

    Vector v = getDirectionVector(direction);
    //System.err.println(v);
    //System.err.println(direction);
    Traversals traversals = new Traversals(direction, size);

    merges.clear();
    resetTiles();
    Tile tile;

    for (int x : traversals.x) {
      for (int y : traversals.y) {
        //System.err.println(String.format("x: %d, y: %d", x, y));

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

            if (merged.value == WIN_VALUE) {
              won = true;
              over = true;
            }
          } else {
            moveTile(tile, positions.furthest);
          }

          // It isn't in it's original place
          if (!tile.isAt(position)) {
            //System.err.println("Detected change of position");
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
    if (over)
      return false;
      
    Tile t;

    for (int x = 0; x < size; ++x) {
      for (int y = 0; y < size; ++y) {
        t = get(x, y);
        if (t != null) {
          for (Tile a : adjacent(x, y)) {
            if (t.value == a.value) {
              return true;
            }
          }
        }
        /*
         * if (t != null) { for (Direction dir : Direction.values()) { Vector v =
         * getDirectionVector(dir); Tile other = get(x + v.x, y + v.y);
         * 
         * if (other != null && t.value == other.value) { return true; } } }
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
    items.add(get(0, 0));
    items.add(get(0, size - 1));
    items.add(get(size - 1, 0));
    items.add(get(size - 1, size - 1));
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
     * Tile t; t = get(m + 1, n); if (t != null) items.add(t); t = get(m - 1, n); if
     * (t != null) items.add(t); t = get(m, n + 1); if (t != null) items.add(t); t =
     * get(m, n - 1); if (t != null) items.add(t);
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
    if (this.getClass().equals(other.getClass()) == false)
      return false;

    GameBoard o = (GameBoard) other;
    if (o.size != this.size)
      return false;

    for (int m = 0; m < size; ++m) {
      for (int n = 0; n < size; ++n) {
        Tile t = this.get(m, n);
        Tile oT = o.get(m, n);
        if (t != null && oT != null && t.equals(oT) == false) {
          return false;
        }
        if (t == null && oT != null || t != null && oT == null) {
          return false;
        }
      }
    }
    return true;
  }
}
