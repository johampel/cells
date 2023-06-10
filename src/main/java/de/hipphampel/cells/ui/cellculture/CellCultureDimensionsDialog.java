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

import static de.hipphampel.cells.ui.common.UiConstants.STYLE_CLASS_CELLS_LARGE;
import static de.hipphampel.cells.ui.common.UiConstants.STYLE_CLASS_CELL_CULTURE32;

import de.hipphampel.cells.ServiceLocator;
import de.hipphampel.cells.model.cellculture.CellCultureDimensions;
import de.hipphampel.cells.model.validation.Severity;
import de.hipphampel.cells.model.validation.ValidationReport;
import de.hipphampel.cells.resources.Resources;
import de.hipphampel.cells.ui.common.BaseDialog;
import de.hipphampel.cells.ui.common.FXMLUtils;
import de.hipphampel.cells.ui.common.IntegerField;
import de.hipphampel.cells.ui.common.ValidationMarker;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class CellCultureDimensionsDialog extends BaseDialog<CellCultureDimensions> {

  private final CellCultureDimensions originalDimensions;

  @FXML
  private IntegerField widthIntegerField;
  @FXML
  private ValidationMarker widthValidationMarker;
  @FXML
  private IntegerField heightIntegerField;
  @FXML
  private ValidationMarker heightValidationMarker;
  @FXML
  private IntegerField cellTypeCountIntegerField;
  @FXML
  private ValidationMarker cellTypeCountValidationMarker;

  public CellCultureDimensionsDialog(String cultureName, CellCultureDimensions dimensions) {
    this.originalDimensions = dimensions;
    Node content = FXMLUtils.load("CellCultureDimensionsDialog.fxml", new GridPane(), this);
    setTitle(Resources.getResource("cellCultureDimensionsDialog.title", cultureName));

    widthIntegerField.intValueProperty().addListener(ignore -> onDimensionsChanged());
    heightIntegerField.intValueProperty().addListener(ignore -> onDimensionsChanged());
    cellTypeCountIntegerField.intValueProperty().addListener(ignore -> onDimensionsChanged());

    widthIntegerField.setIntValue(dimensions.width());
    heightIntegerField.setIntValue(dimensions.height());
    cellTypeCountIntegerField.setIntValue(dimensions.cellTypeCount());

    getDialogPane().setContent(content);

    Label headerLabel = new Label(Resources.getResource("cellCultureDimensionsDialog.header"));
    headerLabel.getStyleClass().addAll(STYLE_CLASS_CELLS_LARGE, STYLE_CLASS_CELL_CULTURE32);
    setHeader(headerLabel);

    getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);
    setResultConverter(button -> button == ButtonType.APPLY ? getCellCultureDimensions() : null);
    validate();
  }

  private void onDimensionsChanged() {
    validate();
  }

  private void validate() {
    CellCultureDimensions dimensions = getCellCultureDimensions();
    ValidationReport report = ServiceLocator.getValidator().validateCellCultureDimensions(dimensions);

    widthValidationMarker.setValidationReport(report.subReportForPath("width"));
    heightValidationMarker.setValidationReport(report.subReportForPath("height"));
    cellTypeCountValidationMarker.setValidationReport(report.subReportForPath("cellTypeCount"));

    Node button = getDialogPane().lookupButton(ButtonType.APPLY);
    boolean unmodified = dimensions.equals(originalDimensions);
    if (button!=null) {
      button.setDisable(report.severity() == Severity.ERROR || unmodified);
    }
  }

  CellCultureDimensions getCellCultureDimensions() {
    return new CellCultureDimensions(
        widthIntegerField.getIntValue(),
        heightIntegerField.getIntValue(),
        cellTypeCountIntegerField.getIntValue());
  }
}
