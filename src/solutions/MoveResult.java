package solutions;

import board.Direction;

public class MoveResult {
    public Direction direction;
    public float score;

    public MoveResult(Direction d, float score) {
        this.direction = d;
        this.score = score;
    }
  }