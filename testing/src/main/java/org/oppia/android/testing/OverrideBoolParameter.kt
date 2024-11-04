package org.oppia.android.testing

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Repeatable
annotation class OverrideBoolParameter(val name: String, val value: Boolean)
