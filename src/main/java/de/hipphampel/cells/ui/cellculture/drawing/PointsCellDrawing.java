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
import java.util.HashSet;
import java.util.Set;

public class PointsCellDrawing implements CellDrawing {

  private CellType cellType;
  private Rectangle rectangle;
  private final Set<Point> points;

  public PointsCellDrawing() {
    points = new HashSet<>();
  }

  @Override
  public CellType getCellType() {
    return cellType;
  }

  @Override
  public void update(Point cellPos) {
    points.add(cellPos);
    if (rectangle == null) {
      this.rectangle = new Rectangle(cellPos.x(), cellPos.y(), 1, 1);
      return;
    }

    int x0 = Math.min(rectangle.getLeft(), cellPos.x());
    int y0 = Math.min(rectangle.getTop(), cellPos.y());
    int x1 = Math.max(rectangle.getRight(), cellPos.x());
    int y1 = Math.max(rectangle.getBottom(), cellPos.y());
    this.rectangle = new Rectangle(x0, y0, x1 - x0 + 1, y1 - y0 + 1);
  }

  @Override
  public void start(Point cellPos, CellType cellType) {
    this.cellType = cellType;
    this.points.clear();
    this.rectangle = null;
    if (cellType != null) {
      update(cellPos);
    }
  }

  @Override
  public void cancel() {
    this.cellType = null;
    this.rectangle = null;
  }

  @Override
  public void draw(Byte2DArrayDrawContext operations) {
    if (!points.isEmpty()) {
      operations.color((byte) cellType.getId());
      points.forEach(operations::set);
    }
  }

  @Override
  public Rectangle getRectangle() {
    return rectangle;
  }
}
