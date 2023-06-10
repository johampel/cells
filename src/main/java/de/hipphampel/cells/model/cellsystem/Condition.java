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

import de.hipphampel.cells.model.ModelUtils;
import de.hipphampel.cells.model.validation.ValidationReport;
import java.util.Objects;
import java.util.UUID;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

public class Condition {

  private final Rule rule;
  private final IntegerProperty cellType;
  private final ObjectProperty<Ranges> ranges;
  private final ObjectProperty<String> rangesString;
  private final ObjectProperty<ValidationReport> validationReport;

  Condition(Rule rule) {
    this.rule = Objects.requireNonNull(rule);
    this.cellType = new SimpleIntegerProperty(this, "cellType", 0);
    this.cellType.addListener(rule.getCellSystem().getModificationListener());
    this.ranges = new SimpleObjectProperty<>(this, "ranges");
    this.ranges.addListener(rule.getCellSystem().getModificationListener());
    this.rangesString = new SimpleObjectProperty<>(this, "rangesString");
    this.rangesString.addListener(ignore -> onRangesStringChanged());
    this.validationReport = new SimpleObjectProperty<>(this, "validationReport");
  }

  Condition(Rule rule, int cellType, Ranges ranges) {
    this(rule);
    this.cellType.set(cellType);
    this.ranges.set(ranges);
    this.rangesString.setValue(ranges==null?null:ranges.toString());
  }

  void onRangesStringChanged() {
    String str = getRangesString();
    try {
      ranges.setValue(Ranges.parse(str));
    } catch (Exception ex) {
      ranges.setValue(null);
    }
  }

  public Condition copyFor(Rule rule) {
    return new Condition(rule, getCellType(), getRanges());
  }

  public CellSystem getCellSystem() {
    return rule.getCellSystem();
  }

  public Rule getRule() {
    return rule;
  }

  public int getCellType() {
    return cellType.get();
  }

  public IntegerProperty cellTypeProperty() {
    return cellType;
  }

  public void setCellType(int cellType) {
    this.cellType.set(cellType);
  }

  public Ranges getRanges() {
    return ranges.get();
  }

  public ReadOnlyObjectProperty<Ranges> rangesProperty() {
    return ranges;
  }

  public void setRanges(Ranges ranges) {
    setRangesString(ranges == null ? null : ranges.toString());
  }

  public String getRangesString() {
    return rangesString.get();
  }

  public ObjectProperty<String> rangesStringProperty() {
    return rangesString;
  }

  public void setRangesString(String rangesString) {
    this.rangesString.set(rangesString);
  }

  boolean removeCellType(int cellType) {
    int cti = getCellType();
    if (cellType == cti) {
      return false;
    }
    if (cellType < cti) {
      this.setCellType(--cti);
    }
    return true;
  }

  void insertCellType(int cellType) {
    int cti = getCellType();
    if (cti >= cellType) {
      this.setCellType(++cti);
    }
  }

  void swapCellTypes(int cellType1, int cellType2) {
    int cti = getCellType();
    this.setCellType(ModelUtils.swapCellType(cellType1, cellType2, cti));
  }

  public ValidationReport getValidationReport() {
    return validationReport.get();
  }

  public ReadOnlyObjectProperty<ValidationReport> validationReportProperty() {
    return validationReport;
  }

  public void setValidationReport(ValidationReport validationReport) {
    this.validationReport.set(validationReport);
  }

  @Override
  public String toString() {
    return "Condition{" + "cellType=" + getCellType() + ", ranges=" + getRanges() + '}';
  }
}
