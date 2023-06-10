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
package de.hipphampel.cells;

import de.hipphampel.cells.persistence.repository.CellCultureRepository;
import de.hipphampel.cells.persistence.repository.CellSystemRepository;
import de.hipphampel.cells.ui.clipboard.ClipboardListener;
import de.hipphampel.cells.ui.common.ImageMerger;
import de.hipphampel.cells.validation.CellsValidator;
import de.hipphampel.validation.core.event.DefaultSubscribableEventPublisher;
import de.hipphampel.validation.core.event.SubscribableEventPublisher;

public class ServiceLocator {

  private static SubscribableEventPublisher eventPublisher;
  private static CellSystemRepository cellSystemRepository;
  private static CellCultureRepository cellCultureRepository;
  private static CellsValidator validator;
  private static ImageMerger imageMerger;
  private static ClipboardListener clipboardListener;

  public static ClipboardListener getClipboardListener() {
    if (clipboardListener==null) {
      clipboardListener = new ClipboardListener();
    }
    return clipboardListener;
  }

  public static SubscribableEventPublisher getEventPublisher() {
    if (eventPublisher == null) {
      eventPublisher = new DefaultSubscribableEventPublisher();
    }
    return eventPublisher;
  }

  public static void setEventPublisher(SubscribableEventPublisher eventPublisher) {
    ServiceLocator.eventPublisher = eventPublisher;
  }

  public static CellCultureRepository getCellCultureRepository() {
    if (cellCultureRepository == null) {
      cellCultureRepository = new CellCultureRepository();
    }
    return cellCultureRepository;
  }

  public static void setCellCultureRepository(CellCultureRepository cellCultureRepository) {
    ServiceLocator.cellCultureRepository = cellCultureRepository;
  }

  public static CellSystemRepository getCellSystemRepository() {
    if (cellSystemRepository == null) {
      cellSystemRepository = new CellSystemRepository();
    }
    return cellSystemRepository;
  }

  public static void setCellSystemRepository(CellSystemRepository cellSystemRepository) {
    ServiceLocator.cellSystemRepository = cellSystemRepository;
  }

  public static CellsValidator getValidator() {
    if (validator == null) {
      validator = new CellsValidator();
    }
    return validator;
  }

  public static void setValidator(CellsValidator validator) {
    ServiceLocator.validator = validator;
  }

  public static ImageMerger getImageMerger() {
    if (imageMerger == null) {
      imageMerger = new ImageMerger();
    }
    return imageMerger;
  }

  public static void setImageMerger(ImageMerger imageMerger) {
    ServiceLocator.imageMerger = imageMerger;
  }
}
