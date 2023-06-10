/*
 * The MIT License
 * Copyright © 2023 Johannes Hampel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.hipphampel.cells.model.cellsystem;

import java.util.Optional;

public record Range(int lower, int upper) {

  public Range {
    if (lower > upper || lower < 0) {
      throw new IllegalArgumentException(
          "Illegal range with lower=" + lower + " and upper=" + upper);
    }
  }

  public static Range parse(String str) {
    if (str == null) {
      return null;
    }

    try {
      var pos = str.indexOf("-");
      if (pos == -1) {
        var bound = Integer.parseInt(str.trim());
        return new Range(bound, bound);
      }

      var lower = str.substring(0, pos).trim();
      var upper = str.substring(pos + 1).trim();
      if (lower.isEmpty()) {
        return new Range(0, Integer.parseInt(upper));
      } else if (upper.isEmpty()) {
        return new Range(Integer.parseInt(lower), Integer.MAX_VALUE);
      } else {
        return new Range(Integer.parseInt(lower), Integer.parseInt(upper));
      }
    } catch (NumberFormatException nfe) {
      throw new IllegalArgumentException("Unparseable range '" + str + "'", nfe);
    }
  }

  public boolean contains(int value) {
    return lower <= value && value <= upper;
  }

  public Optional<Range> union(Range other) {
    if (other.lower < this.lower) {
      return other.union(this);
    }

    // Here is this.lower <= other.lower
    if (this.upper >= other.lower - 1) {
      return Optional.of(new Range(this.lower, Math.max(this.upper, other.upper)));
    }
    return Optional.empty();
  }

  @Override
  public String toString() {
    if (lower == upper) {
      return "" + lower;
    }
    if (lower == 0) {
      if (upper == Integer.MAX_VALUE) {
        return "0-";
      }
      return "-" + upper;
    }
    if (upper == Integer.MAX_VALUE) {
      return lower + "-";
    }
    return lower + "-" + upper;
  }
}
