package org.oppia.android.testing

@Target(AnnotationTarget.FUNCTION)
@Repeatable
annotation class ResetFeatureFlagToDefault(val name: String)
