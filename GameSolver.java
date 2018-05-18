import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;
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
    //return !((m - 1) >= board.size && (n - 1) >= board.size);
  }

  public Tile next() {
    Tile ret = board.get(m, n);
    if (n >= board.size - 1) {
      n = 0;
      m++;
    }
    else {
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
    if (getClass()  != o.getClass())
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
    }
    else if (direction == Direction.Up) {
      Collections.reverse(y);
    }
  }
}

class GameBoard implements Iterable<Tile> {
  public static final Random rand = new Random();
  public static final float twoProbability = 0.9f;


  public boolean won = false;
  public boolean over = false;


  public Tile[][] cells;

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
        //cells[i][j] = new Tile(new Position(i, j), null);
      }
      
    }
    generateNewItem();
  }

  public static GameBoard clone(GameBoard original) {
    int size = original.size;
    GameBoard copied = new GameBoard(size);

    for (int m = 0; m < size; ++m) {
      for (int n = 0; n < size; ++n) {
        Tile oTile = original.get(m, n);
        if (oTile != null) {
          copied.cells[m][n] =  new Tile(m, n, oTile.value);
        }
        else {
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
    set(t.x, t.y, t);
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

    int next = rand.nextInt(freeSpaces.size());
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

    /*switch (direction) {
      case Up:
        v = new Vector(0, 1);
        break;
      case Left:
        v = new Vector(-1, 0);
        break;
      case Down:
        v = new Vector(0, -1);
        break;
      case Right:
        v = new Vector(1, 0);
        break;
      default:
        v = new Vector(0, 0);
    }
    */
    return v;
  }
  public PositionProspect findFurthestPosition(Position p, Vector v) {
    Position previous;

    System.err.println("Calculating furthest position");
    System.err.println(String.format("from P: %s with v: %s", p, v));

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

  public void move(Direction direction) {

    // Check if game is over first


    Vector v = getDirectionVector(direction);
    System.err.println(v);
    System.err.println(direction);
    Traversals traversals = new Traversals(direction, size);
    boolean moved = false;

    resetTiles();

    for (int x : traversals.x) {
      for (int y : traversals.y) {
        System.err.println(String.format("x: %d, y: %d", x, y));

        final Position position = new Position(x, y);
        Tile tile = get(position);

        if (tile != null) {
          System.err.println("Tile not null, it exists");

          PositionProspect positions = findFurthestPosition(position, v);

          System.err.println(String.format("next to furthest position: %s", positions.next));
          System.err.println(String.format("actual furthest position: %s", positions.furthest));

          Tile next = get(positions.next);

          // Determine if these tiles need to merge
          if (next != null &&  next.value == tile.value && next.mergedFrom == null) {
            System.err.println("Merge happening");

            Tile merged = new Tile(positions.next, tile.value * 2);
            merged.mergedFrom = new Tile[]{ tile, next };

            set(merged);
            remove(tile);

            tile.updatePosition(positions.next);

            if (merged.value == 2048) {
              won = true;
            }

          }
          else {
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

  public int score() {
    return -1;
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

  public boolean movesAvailable() {
    return !full() || matchesAvailable();
  }

  public boolean matchesAvailable() {
    Tile t;


    for (int x = 0; x < size; ++x) {
      for (int y = 0; y < size; ++y) {
        t = get(x, y);
        
        for (Direction dir : Direction.values()) {
          Vector v = getDirectionVector(dir);
          Tile other = get(x + v.x, y + v.y);

          if (other != null && t.value == other.value) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public boolean full() {
    List<Position> empty = emptyPositions();
    
    return empty.size() == 0;
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

  public String toString() {
    StringBuilder builder = new StringBuilder();

    for (int i = 0; i < size; ++i) {
      for (int j = 0; j < size; j++) {
        Tile item = get(i, j);

        if (item != null) {
          builder.append(item).append(" ");
        }
        else {
          builder.append("_ ");
        }
      }
      builder.append("\n");
    }
    return builder.toString();
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
        System.out.println("t: " + t);
        System.out.println("oT: " + oT);
        if (t == oT) {
          // Shouldn't have to need this but I do???
          // Otherwise it thinks if they're both null
          if (t != null && oT != null) {
            System.out.println(String.format("%s != %s", t, oT));
            return false;
          }
        }
      }
    }

    return true;
  }
}

public class GameSolver {

  public static void main(String[] args) {
    // Kickoff
    GameBoard board = new GameBoard();    
    Scanner scanner = new Scanner(System.in);

    while (board.over != true) {
      System.out.println(board);
      System.out.println("What would you like to do?");
      int i = 0;
      for (Direction d : Direction.values()) {
        System.out.println(String.format("%d: %s", i, d));
        i++;
      }
      int selected = scanner.nextInt();
      board.move(Direction.values()[selected]);

    }

    System.out.println("Game over");
    if (board.won) {
      System.out.println("You win!");
    }
    else {
      System.out.println("Better luck next time.");
    }
    /*System.out.println(board);
    for (int i = 0; i < 5; i++) {
      board.doUp();
      System.out.println(board);
      
    }
    board.doUp();
    System.out.println(board);
    System.out.println("Starting peek!\n\n");
    GameBoard peek = GameBoard.clone(board);
    System.out.println(peek);
    //peek.doUp();
    System.out.println(peek);
    System.out.println("Original board:");
    System.out.println(board);
    if (board.equals(peek))
      System.out.println("board is peek");
    else
      System.out.println("Nope");
*/
    

  }
}
