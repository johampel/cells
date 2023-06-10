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

import static de.hipphampel.cells.ui.common.UiConstants.IMAGE_EMPTY;
import static de.hipphampel.cells.ui.common.UiConstants.STYLE_CLASS_ERROR16;
import static de.hipphampel.cells.ui.common.UiConstants.STYLE_CLASS_ERROR32;
import static de.hipphampel.cells.ui.common.UiConstants.STYLE_CLASS_INFO16;
import static de.hipphampel.cells.ui.common.UiConstants.STYLE_CLASS_INFO32;
import static de.hipphampel.cells.ui.common.UiConstants.STYLE_CLASS_WARNING16;
import static de.hipphampel.cells.ui.common.UiConstants.STYLE_CLASS_WARNING32;

import de.hipphampel.cells.ServiceLocator;
import de.hipphampel.cells.model.validation.ValidationReport;
import javafx.beans.NamedArg;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class ValidationMarker extends Label {

  private final ObjectProperty<ValidationReport> validationReport;
  private final boolean large;

  public ValidationMarker() {
    this(false);
  }

  public ValidationMarker(@NamedArg("large") boolean large) {
    this.validationReport = new SimpleObjectProperty<>(this, "report");
    this.validationReport.addListener(this::onReportChanged);
    this.large = large;
    onReportChanged(null);
  }

  public ValidationReport getValidationReport() {
    return validationReport.get();
  }

  public ObjectProperty<ValidationReport> validationReportProperty() {
    return validationReport;
  }

  public void setValidationReport(ValidationReport validationReport) {
    this.validationReport.set(validationReport);
  }

  private void onReportChanged(Observable ignore) {
    Image image = ServiceLocator.getImageMerger().getImage(UiUtils.imageNameFor(IMAGE_EMPTY, large));
    Tooltip tooltip = null;

    getStyleClass().removeAll(STYLE_CLASS_INFO16, STYLE_CLASS_WARNING16, STYLE_CLASS_ERROR16, STYLE_CLASS_INFO32, STYLE_CLASS_WARNING32,
        STYLE_CLASS_ERROR32);
    if (getValidationReport() != null && !getValidationReport().isEmpty()) {

      getStyleClass().add(switch (getValidationReport().severity()) {
        case ERROR -> large ? STYLE_CLASS_ERROR32 : STYLE_CLASS_ERROR16;
        case WARNING -> large ? STYLE_CLASS_WARNING32 : STYLE_CLASS_WARNING16;
        default -> large ? STYLE_CLASS_INFO32 : STYLE_CLASS_INFO16;
      });
      VBox vbox = new VBox();
      getValidationReport().messages().stream()
          .map(ValidationMessageRenderer::new)
          .forEach(renderer -> vbox.getChildren().add(renderer));
      tooltip = new Tooltip();
      tooltip.setGraphic(vbox);
    }

    setGraphic(new ImageView(image));
    setTooltip(tooltip);
  }

}
