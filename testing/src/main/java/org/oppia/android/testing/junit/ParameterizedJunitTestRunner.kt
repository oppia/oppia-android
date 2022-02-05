package org.oppia.android.testing.junit

import org.junit.runners.BlockJUnit4ClassRunner
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.Statement

/**
 * A [BlockJUnit4ClassRunner] which supports [OppiaParameterizedTestRunner] when running on a local
 * JVM using JUnit directly.
 *
 * This should be selected as the base runner when the test author wishes to use JUnit without
 * Android dependencies. This should **not** be used for Robolectric (i.e. tests that require
 * Android libraries) tests; use [ParameterizedRobolectricTestRunner] for those, instead.
 *
 * The main advantage that this runner provides beyond the Robolectric one is that it avoids
 * initializing the Android shadows that Robolectric manages.
 */
@Suppress("unused") // This class is constructed using reflection.
class ParameterizedJunitTestRunner internal constructor(
  testClass: Class<*>,
  private val parameterizedMethods: Map<String, ParameterizedMethod>,
  private val methodName: String?,
  private val iterationName: String?
) : BlockJUnit4ClassRunner(testClass),
  OppiaParameterizedBaseRunner,
  ParameterizedRunnerOverrideMethods {
  private val delegate by lazy {
    ParameterizedRunnerDelegate(
      parameterizedMethods,
      methodName,
      iterationName
    ).also { delegate ->
      delegate.fetchChildrenFromParent = { super.getChildren() }
      delegate.fetchTestNameFromParent = { method -> super.testName(method) }
      delegate.fetchMethodInvokerFromParent = { method, test -> super.methodInvoker(method, test) }
    }
  }

  override fun getChildren(): MutableList<FrameworkMethod> = delegate.getChildren()

  override fun testName(method: FrameworkMethod?): String = delegate.testName(method)

  override fun methodInvoker(method: FrameworkMethod?, test: Any?): Statement =
    delegate.methodInvoker(method, test)
}
