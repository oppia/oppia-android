package org.oppia.android.testing

import org.junit.Assert.fail
import kotlin.reflect.KClass
import kotlin.reflect.full.cast

/**
 * Assertion helpers which are generic methods to be used at multiple places
 * Not for methods which are specific to a certain test or part of the codebase
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
