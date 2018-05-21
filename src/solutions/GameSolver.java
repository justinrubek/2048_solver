package solutions;

import java.util.*;
import java.io.*;
import java.text.DecimalFormat;
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
    if (parallel) {
      // Do all at once
      Collection<Solver> tests = new ArrayList<>();
      tests.add(new ScoreTest(seed));
      tests.add(new DeepScoreTest(seed));
      tests.add(new DumbTest(seed));
      tests.add(new SnakeTest(seed));
      tests.add(new AverageTest(seed));
      tests.add(new SmoothnessTest(seed));

      // This test sucks
      // tests.add(new ConcurrentAverageTest(seed));

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
      boolean automate = true;

      if (automate) {
        final int RUNS = 400;

        for (int i = 0; i < RUNS; ++i) {
          Solver smooth = new SmoothnessTest();
          TestResult result = smooth.run();
          System.out.println(result);
        }
      } else {
        // Prompt the user
      }

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
      // Solver deep_score = new DeepScoreTest(seed);
      // TestResult r = deep_score.run();
      // System.out.println(r.output);
      // System.out.println(avg.output);
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
      char selected;
      try {
        selected = (char) System.in.read();
      } catch (Exception e) {
        e.printStackTrace();
        continue;
      }
      int val = 0;
      switch (selected) {
      case 'w':
        val = 0;
        break;
      case 'a':
        val = 1;
        break;
      case 's':
        val = 3;
        break;
      case 'd':
        val = 2;
        break;
      }
      board.move(Direction.values()[val]);

    }

    System.out.println("Game over");
    if (board.won) {
      System.out.println("You win!");
    } else {
      System.out.println("Better luck next time.");
    }
  }

  static String getNextStat(Scanner scanner) {
    return scanner.nextLine().split(":")[1].trim();
  }

  public static void give_stats(String path) {

    try {
      File f = new File(path);
      Scanner scanner = new Scanner(f);

      List<Long> seeds = new ArrayList<>();
      List<Boolean> results = new ArrayList<>();
      List<Integer> moves = new ArrayList<>();
      List<Integer> winningMoves = new ArrayList<>();
      List<Integer> losingMoves = new ArrayList<>();
      List<Long> times = new ArrayList<>();
      List<Long> winningTimes = new ArrayList<>();
      List<Long> timesPer = new ArrayList<>();

      int winCount = 0;
      int count = 0;

      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();

        if (line.contains("test")) {
          count++;
          String name = line.split(":")[1].trim();
          String[] stats = new String[5];
          for (int i = 0; i < 5; i++) {
            stats[i] = getNextStat(scanner);
          }

          long seed = Long.parseLong(stats[0]);
          boolean won = Boolean.parseBoolean(stats[1]);
          int totalmoves = Integer.parseInt(stats[2]);
          long time = Long.parseLong(stats[3]);
          long timePerMove = Long.parseLong(stats[4]);

          seeds.add(seed);
          results.add(won);
          moves.add(totalmoves);
          times.add(time);
          timesPer.add(timePerMove);

          if (won) {
            winningMoves.add(totalmoves);
            winningTimes.add(time);
            winCount++;
          } else {
            winningMoves.add(totalmoves);
          }
        }
      }

      StringBuilder output = new StringBuilder();

      //Double winPercentage = (double) results.stream().filter(b -> b).collect(Collectors.toList()) / count;
      Double winPercentage = (double) winCount / count;
      Double winMoveAverage = winningMoves.stream().mapToInt(v -> v).average().orElse(0.0);
      Double winTimeAverage = winningTimes.stream().mapToLong(v -> v).average().orElse(0);

      output.append("Win Percentage                 :").append(winPercentage).append("\n");
      output.append("Average moves in winning games :").append(new DecimalFormat("###.##").format(winMoveAverage)).append("\n");
      output.append("Average time to win game       :").append(new DecimalFormat("######").format(winTimeAverage / 1000000)).append("ms\n");

      System.out.println(output.toString());

    } catch (FileNotFoundException e) {
      System.out.println("File not found");
    }

  }

  public static void main(String[] args) {
    // Kickoff

    // play(new GameBoard());
    // run_tests();
    give_stats("smooth4deep.txt");

  }
}
