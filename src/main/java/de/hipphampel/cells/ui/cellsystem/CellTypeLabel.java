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

import static de.hipphampel.cells.ui.common.UiConstants.ICON_LARGE_SIZE;
import static de.hipphampel.cells.ui.common.UiConstants.ICON_NORMAL_SIZE;
import static de.hipphampel.cells.ui.common.UiConstants.STYLE_CLASS_CELLS_LARGE;

import de.hipphampel.cells.model.cellsystem.CellType;
import de.hipphampel.cells.resources.Resources;
import de.hipphampel.cells.ui.common.ObjectLabel;
import javafx.beans.NamedArg;
import javafx.beans.property.ObjectProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

public class CellTypeLabel extends ObjectLabel<CellType> {

  private final boolean large;
  private final boolean showFirstCellTypeAsAnyCellType;

  public CellTypeLabel() {
    this(null, false, false);
  }

  public CellTypeLabel(
      @NamedArg(value = "showFirstCellTypeAsAnyCellType", defaultValue = "false") boolean showFirstCellTypeAsAnyCellType,
      @NamedArg(value = "large", defaultValue = "false") boolean large) {
    this(null, showFirstCellTypeAsAnyCellType, large);
  }

  public CellTypeLabel(CellType cellType, boolean showFirstCellTypeAsAnyCellType, boolean large) {
    this.showFirstCellTypeAsAnyCellType = showFirstCellTypeAsAnyCellType;
    this.large = large;
    if (large) {
      this.getStyleClass().add(STYLE_CLASS_CELLS_LARGE);
    }
    setCellType(cellType);
  }


  @Override
  protected void onObjectChanged(CellType oldCellType, CellType newCellType) {
    if (oldCellType != null) {
      textProperty().unbind();
      if (getGraphic() != null) {
        ((Rectangle) getGraphic()).fillProperty().unbind();
      }
      setGraphic(null);
    }

    if (newCellType != null) {
      if (newCellType.getId() != 0 || !showFirstCellTypeAsAnyCellType) {
        textProperty().bind(newCellType.nameProperty());
        Rectangle graphic = createGraphic();
        graphic.fillProperty().bind(newCellType.colorProperty());
        setGraphic(graphic);
      } else {
        textProperty().set(Resources.getResource("cellTypeLabel.anyLivingCellType"));
      }
    }
  }

  private Rectangle createGraphic() {
    Rectangle graphic = new Rectangle(2 * (large ? ICON_LARGE_SIZE : ICON_NORMAL_SIZE), large ? ICON_LARGE_SIZE : ICON_NORMAL_SIZE);
    graphic.setStrokeType(StrokeType.OUTSIDE);
    graphic.setStrokeWidth(1);
    graphic.setStroke(Color.BLACK);
    return graphic;
  }

  public CellType getCellType() {
    return getObject();
  }

  public ObjectProperty<CellType> cellTypeProperty() {
    return objectProperty();
  }

  public void setCellType(CellType cellType) {
    setObject(cellType);
  }
}
