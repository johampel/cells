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
import de.hipphampel.array2dops.model.Byte2DArray;
import de.hipphampel.cells.ServiceLocator;
import de.hipphampel.cells.model.ModificationState;
import de.hipphampel.cells.model.cellculture.CellCulture;
import de.hipphampel.cells.model.cellculture.CellCultureDimensions;
import de.hipphampel.cells.model.event.Action;
import de.hipphampel.cells.model.event.CellCultureEvent;
import de.hipphampel.cells.persistence.entity.CellCultureInfoEntity;
import de.hipphampel.cells.persistence.entity.EntityMapper;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CellCultureRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(CellSystemRepository.class);
  private static final ObjectMapper objectMapper = new ObjectMapper();

  private final Path rootDir;
  private Map<String, InternalInfo> cellCultures;

  public CellCultureRepository() {
    this(null);
  }

  public CellCultureRepository(Path basePath) {
    this.rootDir = RepositoryUtils.ensureRepositoryPathExists(basePath, "cellCultures");
  }

  public CellCulture ensureCellCultureValidated(CellCulture cellCulture) {
    cellCulture.setValidationReport(ServiceLocator.getValidator().validateCellCulture(cellCulture));
    return cellCulture;
  }

  private void ensureCellCultureInfosLoaded() {
    if (cellCultures == null) {
      cellCultures = new ConcurrentHashMap<>();
      loadCellCultureInfos();
    }
  }

  private void loadCellCultureInfos() {
    try (Stream<Path> files = Files.list(rootDir)) {
      files
          .filter(Files::isRegularFile)
          .filter(p -> p.toString().endsWith(".json"))
          .map(this::loadCellCulture)
          .filter(Objects::nonNull)
          .forEach(info -> cellCultures.put(info.cellCulture.getId(), info));
    } catch (IOException ioException) {
      throw new PersistenceException("Failed to read directory '" + rootDir + "'", ioException);
    }
  }

  private InternalInfo loadCellCulture(Path path) {
    try {
      String content = Files.readString(path);
      CellCultureInfoEntity entity = objectMapper.readValue(content, CellCultureInfoEntity.class);
      CellCulture cellCulture = EntityMapper.toCellCultureInfo(entity);
      return new InternalInfo(
          path,
          generateInfoChecksum(cellCulture, entity.dataChecksum()),
          entity.dataChecksum(),
          cellCulture);
    } catch (IOException e) {
      LOGGER.error("Failed to load cell culture from '" + path + "'", e);
      return null;
    }
  }

  public CellCulture newCellCulture() {
    ensureCellCultureInfosLoaded();
    String id = UUID.randomUUID().toString();
    CellCulture cellCulture = new CellCulture();
    cellCulture.setId(id);
    cellCulture.setModificationState(ModificationState.TRANSIENT);
    cellCulture.setDimensions(new CellCultureDimensions(100, 100, 2));
    cellCulture.setData(Byte2DArray.newInstance(100, 100));
    String dataChecksum = generateDataChecksum(cellCulture);
    this.cellCultures.put(id, new InternalInfo(null, generateInfoChecksum(cellCulture, dataChecksum), dataChecksum, cellCulture));

    ServiceLocator.getEventPublisher().publish(this, new CellCultureEvent(id, Action.New));

    return ensureCellCultureValidated(cellCulture);
  }


  public Stream<CellCulture> streamCellCultures() {
    ensureCellCultureInfosLoaded();
    return cellCultures.values().stream()
        .map(CellCultureRepository.InternalInfo::cellCulture)
        .map(this::ensureCellCultureValidated);
  }

  public void lazyLoadCultureData(CellCulture cellCulture) {
    ensureCellCultureInfosLoaded();
    if (cellCulture.getData() != null) {
      return;
    }

    Path dataPath = getDataPath(cellCulture.getId());

    try {
      byte[] data = Files.readAllBytes(dataPath);
      cellCulture.setData(Byte2DArray.newInstance(data, cellCulture.getWidth(), cellCulture.getHeight()));
    } catch (IOException ioe) {
      throw new PersistenceException("Failed to store cell culture '" + cellCulture.getId() + "'", ioe);
    }
  }

  public boolean hasUniqueName(CellCulture cellCulture) {
    ensureCellCultureInfosLoaded();
    return cellCultures.values().stream()
        .map(InternalInfo::cellCulture)
        .filter(cs -> !Objects.equals(cellCulture.getId(), cs.getId()))
        .noneMatch(cs -> Objects.equals(cs.getName(), cellCulture.getName()));
  }

  public boolean deleteCellCulture(String id) {
    ensureCellCultureInfosLoaded();
    InternalInfo info = cellCultures.remove(id);
    if (info != null) {
      if (info.path != null) {
        try {
          Files.deleteIfExists(getInfoPath(id));
          Files.deleteIfExists(getDataPath(id));
        } catch (IOException ioe) {
          throw new PersistenceException("Failed to delete cell culture '" + id + "'", ioe);
        }
      }
      ServiceLocator.getEventPublisher().publish(this, new CellCultureEvent(id, Action.Delete));
    }

    return info != null;
  }

  public boolean deleteCellCultureIfTransient(String id) {
    ensureCellCultureInfosLoaded();
    InternalInfo info = cellCultures.get(id);
    if (info != null && info.path == null) {
      return deleteCellCulture(id);
    } else {
      return false;
    }
  }


  public boolean revertCellCulture(CellCulture cellCulture) {
    ensureCellCultureInfosLoaded();
    if (getCellCultureState(cellCulture) != ModificationState.MODIFIED) {
      return false;
    }

    Path infoPath = getInfoPath(cellCulture.getId());
    InternalInfo info = loadCellCulture(infoPath);
    if (info == null) {
      return false;
    }
    lazyLoadCultureData(info.cellCulture);
    cellCulture.copyFrom(info.cellCulture);
    ensureCellCultureValidated(cellCulture);
    ServiceLocator.getEventPublisher().publish(this, new CellCultureEvent(cellCulture.getId(), Action.Modify));
    return true;
  }

  public void saveCellCulture(CellCulture cellCulture) {
    ensureCellCultureInfosLoaded();
    String id = cellCulture.getId();
    lazyLoadCultureData(cellCulture);

    try {
      Path infoPath = getInfoPath(id);
      Path dataPath = getDataPath(id);

      try (FileOutputStream file = new FileOutputStream(dataPath.toFile())) {
        file.getChannel().write(cellCulture.getData().toByteBuffer(false));
      }
      String dataChecksum = generateDataChecksum(cellCulture);
      objectMapper.writeValue(infoPath.toFile(), EntityMapper.toCellCultureInfoEntity(cellCulture, dataChecksum));

      cellCultures.put(id, new InternalInfo(infoPath, generateInfoChecksum(cellCulture, dataChecksum), dataChecksum, cellCulture));
      cellCulture.setModificationState(ModificationState.UNCHANGED);

      ServiceLocator.getEventPublisher().publish(this, new CellCultureEvent(id, Action.Save));

    } catch (IOException ioe) {
      throw new PersistenceException("Failed to store cell cellculture '" + id + "'", ioe);
    }
  }

  public ModificationState getCellCultureState(CellCulture cellCulture) {
    ensureCellCultureInfosLoaded();
    String id = cellCulture.getId();
    if (id == null) {
      return ModificationState.UNKNOWN;
    }
    InternalInfo info = cellCultures.get(id);
    if (info == null) {
      return ModificationState.UNKNOWN;
    }
    if (info.path == null) {
      return ModificationState.TRANSIENT;
    }

    String dataChecksum = cellCulture.getData() == null ? info.dataChecksum : generateDataChecksum(cellCulture);
    return Objects.equals(generateInfoChecksum(cellCulture, dataChecksum), info.infoChecksum) &&
        Objects.equals(dataChecksum, info.dataChecksum) ? ModificationState.UNCHANGED : ModificationState.MODIFIED;
  }

  private String generateInfoChecksum(CellCulture cellCulture, String dataChecksum) {
    try {
      return RepositoryUtils.generateChecksum(
          ByteBuffer.wrap(objectMapper.writeValueAsBytes(EntityMapper.toCellCultureInfoEntity(cellCulture, dataChecksum))));
    } catch (JsonProcessingException e) {
      throw new PersistenceException("Failed to calculate digest", e);
    }
  }

  private String generateDataChecksum(CellCulture cellCulture) {
    if (cellCulture.getData() == null) {
      return null;
    }
    return RepositoryUtils.generateChecksum(cellCulture.getData().toByteBuffer(false));
  }

  private Path getDataPath(String id) {
    return rootDir.resolve(id + ".data");
  }

  private Path getInfoPath(String id) {
    return rootDir.resolve(id + ".json");
  }

  private record InternalInfo(Path path, String infoChecksum, String dataChecksum, CellCulture cellCulture) {

  }


}
