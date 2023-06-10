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
package de.hipphampel.cells.model.cellculture;

import de.hipphampel.array2dops.model.Byte2DArray;
import de.hipphampel.array2dops.model.Int2DArray;
import de.hipphampel.cells.model.cellsystem.CellSystem;
import de.hipphampel.cells.model.cellsystem.CellType;
import de.hipphampel.cells.model.cellsystem.Condition;
import de.hipphampel.cells.model.cellsystem.Neighbourhood;
import de.hipphampel.cells.model.cellsystem.Rule;
import java.util.Arrays;
import java.util.List;

public class Environment {

  private final CellSystem cellSystem;
  private final CellCulture cellCulture;
  private final Int2DArray[] weights;
  private final int[] weightRadiuses;
  private final long computationFactor;
  private final int[] counters;
  private Byte2DArray data;

  public Environment(CellSystem cellSystem, CellCulture cellCulture) {
    this.cellSystem = cellSystem;
    this.cellCulture = cellCulture;
    this.counters = new int[cellSystem.getCellTypeCount()];
    this.weights = new Int2DArray[cellSystem.getCellTypeCount()];
    this.weightRadiuses = new int[cellSystem.getCellTypeCount()];
    this.data = cellCulture.getData();

    int maxRadius = 1;
    for (int i = 0; i < cellSystem.getCellTypeCount(); i++) {
      CellType cellType = cellSystem.getCellType(i);
      Neighbourhood neighbourhood = cellType.getNeighbourhood() == null ? cellSystem.getNeighbourhood() : cellType.getNeighbourhood();
      weights[i] = neighbourhood.getWeightsArray();
      weightRadiuses[i] = neighbourhood.getRadius();
      maxRadius = Math.max(maxRadius, weightRadiuses[i]);
    }
    this.computationFactor = (2L * maxRadius + 1) * (2L * maxRadius + 1);
  }

  private Environment(Environment source) {
    this.cellSystem = source.cellSystem;
    this.cellCulture = source.cellCulture;
    this.weights = source.weights;
    this.weightRadiuses = source.weightRadiuses;
    this.computationFactor = source.computationFactor;
    this.data = source.data;
    this.counters = new int[cellSystem.getCellTypeCount()];
  }

  public Environment copy() {

    return new Environment(this);
  }

  public long getComputationFactor() {
    return computationFactor;
  }

  public void setData(Byte2DArray data) {
    this.data = data;
  }

  public int computeNextCellType(int x0, int y0) {
    CellType cellType = getCellTypeAt(x0, y0);

    // If the cell type has no rules, there is no need to evaluate deeper
    if (cellType.getRules().isEmpty()) {
      return cellType.getDefaultCellType();
    }

    int cellTypeId = cellType.getId();

    calculateCounters(cellTypeId, x0, y0);
    int newCellTypeId = evaluateRules(cellType.getRules());
    return newCellTypeId == -1 ? cellType.getDefaultCellType() : newCellTypeId;
  }

  private int evaluateRules(List<Rule> rules) {
    for (Rule rule : rules) {
      if (evaluateConditions(rule.getConditions())) {
        return rule.getTargetCellType();
      }
    }
    return -1;
  }

  private boolean evaluateConditions(List<Condition> conditions) {
    for (Condition condition : conditions) {
      int cellTypeId = condition.getCellType();
      int count = counters[cellTypeId];
      if (!condition.getRanges().contains(count)) {
        return false;
      }
    }
    return true;
  }


  private void calculateCounters(int cellTypeId, int x0, int y0) {
    // Initialize
    int w = cellCulture.getWidth();
    int h = cellCulture.getHeight();
    boolean wrapAround = cellCulture.isWrapAround();
    int r = weightRadiuses[cellTypeId];
    Int2DArray ws = weights[cellTypeId];
    Arrays.fill(counters, 0);

    for (int x = 2 * r; x >= 0; x--) {
      for (int y = 2 * r; y >= 0; y--) {
        int weight = ws.getUnsafe(x, y);
        if (weight == 0 || (x == r && y == r)) {
          continue;
        }

        int xc = x0 + x - r;
        int yc = y0 + y - r;

        if (wrapAround) {
          while (xc < 0) {
            xc += w;
          }
          while (xc >= w) {
            xc -= w;
          }
          while (yc < 0) {
            yc += h;
          }
          while (yc >= h) {
            yc -= h;
          }
        } else if (xc < 0 || xc >= data.getWidth() || yc < 0 || yc >= data.getHeight()) {
          continue;
        }

        int type = data.getUnsafe(xc, yc);
        if (type == 0) {
          continue;
        }
        counters[0] += weight;
        counters[type] += weight;
      }
    }
  }

  private CellType getCellTypeAt(int x0, int y0) {
    int cellTypeId = data.getUnsafe(x0, y0);
    return cellSystem.getCellType(cellTypeId);
  }


}
