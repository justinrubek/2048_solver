package board;

import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Set;
import java.util.Queue;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.ArrayDeque;
import java.lang.StringBuilder;

public class Tile {
  public int x;
  public int y;
  public Integer value;

  public Position previous;
  public Tile[] mergedFrom;

  public Tile(int x, int y, Integer value) {
    this.x = x;
    this.y = y;
    this.value = value;
  }

  public Tile(Position p, Integer value) {
    this(p.m, p.n, value);
  }

  public void updatePosition(Position p) {
    this.x = p.m;
    this.y = p.n;
  }

  public void savePosition() {
    previous = new Position(x, y);
  }

  public boolean isAt(Position p) {
    if (x == p.m && y == p.n) {
      return true;
    }
    return false;
  }

  @Override
  public String toString() {
    return value.toString();
  }


  @Override
  public boolean equals(Object o) {
    if (o == null)
      return false;
    if (o == this)
      return true;
    if (getClass() != o.getClass())
      return false;
    Tile other = (Tile) o;
    if (this.value != other.value)
      return false;

    return true;
  }

}

