package solutions;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import board.GameBoard;
import board.Tile;
import board.Direction;

// Tries to only go in 3 directions when possible
public class DumbTest implements Solver {
    public int MAX_SEARCH_DEPTH = 4;
    public int MOVE_LIMIT = 2000;
    public int WIN_POWER = 11;
    public long seed;

    StringBuilder output;

    GameBoard board;

    public DumbTest(long seed) {
        this.seed = seed;

        board = new GameBoard();
        board.setWinValue(WIN_POWER);
        output = new StringBuilder();
    }

    public MoveResult decide(GameBoard board) {
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
        return new MoveResult(dir, 0.0f);
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

        println("DumbTest");
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

        return new TestResult(board).move_count(i).output(output.toString()).name("DumbTest").time_taken(end - start);

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