package org.oppia.android.testing

import org.oppia.android.util.platformparameter.PlatformParameter

/**
 * Repeatable test class or method annotation for overriding integer platform parameter values
 * for tests of the class or the specific method may run on. The platform parameter names are provided
 * by the PlatformParameterConstants.kt.
 *
 * Note that this annotation only works if the test also has an [OppiaTestRule] hooked up.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Repeatable
annotation class OverrideIntParameter(val name: PlatformParameter, val value: Int)
