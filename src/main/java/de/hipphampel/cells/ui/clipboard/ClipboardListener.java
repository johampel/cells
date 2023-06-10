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
package de.hipphampel.cells.ui.clipboard;

import java.util.Set;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.util.Duration;

public class ClipboardListener implements EventHandler<ActionEvent> {

  public static final DataFormat CELL_CULTURE_DATA_SELECTION = new DataFormat("application/x.cells-culture-selection");

  private final Timeline timeline;

  private final ObjectProperty<ClipboardData> data;

  public ClipboardListener() {
    this.timeline = new Timeline(new KeyFrame(Duration.millis(200), this));
    this.timeline.setCycleCount(Timeline.INDEFINITE);
    this.data = new SimpleObjectProperty<>(this, "data");
  }

  public void start() {
    timeline.play();
  }

  public void stop() {
    timeline.stop();
  }

  public ClipboardData getData() {
    return data.get();
  }

  public ReadOnlyObjectProperty<ClipboardData> dataProperty() {
    return data;
  }

  @Override
  public void handle(ActionEvent actionEvent) {
    Clipboard clipboard = Clipboard.getSystemClipboard();
    Set<DataFormat> contentTypes = clipboard.getContentTypes();
    if (contentTypes.contains(CELL_CULTURE_DATA_SELECTION)) {
      data.set((ClipboardData) clipboard.getContent(CELL_CULTURE_DATA_SELECTION));
    }
  }
}

