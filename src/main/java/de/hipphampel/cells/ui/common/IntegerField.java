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

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.TextField;

public class IntegerField extends TextField {

  private final EnterOnceGuard enterOnceGuard;
  private final IntegerProperty intValue;

  public IntegerField() {
    this.enterOnceGuard = new EnterOnceGuard();
    this.intValue = new SimpleIntegerProperty(this, "intValue");
    textProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue.matches("\\d*")) {
        setText(newValue.replaceAll("[^\\d]", ""));
      }
    });
    textProperty().addListener(observable -> enterOnceGuard.run(() -> {
      try {
        setIntValue(Integer.parseInt(getText()));
      } catch (NumberFormatException nfe) {
        // Ignore
      }
    }));
      intValueProperty().addListener(observable -> enterOnceGuard.run(() -> setText(String.valueOf(getIntValue()))));
  }

  public int getIntValue() {
    return intValue.get();
  }

  public IntegerProperty intValueProperty() {
    return intValue;
  }

  public void setIntValue(int intValue) {
    this.intValue.set(intValue);
  }
}
