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
import de.hipphampel.cells.model.ModelUtils;
import de.hipphampel.cells.model.validation.ValidationReport;
import de.hipphampel.cells.model.validation.ValidationReportAware;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.scene.paint.Color;

public final class CellType implements ValidationReportAware {

  private final CellSystem cellSystem;
  private int id;
  private final StringProperty name;
  private final ObjectProperty<Color> color;
  private final StringProperty colorName;
  private final ObjectProperty<Neighbourhood> neighbourhood;
  private final ListProperty<Rule> rules;
  private final IntegerProperty defaultCellType;
  private final ObjectProperty<ValidationReport> validationReport;
  private final ObjectProperty<ValidationReport> nameValidationReport;
  private final ObjectProperty<ValidationReport> colorValidationReport;
  private final ObjectProperty<ValidationReport> neighbourhoodValidationReport;
  private final ObjectProperty<ValidationReport> rulesValidationReport;

  CellType(CellSystem cellSystem) {
    this.cellSystem = Objects.requireNonNull(cellSystem);
    this.id = 0;
    this.name = new SimpleStringProperty(this, "name");
    this.name.addListener(cellSystem.getModificationListener());
    this.color = new SimpleObjectProperty<>(this, "color");
    this.colorName = new SimpleStringProperty(this, "colorName");
    this.color.addListener(ignore -> {
      colorName.set(getColor() == null ? null : String.format("#%06x", ModelUtils.color2rgb(getColor())));
    });
    this.color.addListener(cellSystem.getModificationListener());
    this.neighbourhood = new SimpleObjectProperty<>(this, "neighbourhood");
    this.neighbourhood.addListener(cellSystem.getModificationListener());
    this.defaultCellType = new SimpleIntegerProperty(this, "defaultCellType");
    this.defaultCellType.addListener(cellSystem.getModificationListener());
    this.rules = new SimpleListProperty<>(this, "rules", FXCollections.observableList(new ArrayList<>()));
    this.rules.addListener(cellSystem.getModificationListener());
    this.validationReport = new SimpleObjectProperty<>(this, "validationReport");
    this.nameValidationReport = new SimpleObjectProperty<>(this, "nameValidationReport");
    this.colorValidationReport = new SimpleObjectProperty<>(this, "colorValidationReport");
    this.rulesValidationReport = new SimpleObjectProperty<>(this, "rulesValidationReport");
    this.neighbourhoodValidationReport = new SimpleObjectProperty<>(this, "neighbourhoodValidationReport");
  }

  CellType(CellSystem cellSystem, int id, String name, Color color, Neighbourhood neighbourhood, List<Rule> rules, int defaultCellType) {
    this(cellSystem);
    this.id = id;
    setName(name);
    setColor(color);
    setNeighbourhood(neighbourhood);
    if (rules != null) {
      rules.stream()
          .map(rule -> rule.copyFor(this))
          .forEach(this.rules::add);
    }

    setDefaultCellType(defaultCellType);
  }

  public CellType copyFor(CellSystem cellSystem) {
    return new CellType(
        cellSystem,
        id,
        getName(),
        getColor(),
        getNeighbourhood() == null ? null : getNeighbourhood().copy(),
        getRules(),
        getDefaultCellType());
  }

  public CellSystem getCellSystem() {
    return cellSystem;
  }

  public int getId() {
    return id;
  }

  void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name.get();
  }

  public StringProperty nameProperty() {
    return name;
  }

  public ReadOnlyObjectProperty<ValidationReport> nameValidationReportProperty() {
    return nameValidationReport;
  }

  public void setName(String name) {
    this.name.set(name);
  }

  @JsonIgnore
  public Color getColor() {
    return color.get();
  }

  public ObjectProperty<Color> colorProperty() {
    return color;
  }

  public ReadOnlyObjectProperty<ValidationReport> colorValidationReportProperty() {
    return colorValidationReport;
  }

  public void setColor(Color color) {
    this.color.set(color);
  }

  public String getColorName() {
    return colorName.get();
  }

  public ReadOnlyStringProperty colorNameProperty() {
    return colorName;
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

  public List<Rule> getRules() {
    return Collections.unmodifiableList(rules.get());
  }

  public ReadOnlyListProperty<Rule> rulesProperty() {
    return rules;
  }

  public Rule newRule() {
    Rule rule = new Rule(this);
    rules.add(rule);
    return rule;
  }

  public void removeRule(int index) {
    rules.remove(index);
  }

  public void swapRules(int index1, int index2) {
    Rule rule1 = rules.get(index1);
    Rule rule2 = rules.get(index2);
    rules.set(index1, rule2);
    rules.set(index2, rule1);
  }

  public ReadOnlyObjectProperty<ValidationReport> rulesValidationReportProperty() {
    return rulesValidationReport;
  }

  public int getDefaultCellType() {
    return defaultCellType.get();
  }

  public IntegerProperty defaultCellTypeProperty() {
    return defaultCellType;
  }

  public void setDefaultCellType(int defaultCellType) {
    this.defaultCellType.set(defaultCellType);
  }

  @Override
  public ValidationReport getValidationReport() {
    return validationReport.get();
  }

  @Override
  public ReadOnlyObjectProperty<ValidationReport> validationReportProperty() {
    return validationReport;
  }

  @Override
  public void setValidationReport(ValidationReport validationReport) {
    this.validationReport.set(validationReport);
    this.nameValidationReport.set(validationReport == null ? null : validationReport.subReportForPath("name"));
    this.colorValidationReport.set(validationReport == null ? null : validationReport.subReportForPath("color"));
    this.neighbourhoodValidationReport.set(validationReport == null ? null : validationReport.subReportForPath("neighbourhood"));
    this.rulesValidationReport.set(validationReport == null ? null : validationReport.subReportForPath("rules"));
    for (int i = 0; i < getRules().size(); i++) {
      getRules().get(i).setValidationReport(validationReport == null ? null : validationReport.subReportForPath("rules/" + i + "/"));
    }
  }

  boolean removeCellType(int cellType) {
    int id = getId();
    if (cellType == id) {
      return false;
    }
    if (cellType < id) {
      setId(id - 1);
    }
    if (cellType <= getDefaultCellType()) {
      setDefaultCellType(getDefaultCellType() - 1);
    }
    List<Rule> rules = getRules();
    for (int i = rules.size() - 1; i >= 0; i--) {
      if (!rules.get(i).removeCellType(cellType)) {
        removeRule(i);
      }
    }
    return true;
  }

  void insertCellType(int cellType) {
    if (getId() >= cellType) {
      setId(getId() + 1);
    }
    if (cellType <= getDefaultCellType()) {
      setDefaultCellType(getDefaultCellType() + 1);
    }
    getRules().forEach(rule -> rule.insertCellType(cellType));
  }

  void swapCellTypes(int cellType1, int cellType2) {
    setId(ModelUtils.swapCellType(cellType1, cellType2, getId()));
    setDefaultCellType(ModelUtils.swapCellType(cellType1, cellType2, getDefaultCellType()));
    getRules().forEach(rule -> rule.swapCellTypes(cellType1, cellType2));
  }

  @Override
  public String toString() {
    return "CellType{" + "id=" + getId() + ", name='" + getName() + '\'' + ", color=" + getColorName() + ", neighbourhood="
        + getNeighbourhood() + ", rules="
        + getRules() + ", defaultCellType=" + getDefaultCellType() + '}';
  }
}
