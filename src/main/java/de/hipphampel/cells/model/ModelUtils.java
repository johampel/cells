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
package de.hipphampel.cells.model;

import de.hipphampel.cells.model.cellsystem.CellSystem;
import de.hipphampel.cells.model.cellsystem.CellType;
import de.hipphampel.cells.model.cellsystem.Condition;
import de.hipphampel.cells.model.cellsystem.Neighbourhood;
import de.hipphampel.cells.model.cellsystem.Ranges;
import de.hipphampel.cells.model.cellsystem.Rule;
import de.hipphampel.cells.resources.Resources;
import javafx.scene.input.DataFormat;
import javafx.scene.paint.Color;

public class ModelUtils {

  public static int color2rgb(Color color) {
    if (color==null) {
      return 0;
    }
    return
        (int) (Math.round(color.getRed() * 255) << 16) +
            (int) (Math.round(color.getGreen() * 255) << 8) +
            (int) Math.round(color.getBlue() * 255);
  }

  public static int swapCellType(int swapCellType1, int swapCellType2, int cellType) {
    if (cellType == swapCellType1) {
      return swapCellType2;
    } else if (cellType == swapCellType2) {
      return swapCellType1;
    } else {
      return cellType;
    }
  }

  public static CellSystem fillAsConwaysGameOfLife(CellSystem cellSystem) {
    for (int i = cellSystem.getCellTypeCount() - 1; i >= 0; i--) {
      cellSystem.removeCellType(i);
    }

    cellSystem.setName(Resources.getResource("model.conway.name"));
    cellSystem.setDescription(Resources.getResource("model.conway.description"));
    cellSystem.setNeighbourhood(new Neighbourhood());

    CellType deadCellType = cellSystem.newCellType();
    CellType livingCellType = cellSystem.newCellType();

    deadCellType.setName(Resources.getResource("model.conway.deadCell"));
    deadCellType.setColor(Color.WHITE);
    deadCellType.setDefaultCellType(0);
    Rule deadCellTypeRule = deadCellType.newRule();
    deadCellTypeRule.setTargetCellType(1);
    Condition dealCellTypeCondition = deadCellTypeRule.newCondition();
    dealCellTypeCondition.setCellType(0);
    dealCellTypeCondition.setRanges(Ranges.parse("3"));

    livingCellType.setName(Resources.getResource("model.conway.livingCell"));
    livingCellType.setColor(Color.BLACK);
    livingCellType.setDefaultCellType(1);
    Rule livingCellTypeRule = livingCellType.newRule();
    livingCellTypeRule.setTargetCellType(0);
    Condition livingCellTypeCondition = livingCellTypeRule.newCondition();
    livingCellTypeCondition.setCellType(0);
    livingCellTypeCondition.setRanges(Ranges.parse("-1,4-"));

    return cellSystem;
  }

}
