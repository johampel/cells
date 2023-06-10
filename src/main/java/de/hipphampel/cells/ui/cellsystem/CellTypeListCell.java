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

import de.hipphampel.cells.model.cellsystem.CellType;
import de.hipphampel.cells.ui.common.DecoratedObjectLabel;
import de.hipphampel.cells.ui.common.ValidationMarkerDecoration;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class CellTypeListCell extends ListCell<CellType> {

  private final Node renderer;
  private final CellTypeLabel cellTypeLabel;

  public static Callback<ListView<CellType>, ListCell<CellType>> createFactory(boolean showFirstCellTypeAsAnyCellType,
      boolean showValidationMarker) {
    return param -> new CellTypeListCell(showFirstCellTypeAsAnyCellType, showValidationMarker);
  }

  public CellTypeListCell(boolean showFirstCellTypeAsAnyCellType, boolean showValidationMarker) {
    this.cellTypeLabel = new CellTypeLabel(showFirstCellTypeAsAnyCellType, false);
    this.renderer = showValidationMarker ? new DecoratedObjectLabel<>(cellTypeLabel, new ValidationMarkerDecoration()) : cellTypeLabel;
  }

  @Override
  protected void updateItem(CellType item, boolean empty) {
    super.updateItem(item, empty);
    if (empty || item == null) {
      setGraphic(null);
    } else {
      setGraphic(initRenderer(item));
    }
  }

  private Node initRenderer(CellType item) {
    cellTypeLabel.setCellType(item);
    return renderer;
  }
}
