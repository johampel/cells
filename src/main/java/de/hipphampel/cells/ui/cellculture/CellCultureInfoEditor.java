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
package de.hipphampel.cells.ui.cellculture;

import de.hipphampel.cells.ServiceLocator;
import de.hipphampel.cells.model.ModificationState;
import de.hipphampel.cells.model.cellculture.CellCulture;
import de.hipphampel.cells.model.validation.Severity;
import de.hipphampel.cells.model.validation.ValidationReport;
import de.hipphampel.cells.resources.Resources;
import de.hipphampel.cells.ui.cellsystem.CellSystemComboBox;
import de.hipphampel.cells.ui.common.ConfirmationDialog;
import de.hipphampel.cells.ui.common.FXMLUtils;
import de.hipphampel.cells.ui.common.ValidationMarker;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class CellCultureInfoEditor extends VBox {

  private final ObjectProperty<CellCulture> cellCulture;
  private final InvalidationListener modificationListener;
  private final WeakInvalidationListener weakModificationListener;
  @FXML
  private Button saveButton;
  @FXML
  private Button revertButton;
  @FXML
  private TextField nameTextField;
  @FXML
  private ValidationMarker nameValidationMarker;
  @FXML
  private TextArea descriptionTextArea;
  @FXML
  private ValidationMarker descriptionValidationMarker;
  @FXML
  private Label widthLabel;
  @FXML
  private ValidationMarker dimensionsValidationMarker;
  @FXML
  private Label heightLabel;
  @FXML
  private Label cellTypeCountLabel;
  @FXML
  private CheckBox wrapBordersCheckbox;
  @FXML
  private CellSystemComboBox preferredCellSystemCombobox;
  @FXML
  private ValidationMarker preferredCellSystemValidationMarker;

  public CellCultureInfoEditor() {
    this.cellCulture = new SimpleObjectProperty<>(this, "cellCulture");
    this.cellCulture.addListener(this::onCellCultureChanged);
    this.modificationListener = this::onCellCultureModified;
    this.weakModificationListener = new WeakInvalidationListener(modificationListener);
    FXMLUtils.load("CellCultureInfoEditor.fxml", this);
  }

  public CellCulture getCellCulture() {
    return cellCulture.get();
  }

  public ObjectProperty<CellCulture> cellCultureProperty() {
    return cellCulture;
  }

  public void setCellCulture(CellCulture cellCulture) {
    this.cellCulture.set(cellCulture);
  }

  @FXML
  public void onSaveCellCulture() {
    ServiceLocator.getCellCultureRepository().saveCellCulture(getCellCulture());
    onCellCultureModified(null);
  }

  @FXML
  public void onRevertCellCulture() {
    ConfirmationDialog dialog = new ConfirmationDialog(
        Resources.getResource("cellCultureInfoEditor.revertCellCultureDialog.header"),
        Resources.getResource("cellCultureInfoEditor.revertCellCultureDialog.question"));
    if (dialog.showAndWait().orElse(false)) {
      ServiceLocator.getCellCultureRepository().revertCellCulture(getCellCulture());
      onCellCultureModified(null);
    }
  }


  @FXML
  public void onChangeDimensions() {
    CellCulture cellCulture = getCellCulture();
    if (cellCulture == null) {
      return;
    }

    CellCultureDimensionsDialog dialog = new CellCultureDimensionsDialog(cellCulture.getName(), cellCulture.getDimensions());
    dialog.showAndWait().ifPresent(cellCulture::setDimensions);
  }

  private void onCellCultureChanged(Observable ignore, CellCulture oldCulture, CellCulture newCellCulture) {
    if (oldCulture != null) {
      oldCulture.modificationCounterProperty().removeListener(weakModificationListener);

      nameValidationMarker.validationReportProperty().unbind();
      nameTextField.textProperty().unbindBidirectional(oldCulture.nameProperty());
      descriptionValidationMarker.validationReportProperty().unbind();
      descriptionTextArea.textProperty().unbindBidirectional(oldCulture.descriptionProperty());
      dimensionsValidationMarker.validationReportProperty().unbind();
      widthLabel.textProperty().unbind();
      heightLabel.textProperty().unbind();
      cellTypeCountLabel.textProperty().unbind();
      wrapBordersCheckbox.selectedProperty().unbindBidirectional(oldCulture.wrapAroundProperty());
      preferredCellSystemCombobox.allowedCellTypeCountProperty().unbind();
      preferredCellSystemCombobox.cellSystemIdProperty().bindBidirectional(oldCulture.preferredCellSystemProperty());
      preferredCellSystemValidationMarker.validationReportProperty().unbind();
    }
    if (newCellCulture != null) {
      newCellCulture.modificationCounterProperty().addListener(weakModificationListener);
      nameValidationMarker.validationReportProperty().bind(newCellCulture.nameValidationReportProperty());
      nameTextField.textProperty().bindBidirectional(newCellCulture.nameProperty());
      descriptionValidationMarker.validationReportProperty().bind(newCellCulture.descriptionValidationReportProperty());
      descriptionTextArea.textProperty().bindBidirectional(newCellCulture.descriptionProperty());
      dimensionsValidationMarker.validationReportProperty().bind(newCellCulture.dimensionsValidationReportProperty());
      widthLabel.textProperty().bind(Bindings.format("%d", newCellCulture.widthProperty()));
      heightLabel.textProperty().bind(Bindings.format("%d", newCellCulture.heightProperty()));
      cellTypeCountLabel.textProperty().bind(Bindings.format("%d", newCellCulture.cellTypeCountProperty()));
      wrapBordersCheckbox.selectedProperty().bindBidirectional(newCellCulture.wrapAroundProperty());
      preferredCellSystemCombobox.allowedCellTypeCountProperty().bind(newCellCulture.cellTypeCountProperty());
      preferredCellSystemCombobox.cellSystemIdProperty().bindBidirectional(newCellCulture.preferredCellSystemProperty());
      preferredCellSystemValidationMarker.validationReportProperty().bind(newCellCulture.preferredCellSystemValidationReport());
    }

    onCellCultureModified(ignore);
  }

  private void onCellCultureModified(Observable ignore) {
    CellCulture cellCulture = getCellCulture();
    if (cellCulture == null) {
      return;
    }
    ModificationState state = ServiceLocator.getCellCultureRepository().getCellCultureState(cellCulture);
    ValidationReport report = ServiceLocator.getValidator().validateCellCulture(cellCulture);
    cellCulture.setModificationState(state);
    cellCulture.setValidationReport(report);
    saveButton.setDisable(state == ModificationState.UNCHANGED || report.severity() == Severity.ERROR);
    revertButton.setDisable(state != ModificationState.MODIFIED);
  }

}