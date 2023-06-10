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

import static de.hipphampel.cells.model.cellculture.CellCulture.MAX_SIZE;
import static de.hipphampel.cells.model.cellculture.CellCulture.MIN_SIZE;
import static de.hipphampel.cells.model.cellsystem.CellSystem.MAX_CELLTYPE_COUNT;
import static de.hipphampel.cells.model.cellsystem.CellSystem.MIN_CELLTYPE_COUNT;

import de.hipphampel.cells.ServiceLocator;
import de.hipphampel.cells.model.cellculture.CellCulture;
import de.hipphampel.cells.model.cellculture.CellCultureDimensions;
import de.hipphampel.cells.resources.Resources;
import de.hipphampel.validation.core.annotations.BindFacts;
import de.hipphampel.validation.core.annotations.BindRootFacts;
import de.hipphampel.validation.core.annotations.Metadata;
import de.hipphampel.validation.core.annotations.Precondition;
import de.hipphampel.validation.core.annotations.RuleDef;
import de.hipphampel.validation.core.annotations.RuleRef;
import de.hipphampel.validation.core.condition.Conditions;
import de.hipphampel.validation.core.rule.Result;
import de.hipphampel.validation.core.rule.Rule;
import de.hipphampel.validation.core.rule.RuleBuilder;

public class CellCultureRules {

  @RuleRef
  public final Rule<CellCulture> cellCultureRules = RuleBuilder.dispatchingRule("cellCulture",
          CellCulture.class)
      .withPrecondition(Conditions.rule("common:notNull"))
      .forPaths("name").validateWith("cellCulture:name:.*")
      .forPaths("preferredCellSystem").validateWith("cellCulture:preferredCellSystem:.*")
      .build();

  @RuleDef(
      id = "cellCulture:name:unique",
      metadata = @Metadata(key = "warning", value = "true"),
      preconditions = @Precondition(rules = "common:notEmpty"))
  public Result cellCultureNameUnique(@BindRootFacts CellCulture rootFacts, @BindFacts String facts) {
    boolean duplicateName = !ServiceLocator.getCellCultureRepository().hasUniqueName(rootFacts);
    return duplicateName ? Result.failed(Resources.getResource("validation.cellCultureNameNotUnique", facts)) : Result.ok();
  }

  @RuleDef(
      id = "cellCulture:preferredCellSystem:mustExist",
      metadata = @Metadata(key = "warning", value = "true"))
  public Result cellCulturePreferredCellSysteMustExist(@BindRootFacts CellCulture rootFacts, @BindFacts String facts) {
    boolean notExists = ServiceLocator.getCellSystemRepository().getCellTypeCount(facts) == -1;
    return notExists ? Result.failed(Resources.getResource("validation.cellCulturePreferredCellCultureNotExists")) : Result.ok();
  }

  @RuleDef(
      id = "cellCulture:preferredCellSystem:cellTypeCountMustMatch",
      metadata = @Metadata(key = "warning", value = "true"),
      preconditions = @Precondition(rules = "cellCulture:preferredCellSystem:mustExist"))
  public Result cellCulturePreferredCellSystemCellTypeCountMustMatch(@BindRootFacts CellCulture rootFacts, @BindFacts String facts) {
    boolean cellTypeCountMismatch = ServiceLocator.getCellSystemRepository().getCellTypeCount(facts) != rootFacts.getCellTypeCount();
    return cellTypeCountMismatch ? Result.failed(Resources.getResource("validation.cellCulturePreferredCellCultureHasWrongCellTypeCount")) : Result.ok();
  }


  @RuleRef
  public final Rule<CellCultureDimensions> cellCultureDimensionsRules = RuleBuilder.dispatchingRule("cellCultureDimensions",
          CellCultureDimensions.class)
      .withPrecondition(Conditions.rule("common:notNull"))
      .forPaths("width").validateWith("cellCultureDimensions:width:.*")
      .forPaths("height").validateWith("cellCultureDimensions:height:.*")
      .forPaths("cellTypeCount").validateWith("cellCultureDimensions:cellTypeCount:.*")
      .build();

  @RuleDef(
      id = "cellCultureDimensions:width:inRange")
  public Result cellCultureWidthMustBeInRange(@BindFacts int facts) {
    return facts < MIN_SIZE || facts > MAX_SIZE ? Result.failed(
        Resources.getResource("validation.cellCultureWidthNotValid", facts, MIN_SIZE, MAX_SIZE)) : Result.ok();
  }

  @RuleDef(
      id = "cellCultureDimensions:height:inRange")
  public Result cellCultureHeightMustBeInRange(@BindFacts int facts) {
    return facts < MIN_SIZE || facts > MAX_SIZE ? Result.failed(
        Resources.getResource("validation.cellCultureHeightNotValid", facts, MIN_SIZE, MAX_SIZE)) : Result.ok();
  }

  @RuleDef(
      id = "cellCultureDimensions:cellTypeCount:inRange")
  public Result cellCultureCellTypeCountMustBeInRange(@BindFacts int facts) {
    return facts < MIN_CELLTYPE_COUNT || facts > MAX_CELLTYPE_COUNT ? Result.failed(
        Resources.getResource("validation.cellCultureCellTypeCountNotValid", facts, MIN_CELLTYPE_COUNT, MAX_CELLTYPE_COUNT)) : Result.ok();
  }

}
