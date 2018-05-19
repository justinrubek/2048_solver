package board;
import java.lang.StringBuilder;

public class Vector {
  public int x;
  public int y;

  public Vector(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append("vector:x:").append(x).append("y:").append(y);
    return b.toString();
  }
}

