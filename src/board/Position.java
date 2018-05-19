package board;
import java.lang.StringBuilder;

public class Position {
  public int m;
  public int n;

  public Position(int m, int n) {
    this.m = m;
    this.n = n;
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();

    builder.append("position:").append("m:").append(m).append(":n:").append(n);

    return builder.toString();
  }

}

