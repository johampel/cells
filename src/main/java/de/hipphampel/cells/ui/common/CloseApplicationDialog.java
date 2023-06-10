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

import static de.hipphampel.cells.ui.common.UiConstants.STYLE_CLASS_CELLS_LARGE;
import static de.hipphampel.cells.ui.common.UiConstants.STYLE_CLASS_QUESTION32;

import de.hipphampel.cells.model.ModelObject;
import de.hipphampel.cells.model.Selectable;
import de.hipphampel.cells.model.cellculture.CellCulture;
import de.hipphampel.cells.model.cellsystem.CellSystem;
import de.hipphampel.cells.resources.Resources;
import de.hipphampel.cells.ui.cellculture.CellCultureLabel;
import de.hipphampel.cells.ui.cellsystem.CellSystemLabel;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class CloseApplicationDialog extends BaseDialog<List<ModelObject>> {


  @FXML
  private ListView<Selectable<ModelObject>> objectList;


  public CloseApplicationDialog(List<ModelObject> objects) {
    setTitle(Resources.getResource("confirmationDialog.title"));

    VBox content = new VBox();
    FXMLUtils.load("CloseApplicationDialog.fxml", content, this);
    setContent(content);

    objectList.setCellFactory(list -> new ModelObjectListCell());
    objectList.getSelectionModel().selectedItemProperty().addListener((ign, ov, nv) -> toggleSelection(nv));
    objects.stream()
        .map(value -> new Selectable<>(value, true))
        .forEach(smo -> objectList.getItems().add(smo));
    Label headerLabel = new Label(Resources.getResource("closeApplicationDialog.question"));
    headerLabel.getStyleClass().addAll(STYLE_CLASS_CELLS_LARGE, STYLE_CLASS_QUESTION32);
    setHeader(headerLabel);

    getDialogPane().getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
    setResultConverter(this::convertResult);

  }

  private List<ModelObject> convertResult(ButtonType buttonType) {
    if (buttonType != ButtonType.YES) {
      return null;
    }

    return objectList.getItems().stream()
        .filter(Selectable::isSelected)
        .map(Selectable::getValue)
        .toList();
  }

  @FXML
  public void onSelectAll() {
    objectList.getItems()
        .forEach(mo -> mo.setSelected(true));
  }

  @FXML
  public void onSelectNone() {
    objectList.getItems()
        .forEach(mo -> mo.setSelected(false));
  }

  private static void toggleSelection(Selectable<?> selectable) {
    if (selectable != null) {
      selectable.setSelected(!selectable.isSelected());
    }
  }

  private static class ModelObjectListCell extends ListCell<Selectable<ModelObject>> {

    private final HBox graphic;
    private final CheckBox checkBox;
    private final CellCultureLabel cellCultureLabel;
    private final CellSystemLabel cellSystemLabel;
    private Selectable<ModelObject> currentlyBound;

    public ModelObjectListCell() {
      this.graphic = new HBox();
      this.graphic.setSpacing(5);
      this.checkBox = new CheckBox();
      this.cellCultureLabel = new CellCultureLabel(false, false, false);
      this.cellCultureLabel.setOnMouseClicked(this::onMouseClicked);
      this.cellSystemLabel = new CellSystemLabel(false, false, false);
      this.cellSystemLabel.setOnMouseClicked(this::onMouseClicked);
    }

    private void onMouseClicked(MouseEvent evt) {
      toggleSelection(currentlyBound);
    }

    @Override
    public void updateItem(Selectable<ModelObject> item, boolean empty) {
      super.updateItem(item, empty);

      if (empty) {
        setGraphic(null);
        setText(null);
        return;
      }

      graphic.getChildren().clear();
      if (item.getValue() instanceof CellSystem cellSystem) {
        cellSystemLabel.setCellSystem(cellSystem);
        graphic.getChildren().addAll(checkBox, cellSystemLabel);
      } else if (item.getValue() instanceof CellCulture cellCulture) {
        cellCultureLabel.setCellCulture(cellCulture);
        graphic.getChildren().addAll(checkBox, cellCultureLabel);
      }

      if (currentlyBound != null) {
        checkBox.selectedProperty().unbindBidirectional(currentlyBound.selectedProperty());
      }
      currentlyBound = item;
      if (currentlyBound != null) {
        checkBox.selectedProperty().bindBidirectional(currentlyBound.selectedProperty());
      }

      setGraphic(graphic);
    }
  }

}
