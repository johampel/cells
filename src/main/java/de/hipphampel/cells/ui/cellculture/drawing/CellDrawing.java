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
package de.hipphampel.cells.ui.cellculture.drawing;

import de.hipphampel.array2dops.draw.Byte2DArrayDrawContext;
import de.hipphampel.array2dops.geom.Point;
import de.hipphampel.array2dops.geom.Rectangle;
import de.hipphampel.array2dops.model.Byte2DArray;
import de.hipphampel.cells.model.cellculture.CellCulture;
import de.hipphampel.cells.model.cellsystem.CellType;

public interface CellDrawing {

  void draw(Byte2DArrayDrawContext operations);

  Rectangle getRectangle();

  CellType getCellType();

  default void confirm(Point cellPos, CellCulture cellCulture) {
    update(cellPos);
    Byte2DArray data = cellCulture.getData();
    if (!data.isArrayOwner()) {
      data = data.copy();
    }
    Byte2DArrayDrawContext drawContext = new Byte2DArrayDrawContext(data);
    draw(drawContext);
    cellCulture.setData(Byte2DArray.newInstance(data.getData(), data.getWidth(), data.getHeight()));
  }

  void update(Point cellPos);

  void start(Point cellPos, CellType cellType);

  void cancel();
}
