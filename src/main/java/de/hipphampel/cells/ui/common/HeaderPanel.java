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

import static de.hipphampel.cells.ui.common.UiConstants.STYLE_CLASS_CELLS_HEADER;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class HeaderPanel extends VBox {

  private final ObjectProperty<Node> header;
  private final ObjectProperty<Node> content;
  private final HBox headerContainer;

  public HeaderPanel() {
    this.headerContainer = new HBox();
    VBox.setVgrow(headerContainer, Priority.NEVER);
    this.headerContainer.getStyleClass().add(STYLE_CLASS_CELLS_HEADER);
    this.getChildren().add(headerContainer);
    this.header = new SimpleObjectProperty<>(this, "header");
    this.header.addListener((observable, oldNode, newNode) -> onChangeHeader(oldNode, newNode));
    this.content = new SimpleObjectProperty<>(this, "content");
    this.content.addListener((observable, oldNode, newNode) -> onChangeContent(oldNode, newNode));
  }

  private void onChangeHeader(Node oldHeader, Node newHeader) {
    if (oldHeader != null) {
      headerContainer.getChildren().remove(oldHeader);
    }
    if (newHeader != null) {
      headerContainer.getChildren().add(newHeader);
    }
  }

  private void onChangeContent(Node oldContent, Node newContent) {
    if (oldContent != null) {
      getChildren().remove(oldContent);
    }
    if (newContent != null) {
      getChildren().add(newContent);
    }
  }

  public Node getHeader() {
    return header.get();
  }

  public ObjectProperty<Node> headerProperty() {
    return header;
  }

  public void setHeader(Node header) {
    this.header.set(header);
  }

  public Node getContent() {
    return content.get();
  }

  public ObjectProperty<Node> contentProperty() {
    return content;
  }

  public void setContent(Node content) {
    this.content.set(content);
  }
}
