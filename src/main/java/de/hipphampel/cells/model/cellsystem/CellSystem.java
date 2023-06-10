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
package de.hipphampel.cells.model.cellsystem;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.hipphampel.cells.model.ModelObject;
import de.hipphampel.cells.model.ModificationState;
import de.hipphampel.cells.model.validation.ValidationReport;
import de.hipphampel.cells.model.validation.ValidationReportAware;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javafx.beans.InvalidationListener;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;

public final class CellSystem implements ModelObject, ValidationReportAware {

  public static final int MIN_CELLTYPE_COUNT = 2;
  public static final int MAX_CELLTYPE_COUNT = 255;
  private String id;

  private final IntegerProperty modificationCounter;
  private final InvalidationListener modificationListener;

  private final StringProperty name;
  private final StringProperty description;
  private final ListProperty<CellType> cellTypes;
  private final ObjectProperty<Neighbourhood> neighbourhood;
  private final ObjectProperty<ModificationState> modificationState;
  private final ObjectProperty<ValidationReport> validationReport;
  private final ObjectProperty<ValidationReport> nameValidationReport;
  private final ObjectProperty<ValidationReport> descriptionValidationReport;
  private final ObjectProperty<ValidationReport> neighbourhoodValidationReport;
  private final ObjectProperty<ValidationReport> cellTypesValidationReport;

  public CellSystem() {
    this.id = null;
    this.modificationCounter = new SimpleIntegerProperty(this, "modificationCounter");
    this.modificationListener = ignore -> modificationCounter.set(modificationCounter.get() + 1);

    this.name = new SimpleStringProperty(this, "name");
    this.name.addListener(this.modificationListener);
    this.description = new SimpleStringProperty(this, "description");
    this.description.addListener(this.modificationListener);
    this.cellTypes = new SimpleListProperty<>(this, "cellTypes", FXCollections.observableList(new ArrayList<>()));
    this.cellTypes.addListener(this.modificationListener);
    this.neighbourhood = new SimpleObjectProperty<>(this, "neighbourhood");
    this.neighbourhood.addListener(this.modificationListener);
    this.modificationState = new SimpleObjectProperty<>(this, "modificationState", ModificationState.UNCHANGED);
    this.validationReport = new SimpleObjectProperty<>(this, "validationReport");
    this.nameValidationReport = new SimpleObjectProperty<>(this, "nameValidationReport");
    this.descriptionValidationReport = new SimpleObjectProperty<>(this, "descriptionValidationReport");
    this.neighbourhoodValidationReport = new SimpleObjectProperty<>(this, "neighbourhoodValidationReport");
    this.cellTypesValidationReport = new SimpleObjectProperty<>(this, "cellTypesValidationReport");
  }

  public CellSystem copy() {
    CellSystem copy = new CellSystem();
    return copy.copyFrom(this);
  }

  public CellSystem copyFrom(CellSystem source) {
    name.set(source.getName());
    description.set(source.getDescription());
    cellTypes.clear();
    for (CellType cellType : source.cellTypes) {
      CellType cellTypeCopy = cellType.copyFor(this);
      cellTypes.add(cellTypeCopy);
    }
    neighbourhood.set(source.getNeighbourhood() == null ? null : source.getNeighbourhood().copy());
    return this;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public String getName() {
    return name.get();
  }

  public StringProperty nameProperty() {
    return name;
  }

  public void setName(String name) {
    this.name.set(name);
  }

  public String getDescription() {
    return description.get();
  }

  public ReadOnlyObjectProperty<ValidationReport> nameValidationReportProperty() {
    return nameValidationReport;
  }

  public StringProperty descriptionProperty() {
    return description;
  }

  public void setDescription(String description) {
    this.description.set(description);
  }

  public ReadOnlyObjectProperty<ValidationReport> descriptionValidationReportProperty() {
    return descriptionValidationReport;
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

  public ReadOnlyObjectProperty<ValidationReport> neighbourhoodValidationReportProperty() {
    return neighbourhoodValidationReport;
  }

  public List<CellType> getCellTypes() {
    return Collections.unmodifiableList(cellTypes);
  }

  public ReadOnlyListProperty<CellType> cellTypesProperty() {
    return cellTypes;
  }

  public ReadOnlyObjectProperty<ValidationReport> cellTypesValidationReportProperty() {
    return cellTypesValidationReport;
  }

  @JsonIgnore
  public int getCellTypeCount() {
    return cellTypes.size();
  }

  public CellType getCellType(int index) {
    return index >= 0 && index < cellTypes.size() ? cellTypes.get(index) : null;
  }

  public CellType newCellType() {
    return newCellType(cellTypes.size());
  }

  public CellType newCellType(int index) {
    CellType newCellType = new CellType(this);
    newCellType.setId(index);
    getCellTypes().forEach(cellType -> cellType.insertCellType(index));
    this.cellTypes.add(index, newCellType);
    return newCellType;
  }

  public void swapCellTypes(int index1, int index2) {
    CellType ct1 = cellTypes.get(index1);
    CellType ct2 = cellTypes.get(index2);
    this.cellTypes.forEach(cellType -> cellType.swapCellTypes(index1, index2));
    this.cellTypes.set(index1, ct2);
    this.cellTypes.set(index2, ct1);
  }

  public void removeCellType(int index) {
    List<CellType> cellTypes = this.cellTypes;
    for (int i = cellTypes.size() - 1; i >= 0; i--) {
      if (!cellTypes.get(i).removeCellType(index)) {
        cellTypes.remove(i);
      }
    }
  }

  @Override
  public ModificationState getModificationState() {
    return modificationState.get();
  }

  public ReadOnlyObjectProperty<ModificationState> modificationStateProperty() {
    return modificationState;
  }

  public void setModificationState(ModificationState modificationState) {
    this.modificationState.set(modificationState);
  }

  @Override
  public ValidationReport getValidationReport() {
    return validationReport.get();
  }

  @Override
  public ReadOnlyObjectProperty<ValidationReport> validationReportProperty() {
    return validationReport;
  }

  public void setValidationReport(ValidationReport validationReport) {
    this.validationReport.set(validationReport);
    this.nameValidationReport.set(validationReport == null ? null : validationReport.subReportForPath("name"));
    this.descriptionValidationReport.set(validationReport == null ? null : validationReport.subReportForPath("description"));
    this.neighbourhoodValidationReport.set(validationReport == null ? null : validationReport.subReportForPath("neighbourhood"));
    this.cellTypesValidationReport.set(validationReport == null ? null : validationReport.subReportForPath("cellTypes"));
    for (int i = 0; i < getCellTypeCount(); i++) {
      getCellType(i).setValidationReport(validationReport == null ? null : validationReport.subReportForPath("cellTypes/" + i + "/"));
    }
  }

  public ReadOnlyIntegerProperty modificationCounterProperty() {
    return modificationCounter;
  }

  InvalidationListener getModificationListener() {
    return modificationListener;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    CellSystem that = (CellSystem) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "CellSystem{" + "id='" + getId() + '\'' + ", name='" + getName() + '\'' + ", description='" + getDescription() + '\''
        + ", cellTypes=" + cellTypes.get() + ", neighbourhood=" + getNeighbourhood() + '}';
  }
}
