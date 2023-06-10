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
package de.hipphampel.cells.model;

import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableObjectValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class FallbackBinding<T> extends ObjectBinding<T> {

  private final ObservableObjectValue<T> primary;
  private final ObservableObjectValue<T> fallback;
  private final InvalidationListener listener;

  public FallbackBinding(ObservableObjectValue<T> primary, ObservableObjectValue<T> fallback) {
    this.listener = observable -> {
      if (observable==primary || (isValid() && primary.get()==null)) {
        invalidate();
      }
    };
    this.primary = primary;
    this.primary.addListener(listener);
    this.fallback = fallback;
    this.fallback.addListener(listener);
  }


  @Override
  protected T computeValue() {
    return primary.get() == null ? fallback.get() : primary.get();
  }

  @Override
  public void dispose() {
    primary.removeListener(listener);
    fallback.removeListener(listener);
    super.dispose();
  }

  @Override
  public ObservableList<?> getDependencies() {
    return FXCollections.observableList(List.of(primary, fallback));
  }

}
