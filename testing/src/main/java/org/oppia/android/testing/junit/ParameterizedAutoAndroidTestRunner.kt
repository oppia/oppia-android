package org.oppia.android.testing.junit

import org.junit.runner.Description
import org.junit.runner.Runner
import org.junit.runner.manipulation.Filter
import org.junit.runner.manipulation.Filterable
import org.junit.runner.manipulation.Sortable
import org.junit.runner.manipulation.Sorter
import org.junit.runner.notification.RunNotifier
import org.junit.runners.BlockJUnit4ClassRunner
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.Statement

/**
 * A [BlockJUnit4ClassRunner] which will automatically switch between a local Robolectric and
 * Espresso runner depending on whether the test is running on an active Android platform (mimicking
 * the behavior of ``AndroidJUnit4``).
 *
 * This runner should only be used for tests shared between Espresso & Robolectric (otherwise,
 * prefer to use dedicated runners).
 *
 * Note that for Bazel builds the correct Robolectric or AndroidJUnit runner dependency must be
 * included for the specific test (otherwise there will be a runtime failure when trying to start
 * the test).
 */
@Suppress("unused") // This class is constructed using reflection.
class ParameterizedAutoAndroidTestRunner internal constructor(
  testClass: Class<*>,
  private val parameterizedMethods: Map<String, ParameterizedMethod>,
  private val methodName: String?,
  private val iterationName: String?
) : Runner(),
  Filterable,
  Sortable,
  OppiaParameterizedBaseRunner,
  ParameterizedRunnerOverrideMethods {
  private val runningOnAndroid by lazy {
    System.getProperty("java.runtime.name")?.contains("android", ignoreCase = true) ?: false
  }

  private val runnerClass by lazy {
    System.getProperty("android.junit.runner").also { customRunner ->
      check(customRunner == null) {
        "Detected a custom runner ($customRunner) in a parameterized test. This isn't yet" +
          " supported."
      }
    }

    // Load the runner class using reflection since the Robolectric implementation relies on
    // Robolectric (which can't be pulled into Espresso builds of shared tests).
    val targetRunnerName = if (runningOnAndroid) {
      "org.oppia.android.testing.junit.ParameterizedAndroidJunit4TestRunner"
    } else "org.oppia.android.testing.junit.ParameterizedRobolectricTestRunner"
    return@lazy try {
      Class.forName(targetRunnerName)
    } catch (e: Exception) {
      throw IllegalStateException(
        "Failed to load delegate test runner class ($targetRunnerName). Did you forget to add" +
          " either parameterized_android_junit4_class_runner or" +
          " parameterized_robolectric_test_runner as a dependency?",
        e
      )
    }
  }

  private val delegate by lazy {
    checkNotNull(
      runnerClass.getConstructor(
        Class::class.java, Map::class.java, String::class.java, String::class.java
      ).newInstance(
        testClass, parameterizedMethods, methodName, iterationName
      ) as? ParameterizedRunnerOverrideMethods
    ) {
      "Expected runner to be an instance of ParameterizedRunnerOverrideMethods for runner" +
        " delegation"
    }
  }

  private val delegateRunner by lazy {
    checkNotNull(delegate as? Runner) { "Delegate runner isn't a JUnit runner: $delegate" }
  }
  private val delegateParameterizedRunner by lazy {
    checkNotNull(delegate as? ParameterizedRunnerOverrideMethods) {
      "Delegate runner isn't an instance of ParameterizedRunnerOverrideMethods: $delegate"
    }
  }
  private val delegateFilter by lazy {
    checkNotNull(delegate as? Filterable) { "Delegate runner isn't filterable: $delegate" }
  }
  private val delegateSortable by lazy {
    checkNotNull(delegate as? Sortable) { "Delegate runner isn't sortable: $delegate" }
  }

  override fun getChildren(): MutableList<FrameworkMethod> =
    delegateParameterizedRunner.getChildren()

  override fun testName(method: FrameworkMethod?): String =
    delegateParameterizedRunner.testName(method)

  override fun methodInvoker(method: FrameworkMethod?, test: Any?): Statement =
    delegateParameterizedRunner.methodInvoker(method, test)

  override fun getDescription(): Description = delegateRunner.description

  override fun run(notifier: RunNotifier?) = delegateRunner.run(notifier)

  override fun filter(filter: Filter?) = delegateFilter.filter(filter)

  override fun sort(sorter: Sorter?) = delegateSortable.sort(sorter)
}
