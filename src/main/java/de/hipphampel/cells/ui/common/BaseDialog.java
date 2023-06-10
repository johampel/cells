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
import javafx.collections.ListChangeListener.Change;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.VBox;

public class BaseDialog<T> extends Dialog<T> {

  private final VBox headerContainer;
  private final ObjectProperty<Node> header;
  private final ObjectProperty<Node> content;

  public BaseDialog() {
    this.headerContainer = new VBox();
    this.header = new SimpleObjectProperty<>(this, "header");
    this.content = new SimpleObjectProperty<>(this, "content");

    headerContainer.getStyleClass().add(STYLE_CLASS_CELLS_HEADER);
    header.addListener((ignore, oldValue, newValue) -> onHeaderChanged(oldValue, newValue));
    content.addListener((ignore, oldValue, newValue) -> onContentChanged(oldValue, newValue));

    DialogPane pane = getDialogPane();
    pane.getStylesheets().add(FXMLUtils.getCellsCss());
    pane.getButtonTypes().addListener(this::onButtonTypeChanged);

    pane.setHeader(headerContainer);
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

  private void onContentChanged(Node oldContent, Node newContent) {
    DialogPane pane = getDialogPane();
    if (oldContent != null) {
      pane.setContent(null);
    }
    if (newContent != null) {
      pane.setContent(newContent);
    }
  }

  private void onHeaderChanged(Node oldHeader, Node newHeader) {
    if (oldHeader != null) {
      headerContainer.getChildren().remove(oldHeader);
    }
    if (newHeader != null) {
      headerContainer.getChildren().add(newHeader);
    }
  }

  private void onButtonTypeChanged(Change<? extends ButtonType> buttonTypeChange) {
    while (buttonTypeChange.next()) {
      buttonTypeChange.getAddedSubList().forEach(this::addStyleClassToButton);
    }
  }

  private void addStyleClassToButton(ButtonType buttonType) {
    var button = getDialogPane().lookupButton(buttonType);
    if (button == null) {
      return;
    }

    switch (buttonType.getButtonData()) {
      case YES -> button.getStyleClass().add(UiConstants.STYLE_CLASS_YES);
      case NO -> button.getStyleClass().add(UiConstants.STYLE_CLASS_NO);
      case APPLY -> button.getStyleClass().add(UiConstants.STYLE_CLASS_CONFIRM);
      case CANCEL_CLOSE -> button.getStyleClass().add(UiConstants.STYLE_CLASS_CANCEL);
      case OK_DONE -> button.getStyleClass().add(UiConstants.STYLE_CLASS_CONFIRM);
      default -> {
      }
    }
  }
}
