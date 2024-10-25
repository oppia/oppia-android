package org.oppia.android.testing

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Repeatable
annotation class DisableFeatureFlag(val name: String)
