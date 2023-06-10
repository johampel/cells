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

import static de.hipphampel.cells.ui.common.UiConstants.STYLE_CLASS_INLINE_BUTTON;
import static de.hipphampel.cells.ui.common.UiConstants.STYLE_CLASS_PLAY16;
import static de.hipphampel.cells.ui.common.UiConstants.STYLE_CLASS_PLAY32;

import de.hipphampel.cells.ServiceLocator;
import de.hipphampel.cells.model.cellculture.CellCulture;
import de.hipphampel.cells.model.cellculture.ResolvedCellCulture;
import de.hipphampel.cells.model.cellsystem.CellSystem;
import de.hipphampel.cells.model.validation.Severity;
import de.hipphampel.validation.core.utils.Pair;
import javafx.beans.NamedArg;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Button;

public class PlayButtonDecoration implements ObjectLabelDecoration {

  private final ObjectProperty<CellCulture> cellCulture;
  private final ObjectProperty<CellSystem> cellSystem;
  private final boolean useCultureCellSystem;
  private final Button playButton;
  private final ChangeListener<Object> listener;
  private final WeakChangeListener<Object> weakListener;

  public PlayButtonDecoration(boolean useCultureCellSystem) {
    this(false, useCultureCellSystem);
  }

  public PlayButtonDecoration(@NamedArg("large") boolean large,
      @NamedArg(value = "useCultureCellSystem", defaultValue = "true") boolean useCultureCelLSystem) {
    this.useCultureCellSystem = useCultureCelLSystem;
    this.listener = (ign, oldValue, newValue) -> updatePlayButton();
    this.weakListener = new WeakChangeListener<>(listener);
    this.cellCulture = new SimpleObjectProperty<>(this, "cellCulture");
    this.cellCulture.addListener((ign, oldValue, newValue) -> onCellCultureChanged(oldValue, newValue));
    this.cellSystem = new SimpleObjectProperty<>(this, "cellSystem");
    this.cellSystem.addListener((ign, oldValue, newValue) -> onCellSystemChanged(oldValue, newValue));

    this.playButton = new Button();
    this.playButton.getStyleClass().add(large ? STYLE_CLASS_PLAY32 : STYLE_CLASS_PLAY16);
    this.playButton.getStyleClass().add(STYLE_CLASS_INLINE_BUTTON);
    this.playButton.setOnAction(evt -> onClickPlayButton());
    updatePlayButton();
  }

  @Override
  public Node getNode() {
    return playButton;
  }

  public void setCellCulture(CellCulture cellCulture) {
    this.cellCulture.set(cellCulture);
    if (cellCulture != null && useCultureCellSystem) {
      cellSystem.set(ServiceLocator.getCellSystemRepository().getCellSystem(cellCulture.getPreferredCellSystem()).orElse(null));
    }
    updatePlayButton();
  }

  public void setCellSystem(CellSystem cellSystem) {
    this.cellSystem.set(cellSystem);
    updatePlayButton();
  }

  @Override
  public void onObjectChanged(Object oldValue, Object newValue) {
    if (newValue instanceof CellCulture || oldValue instanceof CellCulture) {
      setCellCulture((CellCulture) newValue);
    } else if (newValue instanceof CellSystem || oldValue instanceof CellSystem) {
      setCellSystem((CellSystem) newValue);
    }
  }

  void onCellCultureChanged(CellCulture oldCellCulture, CellCulture newCellCulture) {
    if (oldCellCulture != null) {
      oldCellCulture.modificationCounterProperty().removeListener(weakListener);
      if (useCultureCellSystem) {
        cellSystem.set(null);
      }
    }
    if (newCellCulture != null) {
      newCellCulture.modificationCounterProperty().addListener(weakListener);
    }
  }

  void onCellSystemChanged(CellSystem oldCellSystem, CellSystem newCellSystem) {
    if (oldCellSystem != null) {
      oldCellSystem.modificationCounterProperty().removeListener(weakListener);
    }
    if (newCellSystem != null) {
      newCellSystem.modificationCounterProperty().addListener(weakListener);
    }
  }

  void onClickPlayButton() {
    Pair<CellCulture, CellSystem> cultureAndSystem = getCultureAndSystem();
    if (cultureAndSystem == null) {
      return;
    }

    CellCulture culture = cultureAndSystem.first();
    CellSystem system = cultureAndSystem.second();
    ServiceLocator.getCellCultureRepository().lazyLoadCultureData(culture);
    ResolvedCellCulture resolvedCellCulture = new ResolvedCellCulture(
        culture.getId() + "-" + system.getId(),
        culture.copy(),
        system.copy());
    UiUtils.createOrSelectViewForPlay(resolvedCellCulture);
  }

  private void updatePlayButton() {
    playButton.setDisable(getCultureAndSystem() == null);
  }


  Pair<CellCulture, CellSystem> getCultureAndSystem() {
    CellCulture culture = cellCulture.get();
    if (culture == null || culture.getValidationReport().severity() == Severity.ERROR) {
      return null;
    }

    CellSystem system = cellSystem.get();
    if (system == null && useCultureCellSystem) {
      cellSystem.set(ServiceLocator.getCellSystemRepository().getCellSystem(culture.getPreferredCellSystem()).orElse(null));
      system = cellSystem.get();
    }
    if (system == null || system.getValidationReport().severity() == Severity.ERROR) {
      return null;
    }

    if (culture.getCellTypeCount() != system.getCellTypeCount()) {
      return null;
    }

    return Pair.of(culture, system);
  }

}
