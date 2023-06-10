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

import de.hipphampel.cells.ServiceLocator;
import de.hipphampel.cells.model.cellculture.CellCulture;
import de.hipphampel.cells.model.cellculture.CellCultureDimensions;
import de.hipphampel.cells.model.cellsystem.CellSystem;
import de.hipphampel.cells.model.cellsystem.Neighbourhood;
import de.hipphampel.cells.model.validation.ValidationReport;
import de.hipphampel.cells.persistence.repository.CellCultureRepository;
import de.hipphampel.validation.core.Validator;
import de.hipphampel.validation.core.ValidatorBuilder;
import de.hipphampel.validation.core.provider.AggregatingRuleRepository;
import de.hipphampel.validation.core.provider.AnnotationRuleRepository;
import de.hipphampel.validation.core.provider.RuleSelector;
import java.util.Objects;

public class CellsValidator {

  private final Validator validator;

  public CellsValidator() {
    validator = ValidatorBuilder.newBuilder()
        .withEventPublisher(ServiceLocator.getEventPublisher())
        .withRuleRepository(new AggregatingRuleRepository(
            AnnotationRuleRepository.ofClass(CommonRules.class),
            AnnotationRuleRepository.ofInstance(new CellSystemRules()),
            AnnotationRuleRepository.ofInstance(new CellCultureRules())))
        .build();

  }

  public ValidationReport validateCellSystem(CellSystem facts) {
    return validator.validate(ValidationReportReporter::new, facts, RuleSelector.of("cellSystem"));
  }

  public void revalidateCellCulturesForCellSystem(CellSystem facts) {
    CellCultureRepository cellCultureRepository = ServiceLocator.getCellCultureRepository();

    cellCultureRepository.streamCellCultures()
        .filter(cellCulture -> Objects.equals(cellCulture.getPreferredCellSystem(), facts.getId()))
        .forEach(cellCultureRepository::ensureCellCultureValidated);
  }

  public ValidationReport validateNeighbourhood(Neighbourhood facts) {
    return validator.validate(ValidationReportReporter::new, facts, RuleSelector.of("neighbourhood"));
  }

  public ValidationReport validateCellCulture(CellCulture facts) {
    return validator.validate(ValidationReportReporter::new, facts, RuleSelector.of("cellCulture"));
  }

  public ValidationReport validateCellCultureDimensions(CellCultureDimensions facts) {
    return validator.validate(ValidationReportReporter::new, facts, RuleSelector.of("cellCultureDimensions"));
  }

}
