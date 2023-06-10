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

import de.hipphampel.cells.model.cellsystem.Condition;
import de.hipphampel.cells.model.cellsystem.Rule;
import de.hipphampel.cells.ui.common.FXMLUtils;
import de.hipphampel.cells.ui.common.ValidationMarker;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

public class RuleEditor extends VBox {

  private final ObjectProperty<Rule> rule;

  @FXML
  private ValidationMarker validationMarker;
  @FXML
  private CellTypeComboBox targetCellTypeComboBox;
  @FXML
  private ListView<Condition> conditionListView;
  @FXML
  private Button removeConditionButton;

  public RuleEditor() {
    FXMLUtils.load("RuleEditor.fxml", this, this);
    this.rule = new SimpleObjectProperty<>(this, "rule");
    this.ruleProperty().addListener(this::onRuleChanged);
    this.ruleProperty().addListener(ignore -> getRule()); // Enforce Refresh
    this.conditionListView.setCellFactory(view -> new ConditionListCell());
    this.conditionListView.getSelectionModel().selectedIndexProperty().addListener(ignore -> onConditionSelectionChanged());
  }

  public Rule getRule() {
    return rule.get();
  }

  public ObjectProperty<Rule> ruleProperty() {
    return rule;
  }

  public void setRule(Rule rule) {
    this.rule.set(rule);
  }

  @FXML
  public void onAddCondition() {
    if (getRule() != null) {
      getRule().newCondition();
      conditionListView.getSelectionModel().selectIndices(getRule().getConditions().size() - 1);
    }
  }

  @FXML
  public void onRemoveCondition() {
    int index = conditionListView.getSelectionModel().getSelectedIndex();
    if (getRule() != null && index != -1) {
      getRule().removeCondition(index);
    }
  }

  private void onRuleChanged(Observable observable, Rule oldRule, Rule newRule) {
    setDisable(newRule == null);
    if (oldRule != null) {
      validationMarker.validationReportProperty().unbind();
      validationMarker.setValidationReport(null);
      targetCellTypeComboBox.cellTypeIdProperty().unbindBidirectional(oldRule.targetCellTypeProperty());
      targetCellTypeComboBox.setCellSystem(null);
      targetCellTypeComboBox.setCellTypeId(-1);
      conditionListView.itemsProperty().unbind();
      conditionListView.setItems(FXCollections.emptyObservableList());
    }
    if (newRule != null) {
      validationMarker.validationReportProperty().bind(newRule.validationReportProperty());
      targetCellTypeComboBox.setCellSystem(newRule.getCellSystem());
      targetCellTypeComboBox.cellTypeIdProperty().bindBidirectional(newRule.targetCellTypeProperty());
      conditionListView.itemsProperty().bind(newRule.conditionsProperty());
    }
    onConditionSelectionChanged();
  }

  private void onConditionSelectionChanged() {
    int index = conditionListView.getSelectionModel().getSelectedIndex();
    removeConditionButton.setDisable(index == -1);
  }
}
