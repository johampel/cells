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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;

public class Rule {

  private final CellType cellType;
  private final IntegerProperty targetCellType;
  private final ListProperty<Condition> conditions;
  private final ObjectProperty<ValidationReport> validationReport;

  Rule(CellType cellType) {
    this.cellType = Objects.requireNonNull(cellType);
    this.targetCellType = new SimpleIntegerProperty(this, "targetCellType");
    this.targetCellType.addListener(cellType.getCellSystem().getModificationListener());
    this.conditions = new SimpleListProperty<>(this, "conditions", FXCollections.observableList(new ArrayList<>()));
    this.conditions.addListener(cellType.getCellSystem().getModificationListener());
    this.validationReport = new SimpleObjectProperty<>(this, "validationReport");
  }

  Rule(CellType cellType, int targetCellType, List<Condition> conditions) {
    this(cellType);
    this.targetCellType.set(targetCellType);
    if (conditions != null) {
      conditions.stream()
          .map(source -> source.copyFor(this))
          .forEach(this.conditions::add);
    }
  }

  public Rule copyFor(CellType cellType) {
    return new Rule(cellType, getTargetCellType(), conditions);
  }

  public CellSystem getCellSystem() {
    return cellType.getCellSystem();
  }

  public CellType getCellType() {
    return cellType;
  }

  public int getTargetCellType() {
    return targetCellType.get();
  }

  public IntegerProperty targetCellTypeProperty() {
    return targetCellType;
  }

  public void setTargetCellType(int targetCellType) {
    this.targetCellType.set(targetCellType);
  }

  public List<Condition> getConditions() {
    return Collections.unmodifiableList(conditions.get());
  }

  public ReadOnlyListProperty<Condition> conditionsProperty() {
    return conditions;
  }

  public Condition newCondition() {
    Condition condition = new Condition(this);
    conditions.add(condition);
    return condition;
  }

  public Condition newCondition(int cellTypeId, Ranges ranges) {
    Condition condition = new Condition(this, cellTypeId, ranges);
    conditions.add(condition);
    return condition;
  }

  public void removeCondition(int index) {
    conditions.remove(index);
  }

  public void swapConditions(int index1, int index2) {
    Condition condition1 = conditions.get(index1);
    Condition condition2 = conditions.get(index2);
    conditions.set(index1, condition2);
    conditions.set(index2, condition1);
  }

  public ValidationReport getValidationReport() {
    return validationReport.get();
  }

  public ReadOnlyObjectProperty<ValidationReport> validationReportProperty() {
    return validationReport;
  }

  public void setValidationReport(ValidationReport validationReport) {
    this.validationReport.set(validationReport);
    for (int i = 0; i < getConditions().size(); i++) {
      getConditions().get(i)
          .setValidationReport(validationReport == null ? null : validationReport.subReportForPath("conditions/" + i + "/"));
    }
  }

  boolean removeCellType(int cellType) {
    if (getTargetCellType() == cellType) {
      return false;
    }
    if (getTargetCellType() > cellType) {
      setTargetCellType(getTargetCellType() - 1);
    }
    List<Condition> conditions = getConditions();
    for (int i = conditions.size() - 1; i >= 0; i--) {
      if (!conditions.get(i).removeCellType(cellType)) {
        removeCondition(i);
      }
    }
    return !conditions.isEmpty();
  }

  void insertCellType(int cellType) {
    if (getTargetCellType() >= cellType) {
      setTargetCellType(getTargetCellType() + 1);
    }
    getConditions().forEach(condition -> condition.insertCellType(cellType));
  }

  void swapCellTypes(int cellType1, int cellType2) {
    setTargetCellType(ModelUtils.swapCellType(cellType1, cellType2, getTargetCellType()));
    getConditions().forEach(condition -> condition.swapCellTypes(cellType1, cellType2));
  }

  @Override
  public String toString() {
    return "Rule{targetCellType=" + getTargetCellType() + ", conditions=" + getConditions() + '}';
  }
}
