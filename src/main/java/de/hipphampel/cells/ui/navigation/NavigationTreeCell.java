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

import static de.hipphampel.cells.ui.common.UiConstants.STYLE_CLASS_FOLDER;

import de.hipphampel.cells.model.cellculture.CellCulture;
import de.hipphampel.cells.model.cellsystem.CellSystem;
import de.hipphampel.cells.resources.Resources;
import de.hipphampel.cells.ui.cellculture.CellCultureLabel;
import de.hipphampel.cells.ui.cellsystem.CellSystemLabel;
import de.hipphampel.cells.ui.common.DecoratedObjectLabel;
import de.hipphampel.cells.ui.common.PlayButtonDecoration;
import de.hipphampel.cells.ui.common.ValidationMarkerDecoration;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;

public class NavigationTreeCell extends TreeCell<NavigationNode> {

  private final Label label;
  private final DecoratedObjectLabel<CellSystem> cellSystemLabel;
  private final DecoratedObjectLabel<CellCulture> cellCultureWithParentCellSystem;
  private final DecoratedObjectLabel<CellSystem> cellSystemLabelWithPlayButton;
  private final DecoratedObjectLabel<CellCulture> cellCultureLabel;

  public NavigationTreeCell() {
    this.label = new Label();
    this.label.getStyleClass().add(STYLE_CLASS_FOLDER);
    this.cellSystemLabel = new DecoratedObjectLabel<>(
        new CellSystemLabel(true, false, true),
        new ValidationMarkerDecoration());
    this.cellSystemLabelWithPlayButton = new DecoratedObjectLabel<>(
        new CellSystemLabel(true, false, true),
        new PlayButtonDecoration(false),
        new ValidationMarkerDecoration());
    this.cellCultureLabel = new DecoratedObjectLabel<>(
        new CellCultureLabel(true, false, true),
        new PlayButtonDecoration(true),
        new ValidationMarkerDecoration());
    this.cellCultureWithParentCellSystem = new DecoratedObjectLabel<>(
        new CellCultureLabel(true, false, true),
        new PlayButtonDecoration(false),
        new ValidationMarkerDecoration());
  }

  @Override
  protected void updateItem(NavigationNode item, boolean empty) {
    super.updateItem(item, empty);
    if (empty || item == null) {
      setGraphic(null);
    } else {
      setGraphic(initRenderer(item));
    }
  }

  private Node initRenderer(NavigationNode item) {
    switch (item.kind()) {
      case AllCellSystems -> {
        label.setText(Resources.getResource("navigationView.cellSystemsItem"));
        return label;
      }
      case AllCellCultures -> {
        label.setText(Resources.getResource("navigationView.cellCulturesItem"));
        return label;
      }
      case CellSystem -> {
        if (item.parentCellCulture() == null) {
          cellSystemLabel.setObject(item.childCellSystem());
          return cellSystemLabel;
        } else {
          cellSystemLabelWithPlayButton.getDecoration(PlayButtonDecoration.class)
              .ifPresent(decoration -> decoration.setCellCulture(item.parentCellCulture()));
          cellSystemLabelWithPlayButton.setObject(item.childCellSystem());
          return cellSystemLabelWithPlayButton;
        }
      }
      case CellCulture -> {
        if (item.parentCellSystem()==null) {
          cellCultureLabel.setObject(item.childCellCulture());
          return cellCultureLabel;
        }else {
          cellCultureWithParentCellSystem.getDecoration(PlayButtonDecoration.class)
              .ifPresent(decoration -> decoration.setCellSystem(item.parentCellSystem()));
          cellCultureWithParentCellSystem.setObject(item.childCellCulture());
          return cellCultureWithParentCellSystem;
        }
      }
      default -> {
        label.setText("");
        return label;
      }
    }
  }
}
