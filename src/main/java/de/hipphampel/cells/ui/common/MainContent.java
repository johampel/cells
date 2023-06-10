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

import static de.hipphampel.cells.ui.common.UiConstants.TAG_CONTENT;
import static de.hipphampel.cells.ui.common.UiConstants.TAG_MAIN_CONTENT;
import static de.hipphampel.cells.ui.common.UiConstants.TAG_NAVIGATION;

import de.hipphampel.cells.ui.navigation.NavigationView;
import de.hipphampel.mv4fx.view.ViewGroupContainer;
import java.util.Set;
import javafx.geometry.Orientation;
import javafx.geometry.Side;

public class MainContent extends ViewGroupContainer {

  public MainContent() {
    this.getStylesheets().add(FXMLUtils.getCellsCss());
    this.setOrientation(Orientation.HORIZONTAL);
    this.setPosition(0.2);

    CellsViewGroup navigationViewGroup = new CellsViewGroup();
    navigationViewGroup.setKind(TAG_NAVIGATION);
    navigationViewGroup.setDragTags(Set.of(TAG_NAVIGATION));
    navigationViewGroup.setDropTags(Set.of(TAG_NAVIGATION));
    navigationViewGroup.setDropSplitSides(Set.of(Side.TOP, Side.BOTTOM));
    navigationViewGroup.addAndSelectView(new NavigationView());

    CellsViewGroup contentViewGroup = new CellsViewGroup();
    contentViewGroup.setKind(TAG_MAIN_CONTENT);
    contentViewGroup.setDragTags(Set.of(TAG_MAIN_CONTENT));
    contentViewGroup.setDropTags(Set.of(TAG_MAIN_CONTENT, TAG_CONTENT));

    this.setLeftTop(navigationViewGroup);
    this.setRightBottom(contentViewGroup);
  }
}
