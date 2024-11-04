package org.oppia.android.testing

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Repeatable
annotation class OverrideStringParameter(val name: String, val value: String)
