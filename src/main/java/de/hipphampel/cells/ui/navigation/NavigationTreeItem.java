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

import de.hipphampel.cells.ServiceLocator;
import de.hipphampel.cells.ui.navigation.NavigationNode.Kind;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

public class NavigationTreeItem extends TreeItem<NavigationNode> {

  private boolean childrenLoaded;

  public NavigationTreeItem(NavigationNode node) {
    super(node);
    this.childrenLoaded = false;
  }

  @Override
  public boolean isLeaf() {
    return getValue().isLeaf();
  }

  @Override
  public ObservableList<TreeItem<NavigationNode>> getChildren() {
    if (childrenLoaded) {
      return super.getChildren();
    }

    ObservableList<TreeItem<NavigationNode>> children = super.getChildren();
    this.childrenLoaded = true;
    updateChildren(children);
    return children;
  }

  public void onCellSystemUpdate() {
    switch (getValue().kind()) {
      case Root -> getNavigationTreeItemChildren().forEach(NavigationTreeItem::onCellSystemUpdate);
      case AllCellSystems -> updateIfChildrenLoaded();
      case AllCellCultures -> {
        if (childrenLoaded) {
          getNavigationTreeItemChildren().forEach(NavigationTreeItem::onCellSystemUpdate);
        }
      }
      case CellCulture -> {
        if (!isLeaf()) {
          updateIfChildrenLoaded();
        }
      }
      default -> {
      }
    }
  }

  public void onCellCultureUpdate() {
    switch (getValue().kind()) {
      case Root -> getNavigationTreeItemChildren().forEach(NavigationTreeItem::updateIfChildrenLoaded);
      case AllCellCultures -> updateIfChildrenLoaded();
      case AllCellSystems -> {
        if (childrenLoaded) {
          getNavigationTreeItemChildren().forEach(NavigationTreeItem::updateIfChildrenLoaded);
        }
      }
      case CellSystem -> {
        if (!isLeaf()) {
          updateIfChildrenLoaded();
        }
      }
      default -> {
      }
    }
  }

  private void updateIfChildrenLoaded() {
    if (!getValue().isLeaf() || childrenLoaded) {
      childrenLoaded = false;
      getChildren().stream()
          .filter(child -> child instanceof NavigationTreeItem)
          .forEach(child -> ((NavigationTreeItem) child).updateIfChildrenLoaded());
    }
  }

  private void updateChildren(ObservableList<TreeItem<NavigationNode>> children) {
    List<NavigationTreeItem> newChildren = loadChildren();
    Map<String, NavigationTreeItem> existingChildren = getNavigationTreeItemChildren()
        .collect(Collectors.toMap(nti -> nti.getValue().key(), Function.identity()));
    existingChildren.keySet().retainAll(newChildren.stream()
        .map(nti -> getValue().key())
        .collect(Collectors.toSet()));
    children.setAll(newChildren.stream()
        .map(item -> existingChildren.getOrDefault(item.getValue().key(), item))
        .collect(Collectors.toList()));
  }

  private List<NavigationTreeItem> loadChildren() {
    int cellTypeCount = getCellTypeCount();
    return switch (getValue().kind()) {
      case Root -> Stream.of(Kind.AllCellCultures, Kind.AllCellSystems)
          .map(item -> new NavigationTreeItem(new NavigationNode(item, null, null)))
          .toList();
      case AllCellSystems -> ServiceLocator.getCellSystemRepository().streamCellSystems()
          .sorted(Comparator.comparing(cs -> String.valueOf(cs.getName())))
          .map(item -> new NavigationTreeItem(new NavigationNode(Kind.CellSystem, null, item)))
          .toList();
      case AllCellCultures -> ServiceLocator.getCellCultureRepository().streamCellCultures()
          .sorted(Comparator.comparing(cc -> String.valueOf(cc.getName())))
          .map(item -> new NavigationTreeItem(new NavigationNode(Kind.CellCulture, null, item)))
          .toList();
      case CellSystem -> getValue().isLeaf() ? List.of() : ServiceLocator.getCellCultureRepository().streamCellCultures()
          .filter(cellCulture -> cellCulture.getCellTypeCount() == cellTypeCount)
          .sorted(Comparator.comparing(cc -> String.valueOf(cc.getName())))
          .map(item -> new NavigationTreeItem(new NavigationNode(Kind.CellCulture, getValue().child(), item)))
          .toList();
      case CellCulture -> getValue().isLeaf() ? List.of() : ServiceLocator.getCellSystemRepository().streamCellSystems()
          .filter(cellSystem -> cellSystem.getCellTypeCount() == cellTypeCount)
          .sorted(Comparator.comparing(cs -> String.valueOf(cs.getName())))
          .map(item -> new NavigationTreeItem(new NavigationNode(Kind.CellSystem, getValue().child(), item)))
          .toList();
    };
  }

  private Stream<NavigationTreeItem> getNavigationTreeItemChildren() {
    return super.getChildren().stream()
        .map(item -> ((NavigationTreeItem) item));
  }

  private int getCellTypeCount() {
    if (getValue().childCellSystem() != null) {
      return getValue().childCellSystem().getCellTypeCount();
    } else if (getValue().childCellCulture() != null) {
      return getValue().childCellCulture().getCellTypeCount();
    } else {
      return 0;
    }
  }
}
