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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javafx.beans.NamedArg;
import javafx.beans.property.ObjectProperty;
import javafx.scene.layout.HBox;

public class DecoratedObjectLabel<T> extends HBox {

  private final ObjectLabel<T> label;
  private final List<ObjectLabelDecoration> decorations;

  public DecoratedObjectLabel(ObjectLabel<T> label, ObjectLabelDecoration... decorations) {
    this(label, Arrays.asList(decorations));
  }

  public DecoratedObjectLabel(@NamedArg("label") ObjectLabel<T> label, @NamedArg("decorations") List<ObjectLabelDecoration> decorations) {
    this.label = label;
    this.decorations = decorations;
    label.objectProperty().addListener((ignore, oldValue, newValue) -> onObjectChanged(oldValue, newValue));

    getChildren().add(label);
    for (ObjectLabelDecoration decoration : decorations) {
      decoration.onObjectChanged(null, label.getObject());
      getChildren().add(decoration.getNode());
    }
    setSpacing(5);
  }

  public ObjectLabel<T> getLabel() {
    return label;
  }

  public <D extends ObjectLabelDecoration> Optional<D> getDecoration(Class<D> decorationType) {
    return decorations.stream()
        .filter(decorationType::isInstance)
        .map(decorationType::cast)
        .findFirst();
  }

  public T getObject() {
    return label.getObject();
  }

  public ObjectProperty<T> objectProperty() {
    return label.objectProperty();
  }

  public void setObject(T object) {
    label.setObject(object);
  }

  private void onObjectChanged(Object oldValue, Object newValue) {
    for (ObjectLabelDecoration decoration : decorations) {
      decoration.onObjectChanged(oldValue, newValue);
    }
  }
}
