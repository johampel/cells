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
package de.hipphampel.cells.ui.cellculture;

import de.hipphampel.array2dops.draw.Byte2DArrayDrawContext;
import de.hipphampel.array2dops.geom.Point;
import de.hipphampel.array2dops.geom.Rectangle;
import de.hipphampel.array2dops.model.Byte2DArray;
import de.hipphampel.cells.model.ModelUtils;
import de.hipphampel.cells.model.cellculture.CellCulture;
import de.hipphampel.cells.model.cellculture.ResolvedCellCulture;
import de.hipphampel.cells.model.cellsystem.CellSystem;
import de.hipphampel.cells.model.cellsystem.CellType;
import de.hipphampel.cells.resources.Resources;
import de.hipphampel.cells.ui.cellculture.drawing.CellDrawing;
import de.hipphampel.cells.ui.clipboard.CellCultureDataSelection;
import de.hipphampel.cells.ui.clipboard.ClipboardListener;
import de.hipphampel.cells.ui.common.FXMLUtils;
import de.hipphampel.cells.ui.common.UiConstants;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Optional;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventType;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.util.Duration;

public class CellCultureDataRenderer extends GridPane {

  private static final int MIN_GRID_SCALE = 4;
  private static final int SCROLLER_SIZE = 16;

  private ResolvedCellCulture cellCulture;
  private final ObjectProperty<Integer> scale;
  private final BooleanProperty showGrid;
  private final BooleanProperty enableSelection;
  private final ObjectProperty<Color> gridColor;
  private CellDrawing cellDrawing;
  private Point selectionStart;
  private Point selectionEnd;
  private final ScrollBar vScrollbar;
  private final ScrollBar hScrollbar;
  private final DataCanvas canvas;
  private boolean resizing;

  public CellCultureDataRenderer() {
    this.cellCulture = null;
    this.scale = new SimpleObjectProperty<>(this, "scale", 1);
    this.showGrid = new SimpleBooleanProperty(this, "showGrid", true);
    this.enableSelection = new SimpleBooleanProperty(this, "enableSelection", false);
    this.cellDrawing = null;
    this.gridColor = new SimpleObjectProperty<>(Color.LIGHTGREY);

    this.vScrollbar = new ScrollBar();
    this.vScrollbar.setId("vScrollbar");
    this.vScrollbar.setOrientation(Orientation.VERTICAL);
    this.vScrollbar.setMaxWidth(SCROLLER_SIZE);
    this.vScrollbar.setPrefWidth(SCROLLER_SIZE);

    this.hScrollbar = new ScrollBar();
    this.hScrollbar.setId("hScrollbar");
    this.hScrollbar.setOrientation(Orientation.HORIZONTAL);
    this.hScrollbar.setMaxHeight(SCROLLER_SIZE);
    this.hScrollbar.setPrefHeight(SCROLLER_SIZE);

    this.canvas = new DataCanvas();
    this.canvas.setId("canvas");

    // Prepare layout
    ColumnConstraints column0 = new ColumnConstraints();
    column0.setHgrow(Priority.ALWAYS);
    ColumnConstraints column1 = new ColumnConstraints();
    column1.setHgrow(Priority.NEVER);
    getColumnConstraints().setAll(column0, column1);
    RowConstraints row0 = new RowConstraints();
    row0.setVgrow(Priority.ALWAYS);
    RowConstraints row1 = new RowConstraints();
    row1.setVgrow(Priority.NEVER);
    getRowConstraints().setAll(row0, row1);
    add(canvas, 0, 0);
    add(vScrollbar, 1, 0);
    add(hScrollbar, 0, 1);

    // Event handling
    widthProperty().addListener(ov -> onResize());
    heightProperty().addListener(ov -> onResize());
    scaleProperty().addListener(ov -> onResize());
    showGridProperty().addListener(ov -> onResize());
    gridColorProperty().addListener(ov -> canvas.draw());
    hScrollbar.valueProperty().addListener(ov -> canvas.draw());
    vScrollbar.valueProperty().addListener(ov -> canvas.draw());
    addEventFilter(MouseEvent.ANY, this::onMouseEvent);
  }

  public int getScale() {
    return scale.get();
  }

  public ObjectProperty<Integer> scaleProperty() {
    return scale;
  }

  public void setScale(int scale) {
    if (scale > 0) {
      this.scale.set(scale);
    }
  }

  public boolean isEnableSelection() {
    return enableSelection.get();
  }

  public BooleanProperty enableSelectionProperty() {
    return enableSelection;
  }

  public void setEnableSelection(boolean enableSelection) {
    this.enableSelection.set(enableSelection);
  }

  public boolean isShowGrid() {
    return showGrid.get();
  }

  public BooleanProperty showGridProperty() {
    return showGrid;
  }

  public void setShowGrid(boolean showGrid) {
    this.showGrid.set(showGrid);
  }

  public Color getGridColor() {
    return gridColor.get();
  }

  public ObjectProperty<Color> gridColorProperty() {
    return gridColor;
  }

  public void setGridColor(Color gridColor) {
    this.gridColor.set(gridColor);
  }


  public void redraw() {
    canvas.draw();
  }

  public void updateCellDrawing(CellDrawing cellDrawing) {
    this.cellDrawing = cellDrawing;
    this.canvas.draw();
  }

  public ResolvedCellCulture getCellCulture() {
    return cellCulture;
  }

  public void setCellCulture(ResolvedCellCulture cellCulture) {
    boolean resize = (cellCulture == null) != (this.cellCulture == null) ||
        (cellCulture != null && (cellCulture.culture().getWidth() != this.cellCulture.culture().getWidth()
            || cellCulture.culture().getHeight() != this.cellCulture.culture().getHeight()));
    this.cellCulture = cellCulture;
    if (resize) {
      onResize();
    } else {
      this.canvas.draw();
    }
  }

  public Point toCellCoordinates(double x, double y) {
    Rectangle2D pixelRect = canvas.getPixelRect();
    return new Point(
        Math.min(cellCulture.culture().getWidth() - 1, Math.max(0, (int) Math.floor((x + pixelRect.getMinX()) / getScale()))),
        Math.min(cellCulture.culture().getHeight() - 1, Math.max(0, (int) Math.floor((y + pixelRect.getMinY()) / getScale()))));
  }


  private void onResize() {
    try {
      resizing = true;
      if (cellCulture == null) {
        showOrHideVScroller(false, 1, 1);
        showOrHideHScroller(false, 1, 1);
        return;
      }
      double requiredWidth = getAllDataWidthInPixel();
      double requiredHeight = getAllDataHeightInPixel();
      double availableWidth = getWidth();
      double availableHeight = getHeight();

      boolean requiresVScroller = availableHeight < requiredHeight;
      boolean requiresHScroller = availableWidth < requiredWidth;
      if (requiresVScroller) {
        availableWidth -= SCROLLER_SIZE;
        requiresHScroller = availableWidth < requiredWidth;
      }
      if (requiresHScroller) {
        availableHeight -= SCROLLER_SIZE;
        requiresVScroller = availableHeight < requiredHeight;
      }

      showOrHideVScroller(requiresVScroller, requiredHeight, availableHeight);
      showOrHideHScroller(requiresHScroller, requiredWidth, availableWidth);
      this.canvas.setWidth(availableWidth);
      this.canvas.setHeight(availableHeight);
    } finally {
      resizing = false;
    }
    this.canvas.draw();
  }

  private void showOrHideVScroller(boolean show, double available, double visible) {
    if (show) {
      if (!getChildren().contains(vScrollbar)) {
        add(vScrollbar, 1, 0);
      }
    } else {
      getChildren().remove(vScrollbar);
      vScrollbar.setValue(0);
    }
    vScrollbar.setMin(0);
    vScrollbar.setMax(available);
    vScrollbar.setVisibleAmount(visible);
    vScrollbar.setValue(Math.max(0, Math.min(available, vScrollbar.getValue())));
  }

  private void showOrHideHScroller(boolean show, double available, double visible) {
    if (show) {
      if (!getChildren().contains(hScrollbar)) {
        add(hScrollbar, 0, 1);
      }
    } else {
      getChildren().remove(hScrollbar);
      hScrollbar.setValue(0);
    }
    hScrollbar.setMin(0);
    hScrollbar.setMax(available);
    hScrollbar.setVisibleAmount(visible);
    hScrollbar.setValue(Math.max(0, Math.min(available, hScrollbar.getValue())));
  }

  private boolean isEffectivelyShowingGrid() {
    if (!isShowGrid()) {
      return false;
    }
    return MIN_GRID_SCALE <= scale.get();
  }

  private double getAllDataWidthInPixel() {
    return cellCulture.culture().getWidth() * getScale() + (isEffectivelyShowingGrid() ? 1 : 0);
  }

  private double getAllDataHeightInPixel() {
    return cellCulture.culture().getHeight() * getScale() + (isEffectivelyShowingGrid() ? 1 : 0);
  }


  private void onMouseEvent(MouseEvent event) {
    if (!isEnableSelection()) {
      return;
    }
    EventType<?> eventType = event.getEventType();
    Point cellPos = toCellCoordinates(event.getX(), event.getY());

    if (eventType == MouseEvent.MOUSE_PRESSED && event.getTarget() == canvas) {
      selectionStart = cellPos;
      selectionEnd = cellPos;
      canvas.draw();
    } else if (selectionStart != null && (eventType == MouseEvent.MOUSE_DRAGGED || eventType == MouseEvent.MOUSE_RELEASED)) {
      selectionEnd = cellPos;
      if (eventType == MouseEvent.MOUSE_RELEASED) {
        copyToClipboard(event.getScreenX(), event.getScreenY());
        selectionEnd = null;
        selectionStart = null;
      }
      canvas.draw();
    }
  }

  private void copyToClipboard(double x, double y) {
    if (selectionStart == null) {
      return;
    }

    Byte2DArray data = cellCulture.culture().getData();
    Rectangle selectionRect = new Rectangle(
        Math.min(selectionStart.x(), selectionEnd.x()),
        Math.min(selectionStart.y(), selectionEnd.y()),
        Math.abs(selectionStart.x() - selectionEnd.x()) + 1,
        Math.abs(selectionStart.y() - selectionEnd.y()) + 1
    );
    Rectangle rect = new Rectangle(0, 0, data.getWidth(), data.getHeight()).intersect(selectionRect).orElse(null);
    if (rect != null) {
      Clipboard.getSystemClipboard().setContent(Map.of(
          ClipboardListener.CELL_CULTURE_DATA_SELECTION,
          new CellCultureDataSelection(cellCulture.culture().getCellTypeCount(),
              data.copyRegion(rect.x(), rect.y(), rect.width(), rect.height()))));

      Popup popup = new Popup();
      Label popupLabel = new Label(Resources.getResource("cellCultureDataRenderer.copiedToClipboardMessage"));
      popupLabel.getStylesheets().add(FXMLUtils.getCellsCss());
      popupLabel.getStyleClass().add(UiConstants.STYLE_CLASS_CELLS_POPUP);
      popup.getContent().add(popupLabel);
      popup.show(this, x, y);
      new Timeline(
          new KeyFrame(Duration.millis(1000), evt -> popup.hide())
      ).play();
    }
  }


  private class DataCanvas extends Canvas {

    private DataCanvas() {
      setSnapToPixel(true);
    }

    @Override
    public double prefWidth(double height) {
      return 0;
    }

    @Override
    public double prefHeight(double width) {
      return 0;
    }

    private void draw() {
      if (resizing) {
        return;
      }
      GraphicsContext gc = getGraphicsContext2D();
      Rectangle2D pixelRect = getPixelRect();

      gc.setFill(Color.WHITE);
      gc.fillRect(0, 0, getWidth(), getHeight());

      if (cellCulture == null) {
        return;
      }

      Rectangle cellRect = getCellRect(pixelRect);
      drawData(gc, pixelRect, cellRect);
      drawCellDrawing(gc, pixelRect, cellRect);
      drawGrid(gc, pixelRect, cellRect);
      drawSelection(gc);
    }

    private void drawCellDrawing(GraphicsContext gc, Rectangle2D pixelRect, Rectangle cellRect) {
      Rectangle rectangle = cellDrawing == null ? null : cellDrawing.getRectangle();
      if (rectangle == null) {
        return;
      }

      Rectangle drawRect = Optional.ofNullable(cellDrawing.getRectangle())
          .flatMap(rect -> rect.intersect(cellRect))
          .orElse(null);
      if (drawRect == null) {
        return;
      }

      Byte2DArray array = Byte2DArray.newInstance(drawRect.width(), drawRect.height());
      Byte2DArrayDrawContext drawContext = new Byte2DArrayDrawContext(array)
          .color((byte) cellCulture.culture().getCellTypeCount())
          .fillRect(0, 0, drawRect.width(), drawRect.height())
          .origin(-drawRect.x(), -drawRect.y());

      cellDrawing.draw(drawContext);
      Image image = createImage(array, new Rectangle(0, 0, drawRect.width(), drawRect.height()), cellDrawing.getCellType());

      gc.setImageSmoothing(false);

      int scale = getScale();
      gc.drawImage(
          image,
          0, 0,
          image.getWidth(), image.getHeight(),
          drawRect.x() * scale - pixelRect.getMinX(), drawRect.y() * scale - pixelRect.getMinY(),
          image.getWidth() * scale, image.getHeight() * scale);
    }

    private void drawSelection(GraphicsContext gc) {
      if (selectionStart == null) {
        return;
      }

      Rectangle selectionRect = new Rectangle(
          Math.min(selectionStart.x(), selectionEnd.x()),
          Math.min(selectionStart.y(), selectionEnd.y()),
          Math.abs(selectionStart.x() - selectionEnd.x()) + 1,
          Math.abs(selectionStart.y() - selectionEnd.y()) + 1
      );
      Rectangle2D rect = getPixelRect(selectionRect);

      gc.setFill(Color.color(0.0, 0.0, 0, 0.1));
      gc.fillRect(rect.getMinX(), rect.getMinY(), rect.getWidth(), rect.getHeight());
      gc.setStroke(Color.color(0.0, 0.0, 0, 1));
      gc.setLineWidth(1);
      gc.strokeRect(rect.getMinX(), rect.getMinY(), rect.getWidth(), rect.getHeight());
    }

    private void drawData(GraphicsContext gc, Rectangle2D pixelRect, Rectangle cellRect) {
      Image image = createImage(cellCulture.culture().getData(), cellRect, null);
      if (image == null) {
        return;
      }
      int grid = isEffectivelyShowingGrid() ? 1 : 0;
      int scale = getScale();
      double dx = cellRect.x() * scale + grid - pixelRect.getMinX();
      double dy = cellRect.y() * scale + grid - pixelRect.getMinY();

      gc.setImageSmoothing(false);

      gc.drawImage(
          image,
          0, 0, image.getWidth(), image.getHeight(),
          dx, dy, image.getWidth() * scale, image.getHeight() * scale);
    }

    private Image createImage(Byte2DArray data, Rectangle rect, CellType cellTypeToDraw) {
      PixelFormat<ByteBuffer> format = createPixelFormat(cellTypeToDraw);
      if (format == null) {
        return null;
      }

      int x0 = Math.max(rect.x(), 0);
      int y0 = Math.max(rect.y(), 0);
      int w = Math.min(rect.width(), data.getWidth() - x0);
      int h = Math.min(rect.height(), data.getHeight() - y0);
      WritableImage image = new WritableImage(w, h);
      PixelWriter writer = image.getPixelWriter();
      ByteBuffer buffer = data.toByteBuffer(false);

      for (int y = 0; y < h; y++) {
        buffer.position(x0 + data.getWidth() * (y0 + y));
        writer.setPixels(0, y, w, 1, format, buffer, w);
      }
      return image;
    }

    private PixelFormat<ByteBuffer> createPixelFormat(CellType cellType) {
      CellSystem cellSystem = cellCulture.system();
      if (cellSystem == null) {
        return null;
      }

      int[] colors = new int[cellSystem.getCellTypeCount() + 1];
      for (int i = 0; i < colors.length - 1; i++) {
        if (cellType == null || cellType.getId() == i) {
          colors[i] = 0xff000000 | ModelUtils.color2rgb(cellSystem.getCellType(i).getColor());
        }
      }
      return PixelFormat.createByteIndexedInstance(colors);
    }

    private void drawGrid(GraphicsContext gc, Rectangle2D pixelRect, Rectangle cellRect) {
      if (!isEffectivelyShowingGrid()) {
        return;
      }

      gc.setStroke(getGridColor());
      gc.setLineWidth(1);
      int scale = getScale();
      double x = cellRect.width() * scale + 0.5 - pixelRect.getMinX() % scale;
      double y = cellRect.height() * scale + 0.5 - pixelRect.getMinY() % scale;
      for (double i = x; i >= 0; i -= scale) {
        gc.strokeLine(i, 0, i, y);
      }
      for (var i = y; i >= 0; i -= scale) {
        gc.strokeLine(0, i, x, i);
      }
    }

    Rectangle getCellRect(Rectangle2D pixelRect) {
      double ofs = isEffectivelyShowingGrid() ? 1 : 0;
      double scale = getScale();
      CellCulture culture = cellCulture.culture();
      return new Rectangle(
          (int) Math.floor(pixelRect.getMinX() / scale),
          (int) Math.floor(pixelRect.getMinY() / scale),
          (int) Math.min(culture.getWidth(), 1 + Math.ceil((pixelRect.getWidth() - ofs) / scale)),
          (int) Math.min(culture.getHeight(), 1 + Math.ceil((pixelRect.getHeight() - ofs) / scale)));
    }

    Rectangle2D getPixelRect(Rectangle cellRect) {
      Rectangle2D pixelRect = getPixelRect();
      int scale = getScale();
      return new Rectangle2D(
          cellRect.x() * scale - pixelRect.getMinX(),
          cellRect.y() * scale - pixelRect.getMinY(),
          cellRect.width() * scale,
          cellRect.height() * scale
      );
    }

    Rectangle2D getPixelRect() {
      if (getCellCulture() == null) {
        return new Rectangle2D(0, 0, getWidth(), getHeight());
      }
      double scrollX = hScrollbar.getValue();
      double scrollY = vScrollbar.getValue();

      // Calculate pixel infos
      double adpw = getAllDataWidthInPixel();
      double adph = getAllDataHeightInPixel();
      double dpx = (adpw - getWidth()) * (scrollX / adpw);
      double dpy = (adph - getHeight()) * (scrollY / adph);
      double dpw = Math.min(getWidth(), adpw);
      double dph = Math.min(getHeight(), adph);
      return new Rectangle2D(dpx, dpy, Math.max(0, dpw), Math.max(0, dph));
    }
  }

}
