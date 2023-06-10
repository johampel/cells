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

import static de.hipphampel.cells.ui.common.UiConstants.TAG_CONTENT;

import de.hipphampel.cells.ServiceLocator;
import de.hipphampel.cells.model.ModificationState;
import de.hipphampel.cells.model.cellsystem.CellSystem;
import de.hipphampel.cells.model.cellsystem.CellType;
import de.hipphampel.cells.model.validation.Severity;
import de.hipphampel.cells.model.validation.ValidationReport;
import de.hipphampel.cells.resources.Resources;
import de.hipphampel.cells.ui.common.CellsViewGroup;
import de.hipphampel.cells.ui.common.ConfirmationDialog;
import de.hipphampel.cells.ui.common.DecoratedObjectLabel;
import de.hipphampel.cells.ui.common.FXMLUtils;
import de.hipphampel.cells.ui.common.HeaderPanel;
import de.hipphampel.cells.ui.common.ValidationMarker;
import de.hipphampel.cells.ui.common.ValidationMarkerDecoration;
import de.hipphampel.mv4fx.view.View;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class CellSystemView extends View {

  private final InvalidationListener modificationListener;
  private final WeakInvalidationListener weakModificationListener;
  private final ObjectProperty<CellSystem> cellSystem;
  @FXML
  private CellSystemLabel headerLabel;
  @FXML
  private Button saveButton;
  @FXML
  private Button revertButton;
  @FXML
  private ValidationMarker nameValidationMarker;
  @FXML
  private TextField nameTextField;
  @FXML
  private ValidationMarker descriptionValidationMarker;
  @FXML
  private TextArea descriptionTextArea;
  @FXML
  private ValidationMarker neighbourhoodValidationMarker;
  @FXML
  private NeighbourhoodRenderer neighbourhoodRenderer;
  @FXML
  private ListView<CellType> cellTypesListView;
  @FXML
  private ValidationMarker cellTypesValidationMarker;
  @FXML
  private CellsViewGroup cellTypesViewGroup;
  @FXML
  private Button removeCellTypeButton;
  @FXML
  private Button cellTypeTopButton;
  @FXML
  private Button cellTypeUpButton;
  @FXML
  private Button cellTypeDownButton;
  @FXML
  private Button cellTypeBottomButton;

  public CellSystemView(CellSystem cellSystem) {
    this();
    setCellSystem(cellSystem);
  }

  public CellSystemView() {
    HeaderPanel content = new HeaderPanel();
    FXMLUtils.load("CellSystemView.fxml", content, this);

    this.modificationListener = this::onCellSystemModified;
    this.weakModificationListener = new WeakInvalidationListener(modificationListener);
    this.cellSystem = new SimpleObjectProperty<>(this, "cellSystem");
    this.cellSystem.addListener(this::onCellSystemChanged);

    this.setTabNode(param -> {
      DecoratedObjectLabel<CellSystem> tabCellSystemLabel = new DecoratedObjectLabel<CellSystem>(
          new CellSystemLabel(),
          new ValidationMarkerDecoration()
      );
      tabCellSystemLabel.objectProperty().bind(cellSystem);
      return tabCellSystemLabel;
    });
    this.setDragTags(Set.of(TAG_CONTENT));

    this.headerLabel.cellSystemProperty().bind(cellSystem);

    this.cellTypesListView.setCellFactory(CellTypeListCell.createFactory(false, true));
    this.cellTypesListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    this.cellTypesListView.getSelectionModel().selectedIndexProperty().addListener(ignore -> onCellTypeSelectionChanged());

    setContent(content);
  }

  public CellSystem getCellSystem() {
    return cellSystem.get();
  }

  public ObjectProperty<CellSystem> cellSystemProperty() {
    return cellSystem;
  }

  public void setCellSystem(CellSystem cellSystem) {
    this.cellSystem.set(cellSystem);
  }

  @Override
  public boolean canClose() {
    if (getCellSystem() == null || getCellSystem().getModificationState() == ModificationState.UNCHANGED) {
      return true;
    }

    boolean closeable = new ConfirmationDialog(Resources.getResource("cellSystemView.confirmCloseDialogHeader"),
        Resources.getResource("cellSystemView.confirmCloseDialogQuestion")).showAndWait().orElse(false);
    if (closeable) {
      ServiceLocator.getCellSystemRepository().revertCellSystem(getCellSystem());
      ServiceLocator.getCellSystemRepository().deleteCellSystemIfTransient(getCellSystem().getId());
    }
    return closeable;
  }

  @FXML
  public void onSaveCellSystem() {
    ServiceLocator.getCellSystemRepository().saveCellSystem(getCellSystem());
    onCellSystemModified(null);
  }

  @FXML
  public void onRevertCellSystem() {
    ConfirmationDialog dialog = new ConfirmationDialog(
        Resources.getResource("cellSystemView.revertCellSystemDialog.header"),
        Resources.getResource("cellSystemView.revertCellSystemDialog.question"));
    if (dialog.showAndWait().orElse(false)) {
      ServiceLocator.getCellSystemRepository().revertCellSystem(getCellSystem());
      reopenCellTypeViews();
      onCellSystemModified(null);
    }
  }

  @FXML
  public void onNeighbourhoodEdit() {
    CellSystemLabel titleRenderer = new CellSystemLabel(getCellSystem(), false, true, false);
    NeighbourhoodEditDialog dialog = new NeighbourhoodEditDialog(
        Resources.getResource("cellSystemView.neighbourhoodEditDialog.title"),
        titleRenderer,
        getCellSystem().getNeighbourhood());
    dialog.showAndWait().ifPresent(newNeighbourhood -> getCellSystem().setNeighbourhood(newNeighbourhood));
  }

  @FXML
  public void onAddCellType() {
    if (getCellSystem() != null) {
      CellType cellType = getCellSystem().newCellType();
      getOrCreateCellTypeView(cellType, true);
    }
  }

  @FXML
  public void onRemoveCellType() {
    CellType cellType = cellTypesListView.getSelectionModel().getSelectedItem();
    if (getCellSystem() != null && cellType != null) {
      closeCellTypeView(cellType);
      getCellSystem().removeCellType(cellType.getId());
      cellTypesListView.getSelectionModel().select(-1);
    }
  }

  @FXML
  public void onCellTypeTop() {
    if (getCellSystem() != null) {
      int index = cellTypesListView.getSelectionModel().getSelectedIndex();
      while (index > 0) {
        getCellSystem().swapCellTypes(index, index - 1);
        index--;
      }
      cellTypesListView.getSelectionModel().select(index);
    }
  }

  @FXML
  public void onCellTypeUp() {
    if (getCellSystem() != null) {
      int index = cellTypesListView.getSelectionModel().getSelectedIndex();
      if (index > 0) {
        getCellSystem().swapCellTypes(index, index - 1);
        cellTypesListView.getSelectionModel().select(index - 1);
      }
    }
  }

  @FXML
  public void onCellTypeDown() {
    if (getCellSystem() != null) {
      int index = cellTypesListView.getSelectionModel().getSelectedIndex();
      if (index + 1 < getCellSystem().getCellTypeCount()) {
        getCellSystem().swapCellTypes(index, index + 1);
        cellTypesListView.getSelectionModel().select(index + 1);
      }
    }
  }

  @FXML
  public void onCellTypeBottom() {
    if (getCellSystem() != null) {
      int index = cellTypesListView.getSelectionModel().getSelectedIndex();
      while (index + 1 < getCellSystem().getCellTypeCount()) {
        getCellSystem().swapCellTypes(index, index + 1);
        index++;
      }
      cellTypesListView.getSelectionModel().select(index);
    }
  }

  private void onCellTypeSelectionChanged() {
    enableAndDisableCellTypeButtons();
    findCellTypeView(cellTypesListView.getSelectionModel().getSelectedItem()).ifPresent(view -> cellTypesViewGroup.selectView(view));
  }

  private void enableAndDisableCellTypeButtons() {
    if (getCellSystem() != null) {
      int index = cellTypesListView.getSelectionModel().getSelectedIndex();
      removeCellTypeButton.setDisable(index == -1 || getCellSystem().getCellTypeCount() < 2);
      cellTypeTopButton.setDisable(index <= 0);
      cellTypeUpButton.setDisable(index <= 0);
      cellTypeDownButton.setDisable(index + 1 >= getCellSystem().getCellTypeCount());
      cellTypeBottomButton.setDisable(index + 1 >= getCellSystem().getCellTypeCount());
    }
  }

  private void onCellSystemChanged(Observable ignore, CellSystem oldCellSystem, CellSystem newCellSystem) {
    if (oldCellSystem != null) {
      oldCellSystem.modificationCounterProperty().removeListener(weakModificationListener);
      nameValidationMarker.validationReportProperty().unbind();
      nameTextField.textProperty().unbindBidirectional(oldCellSystem.nameProperty());
      descriptionValidationMarker.validationReportProperty().unbind();
      descriptionTextArea.textProperty().unbindBidirectional(oldCellSystem.descriptionProperty());
      neighbourhoodValidationMarker.validationReportProperty().unbind();
      neighbourhoodRenderer.neighbourhoodProperty().unbind();
      cellTypesListView.itemsProperty().unbind();
      cellTypesValidationMarker.validationReportProperty().unbind();
    }
    if (newCellSystem != null) {
      newCellSystem.modificationCounterProperty().addListener(weakModificationListener);
      nameValidationMarker.validationReportProperty().bind(newCellSystem.nameValidationReportProperty());
      nameTextField.textProperty().bindBidirectional(newCellSystem.nameProperty());
      descriptionValidationMarker.validationReportProperty().bind(newCellSystem.descriptionValidationReportProperty());
      descriptionTextArea.textProperty().bindBidirectional(newCellSystem.descriptionProperty());
      neighbourhoodValidationMarker.validationReportProperty().bind(newCellSystem.neighbourhoodValidationReportProperty());
      neighbourhoodRenderer.neighbourhoodProperty().bindBidirectional(newCellSystem.neighbourhoodProperty());
      cellTypesListView.itemsProperty().bind(newCellSystem.cellTypesProperty());
      cellTypesValidationMarker.validationReportProperty().bind(newCellSystem.cellTypesValidationReportProperty());
      reopenCellTypeViews();
    }
    onCellSystemModified(null);
  }

  private void onCellSystemModified(Observable ignore) {
    CellSystem cellSystem = getCellSystem();
    if (cellSystem == null) {
      return;
    }
    ModificationState state = ServiceLocator.getCellSystemRepository().getCellSystemState(cellSystem);
    ValidationReport report = ServiceLocator.getValidator().validateCellSystem(cellSystem);
    cellSystem.setModificationState(state);
    cellSystem.setValidationReport(report);
    ServiceLocator.getValidator().revalidateCellCulturesForCellSystem(cellSystem);
    saveButton.setDisable(state == ModificationState.UNCHANGED || report.severity() == Severity.ERROR);
    revertButton.setDisable(state != ModificationState.MODIFIED);
  }

  private void reopenCellTypeViews() {
    ArrayList<View> views = new ArrayList<>(cellTypesViewGroup.getViews());
    for (View view : views) {
      if (view instanceof CellTypeView ctv) {
        ctv.close();
      }
    }

    if (getCellSystem() != null) {
      getCellSystem().getCellTypes().forEach(cellType -> getOrCreateCellTypeView(cellType, false));
      if (getCellSystem().getCellTypeCount() > 0) {
        cellTypesListView.getSelectionModel().select(0);
      }
    }
  }

  private CellTypeView getOrCreateCellTypeView(CellType cellType, boolean select) {
    CellTypeView view = findCellTypeView(cellType)
        .orElseGet(() -> createCellTypeView(cellType));

    if (select) {
      Platform.runLater(() -> cellTypesListView.getSelectionModel().select(cellType));
    }
    return view;
  }

  private CellTypeView createCellTypeView(CellType cellType) {
    CellTypeView view = new CellTypeView();
    view.setCellType(cellType);
    view.setDropTargetTypes(Set.of(DropTargetType.REORDER));
    cellTypesViewGroup.addView(view);
    return view;
  }

  private void closeCellTypeView(CellType cellType) {
    findCellTypeView(cellType).ifPresent(View::close);
  }

  private Optional<CellTypeView> findCellTypeView(CellType cellType) {
    return cellTypesViewGroup.getViews().stream()
        .filter(view -> view instanceof CellTypeView ctv && ctv.getCellType() == cellType)
        .map(view -> (CellTypeView) view)
        .findFirst();
  }
}
