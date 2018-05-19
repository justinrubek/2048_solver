package solutions;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.Random;

import board.GameBoard;
import board.Tile;
import board.Direction;
import solutions.GameSolver.MoveResult;

class AverageResult {
    public float score;
    public float count;

    public AverageResult(float score, float count) {
        this.score = score;
        this.count = count;
    }
}

/*
 * This test goes in a direction, then tries a bunch of random moves until it
 * can no longer move It then uses the average of these scores to try to figure
 * which path is best
 * 
 * The "score" is the sum of all merges happening in the move
 * 
 * There should be room for eventual threading
 */

public class AverageTest implements Solver {
    public int MAX_SEARCH_DEPTH = 4;
    public int MOVE_LIMIT = 2000;
    public int WIN_POWER = 11;
    public int RUNS_TO_MAKE = 50;
    public long seed;

    StringBuilder output;

    GameBoard board;

    public AverageTest(long seed) {
        this.seed = seed;

        board = new GameBoard();
        output = new StringBuilder();
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
        // Print to stderr so we can see it live if we chose, but aren't required to for performance
        // This is probably still not good for performance? I have no idea
        System.err.print(o);
    }

    void println(Object o) {
        output.append(o).append("\n");
        System.err.println(o);
    }

    public TestResult run() {
        TestResult result = new TestResult();

        println("RandomAverageTest");
        print("seed:");
        println(seed);

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

        println(board);
        println("gameover");
        print("seed:");
        println(seed);
        print("totalmoves:");
        println(i);
        if (board.won) {
            println("win");
        } 
        else {
            println("loss");
        }

        result.output = output.toString();
        return result;

    }

    public static float score(GameBoard board) {
        float score = 0.0f;
        for (int i = 2; i <= 2048; i *= 2) {
            int merged = board.merges.getOrDefault(i, 0);
            score += merged * i;
        }

        return score;
    }

    public static AverageResult single_run(GameBoard board, Direction direction) {
        Random rand = new Random();
        GameBoard newBoard = board.peek(direction);
        if (newBoard.moved == false) {
            return null;
        }

        // Score is the sum of all merges that happened this run
        float score = score(newBoard);
        float moves = 1;
        while (board.movesAvailable()) {
            // Go in a random direction
            Direction to = Direction.values()[rand.nextInt(4)];
            boolean moved = newBoard.move(to);
            // System.out.println(newBoard);
            if (moved == false) {
                break;
            }

            score += score(newBoard);
            moves++;
        }
        return new AverageResult(score, moves);
    }

    public static AverageResult multiple_runs(GameBoard board, Direction direction, int amount) {
        float totalScore = 0.0f;
        int performed = 0;

        for (int i = 0; i < amount; i++) {
            // Do the run
            AverageResult run = single_run(board, direction);
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
}