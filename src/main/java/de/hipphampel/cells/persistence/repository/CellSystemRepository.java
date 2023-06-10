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
package de.hipphampel.cells.persistence.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hipphampel.cells.ServiceLocator;
import de.hipphampel.cells.model.ModificationState;
import de.hipphampel.cells.model.cellsystem.CellSystem;
import de.hipphampel.cells.model.event.Action;
import de.hipphampel.cells.model.event.CellSystemEvent;
import de.hipphampel.cells.persistence.entity.CellSystemEntity;
import de.hipphampel.cells.persistence.entity.EntityMapper;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CellSystemRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(CellSystemRepository.class);
  private static final ObjectMapper objectMapper = new ObjectMapper();

  private final Path rootDir;
  private Map<String, InternalInfo> cellSystems;

  public CellSystemRepository() {
    this(null);
  }

  public CellSystemRepository(Path basePath) {
    this.rootDir = RepositoryUtils.ensureRepositoryPathExists(basePath, "cellSystems");
  }

  public CellSystem ensureCellSystemValidated(CellSystem cellSystem) {
    cellSystem.setValidationReport(ServiceLocator.getValidator().validateCellSystem(cellSystem));
    return cellSystem;
  }

  private void ensureCellSystemsLoaded() {
    if (cellSystems == null) {
      cellSystems = new ConcurrentHashMap<>();
      loadCellSystems();
    }
  }

  private void loadCellSystems() {
    try (Stream<Path> files = Files.list(rootDir)) {
      files
          .filter(Files::isRegularFile)
          .map(this::loadCellSystem)
          .filter(Objects::nonNull)
          .forEach(info -> cellSystems.put(info.cellSystem.getId(), info));
    } catch (IOException ioException) {
      throw new PersistenceException("Failed to read directory '" + rootDir + "'", ioException);
    }
  }

  private InternalInfo loadCellSystem(Path path) {
    try {
      String content = Files.readString(path);
      CellSystem cellSystem = EntityMapper.toCellSystem(objectMapper.readValue(content, CellSystemEntity.class));
      String checksum = generateChecksum(cellSystem);
      return new InternalInfo(
          path,
          checksum,
          cellSystem);
    } catch (IOException e) {
      LOGGER.error("Failed to load cell system from '" + path + "'", e);
      return null;
    }
  }

  private String generateChecksum(CellSystem cellSystem) {
    try {
      return RepositoryUtils.generateChecksum(ByteBuffer.wrap(objectMapper.writeValueAsBytes(EntityMapper.toCellSystemEntity(cellSystem))));
    } catch (JsonProcessingException e) {
      throw new PersistenceException("Failed to calculate digest", e);
    }
  }

  public CellSystem newCellSystem() {
    ensureCellSystemsLoaded();
    String id = UUID.randomUUID().toString();
    CellSystem cellSystem = new CellSystem();
    cellSystem.setId(id);
    cellSystem.setModificationState(ModificationState.TRANSIENT);
    this.cellSystems.put(id, new InternalInfo(null, null, cellSystem));

    ServiceLocator.getEventPublisher().publish(this, new CellSystemEvent(id, Action.New));

    return ensureCellSystemValidated(cellSystem);
  }

  public Stream<CellSystem> streamCellSystems() {
    ensureCellSystemsLoaded();
    return cellSystems.values().stream()
        .map(InternalInfo::cellSystem)
        .map(this::ensureCellSystemValidated);
  }

  public Optional<CellSystem> getCellSystem(String id) {
    ensureCellSystemsLoaded();
    return Optional.ofNullable(id)
        .map(cellSystems::get)
        .map(InternalInfo::cellSystem)
        .map(this::ensureCellSystemValidated);
  }

  public boolean hasUniqueName(CellSystem cellSystem) {
    ensureCellSystemsLoaded();
    return cellSystems.values().stream()
        .map(InternalInfo::cellSystem)
        .filter(cs -> !Objects.equals(cellSystem.getId(), cs.getId()))
        .noneMatch(cs -> Objects.equals(cs.getName(), cellSystem.getName()));
  }

  public int getCellTypeCount(String cellSystemId) {
    if (cellSystemId == null) {
      return -1;
    }
    ensureCellSystemsLoaded();
    return Optional.ofNullable(cellSystems.get(cellSystemId))
        .map(InternalInfo::cellSystem)
        .map(CellSystem::getCellTypeCount)
        .orElse(-1);
  }

  public void saveCellSystem(CellSystem cellSystem) {
    ensureCellSystemsLoaded();
    String id = cellSystem.getId() == null ? UUID.randomUUID().toString() : cellSystem.getId();
    try {
      Path path = Optional.ofNullable(cellSystems.get(id))
          .flatMap(info -> Optional.ofNullable(info.path))
          .orElseGet(() -> rootDir.resolve(id + ".json"));
      objectMapper.writeValue(path.toFile(), EntityMapper.toCellSystemEntity(cellSystem));

      cellSystems.put(id, new InternalInfo(path, generateChecksum(cellSystem), cellSystem));
      cellSystem.setModificationState(ModificationState.UNCHANGED);

      ServiceLocator.getEventPublisher().publish(this, new CellSystemEvent(id, Action.Save));

    } catch (IOException ioe) {
      throw new PersistenceException("Failed to store cell system '" + id + "'", ioe);
    }
  }

  public boolean deleteCellSystem(String id) {
    ensureCellSystemsLoaded();
    InternalInfo info = cellSystems.remove(id);
    if (info != null) {
      if (info.path != null) {
        try {
          Files.delete(info.path);
        } catch (IOException ioe) {
          throw new PersistenceException("Failed to delete cell system '" + id + "'", ioe);
        }
      }
      ServiceLocator.getEventPublisher().publish(this, new CellSystemEvent(id, Action.Delete));
    }

    return info != null;
  }

  public boolean deleteCellSystemIfTransient(String id) {
    ensureCellSystemsLoaded();
    InternalInfo info = cellSystems.get(id);
    if (info != null && info.path == null) {
      return deleteCellSystem(id);
    } else {
      return false;
    }
  }

  public boolean revertCellSystem(CellSystem cellSystem) {
    ensureCellSystemsLoaded();
    if (getCellSystemState(cellSystem) != ModificationState.MODIFIED) {
      return false;
    }

    Path path = Optional.ofNullable(cellSystems.get(cellSystem.getId()))
        .map(InternalInfo::path)
        .orElseThrow(() -> new PersistenceException("Unable to determine path of cell system '" + cellSystem.getId() + "'"));
    CellSystem result = Optional.ofNullable(loadCellSystem(path))
        .map(info -> cellSystem.copyFrom(info.cellSystem))
        .map(this::ensureCellSystemValidated)
        .orElse(null);
    if (result != null) {
      ServiceLocator.getEventPublisher().publish(this, new CellSystemEvent(cellSystem.getId(), Action.Modify));
    }
    return result != null;
  }

  public ModificationState getCellSystemState(CellSystem cellSystem) {
    ensureCellSystemsLoaded();
    String id = cellSystem.getId();
    if (id == null) {
      return ModificationState.UNKNOWN;
    }
    InternalInfo info = cellSystems.get(id);
    if (info == null) {
      return ModificationState.UNKNOWN;
    }
    if (info.path == null) {
      return ModificationState.TRANSIENT;
    }
    return Objects.equals(generateChecksum(cellSystem), info.checksum) ? ModificationState.UNCHANGED : ModificationState.MODIFIED;
  }

  private record InternalInfo(Path path, String checksum, CellSystem cellSystem) {

  }

}
