package org.oppia.android.testing

import org.oppia.android.util.platformparameter.FeatureFlag

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Repeatable
annotation class DisableFeatureFlag(val name: FeatureFlag)
