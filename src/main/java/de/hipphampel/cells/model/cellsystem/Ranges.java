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

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class Ranges {

  private final List<Range> ranges;

  public Ranges(String str) {
    this(parseString(str));
  }

  public Ranges(Collection<Range> ranges) {
    if (ranges == null || ranges.isEmpty()) {
      throw new IllegalArgumentException("At least one Range required");
    }
    var optimizedRanges = new ArrayList<Range>();
    ranges.stream()
        .sorted(Comparator.comparing(Range::lower))
        .forEach(range -> {
          if (!optimizedRanges.isEmpty()) {
            optimizedRanges.get(optimizedRanges.size() - 1).union(range)
                .ifPresentOrElse(
                    merged -> optimizedRanges.set(optimizedRanges.size() - 1, merged),
                    () -> optimizedRanges.add(range)
                );
          } else {
            optimizedRanges.add(range);
          }
        });
    this.ranges = Collections.unmodifiableList(optimizedRanges);
  }

  public static Ranges parse(String str) {
    if (str == null) {
      return null;
    }

    return new Ranges(parseString(str));
  }

  private static List<Range> parseString(String str) {
    if (str == null) {
      return Collections.emptyList();
    }

    return Arrays.stream(str.split(","))
        .map(Range::parse)
        .toList();
  }

  public List<Range> getRanges() {
    return ranges;
  }

  public boolean contains(int value) {
    var size = this.ranges.size();
    if (this.ranges.get(0).lower() > value || this.ranges.get(size - 1).upper() < value) {
      return false;
    }
    if (size == 1) {
      return true;
    }

    for (Range range : this.ranges) {
      if (range.contains(value)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Ranges ranges1 = (Ranges) o;
    return Objects.equals(ranges, ranges1.ranges);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ranges);
  }

  @JsonValue
  public String toString() {
    return this.ranges.stream()
        .map(Range::toString)
        .collect(Collectors.joining(","));
  }
}
