package solutions;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.*;

import board.GameBoard;
import board.Tile;
import board.Direction;

/*
    This is a modified version of the original 'neat_test'

    It should check in a snake from each corner, with eight total moves

    Maybe it should check and only run the tests on corners that have values
    in the top 2-3 values of the board
*/
public class SnakeTest implements Solver, Callable<TestResult> {
    // Maybe we want to change these later to tweak
    int MAX_SEARCH_DEPTH = 4;
    int MOVE_LIMIT = 2000;
    int WIN_POWER = 11;
    long seed;

    GameBoard board;

    StringBuilder output;

    public SnakeTest(long seed) {
        this.seed = seed;
        board = new GameBoard();
        board.setSeed(seed);
        board.setWinValue(WIN_POWER);

        output = new StringBuilder();
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

        println("SnakeTest");
        print("seed:");
        println(seed);

        long start = System.nanoTime();

        int i = 0;
        while (!board.over) {
            if (i >= MOVE_LIMIT)
                break;

            println(board);

            MoveResult nextMove = next(board, MAX_SEARCH_DEPTH);
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
        } 
        else {
            println("loss");
        }

        return new TestResult(board).move_count(i).output(output.toString()).name("SnakeTest").time_taken(end - start);
        // return new TestResult(board, i, output.toString(), "SnakeTest");
    }


    public MoveResult decide(GameBoard board) {
        return next(board, MAX_SEARCH_DEPTH);
    }

    public static MoveResult next(GameBoard board, int depth) {
        return next(board, 0, depth);
    }

    public static MoveResult next(GameBoard board, int depth, int maxDepth) {
        float best_score = -Float.MIN_VALUE;
        Direction best_direction = null;
        for (Direction d : Direction.values()) {
            if (board.canMove(d)) {
                GameBoard newBoard = board.peek(d);

                float score = score(newBoard);

                if (depth != maxDepth) {
                    MoveResult result = next(newBoard, depth + 1, maxDepth);
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

    public static float score(GameBoard board) {
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

}