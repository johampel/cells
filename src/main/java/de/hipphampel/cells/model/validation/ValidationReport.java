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
package de.hipphampel.cells.model.validation;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public record ValidationReport(Severity severity, List<ValidationMessage> messages) {

  public static final ValidationReport EMPTY = new ValidationReport(List.of());

  public ValidationReport(List<ValidationMessage> messages) {
    this(maxSeverity(messages), messages);
  }

  public ValidationReport subReportForPath(String prefix) {
    int prefixLength = prefix.length();
    List<ValidationMessage> subMessages = messages.stream()
        .filter(m -> m.path().startsWith(prefix))
        .map(m -> new ValidationMessage(m.severity(), m.path().substring(prefixLength), m.message()))
        .toList();
    return subMessages.isEmpty() ? EMPTY : new ValidationReport(subMessages);
  }

  public boolean isEmpty() {
    return messages.isEmpty();
  }

  private static Severity maxSeverity(List<ValidationMessage> messages) {
    return messages.stream()
        .map(ValidationMessage::severity)
        .max(Comparator.comparing(Function.identity()))
        .orElse(Severity.INFO);

  }
}
