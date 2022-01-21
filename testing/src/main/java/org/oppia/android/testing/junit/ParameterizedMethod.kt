package org.oppia.android.testing.junit

import java.lang.reflect.Field
import java.util.Locale

/**
 * A parameterized method used by [OppiaParameterizedTestRunner] when defining sub-tests that are
 * run as part of the test suite.
 *
 * @property methodName the name of the test method that's been parameterized
 */
internal class ParameterizedMethod(
  val methodName: String,
  private val values: Map<String, List<ParameterValue>>,
  private val parameterFields: List<Field>
) {
  /** The names of all iterations run for this method. */
  val iterationNames: Collection<String> by lazy { values.keys }

  /**
   * Updates the specified [testClassInstance]'s parameter-injected fields to the values
   * corresponding to the specified [iterationName] iteration.
   *
   * This should always be called before the test's execution begins. It's also expected that this
   * method is called for each iteration (since the test method should be executed multiples, once
   * for each of its iteration).
   */
  internal fun initializeForTest(testClassInstance: Any, iterationName: String) {
    // Retrieve the setters for the fields (since these are expected to be used instead of direct
    // property access in Kotlin). Note that these need to be re-fetched since the instance class
    // may change (due to Robolectric instrumentation including custom class loading & bytecode
    // changes).
    val baseClass = testClassInstance.javaClass
    val fieldSetters = parameterFields.map { field ->
      val setterMethod =
        baseClass.getDeclaredMethod("set${field.name.capitalize(Locale.US)}", field.type)
      field.name to setterMethod
    }.toMap()
    values.getValue(iterationName).forEach { parameterValue ->
      val fieldSetter = fieldSetters.getValue(parameterValue.key)
      fieldSetter.invoke(testClassInstance, parameterValue.value)
    }
  }
}
