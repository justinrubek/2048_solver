package solutions;

import java.util.concurrent.*;

import board.GameBoard;

public interface Solver extends Callable<TestResult> {
    MoveResult decide(GameBoard board);
    TestResult run();
    default TestResult call() {
            return run();
    }
}