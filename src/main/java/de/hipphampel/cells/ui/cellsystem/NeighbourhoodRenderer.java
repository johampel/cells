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
package de.hipphampel.cells.ui.cellsystem;

import de.hipphampel.cells.model.cellsystem.Neighbourhood;
import java.util.Comparator;
import java.util.function.Function;
import javafx.beans.InvalidationListener;
import javafx.beans.NamedArg;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class NeighbourhoodRenderer extends Canvas {

  private final InvalidationListener listener;
  private final WeakInvalidationListener weakListener;
  private final ObjectProperty<Neighbourhood> neighbourhood;

  public NeighbourhoodRenderer(@NamedArg("width") double width, @NamedArg("height") double height) {
    super(width, height);
    this.listener = new InvalidationListener() {
      @Override
      public void invalidated(Observable observable) {
        drawNeighbourhood(neighbourhood.get());
      }
    };
    this.weakListener = new WeakInvalidationListener(listener);

    this.neighbourhood = new SimpleObjectProperty<>(this, "neighbourhood");
    this.neighbourhood.addListener((ov, oldNeighbourhood, newNeighbourhood) -> onNeighbourhoodChanged(oldNeighbourhood, newNeighbourhood));
  }

  public Neighbourhood getNeighbourhood() {
    return neighbourhood.get();
  }

  public ObjectProperty<Neighbourhood> neighbourhoodProperty() {
    return neighbourhood;
  }

  public void setNeighbourhood(Neighbourhood neighbourhood) {
    this.neighbourhood.set(neighbourhood);
  }

  private void onNeighbourhoodChanged(Neighbourhood oldNeighbourhood, Neighbourhood newNeighbourhood) {
    if (oldNeighbourhood != null) {
      oldNeighbourhood.weightsProperty().removeListener(weakListener);
    }
    if (newNeighbourhood != null) {
      newNeighbourhood.weightsProperty().addListener(weakListener);
      drawNeighbourhood(newNeighbourhood);
    }
  }

  private void drawNeighbourhood(Neighbourhood neighbourhood) {
    GraphicsContext gc = getGraphicsContext2D();

    int maxWeight = getMaxWeight(neighbourhood);
    double cellSize = calculateCellSize(neighbourhood);
    int r = neighbourhood.getRadius();
    for (int wx = -r; wx <= r; wx++) {
      for (int wy = -r; wy <= r; wy++) {
        double x = (wx + r) * cellSize;
        double y = (wy + r) * cellSize;
        Color color = wx == 0 && wy == 0 ? Color.GOLD : weightToColor(maxWeight, neighbourhood.getWeightAt(wx, wy), Color.WHITE, Color.BLACK);
        gc.setFill(color);
        gc.fillRect(x, y, cellSize, cellSize);
      }
    }
  }

  private Color weightToColor(int maxWeight, int weight, Color lowColor, Color highColor) {
    double factor = (double) weight / maxWeight;
    double red = (highColor.getRed() - lowColor.getRed()) * factor + lowColor.getRed();
    double green = (highColor.getGreen() - lowColor.getGreen()) * factor + lowColor.getGreen();
    double blue = (highColor.getBlue() - lowColor.getBlue()) * factor + lowColor.getBlue();
    return Color.color(red, green, blue);
  }

  private int getMaxWeight(Neighbourhood neighbourhood) {
    return neighbourhood.getWeights().stream().max(Comparator.comparing(Function.identity())).orElse(0);
  }

  private double calculateCellSize(Neighbourhood neighbourhood) {
    double size = Math.min(getWidth(), getHeight());
    return size / (2 * neighbourhood.getRadius() + 1);
  }
}
