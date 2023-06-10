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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class ImageMerger {

  private final Map<List<String>, Image> cache;

  public ImageMerger() {
    cache = new ConcurrentHashMap<>();
  }

  public Image getImage(String mainImage, String... overlays) {
    List<String> layers = Stream.concat(Stream.of(mainImage), Arrays.stream(overlays)).toList();
    return getImage(layers);
  }

  public Image getImage(List<String> layers) {
    Image image = cache.get(layers);
    if (image == null) {
      image = createImage(layers);
      cache.put(layers, image);
    }
    return image;
  }

  private Image createImage(List<String> layers) {
    if (layers.isEmpty()) {
      throw new IllegalArgumentException("layers must not be empty");
    }
    if (layers.size() == 1) {
      return createImageForUrl(layers.get(0));
    } else {
      Image base = getImage(layers.subList(0, layers.size() - 1));
      Image overlay = getImage(layers.subList(layers.size() - 1, layers.size()));
      return mergeImage(base, overlay);
    }
  }

  private Image mergeImage(Image base, Image overlay) {
    double w = base.getWidth();
    double h = base.getHeight();
    Canvas canvas = new Canvas(w, h);
    GraphicsContext gc = canvas.getGraphicsContext2D();

    WritableImage output = new WritableImage((int) w, (int) h);

    gc.setGlobalAlpha(1);
    gc.setFill(Color.TRANSPARENT);
    gc.fillRect(0, 0, w, h);
    gc.drawImage(base, 0, 0, w, h);
    gc.drawImage(overlay, 0, 0, w, h);

    SnapshotParameters sp = new SnapshotParameters();
    sp.setFill(Color.TRANSPARENT);
    canvas.snapshot(sp, output);
    return output;
  }

  private Image createImageForUrl(String url) {
    try (InputStream input = ImageMerger.class.getResourceAsStream(url)) {
      return new Image(input);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }
}
