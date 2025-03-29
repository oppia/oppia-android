package org.oppia.android.testing

import org.oppia.android.util.platformparameter.FeatureFlag

/**
 * Repeatable test class or method annotation for enabling the feature flag for tests of the
 * class or the specific method may run on. The feature flag names are provided by the
 * FeatureFlagConstants.kt.
 *
 * Note that this annotation only works if the test also has an [OppiaTestRule] hooked up.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Repeatable
annotation class EnableFeatureFlag(val name: FeatureFlag)
