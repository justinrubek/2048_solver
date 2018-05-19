package board;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

// A list of positions to traverse in order
public class Traversals {
  public List<Integer> x;
  public List<Integer> y;

  public Traversals(Direction direction, int size) {
    x = new ArrayList<>();
    y = new ArrayList<>();

    for (int i = 0; i < size; i++) {
      x.add(i);
      y.add(i);
    }

    // In order to always traverse from farthest cell in direction
    if (direction == Direction.Right) {
      Collections.reverse(x);
    } else if (direction == Direction.Up) {
      Collections.reverse(y);
    }
  }
}
