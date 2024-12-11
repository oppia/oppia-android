package org.oppia.android.testing

import org.oppia.android.util.platformparameter.FeatureFlag

@Target(AnnotationTarget.FUNCTION)
@Repeatable
annotation class ResetFeatureFlagToDefault(val name: FeatureFlag)
