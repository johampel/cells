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
package de.hipphampel.cells.validation;

import de.hipphampel.cells.model.validation.Severity;
import de.hipphampel.cells.model.validation.ValidationMessage;
import de.hipphampel.cells.model.validation.ValidationReport;
import de.hipphampel.validation.core.report.AbstractReportBasedReporter;
import de.hipphampel.validation.core.report.Report;
import de.hipphampel.validation.core.report.ReportEntry;
import java.util.List;

public class ValidationReportReporter extends AbstractReportBasedReporter<ValidationReport> {

  private static final String METADATA_WARNING = "warning";

  public ValidationReportReporter(Object facts) {
    super(facts, true, entry -> entry.result().isFailed() && entry.result().reason() != null);
  }

  @Override
  protected ValidationReport buildReport(Report report) {
    List<ValidationMessage> messages = report.entries().stream()
        .map(this::toValidationMessage)
        .toList();
    return new ValidationReport(messages);
  }

  private ValidationMessage toValidationMessage(ReportEntry entry) {
    String message = String.valueOf(entry.result().reason());
    Severity severity = entry.rule().getMetadata().containsKey(METADATA_WARNING) ? Severity.WARNING : Severity.ERROR;
    return new ValidationMessage(severity, entry.path().toString(), message);
  }
}
