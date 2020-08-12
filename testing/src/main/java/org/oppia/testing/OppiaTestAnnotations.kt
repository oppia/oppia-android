package org.oppia.testing

import org.oppia.testing.TestPlatform.ESPRESSO
import org.oppia.testing.TestPlatform.ROBOLECTRIC

/** Specifies a test platform to target in conjunction with [RunOn]. */
enum class TestPlatform {
  /** Corresponds to local tests run in the Java VM via Robolectric. */
  ROBOLECTRIC,

  /** Corresponds to instrumented tests that can run on a real device or emulator via Espresso. */
  ESPRESSO
}

/**
 * Test class or method annotation for specifying all of platforms which either the tests of the
 * class or the specific method may run on. By default, tests are assumed to be able to run on both
 * Espresso & Robolectric.
 *
 * The target platforms are specified as varargs of [TestPlatform]s.
 *
 * Note that this annotation only works if the test also has an [OppiaTestRule] hooked up.
 *
 * Note that when defined on both a class and a method, the list of platforms defined on the method
 * is used and any defined at the class level are ignored.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class RunOn(vararg val testPlatforms: TestPlatform = [ROBOLECTRIC, ESPRESSO])
