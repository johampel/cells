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
module de.hipphampel.cells {
  requires com.fasterxml.jackson.annotation;
  requires com.fasterxml.jackson.databind;
  requires de.hipphampel.array2dops;
  requires de.hipphampel.mv4fx;
  requires de.hipphampel.validation.core;
  requires javafx.controls;
  requires javafx.fxml;
  requires org.slf4j;

  exports de.hipphampel.cells;
  exports de.hipphampel.cells.model;
  exports de.hipphampel.cells.model.cellsystem;
  exports de.hipphampel.cells.model.cellculture;
  exports de.hipphampel.cells.model.validation;
  exports de.hipphampel.cells.persistence.repository;
  exports de.hipphampel.cells.ui.common;
  exports de.hipphampel.cells.ui.navigation;
  exports de.hipphampel.cells.ui.clipboard;
  exports de.hipphampel.cells.validation;

  opens de.hipphampel.cells.model.cellsystem to
      de.hipphampel.validation.core;
  opens de.hipphampel.cells.persistence.entity to
      com.fasterxml.jackson.databind;
  opens de.hipphampel.cells.ui.common to
      javafx.fxml;
  opens de.hipphampel.cells.ui.cellsystem to
      javafx.fxml;
  opens de.hipphampel.cells.ui.navigation to
      javafx.fxml;
  opens de.hipphampel.cells.validation to
      de.hipphampel.validation.core;
  opens de.hipphampel.cells.model.validation to
      de.hipphampel.validation.core;
  opens de.hipphampel.cells.ui.cellculture to com.fasterxml.jackson.databind, javafx.fxml;
  opens de.hipphampel.cells.ui.cellculture.drawing to com.fasterxml.jackson.databind, javafx.fxml;
}