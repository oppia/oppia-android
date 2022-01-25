package org.oppia.android.testing.junit

import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.Statement
import org.robolectric.RobolectricTestRunner

/**
 * A [RobolectricTestRunner] which supports [OppiaParameterizedTestRunner] when running on a local
 * JVM using Robolectric.
 */
@Suppress("unused") // This class is constructed using reflection.
internal class ParameterizedRobolectricTestRunner(
  testClass: Class<*>,
  private val parameterizedMethods: Map<String, ParameterizedMethod>,
  private val methodName: String?,
  private val iterationName: String?
) : RobolectricTestRunner(testClass), ParameterizedRunnerOverrideMethods {
  private val delegate by lazy {
    ParameterizedRunnerDelegate(
      parameterizedMethods,
      methodName,
      iterationName
    ).also { delegate ->
      delegate.fetchChildrenFromParent = { super.getChildren() }
      delegate.fetchTestNameFromParent = { method -> super.testName(method) }
    }
  }

  override fun getChildren(): MutableList<FrameworkMethod> = delegate.getChildren()

  override fun testName(method: FrameworkMethod?): String = delegate.testName(method)

  override fun methodInvoker(method: FrameworkMethod?, test: Any?): Nothing {
    throw Exception("Expected this to never be executed in the Robolectric environment.")
  }

  override fun getHelperTestRunner(
    bootstrappedTestClass: Class<*>?
  ): HelperTestRunner {
    return object : HelperTestRunner(bootstrappedTestClass) {
      override fun methodInvoker(method: FrameworkMethod?, test: Any?): Statement {
        delegate.fetchMethodInvokerFromParent = { innerMethod, innerParent ->
          super.methodInvoker(innerMethod, innerParent)
        }
        return delegate.methodInvoker(method, test)
      }
    }
  }
}
