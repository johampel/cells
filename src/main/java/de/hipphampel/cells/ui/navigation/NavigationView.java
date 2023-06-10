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

import static de.hipphampel.cells.ui.common.UiConstants.TAG_NAVIGATION;

import de.hipphampel.cells.ServiceLocator;
import de.hipphampel.cells.model.ModelUtils;
import de.hipphampel.cells.model.cellculture.CellCulture;
import de.hipphampel.cells.model.cellsystem.CellSystem;
import de.hipphampel.cells.model.event.CellCultureEvent;
import de.hipphampel.cells.model.event.CellSystemEvent;
import de.hipphampel.cells.resources.Resources;
import de.hipphampel.cells.ui.common.ConfirmationDialog;
import de.hipphampel.cells.ui.common.FXMLUtils;
import de.hipphampel.cells.ui.common.UiUtils;
import de.hipphampel.cells.ui.navigation.NavigationNode.Kind;
import de.hipphampel.cells.ui.navigation.NewDialog.Outcome;
import de.hipphampel.mv4fx.view.View;
import de.hipphampel.validation.core.event.EventListener;
import de.hipphampel.validation.core.event.WeakEventListener;
import java.util.Set;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class NavigationView extends View {

  private final VBox content;
  @FXML
  private TreeView<NavigationNode> navigationTreeView;
  @FXML
  private Button removeButton;
  @FXML
  private Button copyButton;
  private final EventListener eventListener;
  private final NavigationTreeItem rootItem;

  public NavigationView() {
    setTabLabel(Resources.getResource("navigationView.tabLabel"));
    setTabCloseActionVisibility(TabActionVisibility.NEVER);
    setDropTargetTypes(Set.of());
    setDragTags(Set.of(TAG_NAVIGATION));

    this.content = new VBox();
    FXMLUtils.load("NavigationView.fxml", content, this);

    this.rootItem = new NavigationTreeItem(new NavigationNode(Kind.Root, null, null));
    this.navigationTreeView.setCellFactory(view -> new NavigationTreeCell());
    this.navigationTreeView.setRoot(rootItem);
    this.navigationTreeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    this.navigationTreeView.addEventHandler(MouseEvent.MOUSE_CLICKED, ignore -> onSelectionChanged());
    //this.navigationTreeView.getSelectionModel().selectedItemProperty().addListener(ignore -> onSelectionChanged());
    this.eventListener = event -> {
      if (event.payload() instanceof CellSystemEvent) {
        rootItem.onCellSystemUpdate();
      }
      if (event.payload() instanceof CellCultureEvent) {
        rootItem.onCellCultureUpdate();
      }
    };
    ServiceLocator.getEventPublisher().subscribe(new WeakEventListener(eventListener));

  }

  @Override
  public Node getContent() {
    return content;
  }

  @FXML
  private void onNewButton() {
    NewDialog dialog = new NewDialog();
    switch (dialog.showAndWait().orElse(Outcome.Nothing)) {
      case CellCulture:
        newCellCulture();
        break;
      case CellSystem:
        newCellSystem();
        break;
    }
  }

  @FXML
  private void onCopyButton() {
    TreeItem<NavigationNode> treeItem = navigationTreeView.getSelectionModel().getSelectedItem();
    if (treeItem == null) {
      return;
    }
    if (treeItem.getValue().child() instanceof CellSystem cellSystem) {
      CellSystem copy = ServiceLocator.getCellSystemRepository().newCellSystem();
      copy.copyFrom(cellSystem);
      copy.setName(Resources.getResource("navigationView.copyCellSystemName", cellSystem.getName()));
      UiUtils.createOrSelectViewForCellSystem(copy);
    }
    if (treeItem.getValue().child() instanceof CellCulture cellCulture) {
      CellCulture copy = ServiceLocator.getCellCultureRepository().newCellCulture();
      ServiceLocator.getCellCultureRepository().ensureCellCultureValidated(cellCulture);
      copy.copyFrom(cellCulture);
      copy.setName(Resources.getResource("navigationView.copyCellCultureName", cellCulture.getName()));
      UiUtils.createOrSelectViewForCellCulture(copy);
    }
  }

  @FXML
  private void onRemoveButton() {
    TreeItem<NavigationNode> treeItem = navigationTreeView.getSelectionModel().getSelectedItem();
    if (treeItem == null) {
      return;
    }
    if (treeItem.getValue().child() instanceof CellSystem cellSystem) {
      boolean remove = new ConfirmationDialog(
          Resources.getResource("navigationView.removeCellSystemDialogHeader"),
          Resources.getResource("navigationView.removeCellSystemDialogQuestion", cellSystem.getName()))
          .showAndWait().orElse(false);
      if (remove) {
        UiUtils.findViewForCellSystem(cellSystem).ifPresent(View::close);
        ServiceLocator.getCellSystemRepository().deleteCellSystem(cellSystem.getId());
      }
    }
    if (treeItem.getValue().child() instanceof CellCulture cellCulture) {
      boolean remove = new ConfirmationDialog(
          Resources.getResource("navigationView.removeCellCultureDialogHeader"),
          Resources.getResource("navigationView.removeCellCultureDialogQuestion", cellCulture.getName()))
          .showAndWait().orElse(false);
      if (remove) {
        UiUtils.findViewForCellCulture(cellCulture).ifPresent(View::close);
        ServiceLocator.getCellCultureRepository().deleteCellCulture(cellCulture.getId());
      }
    }

  }

  private void newCellSystem() {
    CellSystem cellSystem = ServiceLocator.getCellSystemRepository().newCellSystem();
    ModelUtils.fillAsConwaysGameOfLife(cellSystem);
    cellSystem.setName(Resources.getResource("navigationView.newCellSystemName"));
    UiUtils.createOrSelectViewForCellSystem(cellSystem);
    selectTreeItemForCellSystem(cellSystem);
  }

  private void newCellCulture() {
    CellCulture cellCulture = ServiceLocator.getCellCultureRepository().newCellCulture();
    cellCulture.setName(Resources.getResource("navigationView.newCellCultureName"));
    UiUtils.createOrSelectViewForCellCulture(cellCulture);
    selectTreeItemForCellCulture(cellCulture);
  }

  private void onSelectionChanged() {
    TreeItem<NavigationNode> treeItem = navigationTreeView.getSelectionModel().getSelectedItem();
    CellSystem cellSystem = treeItem != null ? treeItem.getValue().childCellSystem() : null;
    CellCulture cellCulture = treeItem != null ? treeItem.getValue().childCellCulture() : null;
    removeButton.setDisable(cellSystem == null && cellCulture == null);
    copyButton.setDisable(cellSystem == null && cellCulture == null);

    if (cellSystem != null) {
      UiUtils.createOrSelectViewForCellSystem(cellSystem);
    }
    if (cellCulture != null) {
      ServiceLocator.getCellCultureRepository().lazyLoadCultureData(cellCulture);
      UiUtils.createOrSelectViewForCellCulture(cellCulture);
    }
  }

  private void selectTreeItemForCellSystem(CellSystem cellSystem) {
    rootItem.setExpanded(true);
    TreeItem<NavigationNode> allCellSystemsItem = rootItem.getChildren().stream()
        .filter(item -> item.getValue().kind() == Kind.AllCellSystems)
        .findFirst().orElse(null);
    if (allCellSystemsItem == null) {
      return;
    }
    allCellSystemsItem.setExpanded(true);
    TreeItem<NavigationNode> cellSystemItem = allCellSystemsItem.getChildren().stream()
        .filter(item -> item.getValue().child() == cellSystem)
        .findFirst().orElse(null);
    navigationTreeView.getSelectionModel().select(cellSystemItem);
  }

  private void selectTreeItemForCellCulture(CellCulture cellCulture) {
    rootItem.setExpanded(true);
    TreeItem<NavigationNode> allCellCulturesItem = rootItem.getChildren().stream()
        .filter(item -> item.getValue().kind() == Kind.AllCellCultures)
        .findFirst().orElse(null);
    if (allCellCulturesItem == null) {
      return;
    }
    allCellCulturesItem.setExpanded(true);
    TreeItem<NavigationNode> cellSystemItem = allCellCulturesItem.getChildren().stream()
        .filter(item -> item.getValue().child() == cellCulture)
        .findFirst().orElse(null);
    navigationTreeView.getSelectionModel().select(cellSystemItem);
  }
}
