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

import de.hipphampel.array2dops.draw.Byte2DArrayDrawContext;
import de.hipphampel.array2dops.model.Byte2DArray;
import de.hipphampel.cells.model.ModelObject;
import de.hipphampel.cells.model.ModificationState;
import de.hipphampel.cells.model.validation.ValidationReport;
import de.hipphampel.cells.model.validation.ValidationReportAware;
import java.util.Objects;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public final class CellCulture implements ModelObject, ValidationReportAware {


  public static final int MIN_SIZE = 1;
  public static final int MAX_SIZE = 2048;

  private String id;
  private final IntegerProperty modificationCounter;
  private final InvalidationListener modificationListener;
  private final StringProperty name;
  private final StringProperty description;
  private final ObjectProperty<CellCultureDimensions> dimensions;
  private final IntegerProperty width;
  private final IntegerProperty height;
  private final IntegerProperty cellTypeCount;
  private final StringProperty preferredCellSystem;
  private final BooleanProperty wrapAround;
  private final ObjectProperty<ModificationState> modificationState;
  private final ObjectProperty<ValidationReport> validationReport;
  private final ObjectProperty<ValidationReport> nameValidationReport;
  private final ObjectProperty<ValidationReport> descriptionValidationReport;
  private final ObjectProperty<ValidationReport> dimensionsValidationReport;
  private final ObjectProperty<ValidationReport> preferredCellSystemValidationReport;

  private final ObjectProperty<Byte2DArray> data;

  public CellCulture() {
    this(null, null, null, 0, 0, 0, false, null, null);
  }

  public CellCulture(
      String id,
      String name,
      String description,
      int width,
      int height,
      int cellTypeCount,
      boolean wrapAround,
      String preferredCellSystem,
      Byte2DArray data) {
    this.id = id;
    this.modificationCounter = new SimpleIntegerProperty(this, "modificationCounter");
    this.modificationListener = ignore -> modificationCounter.set(modificationCounter.get() + 1);

    this.name = new SimpleStringProperty(this, "name", name);
    this.name.addListener(this.modificationListener);
    this.description = new SimpleStringProperty(this, "description", description);
    this.description.addListener(this.modificationListener);
    this.dimensions = new SimpleObjectProperty<>(this, "dimensions", new CellCultureDimensions(width, height, cellTypeCount));
    this.dimensions.addListener(this.modificationListener);
    this.dimensions.addListener(this::onChangeDimensions);
    this.width = new SimpleIntegerProperty(this, "width", width);
    this.height = new SimpleIntegerProperty(this, "height", height);
    this.cellTypeCount = new SimpleIntegerProperty(this, "cellTypeCount", cellTypeCount);
    this.preferredCellSystem = new SimpleStringProperty(this, "preferredCellSystem", preferredCellSystem);
    this.preferredCellSystem.addListener(this.modificationListener);
    this.wrapAround = new SimpleBooleanProperty(this, "wrapAround", wrapAround);
    this.wrapAround.addListener(this.modificationListener);
    this.data = new SimpleObjectProperty<>(this, "data", data == null ? null : data.toReadOnly());
    this.data.addListener(this.modificationListener);
    this.modificationState = new SimpleObjectProperty<>(this, "modificationState");
    this.validationReport = new SimpleObjectProperty<>(this, "validationReport");
    this.nameValidationReport = new SimpleObjectProperty<>(this, "nameValidationReport");
    this.descriptionValidationReport = new SimpleObjectProperty<>(this, "descriptionValidationReport");
    this.dimensionsValidationReport = new SimpleObjectProperty<>(this, "dimensionsValidationReport");
    this.preferredCellSystemValidationReport = new SimpleObjectProperty<>(this, "preferredCellSystemValidationReport");
  }

  public CellCulture copy() {
    CellCulture copy = new CellCulture();
    return copy.copyFrom(this);
  }

  public CellCulture copyFrom(CellCulture source) {
    name.set(source.getName());
    description.set(source.getDescription());
    dimensions.set(source.getDimensions());
    preferredCellSystem.set(source.getPreferredCellSystem());
    wrapAround.set(source.isWrapAround());
    data.set(source.getData().copy());
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

  public StringProperty descriptionProperty() {
    return description;
  }

  public void setDescription(String description) {
    this.description.set(description);
  }

  public CellCultureDimensions getDimensions() {
    return dimensions.get();
  }

  public ObjectProperty<CellCultureDimensions> dimensionsProperty() {
    return dimensions;
  }

  public void setDimensions(CellCultureDimensions dimensions) {
    this.dimensions.set(dimensions);
  }

  public int getWidth() {
    return width.get();
  }

  public ReadOnlyIntegerProperty widthProperty() {
    return width;
  }

  public int getHeight() {
    return height.get();
  }

  public ReadOnlyIntegerProperty heightProperty() {
    return height;
  }

  public int getCellTypeCount() {
    return cellTypeCount.get();
  }

  public ReadOnlyIntegerProperty cellTypeCountProperty() {
    return cellTypeCount;
  }

  public String getPreferredCellSystem() {
    return preferredCellSystem.get();
  }

  public StringProperty preferredCellSystemProperty() {
    return preferredCellSystem;
  }

  public void setPreferredCellSystem(String preferredCellSystem) {
    this.preferredCellSystem.set(preferredCellSystem);
  }

  public boolean isWrapAround() {
    return wrapAround.get();
  }

  public BooleanProperty wrapAroundProperty() {
    return wrapAround;
  }

  public void setWrapAround(boolean wrapAround) {
    this.wrapAround.set(wrapAround);
  }

  @Override
  public ModificationState getModificationState() {
    return modificationState.get();
  }

  public ObjectProperty<ModificationState> modificationStateProperty() {
    return modificationState;
  }

  public void setModificationState(ModificationState modificationState) {
    this.modificationState.set(modificationState);
  }

  public Byte2DArray getData() {
    return data.get();
  }

  public ReadOnlyObjectProperty<Byte2DArray> dataProperty() {
    return data;
  }

  public void setData(Byte2DArray data) {
    if (data != null) {
      if (data.getHeight() != getHeight() || data.getWidth() != getWidth()) {
        throw new IllegalArgumentException("Invalid data size");
      }
    }
    this.data.set(data);
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
    this.dimensionsValidationReport.set(validationReport == null ? null : validationReport.subReportForPath("dimensions"));
    this.preferredCellSystemValidationReport.set(validationReport == null ? null : validationReport.subReportForPath("preferredCellSystem"));
  }

  public ObjectProperty<ValidationReport> nameValidationReportProperty() {
    return nameValidationReport;
  }

  public ObjectProperty<ValidationReport> descriptionValidationReportProperty() {
    return descriptionValidationReport;
  }

  public ObjectProperty<ValidationReport> dimensionsValidationReportProperty() {
    return dimensionsValidationReport;
  }

  public ObjectProperty<ValidationReport> preferredCellSystemValidationReport() {
    return preferredCellSystemValidationReport;
  }

  public ReadOnlyIntegerProperty modificationCounterProperty() {
    return modificationCounter;
  }

  InvalidationListener getModificationListener() {
    return modificationListener;
  }

  private void onChangeDimensions(Observable observable) {
    width.set(getDimensions().width());
    height.set(getDimensions().height());
    cellTypeCount.set(getDimensions().cellTypeCount());
    adjustData(getDimensions());
  }

  private void adjustData(CellCultureDimensions newDimensions) {
    Byte2DArray data = getData();
    if (data==null) {
      return;
    }
    int width = newDimensions.width();
    int height = newDimensions.height();
    Byte2DArray newData = data;
    if (width != data.getWidth() || height != data.getHeight()) {
      newData = Byte2DArray.newInstance(width, height);
      new Byte2DArrayDrawContext(newData).image(0, 0, data);
      data = newData;
    }

    int cellTypeCount = newDimensions.cellTypeCount();
    if (getCellTypeCount() > cellTypeCount) {
      if (data.isReadOnly()) {
        data = data.copy();

        for (int x = 0; x < width; x++) {
          for (int y = 0; y < height; y++) {
            data.setUnsafe(x, y, (byte) (data.getUnsafe(x, y) % cellTypeCount));
          }
        }
      }
    }
    setData(data);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CellCulture that = (CellCulture) o;
    return Objects.equals(getId(), that.getId()) && Objects.equals(getName(), that.getName()) && Objects.equals(getDescription(),
        that.getDescription())
        && Objects.equals(getWidth(), that.getWidth()) && Objects.equals(getHeight(), that.getHeight()) && Objects.equals(
        getCellTypeCount(), that.getCellTypeCount())
        && Objects.equals(getPreferredCellSystem(), that.getPreferredCellSystem()) && Objects.equals(isWrapAround(), that.isWrapAround());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getName(), getDescription(), getWidth(), getHeight(), getCellTypeCount(), getPreferredCellSystem(),
        isWrapAround());
  }
}
