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
import de.hipphampel.cells.resources.Resources;
import de.hipphampel.cells.ui.common.ValidationMarker;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import static de.hipphampel.cells.ui.common.UiConstants.STYLE_CLASS_CELLS_CONTENT_LINE;

public class ConditionEditor extends HBox {

  private final CellTypeComboBox cellTypeComboBox;
  private final TextField rangesText;
  private final ValidationMarker validationMarker;
  private final ObjectProperty<Condition> condition;
  public ConditionEditor() {
    this.cellTypeComboBox = new CellTypeComboBox(true);
    this.rangesText = new TextField();
    this.rangesText.setStyle("-fx-pref-width: 5em;");
    this.validationMarker = new ValidationMarker();
    this.getChildren().addAll(
        cellTypeComboBox,
        new Label(Resources.getResource("conditionEditor.middleText")),
        rangesText,
        new Label(Resources.getResource("conditionEditor.lastText")),
        validationMarker);
    this.getStyleClass().add(STYLE_CLASS_CELLS_CONTENT_LINE);
    this.condition = new SimpleObjectProperty<>(this, "condition");
    this.condition.addListener((ign, oldCondition, newCondition) -> onConditionChanged(oldCondition, newCondition));
  }

  private void onConditionChanged(Condition oldCondition, Condition newCondition) {
    if (oldCondition != null) {
      cellTypeComboBox.cellTypeIdProperty().unbindBidirectional(oldCondition.cellTypeProperty());
      cellTypeComboBox.setCellSystem(null);
      rangesText.textProperty().unbindBidirectional(oldCondition.rangesStringProperty());
      rangesText.setText("");
      validationMarker.validationReportProperty().unbind();
      validationMarker.setValidationReport(null);
    }
    if (newCondition != null) {
      cellTypeComboBox.setCellSystem(newCondition.getCellSystem());
      cellTypeComboBox.cellTypeIdProperty().bindBidirectional(newCondition.cellTypeProperty());
      rangesText.textProperty().bindBidirectional(newCondition.rangesStringProperty());
      validationMarker.validationReportProperty().bind(newCondition.validationReportProperty());
    }
  }

  public Condition getCondition() {
    return condition.get();
  }

  public ObjectProperty<Condition> conditionProperty() {
    return condition;
  }

  public void setCondition(Condition condition) {
    this.condition.set(condition);
  }
}
