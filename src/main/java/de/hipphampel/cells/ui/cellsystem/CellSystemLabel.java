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

import static de.hipphampel.cells.ui.common.UiConstants.IMAGE_CELLSYSTEM;
import static de.hipphampel.cells.ui.common.UiConstants.IMAGE_MODIFIED;
import static de.hipphampel.cells.ui.common.UiConstants.IMAGE_NEW;
import static de.hipphampel.cells.ui.common.UiConstants.STYLE_CLASS_CELLS_LARGE;

import de.hipphampel.cells.ServiceLocator;
import de.hipphampel.cells.model.ModificationState;
import de.hipphampel.cells.model.cellsystem.CellSystem;
import de.hipphampel.cells.resources.MessageFormatter;
import de.hipphampel.cells.ui.common.ObjectLabel;
import de.hipphampel.cells.ui.common.UiUtils;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.beans.NamedArg;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;

public class CellSystemLabel extends ObjectLabel<CellSystem> {

  private final boolean large;
  private final boolean showCellTypeCount;
  private final Tooltip tooltip;
  private final ImageView imageView;
  private final InvalidationListener stateListener;
  private final WeakInvalidationListener weakStateListener;

  public CellSystemLabel() {
    this(null, true, false, false);
  }

  public CellSystemLabel(
      @NamedArg(value = "showTooltip", defaultValue = "false") boolean showTooltip,
      @NamedArg(value = "large", defaultValue = "false") boolean large,
      @NamedArg(value = "showCellTypeCount", defaultValue = "false") boolean showCellTypeCount) {
    this(null, showTooltip, large, showCellTypeCount);
  }

  public CellSystemLabel(CellSystem cellSystem) {
    this(cellSystem, true, false, false);
  }

  public CellSystemLabel(CellSystem cellSystem, boolean showTooltip, boolean large, boolean showCellTypeCount) {
    this.large = large;
    this.showCellTypeCount = showCellTypeCount;
    if (large) {
      this.getStyleClass().add(STYLE_CLASS_CELLS_LARGE);
    }
    this.stateListener = this::onModificationStateChanged;
    this.weakStateListener = new WeakInvalidationListener(stateListener);
    this.tooltip = showTooltip ? new Tooltip() : null;
    if (this.tooltip != null) {
      this.tooltip.setPrefWidth(300);
      this.tooltip.setWrapText(true);
    }
    this.imageView = new ImageView();
    setCellSystem(cellSystem);
  }

  @Override
  protected void onObjectChanged(CellSystem oldCellSystem, CellSystem newCellSystem) {
    if (oldCellSystem != null) {
      textProperty().unbind();
      oldCellSystem.modificationStateProperty().removeListener(this.weakStateListener);
      if (tooltip != null) {
        tooltip.textProperty().unbind();
      }
      setGraphic(null);
      setTooltip(null);
      setGraphic(null);
      setText(null);
    }
    if (newCellSystem != null) {
      newCellSystem.modificationStateProperty().addListener(this.weakStateListener);
      if (showCellTypeCount) {
        textProperty().bind(MessageFormatter.format("cellSystemLabel.format",
            newCellSystem.nameProperty(),
            Bindings.size(newCellSystem.cellTypesProperty())));
      } else {
        textProperty().bind(newCellSystem.nameProperty());
      }
      if (tooltip != null) {
        tooltip.textProperty().bind(newCellSystem.descriptionProperty());
      }
      onModificationStateChanged(null);
      setTooltip(tooltip);
      setGraphic(imageView);
    }
  }

  private void onModificationStateChanged(Observable ignore) {
    if (getCellSystem() == null) {
      imageView.setImage(null);
      return;
    }
    List<String> layers = new ArrayList<>();
    layers.add(UiUtils.imageNameFor(IMAGE_CELLSYSTEM, large));
    if (getCellSystem().getModificationState() == ModificationState.TRANSIENT) {
      layers.add(UiUtils.imageNameFor(IMAGE_NEW, large));
    } else if (getCellSystem().getModificationState() == ModificationState.MODIFIED) {
      layers.add(UiUtils.imageNameFor(IMAGE_MODIFIED, large));
    }
    imageView.setImage(ServiceLocator.getImageMerger().getImage(layers));
  }

  public CellSystem getCellSystem() {
    return getObject();
  }

  public ObjectProperty<CellSystem> cellSystemProperty() {
    return objectProperty();
  }

  public void setCellSystem(CellSystem cellSystem) {
    setObject(cellSystem);
  }
}
