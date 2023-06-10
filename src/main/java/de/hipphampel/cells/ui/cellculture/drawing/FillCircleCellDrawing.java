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
import de.hipphampel.cells.model.cellsystem.CellType;

public class FillCircleCellDrawing implements CellDrawing {

  private CellType cellType;
  private Point center;
  private int radius;

  public FillCircleCellDrawing() {
  }

  @Override
  public CellType getCellType() {
    return cellType;
  }


  @Override
  public void update(Point cellPos) {
    int dx = Math.abs(cellPos.x() - center.x());
    int dy = Math.abs(cellPos.y() - center.y());
    this.radius = (int) Math.sqrt(dx * dx + dy * dy);
  }

  @Override
  public void start(Point cellPos, CellType cellType) {
    this.cellType = cellType;
    if (cellType != null) {
      this.center = cellPos;
      this.radius = 0;
    }
  }

  @Override
  public void cancel() {
    this.center = null;
    this.radius = 0;
    this.cellType = null;
  }

  @Override
  public void draw(Byte2DArrayDrawContext operations) {
    Rectangle rect = getRectangle();
    if (rect != null) {
      operations
          .color((byte) cellType.getId())
          .fillCircle(center, radius);
    }
  }

  @Override
  public Rectangle getRectangle() {
    if (this.center == null) {
      return null;
    }
    return new Rectangle(
        center.x() - radius,
        center.y() - radius,
        radius * radius + 2,
        radius * radius + 2);
  }
}
