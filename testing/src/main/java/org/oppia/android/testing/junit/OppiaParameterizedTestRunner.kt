package org.oppia.android.testing.junit

import org.junit.runner.Description
import org.junit.runner.Runner
import org.junit.runner.manipulation.Filter
import org.junit.runner.manipulation.Filterable
import org.junit.runner.manipulation.Sortable
import org.junit.runner.manipulation.Sorter
import org.junit.runner.notification.RunNotifier
import org.junit.runners.Suite
import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.reflect.KClass

/**
 * JUnit test runner that enables support for parameterization, that is, running a single test
 * multiple times with different data values.
 *
 * From a testing correctness perspective, this should only be used to test scenarios of behaviors
 * that are very similar to one another (i.e. only differ in one or two conditions that can be
 * data-driven), and that have a large number (i.e. >5) conditions to test. For other situations,
 * use regular explicit tests, instead (since parameterized tests can hurt test maintainability and
 * readability).
 *
 * This runner behaves like AndroidJUnit4 in that it should work in different environments based on
 * which base runner is configured using [SelectRunnerPlatform] (which automatically pulls in the
 * necessary Bazel dependencies). However, it will only support the platform(s) selected.
 *
 * To introduce parameterized tests, add this runner along with one or more [Parameter]-annotated
 * fields and one or more [Iteration]-annotated methods (where each method should have
 * multiple [Iteration]s defined to describe each test iteration). Note that only strings and
 * primitive types (e.g. [Int], [Long], [Float], [Double], and [Boolean]) are supported for
 * parameter injection. Here's a simple example:
 *
 * ```kotlin
 * @RunWith(OppiaParameterizedTestRunner::class)
 * @SelectRunnerPlatform(ParameterizedRobolectricTestRunner::class)
 * class ExampleParameterizedTest {
 *   @Parameter lateinit var strParam: String
 *   @Parameter var intParam: Int = Int.MIN_VALUE // Inited because primitives can't be lateinit.
 *
 *   @Test
 *   @Iteration("first", "strParam=first value", "intParam=12")
 *   @Iteration("second", "strParam=second value", "intParam=-72")
 *   @Iteration("third", "strParam=third value", "intParam=15")
 *   fun testParams_multipleVals_isConsistent() {
 *     val result = performOperation(strParam, intParam)
 *     assertThat(result).isEqualTo(consistentExpectedValue)
 *   }
 * }
 * ```
 *
 * The test testParams_multipleVals_isConsistent will be run three times, and before each time the
 * specified parameter value corresponding to each iteration will be injected into the parameter
 * field for use by the test.
 *
 * Note that with Bazel specific iterations can be run by utilizing the test and iteration name,
 * e.g.:
 *
 * ```bash
 * bazel run //...:ExampleParameterizedTest --test_filter=testParams_multipleVals_isConsistent_first
 * ```
 *
 * Or, all of the iterations for that test can be run:
 *
 * ```bash
 * bazel run //...:ExampleParameterizedTest --test_filter=testParams_multipleVals_isConsistent
 * ```
 *
 * Finally, regular tests can be added by simply using the JUnit ``Test`` annotation without also
 * annotating with [Iteration]. Such tests should not ever read from the
 * [Parameter]-annotated fields since there's no way to ensure what values those fields will
 * contain (thus they should be treated as undefined outside of tests that specific define their
 * value via [Iteration]).
 */
class OppiaParameterizedTestRunner(private val testClass: Class<*>) : Suite(testClass, listOf()) {
  private val parameterizedMethods = computeParameterizedMethods()
  private val selectedRunnerClass by lazy { fetchSelectedRunnerPlatformClass() }
  private val childrenRunners by lazy {
    // Collect all parameterized methods (for each iteration they support) plus one test runner for
    // all non-parameterized methods.
    parameterizedMethods.flatMap { (methodName, method) ->
      method.iterationNames.map { iterationName ->
        ProxyParameterizedTestRunner(
          selectedRunnerClass, testClass, parameterizedMethods, methodName, iterationName
        )
      }
    } + ProxyParameterizedTestRunner(
      selectedRunnerClass, testClass, parameterizedMethods, methodName = null
    )
  }

  override fun getChildren(): MutableList<Runner> = childrenRunners.toMutableList()

  private fun computeParameterizedMethods(): Map<String, ParameterizedMethod> {
    val fieldsAndParsers = fetchParameterizedFields().map { field ->
      val valueParser = ParameterValue.createParserForField(field)
      checkNotNull(valueParser) {
        "Unsupported field type: ${field.type} for parameterized field ${field.name}"
      }
      return@map field to valueParser
    }.associateBy { (field, _) -> field.name }

    val fields = fieldsAndParsers.map { (_, fieldAndParser) -> fieldAndParser.first }
    val methodDeclarations = fetchParameterizedMethodDeclarations()
    return methodDeclarations.map { (method, rawValues) ->
      val allValues = rawValues.mapValues { (_, values) ->
        values.map { rawValuePair ->
          check('=' in rawValuePair) {
            "Expect all parameter values to be of the form propertyField=value (encountered:" +
              " $rawValuePair)"
          }

          // Use substringBefore/After since values should be allowed to contain '='.
          val fieldName = rawValuePair.substringBefore(delimiter = '=')
          val rawValue = rawValuePair.substringAfter(delimiter = '=')
          check(fieldName in fieldsAndParsers) {
            "Property key does not correspond to any class fields: $fieldName (available:" +
              " ${fieldsAndParsers.keys})"
          }

          val (field, parser) = fieldsAndParsers.getValue(fieldName)
          val value = parser.parseParameter(fieldName, rawValue)
          checkNotNull(value) {
            "Parameterized field ${field.name}'s type is incompatible with raw parameter value:" +
              " $rawValue"
          }
        }
      }.also { allValues ->
        // Validate no duplicate keys.
        allValues.forEach { (iterationName, values) ->
          val allKeys = values.map { it.key }
          val uniqueKeys = allKeys.toSet()
          check(allKeys.size == uniqueKeys.size) {
            val duplicateKeys = allKeys.toMutableList()
            uniqueKeys.forEach { duplicateKeys.remove(it) }
            return@check "Encountered duplicate keys in iteration $iterationName for method" +
              " ${method.name}: ${duplicateKeys.toSet()}"
          }
        }

        // Validate key consistency.
        val allKeys = allValues.values.flatten().map(ParameterValue::key).toSet()
        allValues.forEach { (iterationName, values) ->
          val iterationKeys = values.map { it.key }.toSet()
          check(iterationKeys == allKeys) {
            "Iteration $iterationName in method ${method.name} has missing keys compared with" +
              " other iterations: ${allKeys - iterationKeys}"
          }
        }

        // Validate value ordering.
        val iterationKeys = allValues.mapValues { (_, values) -> values.map { it.key } }
        val expectedOrder = iterationKeys.values.first()
        iterationKeys.forEach { (iterationName, keys) ->
          check(keys == expectedOrder) {
            "Iteration $iterationName in method ${method.name} lists its keys in the order: $keys" +
              " whereas $expectedOrder (for the first iteration) is expected for consistency." +
              " Please pick an order and ensure all iterations are consistent."
          }
        }

        // Validate that all value sets are unique (to detect redundant iterations).
        allValues.entries.forEach { (outerIterationName, outerValues) ->
          allValues.entries.forEach { (innerIterationName, innerValues) ->
            if (outerIterationName != innerIterationName) {
              // Order & counts have been verified above, so the values can be checked in order.
              val differentValues = outerValues.zip(innerValues).any { (outerValue, innerValue) ->
                outerValue.value != innerValue.value
              }
              check(differentValues) {
                "Iterations $outerIterationName and $innerIterationName in method ${method.name}" +
                  " have the same values and are thus redundant. Please remove one of them or" +
                  " update the values."
              }
            }
          }
        }
      }
      return@map ParameterizedMethod(method.name, allValues, fields)
    }.associateBy { it.methodName }
  }

  private fun fetchParameterizedFields(): List<Field> {
    return testClass.declaredFields.mapNotNull { field ->
      field.getDeclaredAnnotation(Parameter::class.java)?.let { field }
    }
  }

  private fun fetchParameterizedMethodDeclarations(): List<ParameterizedMethodDeclaration> {
    return testClass.declaredMethods.mapNotNull { method ->
      method.getDeclaredAnnotationsByType(Iteration::class.java).map { parameters ->
        parameters.name to parameters.keyValuePairs.toList()
      }.takeIf { it.isNotEmpty() }?.let { rawValues ->
        val groupedValues = rawValues.groupBy({ it.first }, { it.second })
        // Verify there are no duplicate iteration names.
        groupedValues.forEach { (iterationName, iterations) ->
          check(iterations.size == 1) {
            "Encountered duplicate iteration name: $iterationName in method ${method.name}"
          }
        }
        val mappedValues = groupedValues.mapValues { (_, iterations) -> iterations.first() }
        ParameterizedMethodDeclaration(method, mappedValues)
      }
    }
  }

  private fun fetchSelectedRunnerPlatformClass(): Class<*> {
    return checkNotNull(testClass.getDeclaredAnnotation(SelectRunnerPlatform::class.java)) {
      "All suites using OppiaParameterizedTestRunner must declare their base platform runner" +
        " using SelectRunnerPlatform."
    }.runnerType.java
  }

  /**
   * Defines which [OppiaParameterizedBaseRunner] should be used for running individual
   * parameterized and non-parameterized test cases.
   *
   * See base classes for options.
   */
  @Target(AnnotationTarget.CLASS)
  annotation class SelectRunnerPlatform(val runnerType: KClass<out OppiaParameterizedBaseRunner>)

  /**
   * Defines a parameter that may have an injected value that comes from per-test [Iteration]
   * definitions.
   *
   * It's recommended to make such fields 'lateinit var', and they must be public.
   *
   * The type of the parameter field will define how [Iteration]-defined values are parsed. The
   * current list of supported types:
   * - [String]s
   * - [Boolean]s
   * - [Int]s
   * - [Long]s
   * - [Float]s
   * - [Double]s
   */
  @Target(AnnotationTarget.FIELD) annotation class Parameter

  /**
   * Defines an iteration to run as part of a parameterized test method.
   *
   * See the KDoc for the runner for example code.
   *
   * @property name the name of the iteration (this should be short, but meaningful since it's
   *     appended to the test name)
   * @property keyValuePairs an array of strings of the form "key=value" where 'key' is the name of
   *     a [Parameter]-annotated field and 'value' is a stringified conforming value based on the
   *     type of that field (incompatible values will result in test failures)
   */
  @Repeatable
  @Target(AnnotationTarget.FUNCTION)
  annotation class Iteration(val name: String, vararg val keyValuePairs: String)

  private data class ParameterizedMethodDeclaration(
    val method: Method,
    val rawValues: Map<String, List<String>>
  )

  private class ProxyParameterizedTestRunner(
    private val runnerClass: Class<*>,
    private val testClass: Class<*>,
    private val parameterizedMethods: Map<String, ParameterizedMethod>,
    private val methodName: String?,
    private val iterationName: String? = null
  ) : Runner(), Filterable, Sortable {
    private val delegate by lazy { constructDelegate() }
    private val delegateRunner by lazy {
      checkNotNull(delegate as? Runner) { "Delegate runner isn't a JUnit runner: $delegate" }
    }
    private val delegateFilter by lazy {
      checkNotNull(delegate as? Filterable) { "Delegate runner isn't filterable: $delegate" }
    }
    private val delegateSortable by lazy {
      checkNotNull(delegate as? Sortable) { "Delegate runner isn't sortable: $delegate" }
    }

    override fun getDescription(): Description = delegateRunner.description

    override fun run(notifier: RunNotifier?) = delegateRunner.run(notifier)

    override fun filter(filter: Filter?) = delegateFilter.filter(filter)

    override fun sort(sorter: Sorter?) = delegateSortable.sort(sorter)

    private fun constructDelegate(): Any {
      val constructor =
        runnerClass.getConstructor(
          Class::class.java, Map::class.java, String::class.java, String::class.java
        )
      return constructor.newInstance(testClass, parameterizedMethods, methodName, iterationName)
    }
  }
}
