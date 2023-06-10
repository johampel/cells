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
package de.hipphampel.cells.model.cellculture;

import de.hipphampel.array2dops.model.Byte2DArray;
import de.hipphampel.cells.model.cellsystem.CellSystem;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

public class Generator {

  public enum State {
    IDLE,
    STEPPING,
    PLAYING,
    STOPPING
  }

  private static final ForkJoinPool pool = new ForkJoinPool();
  private final CellSystem cellSystem;
  private final CellCulture cellCulture;
  private final Environment environment;
  private final Byte2DArray initialData;
  private final IntegerProperty generation;
  private final DoubleProperty speed;
  private final ObjectProperty<State> state;

  public Generator(CellSystem cellSystem, CellCulture cellCulture) {
    this.cellSystem = cellSystem;
    this.cellCulture = cellCulture;
    this.initialData = cellCulture.getData().copy();
    this.environment = new Environment(cellSystem, cellCulture);
    this.generation = new SimpleIntegerProperty(this, "generation");
    this.state = new SimpleObjectProperty<>(this, "state", State.IDLE);
    this.speed = new SimpleDoubleProperty(this, "speed");
  }

  public int getGeneration() {
    return generation.get();
  }

  public ReadOnlyIntegerProperty generationProperty() {
    return generation;
  }

  public State getState() {
    return state.get();
  }

  public ReadOnlyObjectProperty<State> stateProperty() {
    return state;
  }

  public double getSpeed() {
    return speed.get();
  }

  public DoubleProperty speedProperty() {
    return speed;
  }

  public void setSpeed(double speed) {
    this.speed.set(speed);
  }

  public void reset() {
    if (getState() != State.IDLE) {
      return;
    }
    cellCulture.setData(initialData.copy());
    generation.set(0);
  }

  public void step() {
    if (getState() != State.IDLE) {
      return;
    }
    state.set(State.STEPPING);
    pool.submit(new NextGeneration());
  }

  public void play() {
    if (getState() != State.IDLE) {
      return;
    }

    state.set(State.PLAYING);
    pool.submit(new PlayLoop());
  }

  public void stop() {
    if (getState() != State.PLAYING) {
      return;
    }
    state.set(State.STOPPING);
  }

  class PlayLoop implements Runnable {

    @Override
    public void run() {
      while (getState() == State.PLAYING) {
        ForkJoinTask<Void> action = pool.submit(new NextGeneration());
        try {
          long start = System.nanoTime();
          action.get();

          double requiredSleep = (Math.abs(getSpeed() * 1_000_000) - System.nanoTime() + start) / 1_000_000;
          if (requiredSleep >= 1) {
            Thread.sleep((long) requiredSleep);
          }

        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new RuntimeException(e);
        } catch (ExecutionException e) {
          throw new RuntimeException(e);
        }
      }
      if (getState() == State.STOPPING) {
        Platform.runLater(() -> state.set(State.IDLE));
      }
    }
  }

  class NextGeneration extends RecursiveAction {

    private final Byte2DArray source;
    private final Byte2DArray target;
    private final Environment environment;
    private final int x0;
    private final int y0;
    private final int w;
    private final int h;
    private final boolean initialTask;

    NextGeneration() {
      this(
          cellCulture.getData(),
          Byte2DArray.newInstance(cellCulture.getWidth(), cellCulture.getHeight()),
          Generator.this.environment,
          0, 0,
          cellCulture.getWidth(), cellCulture.getHeight(),
          true
      );
      this.environment.setData(source);
    }

    private NextGeneration(
        Byte2DArray source,
        Byte2DArray target,
        Environment environment,
        int x0, int y0,
        int w, int h,
        boolean initialTask) {

      this.source = source;
      this.target = target;
      this.environment = environment.copy();
      this.x0 = x0;
      this.y0 = y0;
      this.w = w;
      this.h = h;
      this.initialTask = initialTask;
    }

    @Override
    protected void compute() {
      int limitFactor = 10_000_000;
      long realFactor = w * h * environment.getComputationFactor();
      if (limitFactor < realFactor) {
        int mw = w / 2;
        int mh = h / 2;
        invokeAll(
            new NextGeneration(source, target, environment, x0, y0, mw, mh, false),
            new NextGeneration(source, target, environment, x0 + mw, y0, w - mw, mh, false),
            new NextGeneration(source, target, environment, x0, y0 + mh, mw, h - mh, false),
            new NextGeneration(source, target, environment, x0 + mw, y0 + mh, w - mw, h - mh, false)
        );

      } else {
        for (int x = 0; x < w; x++) {
          for (int y = 0; y < h; y++) {
            target.setUnsafe(x + x0, y + y0, (byte) environment.computeNextCellType(x + x0, y + y0));
          }
        }
      }

      if (initialTask) {
        onGenerationFinished();
      }
    }

    private void onGenerationFinished() {
      CountDownLatch latch = new CountDownLatch(1);
      Platform.runLater(() -> signalComputationDone(latch));
      try {
        latch.await();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(e);
      }
    }

    private void signalComputationDone(CountDownLatch latch) {
      generation.set(generation.get() + 1);
      cellCulture.setData(target);
      if (getState() != State.PLAYING) {
        state.set(State.IDLE);
      }
      latch.countDown();
    }
  }

}
