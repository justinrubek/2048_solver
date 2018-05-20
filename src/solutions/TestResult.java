package solutions;

import board.GameBoard;

public class TestResult {
    public boolean won;
    public int move_count;
    public GameBoard final_board;
    public String output;
    public String name;
    public long time_taken;

    public TestResult(GameBoard board) {
        this(board, -1);
    }

    public TestResult(GameBoard board, int move_count) {
        this(board, move_count, "");
    }

    public TestResult(GameBoard board, int move_count, String output) {
        this(board, move_count, output, "GenericTest");
    }

    public TestResult(GameBoard board, int move_count, String output, String name) {
        this.won = board.won;
        this.final_board = board;
        this.move_count = move_count;
        this.output = output;
    }

    public TestResult move_count(int count) {
        this.move_count = count;
        return this;
    }

    public TestResult time_taken(long time) {
        this.time_taken = time;
        return this;
    }

    public TestResult name(String name) {
        this.name = name;
        return this;
    }

    public TestResult output(String output) {
        this.output = output;
        return this;
    }

    public TestResult final_board(GameBoard board) {
        this.final_board = board;
        this.won = board.won;
        return this;
    }


    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("test:").append(name).append("\n");
        builder.append("won:").append(won).append("\n");
        builder.append("totalmoves:").append(move_count).append("\n");
        builder.append("time:").append(time_taken).append("\n");
        builder.append("ns/move:").append(time_taken / move_count).append("\n");
        builder.append("finalboard:").append("\n").append(final_board).append("\n");

        return builder.toString();
    }
}