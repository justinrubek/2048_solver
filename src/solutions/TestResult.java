package solutions;

import board.GameBoard;

public class TestResult {
    public boolean won;
    public int move_count;
    public GameBoard final_board;
    public String output;

    public TestResult(GameBoard board) {
        this(board, -1);
    }

    public TestResult(GameBoard board, int move_count) {
        this(board, move_count, "");
    }

    public TestResult(GameBoard board, int move_count, String output) {
        this.won = board.won;
        this.final_board = board;
        this.move_count = move_count;
        this.output = output;
    }
}