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

import de.hipphampel.cells.model.FallbackBinding;
import de.hipphampel.cells.model.cellsystem.CellType;
import de.hipphampel.cells.model.cellsystem.Rule;
import de.hipphampel.cells.resources.Resources;
import de.hipphampel.cells.ui.common.DecoratedObjectLabel;
import de.hipphampel.cells.ui.common.FXMLUtils;
import de.hipphampel.cells.ui.common.ValidationMarker;
import de.hipphampel.cells.ui.common.ValidationMarkerDecoration;
import de.hipphampel.mv4fx.view.View;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class CellTypeView extends View {

  private final ObjectProperty<CellType> cellType;
  @FXML
  private TextField nameTextField;
  @FXML
  private ValidationMarker nameValidationMarker;
  @FXML
  private ColorPicker colorPicker;
  @FXML
  private ValidationMarker colorValidationMarker;
  @FXML
  private HBox neighbourhoodBox;
  @FXML
  private CheckBox ownNeighbourhoodCheckBox;
  @FXML
  private ValidationMarker neighbourhoodValidationMarker;
  @FXML
  private NeighbourhoodRenderer neighbourhoodRenderer;
  @FXML
  private CellTypeComboBox defaultCellTypeComboBox;
  @FXML
  private ValidationMarker rulesValidationMarker;
  @FXML
  private Button removeRuleButton;
  @FXML
  private ListView<Rule> rulesListView;
  @FXML
  private Button ruleTopButton;
  @FXML
  private Button ruleUpButton;
  @FXML
  private Button ruleDownButton;
  @FXML
  private Button ruleBottomButton;
  @FXML
  private RuleEditor ruleEditor;

  public CellTypeView() {
    HBox content = new HBox();
    FXMLUtils.load("CellTypeView.fxml", content, this);

    this.cellType = new SimpleObjectProperty<>(this, "cellType");
    this.cellType.addListener(this::onCellTypeChanged);
    this.setTabCloseActionVisibility(TabActionVisibility.NEVER);

    this.setTabNode(side -> {
      DecoratedObjectLabel<CellType> tabCellTypeLabel = new DecoratedObjectLabel<>(
          new CellTypeLabel(),
          new ValidationMarkerDecoration()
      );
      tabCellTypeLabel.objectProperty().bind(cellType);
      return tabCellTypeLabel;
    });

    this.rulesListView.setCellFactory(RuleListCell.createFactory());
    this.rulesListView.getSelectionModel().selectedIndexProperty().addListener(ignore -> onRuleSelectionChanged());

    this.setContent(content);
  }

  public CellType getCellType() {
    return cellType.get();
  }

  public ObjectProperty<CellType> cellTypeProperty() {
    return cellType;
  }

  public void setCellType(CellType cellType) {
    this.cellType.set(cellType);
  }

  @FXML
  public void onOwnNeighbourhoodChanged() {
    if (getCellType() == null) {
      return;
    }
    neighbourhoodBox.setDisable(!ownNeighbourhoodCheckBox.isSelected());
    if (ownNeighbourhoodCheckBox.isSelected()) {
      if (getCellType().getNeighbourhood() == null) {
        getCellType().setNeighbourhood(getCellType().getCellSystem().getNeighbourhood().copy());
      }
    } else {
      getCellType().setNeighbourhood(null);
    }
  }

  @FXML
  public void onNeighbourhoodEdit() {
    CellTypeLabel titleRenderer = new CellTypeLabel(getCellType(), false, true);
    NeighbourhoodEditDialog dialog = new NeighbourhoodEditDialog(
        Resources.getResource("cellSystemView.neighbourhoodEditDialog.title"),
        titleRenderer,
        getCellType().getNeighbourhood());
    dialog.showAndWait().ifPresent(newNeighbourhood -> getCellType().setNeighbourhood(newNeighbourhood));
  }

  @FXML
  public void onAddRule() {
    if (getCellType() != null) {
      getCellType().newRule();
      rulesListView.getItems(); // Enforce refresh
      rulesListView.getSelectionModel().selectIndices(getCellType().getRules().size() - 1);
    }
  }

  @FXML
  public void onRemoveRule() {
    int index = rulesListView.getSelectionModel().getSelectedIndex();
    if (getCellType() != null && index != -1) {
      getCellType().removeRule(index);
      rulesListView.getSelectionModel().select(-1);
    }
  }

  @FXML
  public void onRuleTop() {
    if (getCellType() != null) {
      int index = rulesListView.getSelectionModel().getSelectedIndex();
      while (index > 0) {
        getCellType().swapRules(index, index - 1);
        index--;
      }
      rulesListView.getSelectionModel().select(index);
    }
  }

  @FXML
  public void onRuleUp() {
    if (getCellType() != null) {
      int index = rulesListView.getSelectionModel().getSelectedIndex();
      if (index > 0) {
        getCellType().swapRules(index, index - 1);
        rulesListView.getSelectionModel().select(index - 1);
      }
    }
  }

  @FXML
  public void onRuleDown() {
    if (getCellType() != null) {
      int index = rulesListView.getSelectionModel().getSelectedIndex();
      if (index + 1 < getCellType().getRules().size()) {
        getCellType().swapRules(index, index + 1);
        rulesListView.getSelectionModel().select(index + 1);
      }
    }
  }

  @FXML
  public void onRuleBottom() {
    if (getCellType() != null) {
      int index = rulesListView.getSelectionModel().getSelectedIndex();
      while (index + 1 < getCellType().getRules().size()) {
        getCellType().swapRules(index, index + 1);
        index++;
      }
      rulesListView.getSelectionModel().select(index);
    }
  }

  private void onRuleSelectionChanged() {
    enableAndDisableRuleButtons();
    Rule rule = rulesListView.getSelectionModel().getSelectedItem();
    ruleEditor.setRule(rule);
  }

  private void enableAndDisableRuleButtons() {
    if (getCellType() != null) {
      int index = rulesListView.getSelectionModel().getSelectedIndex();
      removeRuleButton.setDisable(index == -1);
      ruleTopButton.setDisable(index <= 0);
      ruleUpButton.setDisable(index <= 0);
      ruleDownButton.setDisable(index + 1 >= getCellType().getRules().size());
      ruleBottomButton.setDisable(index + 1 >= getCellType().getRules().size());
    }
  }

  private void onCellTypeChanged(Observable ignore, CellType oldCellType, CellType newCellType) {
    if (oldCellType != null) {
      nameTextField.textProperty().unbindBidirectional(oldCellType.nameProperty());
      nameValidationMarker.validationReportProperty().unbind();
      colorPicker.valueProperty().unbindBidirectional(oldCellType.colorProperty());
      colorValidationMarker.validationReportProperty().unbind();
      neighbourhoodValidationMarker.validationReportProperty().unbind();
      neighbourhoodRenderer.neighbourhoodProperty().unbind();
      defaultCellTypeComboBox.cellTypeIdProperty().unbindBidirectional(oldCellType.defaultCellTypeProperty());
      rulesValidationMarker.validationReportProperty().unbind();
      rulesListView.itemsProperty().unbind();
    }
    if (newCellType != null) {
      nameTextField.textProperty().bindBidirectional(newCellType.nameProperty());
      nameValidationMarker.validationReportProperty().bind(newCellType.nameValidationReportProperty());
      colorPicker.valueProperty().bindBidirectional(newCellType.colorProperty());
      colorValidationMarker.validationReportProperty().bind(newCellType.colorValidationReportProperty());
      neighbourhoodValidationMarker.validationReportProperty().bind(newCellType.neighbourhoodValidationReportProperty());
      neighbourhoodRenderer.neighbourhoodProperty().bind(new FallbackBinding<>(
          newCellType.neighbourhoodProperty(),
          newCellType.getCellSystem().neighbourhoodProperty()
      ));
      defaultCellTypeComboBox.setCellSystem(newCellType.getCellSystem());
      defaultCellTypeComboBox.cellTypeIdProperty().bindBidirectional(newCellType.defaultCellTypeProperty());
      rulesValidationMarker.validationReportProperty().bind(newCellType.rulesValidationReportProperty());
      rulesListView.itemsProperty().bind(newCellType.rulesProperty());
    }
  }

}
