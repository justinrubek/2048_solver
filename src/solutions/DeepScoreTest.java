package solutions;

import board.GameBoard;
import board.Direction;

public class DeepScoreTest extends ScoreTest {
    int MAX_DEPTH;

    public DeepScoreTest(long seed) {
        this(seed, 4);
    }

    public DeepScoreTest(long seed, int MAX_DEPTH) {
        super(seed);

        this.MAX_DEPTH = MAX_DEPTH;
    }

    @Override
    public MoveResult decide(GameBoard board) {
        return next(board, MAX_DEPTH);
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

                float score = super.score(newBoard, board);

                if (depth != maxDepth) {
                    MoveResult result = next(newBoard, depth + 1, maxDepth);
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
}