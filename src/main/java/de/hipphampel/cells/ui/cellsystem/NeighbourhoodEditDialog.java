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

import de.hipphampel.cells.ServiceLocator;
import de.hipphampel.cells.model.cellsystem.Neighbourhood;
import de.hipphampel.cells.model.validation.Severity;
import de.hipphampel.cells.model.validation.ValidationReport;
import de.hipphampel.cells.resources.Resources;
import de.hipphampel.cells.ui.common.BaseDialog;
import de.hipphampel.cells.ui.common.FXMLUtils;
import de.hipphampel.cells.ui.common.ValidationMarker;
import de.hipphampel.cells.validation.CellsValidator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

public class NeighbourhoodEditDialog extends BaseDialog<Neighbourhood> {

  private final CellsValidator validator;
  private final Neighbourhood originalNeighbourhood;
  final ObjectProperty<Neighbourhood> neighbourhood;
  private final ValidationMarker validationMarker;
  private final Map<Pair<Integer, Integer>, Spinner<Integer>> spinners;

  @FXML
  private ComboBox<Integer> radiusComboBox;
  @FXML
  private GridPane spinnerContainer;
  @FXML
  private Button resetButton;

  public NeighbourhoodEditDialog(String title, Node headerNode, Neighbourhood neighbourhood) {
    this.validator = ServiceLocator.getValidator();
    this.originalNeighbourhood = neighbourhood;
    this.neighbourhood = new SimpleObjectProperty<>(this, "neighbourhood");
    this.neighbourhood.addListener((ov, oldNeighbourhood, newNeighbourhood) -> onNeighbourhoodChanged(newNeighbourhood));

    NeighbourhoodRenderer neighbourhoodRenderer = new NeighbourhoodRenderer(36, 36);
    neighbourhoodRenderer.neighbourhoodProperty().bind(this.neighbourhood);

    this.validationMarker = new ValidationMarker(true);
    this.spinners = new HashMap<>();

    setTitle(title);

    HBox headerBox = new HBox();
    headerBox.setSpacing(5);
    headerBox.getChildren().addAll(
        neighbourhoodRenderer,
        new Label(Resources.getResource("neighbourhoodEditDialog.headerLabel")),
        headerNode,
        validationMarker);
    setHeader(headerBox);

    setContent(FXMLUtils.load("NeighbourhoodEditDialog.fxml", new VBox(), this));

    radiusComboBox.getItems().addAll(IntStream.range(Neighbourhood.MIN_RADIUS, Neighbourhood.MAX_RADIUS + 1).boxed().toList());
    radiusComboBox.valueProperty().addListener((ov, oldRadius, newRadius) -> onRadiusChanged(newRadius));

    // Editors
    HBox centerPane = new HBox();
    centerPane.getStyleClass().add("center");
    spinnerContainer.add(centerPane, Neighbourhood.MAX_RADIUS, Neighbourhood.MAX_RADIUS);

    forEachWeight((r, c) -> {
      Spinner<Integer> spinner = new Spinner<Integer>(0, 100, 0);
      spinners.put(new Pair<>(r, c), spinner);
      spinner.setEditable(true);
      spinner.setId("spinner_" + r + "_" + c);
      spinner.valueProperty().addListener((ov, oldValue, newValue) -> onWeightChanged(r, c, newValue));
      HBox spinnerPane = new HBox(spinner);

      if (r == -Neighbourhood.MAX_RADIUS && c == -Neighbourhood.MAX_RADIUS) {
        spinnerPane.getStyleClass().add("left-top");
      } else if (r == -Neighbourhood.MAX_RADIUS && c == Neighbourhood.MAX_RADIUS) {
        spinnerPane.getStyleClass().add("right-top");
      } else if (r == Neighbourhood.MAX_RADIUS && c == Neighbourhood.MAX_RADIUS) {
        spinnerPane.getStyleClass().add("right-bottom");
      } else if (r == Neighbourhood.MAX_RADIUS && c == -Neighbourhood.MAX_RADIUS) {
        spinnerPane.getStyleClass().add("left-bottom");
      } else if (c == -Neighbourhood.MAX_RADIUS) {
        spinnerPane.getStyleClass().add("left");
      } else if (c == Neighbourhood.MAX_RADIUS) {
        spinnerPane.getStyleClass().add("right");
      } else if (r == -Neighbourhood.MAX_RADIUS) {
        spinnerPane.getStyleClass().add("top");
      } else if (r == Neighbourhood.MAX_RADIUS) {
        spinnerPane.getStyleClass().add("bottom");
      } else {
        spinnerPane.getStyleClass().add("middle");
      }
      spinnerContainer.add(spinnerPane, c + Neighbourhood.MAX_RADIUS, r + Neighbourhood.MAX_RADIUS);
    });

    getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);

    this.neighbourhood.set(neighbourhood.copy());
    setResultConverter(buttonType -> buttonType == ButtonType.APPLY ? this.neighbourhood.get() : null);
  }

  private void onNeighbourhoodChanged(Neighbourhood newNeighbourhood) {
    if (newNeighbourhood == null) {
      return;
    }
    forEachWeight((r, c) -> {
      int radius = newNeighbourhood.getRadius();
      boolean visible = r >= -radius && r <= radius && c >= -radius && c <= radius;
      if (visible) {
        Spinner<Integer> spinner = spinners.get(new Pair<>(r, c));
        spinner.getValueFactory().setValue(newNeighbourhood.getWeightAt(c, r));
      }
    });
    radiusComboBox.valueProperty().set(newNeighbourhood.getRadius());
    validate();
  }

  @FXML
  public void onResetNeighbourhood() {
    neighbourhood.set(originalNeighbourhood.copy());
  }

  @FXML
  public void onFillWithZeros() {
    int radius = this.neighbourhood.get().getRadius();
    forEachWeight((r, c) -> {
      boolean valid = r >= -radius && r <= radius && c >= -radius && c <= radius;
      if (valid) {
        Spinner<Integer> spinner = spinners.get(new Pair<>(r, c));
        spinner.getValueFactory().setValue(0);
      }
    });
  }

  @FXML
  public void onFillWithOnes() {
    int radius = this.neighbourhood.get().getRadius();
    forEachWeight((r, c) -> {
      boolean valid = r >= -radius && r <= radius && c >= -radius && c <= radius;
      if (valid) {
        Spinner<Integer> spinner = spinners.get(new Pair<>(r, c));
        spinner.getValueFactory().setValue(1);
      }
    });
  }

  private void onRadiusChanged(int newRadius) {
    Neighbourhood neighbourhood = this.neighbourhood.get();
    neighbourhood.setRadius(newRadius);
    forEachWeight((r, c) -> {
      Spinner<Integer> spinner = spinners.get(new Pair<>(r, c));
      boolean visible = r >= -newRadius && r <= newRadius && c >= -newRadius && c <= newRadius;
      spinner.setVisible(visible);
      if (visible) {
        neighbourhood.setWeightAt(c, r, spinner.valueProperty().getValue());
      }
    });
    validate();
  }

  private void onWeightChanged(int row, int column, int weight) {
    Neighbourhood neighbourhood = this.neighbourhood.get();
    neighbourhood.setWeightAt(column, row, weight);
    validate();
  }

  private void validate() {
    boolean unmodified = Objects.equals(originalNeighbourhood, neighbourhood.get());
    ValidationReport report = validator.validateNeighbourhood(neighbourhood.get());
    validationMarker.setValidationReport(report);
    Node button = getDialogPane().lookupButton(ButtonType.APPLY);
    button.setDisable(report.severity() == Severity.ERROR || unmodified);
    resetButton.setDisable(unmodified);
  }

  private void forEachWeight(BiConsumer<Integer, Integer> consumer) {
    for (int c = -Neighbourhood.MAX_RADIUS; c <= Neighbourhood.MAX_RADIUS; c++) {
      for (int r = -Neighbourhood.MAX_RADIUS; r <= Neighbourhood.MAX_RADIUS; r++) {
        if (r == 0 && c == 0) {
          continue;
        }
        consumer.accept(r, c);
      }
    }
  }
}
