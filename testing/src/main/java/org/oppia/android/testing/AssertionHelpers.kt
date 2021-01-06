package org.oppia.android.testing

import org.junit.Assert.fail
import kotlin.reflect.KClass
import kotlin.reflect.full.cast

/* This file contains generic assertion helpers which are meant to be usable in any test.
   Not for methods which are specific to a certain test or part of the codebase. */

/**
 * A replacement to JUnit5's assertThrows() that asserts an execution of the supplied operation
 * throws an exception of the supplied type
 *
 * An example of this might be:
 *
 * val exception = assertThrows(IllegalArgumentException::class) {
 *  stringToRatioParser.parseRatioOrThrow("a:b:c")
 * }
 * assertThat(exception)
 *  .hasMessageThat()
 *  .contains("Incorrectly formatted ratio: a:b:c")
 *
 * @param type the type of exception to be thrown
 * @param operation the operation being run
 * @return the exception being thrown
 * @throws AssertionError if the specified exception is not thrown
 */
fun <T : Throwable> assertThrows(type: KClass<T>, operation: () -> Unit): T {
  try {
    operation()
    fail("Expected to encounter exception of $type")
  } catch (t: Throwable) {
    if (type.isInstance(t)) {
      return type.cast(t)
    }
    // Unexpected exception; throw it.
    throw t
  }
  throw AssertionError(
    "Reached an impossible state when verifying that an exception was thrown."
  )
}
