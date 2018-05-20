package solutions;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
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
  public static void run_tests() {
    final long seed = System.currentTimeMillis();
    run_tests(seed);
  }

  static boolean parallel = false;
  static final int THREAD_COUNT = 5;
  public static void run_tests(long seed) {
    // Prepare a lit of Solvers
    // We'll use this to run them all concurrently, or to have the user select
    if (parallel) {
      // Do all at onces
      Collection<Solver> tests = new ArrayList<>();
      tests.add(new SnakeTest(seed));
      tests.add(new AverageTest(seed));
      tests.add(new DumbTest(seed));
      tests.add(new ScoreTest(seed));
      tests.add(new DeepScoreTest(seed));

      ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
      try {
        List<Future<TestResult>> results = executor.invokeAll(tests);

        for (Future<TestResult> result : results) {
          TestResult r = result.get();
          System.out.println(r);
        }
        System.out.println("Done");
      } catch (Exception e) {
        e.printStackTrace();
      }
      executor.shutdown();

    } else {

      // Maybe compare time here?
      // first_test(seed);
      // second_test(seed);
      // dumb_test(seed);
      // neat_test(seed);
      // average_test(seed);
      // Solver ranSolver = new AverageTest(seed);
      // TestResult avg = ranSolver.run();
      // Solver snake = new SnakeTest(seed);
      // TestResult r = snake.run();

      // Solver score = new ScoreTest(seed);
      Solver deep_score = new DeepScoreTest(seed);
      TestResult r = deep_score.run();
      System.out.println(r.output);
      // System.out.println(avg.output);
    }

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
    // GameBoard b = new GameBoard();
    // GameBoard c = new GameBoard();
    // System.out.println(b.equals(c));
    // play(new GameBoard());
  }
}
