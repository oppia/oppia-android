package org.oppia.android.testing.junit

import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.Statement

/**
 * A common helper for platform-specific helper runners.
 *
 * This class performs the actual field injection and execution delegation for running each
 * parameterized test method.
 *
 * @property restrictMethodNamesForPaths ensure method names don't contain path separators (default
 *     is false). Note that '/' is replaced with '_slash_' in such cases.
 */
class ParameterizedRunnerDelegate(
  private val parameterizedMethods: Map<String, ParameterizedMethod>,
  private val methodName: String?,
  private val iterationName: String?,
  private val restrictMethodNamesForPaths: Boolean = false
) : ParameterizedRunnerOverrideMethods {
  /**
   * A lambda used to call into the parent runner's [getChildren] method. This should be set by
   * helper parameterized test runners.
   */
  lateinit var fetchChildrenFromParent: () -> MutableList<FrameworkMethod>

  /**
   * A lambda used to call into the parent runner's [testName] method. This should be set by helper
   * parameterized test runners.
   */
  lateinit var fetchTestNameFromParent: (FrameworkMethod?) -> String

  /**
   * A lambda used to call into the parent runner's [methodInvoker] method. This should be set by
   * helper parameterized test runners.
   */
  lateinit var fetchMethodInvokerFromParent: (FrameworkMethod?, Any?) -> Statement

  override fun getChildren(): MutableList<FrameworkMethod> {
    return fetchChildrenFromParent().filter {
      // Either only match to the specific method, or no parameterized methods.
      if (methodName != null) {
        it.method.name == methodName
      } else it.method.name !in parameterizedMethods.keys
    }.toMutableList()
  }

  override fun testName(method: FrameworkMethod?): String {
    return if (methodName != null) {
      val partName = if (restrictMethodNamesForPaths) {
        iterationName?.replace("/", "_slash_")
      } else iterationName
      "${fetchTestNameFromParent(method)}_$partName"
    } else fetchTestNameFromParent(method)
  }

  override fun methodInvoker(method: FrameworkMethod?, test: Any?): Statement {
    val invoker = fetchMethodInvokerFromParent(method, test)
    checkNotNull(test) { "Expected test to be initialized." }
    return if (methodName != null && iterationName != null) {
      val parameterizedMethod = checkNotNull(parameterizedMethods[method?.name]) {
        "Expected to find registered parameterized method: ${method?.name}, available:" +
          " ${parameterizedMethods.keys}"
      }
      object : Statement() {
        override fun evaluate() {
          // Initialize the test prior to execution.
          parameterizedMethod.initializeForTest(test, iterationName)
          println("@@@@@ INVOKE: $methodName in $iterationName")
          invoker.evaluate()
        }
      }
    } else invoker
  }
}
