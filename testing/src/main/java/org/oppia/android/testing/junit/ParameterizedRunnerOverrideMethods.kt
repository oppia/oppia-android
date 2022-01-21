package org.oppia.android.testing.junit

import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.Statement

/**
 * Specifies methods that the helper parameterized runners should override from JUnit's test runner.
 */
internal interface ParameterizedRunnerOverrideMethods {
  /** See [org.junit.runners.BlockJUnit4ClassRunner.getChildren]. */
  fun getChildren(): MutableList<FrameworkMethod>

  /** See [org.junit.runners.BlockJUnit4ClassRunner.testName]. */
  fun testName(method: FrameworkMethod?): String

  /** See [org.junit.runners.BlockJUnit4ClassRunner.methodInvoker]. */
  fun methodInvoker(method: FrameworkMethod?, test: Any?): Statement
}
