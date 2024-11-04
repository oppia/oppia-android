package org.oppia.android.testing

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Repeatable
annotation class OverrideIntParameter(val name: String, val value: Int)
