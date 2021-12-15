package org.oppia.android.testing

/**
 * Test class or method annotation for disabling the espresso-accessibility tests of the
 * class or the specific method may run on. By default, tests are assumed to be able to run the
 * accessibility tests on Espresso.
 *
 * Note that this annotation only works if the test also has an [OppiaTestRule] hooked up.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class DisableAccessibilityChecks
