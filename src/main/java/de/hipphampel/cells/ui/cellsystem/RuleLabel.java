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

import de.hipphampel.cells.model.cellsystem.Rule;
import de.hipphampel.cells.resources.Resources;
import de.hipphampel.cells.ui.common.UiConstants;
import de.hipphampel.cells.ui.common.ValidationMarker;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class RuleLabel extends HBox {

  private final ObjectProperty<Rule> rule = new SimpleObjectProperty<>(this, "rule");
  private final CellTypeLabel cellTypeLabel = new CellTypeLabel();
  private final Label conditionLabel = new Label();
  private final ValidationMarker validationReportMarker = new ValidationMarker();
  private final InvalidationListener changeListener = ignore -> onRuleModified();
  private final WeakInvalidationListener weakChangeListener = new WeakInvalidationListener(changeListener);

  public RuleLabel() {
    rule.addListener((ign, oldRule, newRule) -> onRuleChanged(oldRule, newRule));

    this.getStyleClass().add(UiConstants.STYLE_CLASS_CELLS_CONTENT_LINE);
    this.getChildren().addAll(
        new Label(Resources.getResource("ruleLabel.startLabel")),
        cellTypeLabel,
        conditionLabel,
        validationReportMarker);
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

  private void onRuleChanged(Rule oldRule, Rule newRule) {
    if (oldRule != null) {
      oldRule.targetCellTypeProperty().removeListener(weakChangeListener);
      oldRule.conditionsProperty().removeListener(weakChangeListener);
      validationReportMarker.validationReportProperty().unbind();
    }
    if (newRule != null) {
      newRule.targetCellTypeProperty().addListener(weakChangeListener);
      newRule.conditionsProperty().addListener(weakChangeListener);
      validationReportMarker.validationReportProperty().bind(newRule.validationReportProperty());
    }
    onRuleModified();
  }


  private void onRuleModified() {
    Rule rule = getRule();
    if (rule != null) {
      cellTypeLabel.setCellType(rule.getCellSystem().getCellType(rule.getTargetCellType()));
      conditionLabel.setText(Resources.getResource("ruleLabel.conditionLabel", rule.getConditions().size()));
    } else {
      cellTypeLabel.setCellType(null);
      conditionLabel.setText("");
    }
  }
}
