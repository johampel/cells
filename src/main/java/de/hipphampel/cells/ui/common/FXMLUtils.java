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
package de.hipphampel.cells.ui.common;

import de.hipphampel.cells.resources.Resources;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.io.IOException;

public class FXMLUtils {

  private static final String BASE_PATH = "/fxml/";
  private static final String CELLS_CSS = "/css/cells.css";

  public static FXMLLoader newLoader(String path) {
    var url = FXMLUtils.class.getResource(BASE_PATH + path);
    if (url == null) {
      throw new IllegalArgumentException(
          "No such fxml file: '" + BASE_PATH + path + "' in class path");
    }
    return new FXMLLoader(url, Resources.getBundle());
  }

  public static <T extends Node> T load(String path) {
    var loader = newLoader(path);
    try {
      return loader.load();
    } catch (IOException e) {
      throw new RuntimeException("Failed to load the FXML at '" + BASE_PATH + path + "'", e);
    }
  }

  public static <T extends Node> T load(String path, Node rootAndController) {
    return load(path, rootAndController, rootAndController);
  }

  public static <T extends Node> T load(String path, Node root, Object controller) {
    var loader = newLoader(path);
    loader.setRoot(root);
    loader.setController(controller);
    try {
      return loader.load();
    } catch (IOException e) {
      throw new RuntimeException("Failed to load the FXML at '" + BASE_PATH + path + "'", e);
    }
  }

  public static String getCellsCss() {
    return FXMLUtils.class.getResource(CELLS_CSS).toExternalForm();
  }
}
