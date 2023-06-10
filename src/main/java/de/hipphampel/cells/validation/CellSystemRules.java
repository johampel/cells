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
import de.hipphampel.cells.model.ModificationState;
import de.hipphampel.cells.model.cellsystem.CellSystem;
import de.hipphampel.cells.model.cellsystem.CellType;
import de.hipphampel.cells.model.cellsystem.Condition;
import de.hipphampel.cells.model.cellsystem.Neighbourhood;
import de.hipphampel.cells.model.cellsystem.Ranges;
import de.hipphampel.cells.model.cellsystem.Rule;
import de.hipphampel.cells.resources.Resources;
import de.hipphampel.validation.core.annotations.BindFacts;
import de.hipphampel.validation.core.annotations.BindParentFacts;
import de.hipphampel.validation.core.annotations.BindRootFacts;
import de.hipphampel.validation.core.annotations.Metadata;
import de.hipphampel.validation.core.annotations.Precondition;
import de.hipphampel.validation.core.annotations.RuleDef;
import de.hipphampel.validation.core.annotations.RuleRef;
import de.hipphampel.validation.core.condition.Conditions;
import de.hipphampel.validation.core.rule.Result;
import de.hipphampel.validation.core.rule.RuleBuilder;
import java.util.List;
import java.util.Objects;

public class CellSystemRules {

  @RuleRef
  public final de.hipphampel.validation.core.rule.Rule<CellSystem> cellSystemRules = RuleBuilder.dispatchingRule("cellSystem",
          CellSystem.class)
      .withPrecondition(Conditions.rule("common:notNull"))
      .forPaths("name").validateWith("cellSystem:name:.*")
      .forPaths("neighbourhood").validateWith("common:notNull", "neighbourhood")
      .forPaths("cellTypes").validateWith("cellSystem:cellTypes:.*")
      .forPaths("cellTypes/*").validateWith("cellType")
      .build();

  @RuleDef(
      id = "cellSystem:name:unique",
      metadata = @Metadata(key = "warning", value = "true"),
      preconditions = @Precondition(rules = "common:notEmpty"))
  public Result cellSystemNameUnique(@BindRootFacts CellSystem rootFacts, @BindFacts String facts) {
    boolean duplicateName = !ServiceLocator.getCellSystemRepository().hasUniqueName(rootFacts);
    return duplicateName ? Result.failed(Resources.getResource("validation.cellSystemNameNotUnique", facts)) : Result.ok();
  }

  @RuleDef(
      id = "cellSystem:cellTypes:atLeastTwoPresent",
      preconditions = @Precondition(rules = "common:notNull"))
  public Result cellSystemCellTypesAtLeastTwoPresent(@BindFacts List<CellType> facts) {
    boolean lessThanTwoCellTypes = facts.size() < CellSystem.MIN_CELLTYPE_COUNT;
    return lessThanTwoCellTypes ? Result.failed(Resources.getResource("validation.cellSystemHasLessThanTwoCellTypes")) : Result.ok();
  }

  @RuleDef(
      id = "cellSystem:cellTypes:atMostMaxCountPresent",
      preconditions = @Precondition(rules = "common:notNull"))
  public Result cellSystemCellTypesAtMost255Present(@BindFacts List<CellType> facts) {
    boolean tooMany = facts.size() > CellSystem.MAX_CELLTYPE_COUNT;
    return tooMany ? Result.failed(Resources.getResource("validation.cellSystemHasTooManyCellTypes", CellSystem.MAX_CELLTYPE_COUNT))
        : Result.ok();
  }

//  @RuleDef(
//      id = "cellSystem:cellTypes:countMustNotChangeIfUsed",
//      preconditions = @Precondition(rules = "common:notNull"))
//  public Result cellSystemCellTypeCountMustNotChangeIfInUse(@BindRootFacts CellSystem cellSystem) {
//    int count = cellSystem.getCellTypeCount();
//    boolean countMismatch = ServiceLocator.getCellCultureRepository().streamCellCultures()
//        .filter(cellCulture -> cellCulture.getModificationState() == ModificationState.UNCHANGED)
//        .filter(cellCulture -> Objects.equals(cellCulture.getPreferredCellSystem(), cellSystem.getId()))
//        .anyMatch(cellCulture -> cellCulture.getCellTypeCount() != count);
//    return countMismatch ? Result.failed("CRASH!") : Result.ok();
//  }

  @RuleRef
  public final de.hipphampel.validation.core.rule.Rule<CellType> cellTypeRules = RuleBuilder.dispatchingRule("cellType", CellType.class)
      .forPaths("id").validateWith("cellType:id:.*")
      .forPaths("name").validateWith("cellType:name:.*")
      .forPaths("color").validateWith("common:notNull")
      .forPaths("neighbourhood").validateWith("neighbourhood", "cellType:neighbourhood:.*")
      .forPaths("rules/*").validateWith("rule")
      .forPaths("defaultCellType").validateWith("cellType:defaultCellType:.*")
      .build();


  @RuleDef(
      id = "cellType:neighbourhood:shouldBeDifferent",
      metadata = @Metadata(key = "warning", value = "true"))
  public Result neighbourhoodOfCellTypeShouldNotBeTheSameOfCellSystem(@BindRootFacts CellSystem cellSystem,
      @BindParentFacts CellType cellType, @BindFacts Neighbourhood neighbourhood) {
    if (!Objects.equals(cellSystem.getNeighbourhood(), neighbourhood)) {
      return Result.ok();
    }
    return Result.failed(Resources.getResource("validation.cellTypeNeighbourhoodIsSameAsCellSystemNeighbourhood"));
  }


  @RuleDef(
      id = "cellType:id:idIsSameAsIndexInCellSystem",
      preconditions = @Precondition(rules = "cellTypeIdIsValid"))
  public final Result cellTypeIsSameAsIndexInCellSystem(@BindRootFacts CellSystem cellSystem, @BindParentFacts CellType cellType,
      @BindFacts Integer cellTypeId) {
    boolean failed = cellSystem.getCellType(cellTypeId) != cellType;
    return failed ? Result.failed(Resources.getResource("validation.cellTypeIdIndexMismatch")) : Result.ok();
  }

  @RuleDef(
      id = "cellType:name:nameShouldBeUnique",
      metadata = @Metadata(key = "warning", value = "true"),
      preconditions = @Precondition(rules = "common:notEmpty"))
  public final Result cellTypeNameShouldBeUnique(@BindRootFacts CellSystem cellSystem, @BindParentFacts CellType cellType,
      @BindFacts String name) {
    boolean failed = cellSystem.getCellTypes().stream()
        .filter(ct -> Objects.equals(name, ct.getName()))
        .count() > 1;
    return failed ? Result.failed(Resources.getResource("validation.cellTypeNameNotUnique", name)) : Result.ok();
  }

  @RuleDef(
      id = "cellTypeIdIsValid")
  public final Result cellTypeIdIsValid(@BindRootFacts CellSystem cellSystem, Integer cellTypeId) {
    boolean failed = cellTypeId < 0 || cellTypeId >= cellSystem.getCellTypeCount();
    return failed ? Result.failed(Resources.getResource("validation.cellTypeIdNotValid", cellTypeId, cellSystem.getCellTypeCount() - 1))
        : Result.ok();
  }

  @RuleRef
  public final de.hipphampel.validation.core.rule.Rule<Neighbourhood> neighbourhoodRules = RuleBuilder.dispatchingRule("neighbourhood",
          Neighbourhood.class)
      .forPaths("radius").validateWith("neighbourhood:radius:.*")
      .forPaths("weights").validateWith("neighbourhood:weights:.*")
      .build();

  @RuleDef(
      id = "neighbourhood:radius:inAllowedRange")
  public Result neighbourhoodRadiusInAllowedRange(@BindFacts Integer facts) {
    return facts < Neighbourhood.MIN_RADIUS || facts > Neighbourhood.MAX_RADIUS ? Result.failed(
        Resources.getResource("validation.neighbourhoodRadiusLessNotInAllowedRange", Neighbourhood.MIN_RADIUS, Neighbourhood.MAX_RADIUS,
            facts)) : Result.ok();
  }

  @RuleDef(
      id = "neighbourhood:weights:notNegative")
  public Result neighborhoodWeightsNotNegative(@BindFacts List<Integer> facts) {
    boolean negativeWeight = facts.stream()
        .anyMatch(weight -> weight < 0);
    return negativeWeight ? Result.failed(Resources.getResource("validation.neighbourhoodWeightNegative")) : Result.ok();
  }

  @RuleDef(
      id = "neighbourhood:weights:centerNotZero")
  public Result neighborhoodWeightsCenterNotZero(@BindParentFacts Neighbourhood parentFacts) {
    boolean centerNotZero = parentFacts.getWeightAt(0, 0) != 0;
    return centerNotZero ? Result.failed(Resources.getResource("validation.neighbourhoodWeightCenterNotZero")) : Result.ok();
  }

  @RuleDef(
      id = "neighbourhood:weights:notAllZero")
  public Result neighborhoodWeightsNotAllZero(@BindFacts List<Integer> facts) {
    boolean allZeroWeights = facts.size() > 1 && facts.stream()
        .allMatch(weight -> weight == 0);
    return allZeroWeights ? Result.failed(Resources.getResource("validation.neighbourhoodWeightsAllZero")) : Result.ok();
  }

  @RuleRef
  public final de.hipphampel.validation.core.rule.Rule<Rule> ruleRule = RuleBuilder.dispatchingRule("rule", Rule.class)
      .forPaths("targetCellType").validateWith("cellTypeIdIsValid")
      .forPaths("conditions").validateWith("rule:conditions:.*")
      .forPaths("conditions/*").validateWith("condition")
      .build();

  @RuleDef(
      id = "rule:conditions:notEmpty",
      metadata = @Metadata(key = "warning", value = "true"))
  public final Result cellTypeNameShouldBeUnique(@BindFacts List<Condition> conditions) {
    boolean failed = conditions == null || conditions.isEmpty();
    return failed ? Result.failed(Resources.getResource("validation.conditionsShouldNotBeEmpty")) : Result.ok();
  }

  @RuleRef
  public final de.hipphampel.validation.core.rule.Rule<Condition> conditionRule = RuleBuilder.dispatchingRule("condition", Condition.class)
      .forPaths("cellType").validateWith("cellTypeIdIsValid")
      .forPaths("ranges").validateWith("condition:ranges:.*")
      .build();

  @RuleDef(
      id = "condition:ranges:notMatchesAll",
      metadata = @Metadata(key = "warning", value = "true"),
      preconditions = @Precondition(rules = "common:notEmpty"))
  public final Result conditionRangesShouldNotMatchAll(@BindFacts Ranges ranges) {
    boolean failed = ranges.toString().equals("0-");
    return failed ? Result.failed(Resources.getResource("validation.rangeShouldNotContainAllWeights")) : Result.ok();
  }

}
