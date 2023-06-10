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

import static de.hipphampel.cells.ui.common.UiConstants.STYLE_CLASS_ERROR16;
import static de.hipphampel.cells.ui.common.UiConstants.STYLE_CLASS_INFO16;
import static de.hipphampel.cells.ui.common.UiConstants.STYLE_CLASS_WARNING16;

import de.hipphampel.cells.model.validation.ValidationMessage;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;

public class ValidationMessageRenderer extends Label {


  private final ObjectProperty<ValidationMessage> validationMessage;

  public ValidationMessageRenderer(ValidationMessage message) {
    this();
    setValidationMessage(message);
  }

  public ValidationMessageRenderer() {
    validationMessage = new SimpleObjectProperty<>(this, "validationMessage");
    validationMessage.addListener(ignore -> onValidationMessageChanged());
  }

  private void onValidationMessageChanged() {
    ValidationMessage message = getValidationMessage();
    getStyleClass().removeAll(STYLE_CLASS_INFO16, STYLE_CLASS_WARNING16, STYLE_CLASS_ERROR16);
    setText(null);

    if (message != null) {
      setText(message.message());
      getStyleClass().add(switch (message.severity()) {
        case ERROR -> STYLE_CLASS_ERROR16;
        case WARNING -> STYLE_CLASS_WARNING16;
        default -> STYLE_CLASS_INFO16;
      });
    }
  }

  public ValidationMessage getValidationMessage() {
    return validationMessage.get();
  }

  public ObjectProperty<ValidationMessage> validationMessageProperty() {
    return validationMessage;
  }

  public void setValidationMessage(ValidationMessage validationMessage) {
    this.validationMessage.set(validationMessage);
  }
}
