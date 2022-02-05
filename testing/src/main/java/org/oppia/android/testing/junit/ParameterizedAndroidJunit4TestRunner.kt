package org.oppia.android.testing.junit

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.Statement

/**
 * A [AndroidJUnit4ClassRunner] which supports [OppiaParameterizedTestRunner] when running on an
 * Espresso-driven platform.
 *
 * This should be selected as the base runner when the test author wishes to use Espresso.
 */
@Suppress("unused") // This class is constructed using reflection.
class ParameterizedAndroidJunit4TestRunner internal constructor(
  testClass: Class<*>,
  private val parameterizedMethods: Map<String, ParameterizedMethod>,
  private val methodName: String?,
  private val iterationName: String?
) : AndroidJUnit4ClassRunner(testClass),
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
