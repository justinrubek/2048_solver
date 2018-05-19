package solutions;

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
import java.util.concurrent.Callable;
import java.util.Queue;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.ArrayDeque;
import java.lang.StringBuilder;

import board.*;

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
   * Direction first_search(GameBoard start, int maxDepth) {
   * 
   * Set<GameBoard> seen = new HashSet<>(); Queue<GameBoard> q = new
   * ArrayDeque<>();
   * 
   * int depth = 0; int elementsToDepthIncrease = 1; int
   * nextElementsToDepthIncrease = 0;
   * 
   * while (!q.isEmpty()) { GameBoard current = q.poll(); if
   * (seen.contains(current)) { continue;b }
   * 
   * 
   * // Calculate score
   * 
   * nextElementsToDepthIncrease += 4; if (--elementsToDepthIncrease == 0) { if
   * (++depth > maxDepth) return;
   * 
   * elementsToDepthIncrease = nextElementsToDepthIncrease;
   * nextElementsToDepthIncrease = 0; } for (GameBoard b : b.enumerate()) {
   * q.add(b); } } }
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

    // System.out.println(score);
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
          MoveResult result = second_next(newBoard, depth + 1, maxDepth);
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
       * if (board.count_value(128) >= 2) { play(board); }
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
      if (board.canMove(Direction.Down))
        dir = Direction.Down;
      if (board.canMove(Direction.Right))
        dir = Direction.Right;
      if (board.canMove(Direction.Left))
        dir = Direction.Left;
    }

    return dir;
  }

  static void dumb_test(long seed) {
    final int LIMIT = 1500;
    GameBoard board = new GameBoard();
    board.setSeed(seed);

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
    return neat_next(board, 0, depth);
  }

  public static float neat_score(GameBoard board) {
    /*
     * We're going to try to go back and forth down the grid, like a snake. Start
     * from a corner 8 possible paths
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
        if (t != null) {
          score += t.value * weight;
        } // Still decay weight when we find nothing
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
        if (t != null) {
          score += t.value * weight;
        } 
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
        if (t != null) {
          score += t.value * weight;
        } 
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
        if (t != null) {
          score += t.value * weight;
        } 
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
        if (t != null) {
          score += t.value * weight;
        } 
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
        if (t != null) {
          score += t.value * weight;
        } 
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
        if (t != null) {
          score += t.value * weight;
        } 
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
        if (t != null) {
          score += t.value * weight;
        } 
        weight *= decay;
      }
      reversed = !reversed;
    }

    scores.add(score);

    float max = Collections.max(scores);

    for (Tile t : board.corners()) {
      if (t != null) {
        max += t.value;
      }
    }

    return Collections.max(scores);
  }

  public static MoveResult neat_next(GameBoard board, int depth, int maxDepth) {
    float best_score = -Float.MIN_VALUE;
    Direction best_direction = null;
    for (Direction d : Direction.values()) {
      if (board.canMove(d)) {
        GameBoard newBoard = board.peek(d);

        float score = neat_score(newBoard);

        if (depth != maxDepth) {
          MoveResult result = neat_next(newBoard, depth + 1, maxDepth);
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
    final int SEARCH_DEPTH = 4;
    GameBoard board = new GameBoard();
    board.setSeed(seed);
    // We need to try searching deeper, so maybe we can make a better prediction

    System.out.println("Welcome to the last test you'll ever need.\n");
    System.out.println("Seed: " + seed);
    int i = 0;
    while (!board.over) {
      if (i >= LIMIT)
        break;

      System.out.println(board);

      MoveResult nextMove = neat_next(board, SEARCH_DEPTH);
      System.out.println("Decided to go in direction: " + nextMove.direction);

      if (nextMove.direction == null) {
        break;
      }
      board.move(nextMove.direction);
      i++;
    }

    System.out.println(board);
    System.out.println("Game over");
    System.out.println("Seed: " + seed);
    System.out.println("Total moves mode: " + i);
    if (board.won) {
      System.out.println("You win!");
      return true;
    } else {
      System.out.println("Better luck next time.");
      return false;
    }

  }

  static class AverageResult {
    public float score;
    public float count;

    public AverageResult(float score, float count) {
      this.score = score;
      this.count = count;
    }
  }

  public static float merge_score(GameBoard board) {
    float score = 0.0f;
    for(int i = 2; i <= 2048; i *= 2) {
      int merged = board.merges.getOrDefault(i, 0);
      score += merged * i;
    }

    return score;
  }

  public static AverageResult average_run(GameBoard board, Direction direction) {
    Random rand = new Random();
      GameBoard newBoard = board.peek(direction);
      if(newBoard.moved == false) {
        return null;
      }

      // Score is the sum of all merges that happened this run
      float score = merge_score(newBoard);
      float moves = 1;
      while (board.movesAvailable()) {
        // Go in a random direction
        Direction to = Direction.values()[rand.nextInt(4)];
        boolean moved = newBoard.move(to);
        //System.out.println(newBoard);
        if (moved == false) {
          break;
        }

        score += merge_score(newBoard);
        moves++;
      }
      return new AverageResult(score, moves);
  }

  public static AverageResult average_perform_runs(GameBoard board, Direction direction, int amount) {
    float totalScore = 0.0f;
    int performed = 0;

    for (int i = 0; i < amount; i++) {
      // Do the run
      AverageResult run = average_run(board, direction);
      if (run == null) {
        return null;
      }
      totalScore += run.score;
      performed += run.count;
    }

    float average = totalScore / amount;
    float moves = performed / amount;

    return new AverageResult(average, moves);
  }

  public static MoveResult average_next(GameBoard board, int RUN_COUNT) {
    float bestScore = 0.0f;
    Direction bestDirection = null;

    for (Direction d : Direction.values()) {
      AverageResult result = average_perform_runs(board, d, RUN_COUNT);
      if (result == null) {
        continue;
      }

      if (result.score > bestScore) {
        bestScore = result.score;
        bestDirection = d;
        
      }
    }

    return new MoveResult(bestDirection, bestScore);
  }

  public static boolean average_test(long seed) {
    /*
     * Gather the results of performing a move several times randomly. Then we can gauge the
     * average score increase per move direction
     */
    final int LIMIT = 2000;
    final int SEARCH_DEPTH = 4;
    final int RUNS_TO_AVERAGE = 150;
    GameBoard board = new GameBoard();
    board.setSeed(seed);

    System.out.println("RandomAverage");
    System.out.println("seed:" + seed);
    int i = 0;
    while (board.count_value(2048) <= 0) {
      if (i >= LIMIT)
        break;

      System.out.println(board);

      MoveResult nextMove = average_next(board, RUNS_TO_AVERAGE);
      System.out.println("direction:" + nextMove.direction);

      if (nextMove.direction == null) {
        break;
      }
      board.move(nextMove.direction);
      i++;
    }

    System.out.println(board);
    System.out.println("GameOver");
    System.out.println("Seed:" + seed);
    System.out.println("TotalMoves:" + i);
    if (board.won) {
      System.out.println("Win");
      return true;
    } else {
      System.out.println("Loss");
      return false;
    }

  }

  public static void run_tests() {
    final long seed = System.currentTimeMillis();
    run_tests(seed);
  }

  public static void run_tests(long seed) {
    // Maybe compare time here?
    // first_test(seed);
    // second_test(seed);
    // dumb_test(seed);
    // neat_test(seed);
    // average_test(seed);
    Solver snake = new SnakeTest(seed);
    TestResult r = snake.run();
    
    System.out.println(r.output);

  }

  // Shouldn't really be needed
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
      } else {
        System.out.println("T is null");
      }
    }
  }

  // Give control to the terminal
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
    } else {
      System.out.println("Better luck next time.");
    }
  }

  public static void main(String[] args) {
    // Kickoff
    Scanner scanner = new Scanner(System.in);

    run_tests();
//    GameBoard b = new GameBoard();
//    GameBoard c = new GameBoard();
//    System.out.println(b.equals(c));
    // play(new GameBoard());
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
