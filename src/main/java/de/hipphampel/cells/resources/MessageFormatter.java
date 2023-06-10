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
package de.hipphampel.cells.resources;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.binding.StringExpression;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


// Adapted from com.sun.javafx.binding.StringFormatter
public abstract class MessageFormatter extends StringBinding {


  private static Object extractValue(Object obj) {
    return obj instanceof ObservableValue ? ((ObservableValue<?>) obj).getValue() : obj;
  }

  private static Object[] extractValues(Object[] objs) {
    final int n = objs.length;
    final Object[] values = new Object[n];
    for (int i = 0; i < n; i++) {
      values[i] = extractValue(objs[i]);
    }
    return values;
  }

  private static ObservableValue<?>[] extractDependencies(Object... args) {
    final List<ObservableValue<?>> dependencies = new ArrayList<ObservableValue<?>>();
    for (final Object obj : args) {
      if (obj instanceof ObservableValue) {
        dependencies.add((ObservableValue<?>) obj);
      }
    }
    return dependencies.toArray(new ObservableValue[dependencies.size()]);
  }

  public static StringExpression convert(final ObservableValue<?> observableValue) {
    if (observableValue == null) {
      throw new NullPointerException("ObservableValue must be specified");
    }
    if (observableValue instanceof StringExpression) {
      return (StringExpression) observableValue;
    } else {
      return new StringBinding() {
        {
          super.bind(observableValue);
        }

        @Override
        public void dispose() {
          super.unbind(observableValue);
        }

        @Override
        protected String computeValue() {
          final Object value = observableValue.getValue();
          return (value == null) ? "null" : value.toString();
        }

        @Override
        public ObservableList<ObservableValue<?>> getDependencies() {
          return FXCollections.<ObservableValue<?>>singletonObservableList(observableValue);
        }
      };
    }
  }

  public static StringExpression format(final String format, final Object... args) {
    if (format == null) {
      throw new NullPointerException("Format cannot be null.");
    }
    if (extractDependencies(args).length == 0) {
      return Bindings.format(format, args);
    }
    final MessageFormatter formatter = new MessageFormatter() {
      {
        super.bind(extractDependencies(args));
      }

      @Override
      public void dispose() {
        super.unbind(extractDependencies(args));
      }

      @Override
      protected String computeValue() {
        final Object[] values = extractValues(args);
        return Resources.getResource(format, values);
      }

      @Override
      public ObservableList<ObservableValue<?>> getDependencies() {
        return FXCollections.unmodifiableObservableList(FXCollections
            .observableArrayList(extractDependencies(args)));
      }
    };
    // Force calculation to check format
    formatter.get();
    return formatter;
  }

}
