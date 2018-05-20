package solutions;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.*;
import java.util.Random;

import board.GameBoard;
import board.Tile;
import board.Direction;

/*
 * This test goes in a direction, then tries a bunch of random moves until it
 * can no longer move It then uses the average of these scores to try to figure
 * which path is best
 * 
 * The "score" is the sum of all merges happening in the move
 * 
 * There should be room for eventual threading
 */

public class ConcurrentAverageTest implements Solver {
    public int MAX_SEARCH_DEPTH = 4;
    public int MOVE_LIMIT = 2000;
    public int WIN_POWER = 11;
    public int RUNS_TO_MAKE = 150;
    public int THREAD_COUNT = 5;
    public long seed;

    StringBuilder output;

    GameBoard board;

    public ConcurrentAverageTest(long seed) {
        this.seed = seed;

        board = new GameBoard();
        board.setWinValue(WIN_POWER);
        output = new StringBuilder();
    }

    public void setThreadCount(int count) {
        THREAD_COUNT = count;
    }

    public MoveResult decide(GameBoard board) {
        float bestScore = 0.0f;
        Direction bestDirection = null;

        for (Direction d : Direction.values()) {
            AverageResult result = multiple_runs(board, d, RUNS_TO_MAKE);
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

    void print(Object o) {
        output.append(o);
        // Print to stderr so we can see it live if we chose, but aren't required to for
        // performance
        // This is probably still not good for performance? I have no idea
        System.err.print(o);
    }

    void println(Object o) {
        output.append(o).append("\n");
        System.err.println(o);
    }

    public TestResult run() {

        println("ConcurrentAverageTest");
        print("seed:");
        println(seed);

        long start = System.nanoTime();

        int i = 0;
        while (!board.over) {
            if (i >= MOVE_LIMIT)
                break;

            println(board);

            MoveResult nextMove = decide(board);
            print("direction:");
            println(nextMove.direction);

            if (nextMove.direction == null) {
                break;
            }
            board.move(nextMove.direction);
            i++;
        }

        long end = System.nanoTime();
        println(board);
        println("gameover");
        print("seed:");
        println(seed);
        print("totalmoves:");
        println(i);
        if (board.won) {
            println("win");
        } else {
            println("loss");
        }

        return new TestResult(board).move_count(i).output(output.toString()).name("ConcurrentAverageTest")
                .time_taken(end - start);
    }

    public static float score(GameBoard board) {
        float score = 0.0f;
        for (int i = 2; i <= 2048; i *= 2) {
            int merged = board.merges.getOrDefault(i, 0);
            score += merged * i;
        }

        return score;
    }

    class AverageRun implements Callable<AverageResult> {
        GameBoard board;
        Direction direction;
        Random rand = new Random();

        public AverageRun(GameBoard board, Direction direction) {
            this.board = board;
            this.direction = direction;
        }

        @Override
        public AverageResult call() throws Exception {
            return single_run(board, direction);
        }

        public AverageResult single_run(GameBoard board, Direction direction) {
            if (board.move(direction) == false) {
                return null;
            }

            // Score is the sum of all merges that happened this run
            float score = score(board);
            float moves = 1;
            while (board.movesAvailable()) {
                // Go in a random direction
                Direction to = Direction.values()[rand.nextInt(4)];
                boolean moved = board.move(to);
                // System.out.println(newBoard);
                if (moved == false) {
                    break;
                }

                score += score(board);
                moves++;
            }
            return new AverageResult(score, moves);
        }
    }

    public AverageResult multiple_runs(GameBoard board, Direction direction, int amount) {
        float totalScore = 0.0f;
        int performed = 0;

        Collection<AverageRun> runs = new ArrayList<>(amount);
        for (int i = 0; i < amount; i++) {
            AverageRun run = new AverageRun(GameBoard.clone(board), direction);
            runs.add(run);
        }
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        try {
            List<Future<AverageResult>> results = executor.invokeAll(runs);

            for (Future<AverageResult> result : results) {
                AverageResult r = result.get();

                if (r == null) {
                    executor.shutdown();
                    return null;
                }
                // This could be improved by not waiting for them all to complete
                totalScore += r.score;
                performed += r.count;


            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        executor.shutdown();

        float average = totalScore / amount;
        float moves = performed / amount;

        return new AverageResult(average, moves);
    }
}