package solutions;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.*;
import java.util.Random;

import board.GameBoard;
import board.Tile;
import board.Direction;

public class ScoreTest implements Solver {
    float EMPTY_SPACES_WEIGHT = 10000.0f;
    float SUM_WEIGHT = 400.0f;
    float MERGED_COUNT_WEIGHT = 2000.0f;
    float CORNER_WEIGHT = 400.0f;
    float MERGE_POTENTIAL_WEIGHT = 500.0f;

    float LOST_PENALTY = 500000.0f;
    float MULTIPLE_HIGH_CORNER_PENALTY = 1700.0f;
    float HIGH_OVERFLOW_LIMIT = 2;
    float HIGH_OVERFLOW_PENALTY_MULT = 70.0f;

    float HIGH_MERGE_MULTIPLIER = 3.0f;
    float HIGH_MERGE_EXP = 1.3f;

    float MAX_VALUE_MULTIPLIER = 2.0f;
    float MAX_VALUE_EXP = 2.0f;

    int HIGH_VALUE = 128;
    public int MOVE_LIMIT = 2000;
    public int WIN_POWER = 11;
    public long seed;

    StringBuilder output;

    GameBoard board;
    EvictingQueue previous;

    public ScoreTest(long seed) {
        this.seed = seed;

        board = new GameBoard();
        board.setWinValue(WIN_POWER);
        output = new StringBuilder();

        // Used to see if we've seen a pattern before
        previous = new EvictingQueue(30);
    }

    public MoveResult decide(GameBoard board) {
        float highest_score = 0;
        Direction best = null;

        for (Direction d : Direction.values()) {
            GameBoard newBoard = board.peek(d);
            if (previous.contains(board)) {
                continue;
            }
            if (!board.canMove(d)) {
                continue;
            }

            float score = score(newBoard, board);
            // Check if it's higher
            if (score > highest_score) {
                best = d;
                highest_score = score;
            }
        }
        return new MoveResult(best, highest_score);
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

        println("ScoreTest");
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

        return new TestResult(board).move_count(i).output(output.toString()).name("ScoreTest")
                .time_taken(end - start);
    }

    public float score(GameBoard board, GameBoard prev) {
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
        return score;
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