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

import static de.hipphampel.cells.ui.common.UiConstants.TAG_CONTENT;

import de.hipphampel.array2dops.draw.Byte2DArrayDrawContext;
import de.hipphampel.array2dops.geom.Point;
import de.hipphampel.array2dops.model.Byte2DArray;
import de.hipphampel.cells.ServiceLocator;
import de.hipphampel.cells.model.ModificationState;
import de.hipphampel.cells.model.cellculture.CellCulture;
import de.hipphampel.cells.model.cellculture.ResolvedCellCulture;
import de.hipphampel.cells.model.cellsystem.CellSystem;
import de.hipphampel.cells.model.event.CellCultureEvent;
import de.hipphampel.cells.model.validation.Severity;
import de.hipphampel.cells.resources.Resources;
import de.hipphampel.cells.ui.cellculture.drawing.CellDrawing;
import de.hipphampel.cells.ui.cellculture.drawing.CircleCellDrawing;
import de.hipphampel.cells.ui.cellculture.drawing.FillCircleCellDrawing;
import de.hipphampel.cells.ui.cellculture.drawing.FillRectCellDrawing;
import de.hipphampel.cells.ui.cellculture.drawing.LineCellDrawing;
import de.hipphampel.cells.ui.cellculture.drawing.PasteCellDrawing;
import de.hipphampel.cells.ui.cellculture.drawing.PointsCellDrawing;
import de.hipphampel.cells.ui.cellculture.drawing.RectCellDrawing;
import de.hipphampel.cells.ui.cellculture.drawing.SelectCellDrawing;
import de.hipphampel.cells.ui.cellsystem.CellTypeComboBox;
import de.hipphampel.cells.ui.clipboard.CellCultureDataSelection;
import de.hipphampel.cells.ui.clipboard.ClipboardData;
import de.hipphampel.cells.ui.common.ConfirmationDialog;
import de.hipphampel.cells.ui.common.DecoratedObjectLabel;
import de.hipphampel.cells.ui.common.FXMLUtils;
import de.hipphampel.cells.ui.common.HeaderPanel;
import de.hipphampel.cells.ui.common.ValidationMarkerDecoration;
import de.hipphampel.mv4fx.view.View;
import de.hipphampel.validation.core.event.EventListener;
import de.hipphampel.validation.core.event.WeakEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class CellCultureView extends View {

  private final ObjectProperty<CellCulture> cellCulture;
  private final InvalidationListener modifyListener = this::onCellCultureModified;
  private final WeakInvalidationListener weakModifyListener = new WeakInvalidationListener(modifyListener);

  private final VBox content;
  @FXML
  private HeaderPanel infoHeaderPanel;
  @FXML
  private VBox dataVBox;
  @FXML
  private VBox noDataVBox;
  @FXML
  private CellCultureLabel headerLabel;
  @FXML
  private CellCultureInfoEditor cellCultureInfoEditor;
  @FXML
  private ToggleGroup toolGroup;
  @FXML
  private CellCultureDataRenderer cellCultureDataRenderer;
  @FXML
  private CellTypeComboBox cellTypeComboBox;
  @FXML
  private Spinner<Integer> scaleSpinner;
  @FXML
  private ToggleButton gridToggleButton;
  @FXML
  private ToggleButton pasteDrawToolButton;
  @FXML
  private Button undoButton;
  @FXML
  private Label cellPositionLabel;
  private CellDrawing drawing;
  private final ChangeListener<ClipboardData> clipboardChangeListener = this::onClipboardChanged;
  private CellCultureDataSelection selection;
  private Byte2DArray initialData;
  private final List<CellDrawing> drawStack;
  private final EventListener eventListener;


  public CellCultureView() {
    this.content = new VBox();
    FXMLUtils.load("CellCultureView.fxml", content, this);

    this.cellCulture = new SimpleObjectProperty<>(this, "cellCulture");
    this.cellCulture.addListener((ign, oldCulture, newCulture) -> onCellCultureChanged(oldCulture, newCulture));
    this.setTabNode(param -> {
      DecoratedObjectLabel<CellCulture> tabCellCultureLabel = new DecoratedObjectLabel<>(
          new CellCultureLabel(false, false, false),
          new ValidationMarkerDecoration()
      );
      tabCellCultureLabel.objectProperty().bind(this.cellCulture);
      return tabCellCultureLabel;
    });
    this.setDragTags(Set.of(TAG_CONTENT));

    this.headerLabel.cellCultureProperty().bind(cellCulture);
    this.cellCultureInfoEditor.cellCultureProperty().bind(cellCulture);
    this.scaleSpinner.getValueFactory().valueProperty().bindBidirectional(this.cellCultureDataRenderer.scaleProperty());
    this.gridToggleButton.selectedProperty().bindBidirectional(this.cellCultureDataRenderer.showGridProperty());

    this.drawStack = new ArrayList<>();
    ServiceLocator.getClipboardListener().dataProperty().addListener(new WeakChangeListener<>(clipboardChangeListener));
    updateSelection();

    this.cellCultureDataRenderer.setScale(10);
    this.cellCultureDataRenderer.setShowGrid(true);
    this.cellCultureDataRenderer.setEnableSelection(false);
    this.cellCultureDataRenderer.addEventHandler(MouseEvent.ANY, this::onMouseEvent);
    setContent(content);

    this.eventListener = event -> {
      if ((event.payload() instanceof CellCultureEvent cce) && Objects.equals(cce.id(), getCellCulture().getId())) {
        onCellCultureModified(null);
      }
    };
    ServiceLocator.getEventPublisher().subscribe(new WeakEventListener(eventListener));
  }

  public CellCultureView(CellCulture cellCulture) {
    this();
    setCellCulture(cellCulture);
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

  @Override
  public boolean canClose() {
    if (getCellCulture() == null || getCellCulture().getModificationState() == ModificationState.UNCHANGED) {
      return true;
    }

    boolean closeable = new ConfirmationDialog(Resources.getResource("cellCultureView.confirmCloseDialogHeader"),
        Resources.getResource("cellCultureView.confirmCloseDialogQuestion")).showAndWait().orElse(false);
    if (closeable) {
      ServiceLocator.getCellCultureRepository().revertCellCulture(getCellCulture());
      ServiceLocator.getCellCultureRepository().deleteCellCultureIfTransient(getCellCulture().getId());
    }
    return closeable;
  }

  private void onMouseEvent(MouseEvent event) {
    if (cellCultureDataRenderer.getCellCulture() == null) {
      return;
    }
    EventType<?> eventType = event.getEventType();
    Point cellPos = cellCultureDataRenderer.toCellCoordinates(event.getX(), event.getY());
    cellPositionLabel.setText(String.format("(%d,%d)", cellPos.x(), cellPos.y()));

    if (MouseEvent.MOUSE_PRESSED.equals(eventType)) {
      drawing = newDrawing();
    }

    if (drawing == null) {
      return;
    }

    if (MouseEvent.MOUSE_PRESSED.equals(eventType)) {
      drawing.start(cellPos, cellTypeComboBox.getCellType());
      cellCultureDataRenderer.updateCellDrawing(drawing);
      cellCultureDataRenderer.setEnableSelection(drawing instanceof SelectCellDrawing);
    } else if (MouseEvent.MOUSE_DRAGGED.equals(eventType)) {
      drawing.update(cellPos);
      cellCultureDataRenderer.updateCellDrawing(drawing);
    } else if (MouseEvent.MOUSE_RELEASED.equals(eventType)) {
      drawing.confirm(cellPos, getCellCulture());
      cellCultureDataRenderer.updateCellDrawing(null);
      drawStack.add(drawing);
      drawing = null;
      onHistoryChanged();
    }
  }

  private CellDrawing newDrawing() {
    if (toolGroup.getSelectedToggle() == null) {
      return null;
    }
    return switch ((String) toolGroup.getSelectedToggle().getUserData()) {
      case "select" -> new SelectCellDrawing();
      case "paste" -> (selection != null ? new PasteCellDrawing(selection.data()) : null);
      case "points" -> new PointsCellDrawing();
      case "line" -> new LineCellDrawing();
      case "rect" -> new RectCellDrawing();
      case "fillRect" -> new FillRectCellDrawing();
      case "circle" -> new CircleCellDrawing();
      case "fillCircle" -> new FillCircleCellDrawing();
      default -> null;
    };
  }

  @FXML
  public void onUndo() {
    if (initialData == null || drawStack.isEmpty()) {
      return;
    }
    drawStack.remove(drawStack.size() - 1);
    Byte2DArrayDrawContext context = new Byte2DArrayDrawContext(initialData.copy());

    getCellCulture().setData(context.getCanvas());
    drawStack.forEach(drawing -> drawing.draw(context));
    ResolvedCellCulture cellCulture = createResolveCellCulture(getCellCulture());
    cellCultureDataRenderer.setCellCulture(cellCulture);
    onHistoryChanged();
  }

  private void onHistoryChanged() {
    undoButton.setDisable(initialData == null || drawStack.isEmpty());
  }


  @FXML
  public void onMaximizeOrRestore() {
    if (isMaximized()) {
      setMaximized(false);
      content.getChildren().setAll(infoHeaderPanel, dataVBox);
    } else {
      setMaximized(true);
      content.getChildren().setAll(dataVBox);
    }
  }

  private void onClipboardChanged(Observable observable, ClipboardData oldData, ClipboardData newData) {
    updateSelection();
  }

  private void updateSelection() {
    if (ServiceLocator.getClipboardListener().getData() instanceof CellCultureDataSelection current
        && getCellCulture() != null && current.cellTypeCount() == getCellCulture().getCellTypeCount()) {
      this.selection = current;
    } else {
      this.selection = null;
    }
    pasteDrawToolButton.setDisable(this.selection == null);
  }


  private void onCellCultureChanged(CellCulture oldCellCulture, CellCulture newCellCulture) {
    if (oldCellCulture != null) {
      oldCellCulture.dimensionsProperty().removeListener(weakModifyListener);
      oldCellCulture.preferredCellSystemProperty().removeListener(weakModifyListener);
      oldCellCulture.preferredCellSystemValidationReport().removeListener(weakModifyListener);
    }
    if (newCellCulture != null) {
      newCellCulture.dimensionsProperty().addListener(weakModifyListener);
      newCellCulture.preferredCellSystemProperty().addListener(weakModifyListener);
      newCellCulture.preferredCellSystemValidationReport().addListener(weakModifyListener);
    }
    onCellCultureModified(null);
  }

  private void onCellCultureModified(Observable ignore) {

    ResolvedCellCulture cellCulture = createResolveCellCulture(getCellCulture());

    cellCultureDataRenderer.setCellCulture(cellCulture);
    if (cellCulture == null) {
      cellTypeComboBox.setCellSystem(null);
      initialData = null;
    } else {
      if (!cellCulture.system().equals(cellTypeComboBox.getCellSystem())) {
        cellTypeComboBox.setCellSystem(cellCulture.system());
      }
      initialData = cellCulture.culture().getData().copy();
    }

    drawStack.clear();

    onHistoryChanged();
    updateSelection();

    boolean cellSystemValid = false;
    if (getCellCulture() != null) {
      cellSystemValid = getCellCulture().preferredCellSystemValidationReport().get().severity() == Severity.INFO;
    }
    dataVBox.setVisible(cellSystemValid);
    noDataVBox.setVisible(!cellSystemValid);
  }

  private ResolvedCellCulture createResolveCellCulture(CellCulture cellCulture) {
    CellSystem cellSystem = cellCulture == null ? null
        : ServiceLocator.getCellSystemRepository().getCellSystem(cellCulture.getPreferredCellSystem()).orElse(null);
    if (cellSystem == null) {
      return null;
    }
    ServiceLocator.getCellCultureRepository().lazyLoadCultureData(cellCulture);
    return new ResolvedCellCulture(cellCulture.getId(), cellCulture, cellSystem);
  }
}
