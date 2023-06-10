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
package de.hipphampel.cells.validation;

import de.hipphampel.cells.resources.Resources;
import de.hipphampel.validation.core.annotations.BindContext;
import de.hipphampel.validation.core.annotations.BindFacts;
import de.hipphampel.validation.core.annotations.Precondition;
import de.hipphampel.validation.core.annotations.RuleDef;
import de.hipphampel.validation.core.execution.ValidationContext;
import de.hipphampel.validation.core.path.ComponentPath;
import de.hipphampel.validation.core.path.ComponentPath.Component;
import de.hipphampel.validation.core.rule.Result;
import de.hipphampel.validation.core.rule.Rule;
import java.util.List;

public class CommonRules {

  @RuleDef(
      id = "common:notEmpty",
      preconditions = @Precondition(rules = "common:notNull"))
  public static Result objectNotEmpty(@BindContext ValidationContext context, @BindFacts Object facts) {
    boolean failed = (facts instanceof String str && str.trim().isEmpty()) ||
        (facts instanceof List<?> lst && lst.isEmpty());
    return failed ? Result.failed(Resources.getResource(getResourceKey(context, "mustNotBeEmpty"))) : Result.ok();
  }

  @RuleDef(
      id = "common:notNull")
  public static Result objectNotNull(@BindContext ValidationContext context, @BindFacts Object facts) {
    boolean failed = facts == null;
    return failed ? Result.failed(Resources.getResource(getResourceKey(context, "mustNotBeNull"))) : Result.ok();
  }

  private static String getResourceKey(ValidationContext context, String keySuffix) {
    String component = getLastPathComponent(context);
    return "validation." + (component == null ? keySuffix : component + firstToUpper(keySuffix));
  }

  private static String firstToUpper(String str) {
    if (str == null || str.isEmpty()) {
      return str;
    }
    return Character.toUpperCase(str.charAt(0)) + str.substring(1);
  }

  private static String getLastPathComponent(ValidationContext context) {
    if (context.getCurrentPath() instanceof ComponentPath path) {
      return path.getLastComponent().map(Component::name).orElse(null);
    } else {
      return null;
    }
  }

}
