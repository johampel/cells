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

import static de.hipphampel.cells.ui.common.UiConstants.ICON_LARGE_SIZE;
import static de.hipphampel.cells.ui.common.UiConstants.ICON_NORMAL_SIZE;
import static de.hipphampel.cells.ui.common.UiConstants.TAG_MAIN_CONTENT;

import de.hipphampel.cells.model.cellculture.CellCulture;
import de.hipphampel.cells.model.cellculture.ResolvedCellCulture;
import de.hipphampel.cells.model.cellsystem.CellSystem;
import de.hipphampel.cells.ui.cellculture.CellCultureView;
import de.hipphampel.cells.ui.cellsystem.CellSystemView;
import de.hipphampel.cells.ui.cellculture.PlayView;
import de.hipphampel.mv4fx.view.View;
import de.hipphampel.mv4fx.view.ViewGroup;
import de.hipphampel.mv4fx.view.ViewManager;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class UiUtils {

  public static String imageNameFor(String baseName, boolean large) {
    return String.format("/img/%s%d.png", baseName, large ? ICON_LARGE_SIZE : ICON_NORMAL_SIZE);
  }

  public static View createOrSelectViewForCellSystem(CellSystem cellSystem) {
    View view = findViewForCellSystem(cellSystem).orElse(null);
    if (view != null && view.getViewGroup() != null) {
      view.getViewGroup().selectView(view);
      return view;
    }

    view = new CellSystemView(cellSystem);
    findMainContentViewGroup().orElseThrow().addAndSelectView(view);
    return view;
  }

  public static View createOrSelectViewForCellCulture(CellCulture cellCulture) {
    View view = findViewForCellCulture(cellCulture).orElse(null);
    if (view != null && view.getViewGroup() != null) {
      view.getViewGroup().selectView(view);
      return view;
    }

    view = new CellCultureView(cellCulture);
    findMainContentViewGroup().orElseThrow().addAndSelectView(view);
    return view;
  }

  public static View createOrSelectViewForPlay(ResolvedCellCulture cellCulture) {
    View view = findViewForPlay(cellCulture).orElse(null);
    if (view != null && view.getViewGroup() != null) {
      view.getViewGroup().selectView(view);
      return view;
    }

    view = new PlayView(cellCulture);
    findMainContentViewGroup().orElseThrow().addAndSelectView(view);
    return view;
  }

  public static Optional<View> findViewForPlay(ResolvedCellCulture cellCulture) {
    return ViewManager.getAllViews()
        .filter(v -> v instanceof PlayView pv && pv.getCellCulture() != null && Objects.equals(pv.getCellCulture().id(),
            cellCulture.id()))
        .findFirst();
  }

  public static Optional<View> findViewForCellSystem(CellSystem cellSystem) {
    return ViewManager.getAllViews()
        .filter(v -> v instanceof CellSystemView csv && csv.getCellSystem() != null && Objects.equals(csv.getCellSystem().getId(),
            cellSystem.getId()))
        .findFirst();
  }

  public static Optional<View> findViewForCellCulture(CellCulture cellCulture) {
    return ViewManager.getAllViews()
        .filter(v -> v instanceof CellCultureView ccv && ccv.getCellCulture() != null && Objects.equals(ccv.getCellCulture().getId(),
            cellCulture.getId()))
        .findFirst();
  }

  public static Optional<ViewGroup> findMainContentViewGroup() {
    return getFirstOfKind(TAG_MAIN_CONTENT);
  }

  public static Stream<ViewGroup> getViewGroupsWithDropTags(Set<String> dropTags) {
    return ViewManager.getAllViewGroups()
        .filter(vg -> vg.getDropTags().containsAll(dropTags));
  }

  public static Optional<ViewGroup> getFirstOfKind(String kind) {
    return getViewGroupsByKind(kind).findFirst();
  }

  public static Stream<ViewGroup> getViewGroupsByKind(String kind) {
    return ViewManager.getAllViewGroups()
        .filter(vg -> (vg instanceof CellsViewGroup cvg) && Objects.equals(cvg.getKind(), kind));
  }
}
