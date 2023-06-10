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

import de.hipphampel.array2dops.model.Int2DArray;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Neighbourhood {

  public static final int MIN_RADIUS = 1;
  public static final int MAX_RADIUS = 4;

  private int radius;
  private final ObjectProperty<ObservableList<Integer>> weights;

  public Neighbourhood() {
    this.weights = new SimpleObjectProperty<>(this, "weights", FXCollections.observableList(new ArrayList<>()));
    setWeights(Collections.nCopies(9, 1));
  }

  public Neighbourhood(int radius) {
    this.weights = new SimpleObjectProperty<>(this, "weights", FXCollections.observableList(new ArrayList<>()));
    setRadius(radius);
  }

  public Neighbourhood(List<Integer> weights) {
    this.weights = new SimpleObjectProperty<>(this, "weights", FXCollections.observableList(new ArrayList<>()));
    setWeights(weights);
  }

  public Neighbourhood copy() {
    return new Neighbourhood(getWeights());
  }

  public List<Integer> getWeights() {
    return this.weights.get();
  }

  public ObjectProperty<ObservableList<Integer>> weightsProperty() {
    return weights;
  }

  public void setWeights(List<Integer> weights) {
    int newRadius = (int) (Math.sqrt(weights.size()) - 1) / 2;
    if ((newRadius * 2 + 1) * (newRadius * 2 + 1) != weights.size()) {
      throw new IllegalArgumentException("Invalid weights array");
    }
    this.weights.set(FXCollections.observableList(new ArrayList<>(weights)));
    this.weights.get().set(toIndex(newRadius, 0, 0), 0);
    this.radius = newRadius;
  }

  public Int2DArray getWeightsArray() {
    Int2DArray array = Int2DArray.newInstance(2 * radius + 1, 2 * radius + 1);
    for (int x = -radius; x <= radius; x++) {
      for (int y = -radius; y <= radius; y++) {
        array.setUnsafe(x + radius, y + radius, getWeightAt(x, y));
      }
    }
    return array;
  }

  public int getRadius() {
    return radius;
  }

  public void setRadius(int newRadius) {
    if (getRadius() == newRadius) {
      return;
    }
    if (newRadius < 0) {
      throw new IllegalArgumentException("Radius must not be negative");
    }

    List<Integer> oldWeights = getWeights();
    List<Integer> newWeights = new ArrayList<>(Collections.nCopies((2 * newRadius + 1) * (2 * newRadius + 1), 0));
    int oldRadius = getRadius();

    if (oldWeights != null && !oldWeights.isEmpty()) {
      int minRadius = Math.min(oldRadius, newRadius);
      for (int x = -minRadius; x <= minRadius; x++) {
        for (int y = -minRadius; y <= minRadius; y++) {
          if (x == 0 && y == 0) {
            continue;
          }
          newWeights.set(toIndex(newRadius, x, y), oldWeights.get(toIndex(oldRadius, x, y)));
        }
      }
    }

    this.radius = newRadius;
    this.weights.set(FXCollections.observableList(newWeights));
  }

  public void setWeightAt(int x, int y, int value) {
    List<Integer> newWeights = new ArrayList<>(weights.get());
    newWeights.set(toIndex(getRadius(), x, y), value);
    setWeights(newWeights);
  }

  public int getWeightAt(int x, int y) {
    return getWeights().get(toIndex(getRadius(), x, y));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Neighbourhood that = (Neighbourhood) o;
    return getRadius() == that.getRadius() && Objects.equals(getWeights(), that.getWeights());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getRadius(), getWeights());
  }

  @Override
  public String toString() {
    return "Neighbourhood{" + "radius=" + getRadius() + ", weights=" + getWeights() + '}';
  }

  private static int toIndex(int radius, int x, int y) {
    checkRange(radius, x, y);
    return (x + radius) + (radius + y) * (2 * radius + 1);
  }

  private static void checkRange(int radius, int x, int y) {
    if (x < -radius || x > radius || y < -radius || y > radius) {
      throw new IllegalArgumentException("Invalid position (" + x + "/" + y + ") for radius " + radius);
    }
  }
}