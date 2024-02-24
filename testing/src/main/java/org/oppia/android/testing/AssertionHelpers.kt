package org.oppia.android.testing

/* This file contains generic assertion helpers which are meant to be usable in any test.
   Not for methods which are specific to a certain test or part of the codebase. */

/**
 * A replacement to JUnit5's assertThrows() that asserts an execution of the supplied operation
 * throws an exception of the indicated type.
 *
 * An example of this might be:
 *
 * ```kotlin
 * val exception = assertThrows<IllegalArgumentException>() {
 *  stringToRatioParser.parseRatioOrThrow("a:b:c")
 * }
 * assertThat(exception).hasMessageThat().contains("Incorrectly formatted ratio: a:b:c")
 * ```
 *
 * @param operation the operation being run
 * @return the exception being thrown
 * @throws AssertionError if the specified exception is not thrown
 */
inline fun <reified T : Throwable> assertThrows(noinline operation: () -> Unit): T {
  return when (val result = try { operation() } catch (t: Throwable) { t }) {
    is T -> result
    is Throwable -> {
      throw AssertionError(
        "Expected exception of type: ${T::class.java}, not: ${result::class.java}.", result
      )
    }
    else -> {
      throw AssertionError(
        "Expected exception of type: ${T::class.java}. No exception was thrown."
      )
    }
  }
}
