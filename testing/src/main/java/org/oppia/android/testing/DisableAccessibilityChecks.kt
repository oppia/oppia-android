package org.oppia.android.testing

/**
 * Test class or method annotation for enabling/disabling the espresso-accessibility tests of the
 * class or the specific method may run on. By default, tests are assumed to be able to run the
 * accessibility tests on Espresso.
 *
 * Note that this annotation only works if the test also has an [AccessibilityTestRule] hooked up.
 *
 * Note that when defined on both a class and a method, the list of platforms defined on the method
 * is used and any defined at the class level are ignored.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class DisableAccessibilityChecks(val isDisabled: Boolean)
