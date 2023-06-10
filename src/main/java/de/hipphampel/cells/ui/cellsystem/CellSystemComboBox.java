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

import de.hipphampel.cells.ServiceLocator;
import de.hipphampel.cells.model.cellsystem.CellSystem;
import de.hipphampel.cells.model.event.CellSystemEvent;
import de.hipphampel.validation.core.event.EventListener;
import de.hipphampel.validation.core.event.WeakEventListener;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.ComboBox;

public class CellSystemComboBox extends ComboBox<CellSystem> {

  private final IntegerProperty allowedCellTypeCount;
  private final EventListener eventListener = evt -> {
    if (evt.payload() instanceof CellSystemEvent) {
      reloadCellSystems();
    }
  };

  private final StringProperty cellSystemId;
  private boolean updating;

  public CellSystemComboBox() {
    this.allowedCellTypeCount = new SimpleIntegerProperty(this, "allowedCellTypeCount");
    this.allowedCellTypeCount.addListener(ignore -> reloadCellSystems());
    this.cellSystemId = new SimpleStringProperty(this, "cellSystemId");
    this.cellSystemId.addListener(ignore -> onCellSystemIdChanged());
    this.valueProperty().addListener(ignore -> onCellSystemChanged());
    setCellFactory(CellSystemListCell.createFactory());
    setButtonCell(new CellSystemListCell());
    ServiceLocator.getEventPublisher().subscribe(new WeakEventListener(eventListener));
    reloadCellSystems();
  }

  public Integer getAllowedCellTypeCount() {
    return allowedCellTypeCount.get();
  }

  public IntegerProperty allowedCellTypeCountProperty() {
    return allowedCellTypeCount;
  }

  public void setAllowedCellTypeCount(Integer allowedCellTypeCount) {
    this.allowedCellTypeCount.set(allowedCellTypeCount);
  }

  public String getCellSystemId() {
    return cellSystemId.get();
  }

  public StringProperty cellSystemIdProperty() {
    return cellSystemId;
  }

  public void setCellSystemId(String cellSystemId) {
    this.cellSystemId.set(cellSystemId);
  }

  private void reloadCellSystems() {
    Integer cellTypeCount = getAllowedCellTypeCount();
    List<CellSystem> available = ServiceLocator.getCellSystemRepository().streamCellSystems()
        .filter(cellSystem -> cellTypeCount == null || cellSystem.getCellTypeCount() == cellTypeCount)
        .sorted(Comparator.comparing(CellSystem::getName))
        .toList();

    String id = getValue() == null ? null : getValue().getId();
    getItems().setAll(available);
    setValue(available.stream()
        .filter(cs -> Objects.equals(cs.getId(), id))
        .findFirst().orElse(null));
  }

  private void onCellSystemChanged() {
    if (updating) {
      return;
    }
    try {
      updating = true;
      CellSystem cellSystem = getValue();
      setCellSystemId(cellSystem == null ? null : cellSystem.getId());
    } finally {
      updating = false;
    }
  }

  private void onCellSystemIdChanged() {
    if (updating) {
      return;
    }
    try {
      updating = true;

      String id = getCellSystemId();
      CellSystem cellSystem = getItems().stream()
          .filter(cs -> Objects.equals(cs.getId(), id))
          .findFirst().orElse(null);
      setValue(cellSystem);
    } finally {
      updating = false;
    }
  }

}
