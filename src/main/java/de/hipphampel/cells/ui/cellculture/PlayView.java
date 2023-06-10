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

import static de.hipphampel.cells.ui.common.UiConstants.STYLE_CLASS_PAUSE16;
import static de.hipphampel.cells.ui.common.UiConstants.STYLE_CLASS_PLAY16;
import static de.hipphampel.cells.ui.common.UiConstants.TAG_CONTENT;

import de.hipphampel.cells.model.cellculture.Generator;
import de.hipphampel.cells.model.cellculture.Generator.State;
import de.hipphampel.cells.model.cellculture.ResolvedCellCulture;
import de.hipphampel.cells.resources.Resources;
import de.hipphampel.cells.ui.common.FXMLUtils;
import de.hipphampel.mv4fx.view.View;
import java.util.Set;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

public class PlayView extends View {

  private final ResolvedCellCulture cellCulture;
  private final Generator generator;
  private final Label tabLabel;

  @FXML
  private CellCultureDataRenderer cellCultureDataRenderer;
  @FXML
  private Button revertButton;
  @FXML
  private Button stepButton;
  @FXML
  private Button playButton;
  @FXML
  private Button pauseButton;
  @FXML
  private Slider speedSlider;
  @FXML
  private Spinner<Integer> scaleSpinner;
  @FXML
  private ToggleButton gridToggleButton;

  public PlayView(ResolvedCellCulture cellCulture) {
    VBox content = new VBox();
    FXMLUtils.load("PlayView.fxml", content, this);
    setContent(content);

    this.cellCulture = cellCulture;
    this.generator = new Generator(cellCulture.system(), cellCulture.culture());
    this.generator.generationProperty().addListener((ign, oldValue, newValue) -> onGenerationChanged(newValue.longValue()));
    this.generator.stateProperty().addListener(ign -> onGeneratorStateChanged());

    this.tabLabel = new Label();
    tabLabel.setText(Resources.getResource("playView.title", cellCulture.culture().getName(), cellCulture.system().getName()));
    tabLabel.getStyleClass().add(STYLE_CLASS_PLAY16);
    this.setTabNode(param -> tabLabel);
    this.setDragTags(Set.of(TAG_CONTENT));

    generator.setSpeed(speedSlider.getValue());
    speedSlider.valueProperty().addListener((ign, oldValue, newValue) -> generator.setSpeed(newValue.doubleValue()));
    this.scaleSpinner.getValueFactory().valueProperty().bindBidirectional(this.cellCultureDataRenderer.scaleProperty());
    this.gridToggleButton.selectedProperty().bindBidirectional(this.cellCultureDataRenderer.showGridProperty());

    cellCultureDataRenderer.setCellCulture(cellCulture);
    this.cellCultureDataRenderer.setScale(10);
    this.cellCultureDataRenderer.setShowGrid(true);
    this.cellCultureDataRenderer.setEnableSelection(true);

    onGeneratorStateChanged();
    onGenerationChanged(0);
  }

  public ResolvedCellCulture getCellCulture() {
    return cellCulture;
  }

  @FXML
  public void onMaximizeOrRestore() {
    setMaximized(!isMaximized());
  }

  @Override
  public void close() {
    generator.stop();
    super.close();
  }

  @FXML
  public void onStep() {
    generator.step();
  }

  @FXML
  public void onPlay() {
    generator.play();
  }

  @FXML
  public void onPause() {
    generator.stop();
  }

  @FXML
  public void onReset() {
    generator.reset();
  }
  private void onGenerationChanged(long generation) {
    cellCultureDataRenderer.redraw();
    revertButton.setDisable(generator.getState() != State.IDLE || generation == 0);
  }

  private void onGeneratorStateChanged() {
    Generator.State state = generator.getState();
    revertButton.setDisable(state != State.IDLE || generator.getGeneration() == 0);
    stepButton.setDisable(state != State.IDLE);
    playButton.setDisable(state != State.IDLE);
    pauseButton.setDisable(state != State.PLAYING);
  }
}
