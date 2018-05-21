package solutions;

import java.lang.StringBuilder;

import board.GameBoard;
import board.Tile;
import board.Direction;

/* 
    Start at a corner, and do similar to SnakeTest, but only down and to right
    Score is assigned by adding a tile if it is to the right or below a tile that is
    greater than or equal to it (Down and to the right is for the top left corner)

    The corner space itself gets multiplied by some value to promote higher squares in the corner 
*/
public class SmoothnessTest implements Solver {
    public float CORNER_MULTIPLIER = 2.5f;
    public float SMOOTH_MULTIPLIER = 1.5f;
    public int MAX_SEARCH_DEPTH = 4;
    // 8 means evaluate the whole board
    public int MAX_SMOOTHNESS_DEPTH = 7;
    public int MOVE_LIMIT = 2000;
    public int WIN_POWER = 11;

    public float CROWDED_PENALTY_MULT = 0.4f;
    // TODO: make a percentage instead of a flat value?
    // Although maybe it works better as a value, for increasing board sizes?
    public int CROWDED_CAPACITY = 8;

    long seed;

    StringBuilder output;
    GameBoard board;

    public SmoothnessTest() {
        this(System.currentTimeMillis());
    }

    public SmoothnessTest(long seed) {
        this.seed = seed;

        board = new GameBoard();
        board.setSeed(seed);

        output = new StringBuilder();
    }

    @Override
    public MoveResult decide(GameBoard board) {
        return next(board, MAX_SEARCH_DEPTH);
    }

    @Override
    public TestResult run() {
        println("SmoothnessTest");
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

        return new TestResult(board).move_count(i).output(output.toString()).name("SmoothnessTest")
                .time_taken(end - start).seed(seed);
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

    public MoveResult next(GameBoard board, int depth) {
        return next(board, 0, depth);
    }

    public MoveResult next(GameBoard board, int depth, int maxDepth) {
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

    public float score(GameBoard board, Tile tile, int depth) {
        if (depth >= MAX_SMOOTHNESS_DEPTH) {
            return 0.0f;
        }
        float score = 0.0f;
        if (tile != null) {
            Tile down = board.get(tile.x + 1, tile.y);
            Tile right = board.get(tile.x, tile.y + 1);

            if (down != null) {
                float down_score = score(board, down, depth + 1);
                if (down.value.equals(tile.value / 2)) {
                    score += down_score * SMOOTH_MULTIPLIER;
                } else if (down.value.compareTo(tile.value) < 0) {
                    score += down_score;
                }
            }
            if (right != null) {
                float right_score = score(board, right, depth + 1);
                if (right.value.equals(tile.value / 2)) {
                    score += right_score * SMOOTH_MULTIPLIER;
                } else if (right.value.compareTo(tile.value) < 0) {
                    score += right_score;
                }
            }

            score += tile.value;
        }

        int emptySpaces = board.emptySpaces();
        if (emptySpaces < CROWDED_CAPACITY) {
            if (emptySpaces < CROWDED_CAPACITY / 2) {
                score *= CROWDED_PENALTY_MULT;
            }
            score *= CROWDED_PENALTY_MULT;
        }
        return score;
    }

    public float score(GameBoard board) {
        // Just checking one corner for now
        if (board.merges.containsKey((int) Math.pow(2, WIN_POWER))) {
            return Float.MAX_VALUE;
        }
        int x = 0;
        int y = 0;
        Tile start = board.get(x, y);
        float score = score(board, start, 0);
        if (start != null) {
            score += start.value * CORNER_MULTIPLIER;
        }

        return score;
    }
}
