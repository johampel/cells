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

import de.hipphampel.cells.model.cellsystem.CellSystem;
import de.hipphampel.cells.model.cellsystem.CellType;
import javafx.beans.NamedArg;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.ComboBox;

public class CellTypeComboBox extends ComboBox<CellType> {

  private CellSystem cellSystem;
  private final IntegerProperty cellTypeId;
  private boolean updating;

  public CellTypeComboBox(
      @NamedArg(value = "showFirstCellTypeAsAnyCellType", defaultValue = "false") boolean showFirstCellTypeAsAnyCellType) {
    this.cellSystem = null;

    this.cellTypeId = new SimpleIntegerProperty(this, "cellTypeId");
    this.cellTypeId.addListener(observable -> {
      if (updating) {
        return;
      }
      updating = true;
      if (cellSystem != null) {
        int cellTypeId = getCellTypeId();
        if (cellTypeId >= 0 && cellTypeId < cellSystem.getCellTypeCount()) {
          valueProperty().set(cellSystem.getCellType(cellTypeId));
        }
      }
      updating = false;
    });
    this.valueProperty().addListener(observable -> {
      if (updating) {
        return;
      }
      updating = true;
      if (getValue() != null) {
        setCellTypeId(getValue().getId());
      }
      updating = false;
    });
    setCellFactory(CellTypeListCell.createFactory(showFirstCellTypeAsAnyCellType, false));
    CellTypeListCell buttonCell = new CellTypeListCell(showFirstCellTypeAsAnyCellType, false);
    setButtonCell(buttonCell);
  }

  public CellSystem getCellSystem() {
    return cellSystem;
  }

  public void setCellSystem(CellSystem cellSystem) {
    if (this.cellSystem == cellSystem) {
      return;
    }

    if (this.cellSystem != null) {
      itemsProperty().unbind();
    }
    this.cellSystem = cellSystem;

    if (this.cellSystem != null) {
      int cellTypeId = getCellTypeId();
      itemsProperty().bind(cellSystem.cellTypesProperty());
      setCellTypeId(-1);
      setCellTypeId(cellTypeId);
    }
  }

  public CellType getCellType() {
    int id = getCellTypeId();
    if (id >= 0 && id < getCellSystem().getCellTypeCount()) {
      return getCellSystem().getCellType(id);
    }
    return null;
  }

  public int getCellTypeId() {
    return cellTypeId.get();
  }

  public IntegerProperty cellTypeIdProperty() {
    return cellTypeId;
  }

  public void setCellTypeId(int cellTypeId) {
    this.cellTypeId.set(cellTypeId);
  }

}
