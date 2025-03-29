package org.oppia.android.testing

import org.oppia.android.util.platformparameter.FeatureFlag

/**
 * Repeatable test method annotation for resetting the feature flag for tests of the
 * specific method may run on. The feature flag names are provided by the
 * FeatureFlagConstants.kt.
 *
 * Note that this annotation only works if the test also has an [OppiaTestRule] hooked up.
 */
@Target(AnnotationTarget.FUNCTION)
@Repeatable
annotation class ResetFeatureFlagToDefault(val name: FeatureFlag)
