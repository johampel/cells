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
package de.hipphampel.cells.ui.navigation;

import static de.hipphampel.cells.ui.common.UiConstants.STYLE_CLASS_ADD32;
import static de.hipphampel.cells.ui.common.UiConstants.STYLE_CLASS_CELLS_LARGE;

import de.hipphampel.cells.resources.Resources;
import de.hipphampel.cells.ui.common.BaseDialog;
import de.hipphampel.cells.ui.common.FXMLUtils;
import de.hipphampel.cells.ui.navigation.NewDialog.Outcome;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.VBox;

public class NewDialog extends BaseDialog<Outcome> {

  public enum Outcome {
    Nothing,
    CellCulture,
    CellSystem
  }

  @FXML
  private RadioButton cellSystemRadioButton;

  public NewDialog() {
    setTitle(Resources.getResource("newDialog.title"));

    Label headerLabel = new Label(Resources.getResource("newDialog.header"));
    headerLabel.getStyleClass().addAll(STYLE_CLASS_CELLS_LARGE, STYLE_CLASS_ADD32);
    setHeader(headerLabel);

    Node content = FXMLUtils.load("NewDialog.fxml", new VBox(), this);
    getDialogPane().setContent(content);

    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    setResultConverter(
        buttonType -> buttonType == ButtonType.OK ? (cellSystemRadioButton.isSelected() ? Outcome.CellSystem : Outcome.CellCulture)
            : Outcome.Nothing);
  }
}
