package org.oppia.android.testing

import org.oppia.android.app.model.FeatureFlagId

/**
 * Repeatable test class or method annotation for disabling the feature flag for tests of the
 * class or the specific method may run on. The feature flag names are provided by the
 * FeatureFlagConstants.kt.
 *
 * Note that this annotation only works if the test also has an [OppiaTestRule] hooked up.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Repeatable
annotation class DisableFeatureFlag(val id: FeatureFlagId)
