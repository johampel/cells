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

import static de.hipphampel.cells.ui.common.UiConstants.IMAGE_CELLSYSTEM;

import de.hipphampel.cells.model.ModelObject;
import de.hipphampel.cells.model.ModificationState;
import de.hipphampel.cells.model.cellculture.CellCulture;
import de.hipphampel.cells.model.cellsystem.CellSystem;
import de.hipphampel.cells.model.validation.Severity;
import de.hipphampel.cells.resources.Resources;
import de.hipphampel.cells.ui.cellculture.CellCultureView;
import de.hipphampel.cells.ui.cellsystem.CellSystemView;
import de.hipphampel.cells.ui.common.CloseApplicationDialog;
import de.hipphampel.cells.ui.common.MainContent;
import de.hipphampel.cells.ui.common.UiUtils;
import de.hipphampel.mv4fx.view.View;
import de.hipphampel.mv4fx.view.ViewManager;
import java.util.List;
import java.util.Optional;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class App extends Application {

  public static void main(String[] args) {
    launch();
  }

  @Override
  public void start(Stage stage) {
    ServiceLocator.getClipboardListener().start();

    Scene scene = new Scene(new MainContent(), 1500, 840);
    stage.setScene(scene);
    stage.getIcons().addAll(
        ServiceLocator.getImageMerger().getImage(UiUtils.imageNameFor(IMAGE_CELLSYSTEM, false))
    );
    stage.setTitle(Resources.getResource("application.title"));
    stage.setOnCloseRequest(this::canCloseApplication);
    stage.show();
  }

  void canCloseApplication(WindowEvent evt) {
    List<ModelObject> modifiedObjects = ViewManager.getAllViews()
        .map(this::toModelObject)
        .flatMap(Optional::stream)
        .filter(mo -> mo.getModificationState() != ModificationState.UNCHANGED)
        .toList();
    if (modifiedObjects.isEmpty()) {
      return;
    }

    CloseApplicationDialog dialog = new CloseApplicationDialog(modifiedObjects);
    List<ModelObject> toBeSaved = dialog.showAndWait().orElse(null);
    if (toBeSaved == null) {
      evt.consume();
      return;
    }

    if (!saveAll(toBeSaved)) {

    }
  }

  private boolean saveAll(List<ModelObject> toBeSaved) {
    return toBeSaved.stream().anyMatch(this::save);
  }

  private boolean save(ModelObject modelObject) {
    if (modelObject instanceof CellSystem cellSystem) {
      if (ServiceLocator.getValidator().validateCellSystem(cellSystem).severity()== Severity.ERROR) {
        return false;
      }
      ServiceLocator.getCellSystemRepository().saveCellSystem(cellSystem);
      return true;
    }else if(modelObject instanceof CellCulture cellCulture) {
      if (ServiceLocator.getValidator().validateCellCulture(cellCulture).severity()== Severity.ERROR) {
        return false;
      }
      ServiceLocator.getCellCultureRepository().saveCellCulture(cellCulture);
      return true;
    }
    return false;
  }

  Optional<ModelObject> toModelObject(View view) {
    if (view instanceof CellSystemView cellSystemView) {
      return Optional.ofNullable(cellSystemView.getCellSystem());
    } else if (view instanceof CellCultureView cellCultureView) {
      return Optional.ofNullable(cellCultureView.getCellCulture());
    } else {
      return Optional.empty();
    }
  }
}
