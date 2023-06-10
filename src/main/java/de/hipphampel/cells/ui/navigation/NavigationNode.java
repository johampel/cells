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

import de.hipphampel.cells.model.cellculture.CellCulture;
import de.hipphampel.cells.model.cellsystem.CellSystem;

public record NavigationNode(Kind kind, Object parent, Object child) {

  public enum Kind {
    Root,
    AllCellSystems,
    AllCellCultures,
    CellCulture,
    CellSystem,
  }

  public String key() {
    String parentKey = parent instanceof CellSystem cs ? cs.getId() : parent instanceof CellCulture cc ? cc.getId() : "";
    String childKey = child instanceof CellSystem cs ? cs.getId() : child instanceof CellCulture cc ? cc.getId() : "";
    return kind + "-" + parentKey + "-" + childKey;
  }


  public boolean isLeaf() {
    return (parent instanceof CellCulture || parent instanceof CellSystem) && (child instanceof CellCulture || child instanceof CellSystem);
  }

  public CellSystem parentCellSystem() {
    return parent instanceof CellSystem cs ? cs : null;
  }

  public CellSystem childCellSystem() {
    return child instanceof CellSystem cs ? cs : null;
  }

  public CellCulture parentCellCulture() {
    return parent instanceof CellCulture cc ? cc : null;
  }

  public CellCulture childCellCulture() {
    return child instanceof CellCulture cc ? cc : null;
  }
}
