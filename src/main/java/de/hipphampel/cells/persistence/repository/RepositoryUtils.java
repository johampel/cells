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

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class RepositoryUtils {

  public static Path ensureRepositoryPathExists(Path basePath, String subDirectory) {
    Path repositoryPath = getRepositoryPath(basePath, subDirectory);
    if (!Files.exists(repositoryPath)) {
      try {
        Files.createDirectories(repositoryPath);
      } catch (IOException e) {
        throw new PersistenceException("Failed to create '" + repositoryPath + "'", e);
      }
    }

    if (!Files.isDirectory(repositoryPath)) {
      throw new PersistenceException("'" + repositoryPath + "' is not a directory");
    }

    return repositoryPath;
  }

  public static Path getRepositoryPath(Path basePath, String subDirectory) {
    Objects.requireNonNull(subDirectory);

    if (basePath == null) {
      String userHome = System.getProperty("user.home");
      basePath = Path.of(userHome, ".cells");
    }

    return basePath.resolve(subDirectory).toAbsolutePath();
  }

  public static String generateChecksum(ByteBuffer content) {
    try {
      MessageDigest digester = MessageDigest.getInstance("MD5");
      digester.update(content);
      byte[] digest = digester.digest();
      return new BigInteger(1, digest).toString(16);
    } catch (NoSuchAlgorithmException e) {
      throw new PersistenceException("Failed to calculate digest", e);
    }
  }

}
