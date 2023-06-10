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
package de.hipphampel.cells.persistence.entity;

import de.hipphampel.cells.model.cellsystem.CellSystem;
import de.hipphampel.cells.model.cellsystem.CellType;
import de.hipphampel.cells.model.cellsystem.Condition;
import de.hipphampel.cells.model.cellsystem.Neighbourhood;
import de.hipphampel.cells.model.cellsystem.Rule;
import de.hipphampel.cells.model.cellculture.CellCulture;
import javafx.scene.paint.Color;

public class EntityMapper {

  public static CellSystemEntity toCellSystemEntity(CellSystem cellSystem) {
    if (cellSystem == null) {
      return null;
    }
    return new CellSystemEntity(
        cellSystem.getId(),
        cellSystem.getName(),
        cellSystem.getDescription(),
        cellSystem.getNeighbourhood() != null ? cellSystem.getNeighbourhood().getWeights() : null,
        cellSystem.getCellTypes().stream().map(EntityMapper::toCellTypeEntity).toList()
    );
  }

  static CellTypeEntity toCellTypeEntity(CellType cellType) {
    return new CellTypeEntity(
        cellType.getName(),
        cellType.getColorName(),
        cellType.getDefaultCellType(),
        cellType.getNeighbourhood() != null ? cellType.getNeighbourhood().getWeights() : null,
        cellType.getRules().stream().map(EntityMapper::toRuleEntity).toList()
    );
  }

  static RuleEntity toRuleEntity(Rule rule) {
    return new RuleEntity(
        rule.getTargetCellType(),
        rule.getConditions().stream().map(EntityMapper::toConditionEntity).toList()
    );
  }

  static ConditionEntity toConditionEntity(Condition condition) {
    return new ConditionEntity(
        condition.getCellType(),
        condition.getRanges() != null ? condition.getRanges().toString() : null
    );
  }

  public static CellSystem toCellSystem(CellSystemEntity cellSystemEntity) {
    if (cellSystemEntity == null) {
      return null;
    }
    CellSystem cellSystem = new CellSystem();
    cellSystem.setId(cellSystemEntity.id());
    cellSystem.setName(cellSystemEntity.name());
    cellSystem.setDescription(cellSystemEntity.description());
    cellSystem.setNeighbourhood(cellSystemEntity.neighbourhood() != null ? new Neighbourhood(cellSystemEntity.neighbourhood()) : null);
    cellSystemEntity.cellTypes().forEach(ignore -> cellSystem.newCellType());
    for (int i = 0; i < cellSystemEntity.cellTypes().size(); i++) {
      fillCellType(cellSystem.getCellType(i), cellSystemEntity.cellTypes().get(i));
    }
    return cellSystem;
  }

  static void fillCellType(CellType cellType, CellTypeEntity cellTypeEntity) {
    cellType.setName(cellTypeEntity.name());
    cellType.setColor(cellTypeEntity.color() != null ? Color.valueOf(cellTypeEntity.color()) : null);
    cellType.setNeighbourhood(cellTypeEntity.neighbourhood() != null ? new Neighbourhood(cellTypeEntity.neighbourhood()) : null);
    cellType.setDefaultCellType(cellTypeEntity.defaultCellType());
    cellTypeEntity.rules().forEach(ruleEntity -> fillRule(cellType.newRule(), ruleEntity));
  }

  static void fillRule(Rule rule, RuleEntity ruleEntity) {
    rule.setTargetCellType(ruleEntity.targetCellType());
    ruleEntity.conditions().forEach(conditionEntity -> fillCondition(rule.newCondition(), conditionEntity));
  }

  static void fillCondition(Condition condition, ConditionEntity conditionEntity) {
    condition.setCellType(conditionEntity.cellType());
    condition.setRangesString(conditionEntity.ranges());
  }

  public static CellCultureInfoEntity toCellCultureInfoEntity(CellCulture cellCulture, String dataChecksum) {
    if (cellCulture == null) {
      return null;
    }

    return new CellCultureInfoEntity(
        cellCulture.getId(),
        cellCulture.getName(),
        cellCulture.getDescription(),
        cellCulture.getDimensions().width(),
        cellCulture.getDimensions().height(),
        cellCulture.getDimensions().cellTypeCount(),
        cellCulture.isWrapAround(),
        cellCulture.getPreferredCellSystem(),
        dataChecksum);
  }

  public static CellCulture toCellCultureInfo(CellCultureInfoEntity entity) {
    if (entity == null) {
      return null;
    }
    return new CellCulture(
        entity.id(),
        entity.name(),
        entity.description(),
        entity.width(),
        entity.height(),
        entity.cellTypeCount(),
        entity.wrapAround(),
        entity.preferredCellSystem(),
        null);
  }
}
