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
package de.hipphampel.cells.ui.common;

import de.hipphampel.cells.resources.Resources;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;

import static de.hipphampel.cells.ui.common.UiConstants.STYLE_CLASS_CELLS_LARGE;
import static de.hipphampel.cells.ui.common.UiConstants.STYLE_CLASS_QUESTION32;

public class ConfirmationDialog extends BaseDialog<Boolean> {

  public ConfirmationDialog(String header, String question) {
    setTitle(Resources.getResource("confirmationDialog.title"));

    Label headerLabel = new Label(header);
    headerLabel.getStyleClass().addAll(STYLE_CLASS_CELLS_LARGE, STYLE_CLASS_QUESTION32);
    setHeader(headerLabel);

    Label content = new Label();
    content.setWrapText(true);
    content.setMaxWidth(200);
    content.setText(question);
    content.setPadding(new Insets(20, 20, 30, 20));
    setContent(content);
    getDialogPane().getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
    setResultConverter(buttonType -> buttonType == ButtonType.YES);
  }
}
