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
package de.hipphampel.cells.resources;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Resources {

  private static final String NAME = "i18n.Cells";
  private static ResourceBundle bundle;

  public static ResourceBundle getBundle() {
    createBundleIfRequired();
    return bundle;
  }

  public static String getResource(String key, Object... args) {
    createBundleIfRequired();
    try {
      var value = bundle.getString(key);
      if (args.length == 0) {
        return value;
      }
      MessageFormat format = new MessageFormat(value);
      return format.format(args);

    } catch (MissingResourceException mre) {
      return key;
    }
  }

  private static void createBundleIfRequired() {
    if (bundle != null) {
      return;
    }

    bundle = ResourceBundle.getBundle(NAME);
  }

  // For testing only
  public static void unloadBundle() {
    bundle = null;
  }

}
